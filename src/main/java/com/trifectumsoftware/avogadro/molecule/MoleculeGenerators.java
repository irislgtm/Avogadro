package com.trifectumsoftware.avogadro.molecule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MoleculeGenerators {

    public static final int MAX_ATOMS = 2048;

    private static final double SQRT3 = Math.sqrt(3.0);
    private static final double GOLDEN_RATIO = (1.0 + Math.sqrt(5.0)) / 2.0;
    private static final double GRAPHITE_LAYER_GAP = 3.354 / 1.421;

    private MoleculeGenerators() {}

    public static MoleculeStructure fullerene(String id, int carbons) {
        if (carbons != 60) {
            throw new IllegalArgumentException("Only C60 fullerene is supported");
        }

        double[][] seeds = { { 0.0, 1.0, 3.0 * GOLDEN_RATIO }, { 1.0, 2.0 + GOLDEN_RATIO, 2.0 * GOLDEN_RATIO },
            { GOLDEN_RATIO, 2.0, 2.0 * GOLDEN_RATIO + 1.0 } };
        List<double[]> vertices = new ArrayList<double[]>();
        Set<String> seen = new HashSet<String>();

        int[] permutations = { 0, 3, 4 };
        for (double[] seed : seeds) {
            for (int permutation : permutations) {
                double[] p = permute(seed, permutation);
                for (int signs = 0; signs < 8; signs++) {
                    double x = ((signs & 1) == 0 ? 1.0 : -1.0) * p[0];
                    double y = ((signs & 2) == 0 ? 1.0 : -1.0) * p[1];
                    double z = ((signs & 4) == 0 ? 1.0 : -1.0) * p[2];
                    if (seen.add(positionKey(x, y, z))) {
                        vertices.add(new double[] { x, y, z });
                    }
                }
            }
        }
        if (vertices.size() != 60) {
            throw new IllegalStateException("Fullerene vertex generation produced " + vertices.size() + " atoms");
        }

        MoleculeStructure.Builder builder = MoleculeStructure.builder(id);
        for (double[] vertex : vertices) {

            builder.addAtom(
                AtomElement.CARBON,
                (float) (vertex[0] * 0.5),
                (float) (vertex[1] * 0.5),
                (float) (vertex[2] * 0.5));
        }
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                double dx = vertices.get(i)[0] - vertices.get(j)[0];
                double dy = vertices.get(i)[1] - vertices.get(j)[1];
                double dz = vertices.get(i)[2] - vertices.get(j)[2];
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (Math.abs(distance - 2.0) < 1e-4) {
                    builder.bond(i, j);
                }
            }
        }
        return builder.build();
    }

    public static MoleculeStructure nanotube(String id, int n, int m, int cells) {
        if (n < 0 || m < 0 || (n == 0 && m == 0)) {
            throw new IllegalArgumentException("Nanotube chiral indices must be non-negative and not both zero");
        }
        if (cells < 1) {
            throw new IllegalArgumentException("Nanotube needs at least one unit cell");
        }

        double a1x = 1.5, a1y = SQRT3 / 2.0;
        double a2x = 1.5, a2y = -SQRT3 / 2.0;
        double chx = n * a1x + m * a2x;
        double chy = n * a1y + m * a2y;
        double circumference = Math.sqrt(chx * chx + chy * chy);

        int divisor = gcd(2 * m + n, 2 * n + m);
        int t1 = (2 * m + n) / divisor;
        int t2 = -(2 * n + m) / divisor;
        double tx = t1 * a1x + t2 * a2x;
        double ty = t1 * a1y + t2 * a2y;
        double determinant = cross(chx, chy, tx, ty);
        if (Math.abs(determinant) < 1e-6) {
            throw new IllegalArgumentException("Degenerate nanotube vectors");
        }

        double ecx = chx / circumference, ecy = chy / circumference;
        double etx = -ecy, ety = ecx;
        if (tx * etx + ty * ety < 0.0) {
            etx = -etx;
            ety = -ety;
        }

        int range = Math.max(Math.abs(n) + cells * Math.abs(t1), Math.abs(m) + cells * Math.abs(t2)) + 5;
        List<TubeAtom> atoms = new ArrayList<TubeAtom>();
        Set<String> seen = new HashSet<String>();
        for (int i = -range; i <= range; i++) {
            for (int j = -range; j <= range; j++) {
                double px = i * a1x + j * a2x;
                double py = i * a1y + j * a2y;
                double u = cross(px, py, tx, ty) / determinant;
                double v = cross(chx, chy, px, py) / determinant;
                if (u < -1e-6 || u >= 1.0 - 1e-6 || v < -1e-6 || v >= cells - 1e-6) {
                    continue;
                }
                addTubeAtom(atoms, seen, px, py, ecx, ecy, etx, ety, circumference);

                addTubeAtom(atoms, seen, px + 1.0, py, ecx, ecy, etx, ety, circumference);
            }
        }
        if (atoms.isEmpty()) {
            throw new IllegalStateException("Nanotube generation produced no atoms");
        }
        if (atoms.size() > MAX_ATOMS) {
            throw new IllegalArgumentException("Nanotube is too large (" + atoms.size() + " atoms)");
        }

        double minT = Double.MAX_VALUE;
        for (TubeAtom atom : atoms) {
            if (atom.t < minT) minT = atom.t;
        }
        double radius = circumference / (2.0 * Math.PI);

        MoleculeStructure.Builder builder = MoleculeStructure.builder(id);
        for (TubeAtom atom : atoms) {
            double theta = 2.0 * Math.PI * atom.s / circumference;
            builder.addAtom(
                AtomElement.CARBON,
                (float) (radius * Math.cos(theta)),
                (float) (radius * Math.sin(theta)),
                (float) (atom.t - minT));
        }

        for (int i = 0; i < atoms.size(); i++) {
            TubeAtom a = atoms.get(i);
            for (int j = i + 1; j < atoms.size(); j++) {
                TubeAtom b = atoms.get(j);
                if (Math.abs(a.t - b.t) > 1.2) {
                    continue;
                }
                double dx = a.gx - b.gx;
                double dy = a.gy - b.gy;
                long wraps = Math.round((dx * chx + dy * chy) / (circumference * circumference));
                dx -= wraps * chx;
                dy -= wraps * chy;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (Math.abs(distance - 1.0) < 0.05) {
                    builder.bond(i, j);
                }
            }
        }
        return builder.build();
    }

    public static MoleculeStructure crystal(String id, String lattice, List<String> elements, int cells, int layers) {
        if (cells < 1) {
            throw new IllegalArgumentException("Crystal needs at least one unit cell");
        }
        String type = lattice == null ? "" : lattice.toLowerCase(Locale.ROOT);
        boolean salt = type.equals("rock_salt") || type.equals("nacl") || type.equals("salt");
        String first = element(elements, 0, salt ? "Na" : "C");
        String second = element(elements, 1, "Cl");

        switch (type) {
            case "diamond":
                return diamond(id, first, cells);
            case "graphite":
                return graphite(id, first, cells, Math.max(1, layers));
            case "graphene":
                return graphite(id, first, cells, 1);
            case "simple":
            case "simple_cubic":
            case "cubic":
                return simpleCubic(id, first, cells);
            case "bcc":
                return bodyCentredCubic(id, first, cells);
            case "fcc":
                return faceCentredCubic(id, first, cells);
            case "rock_salt":
            case "nacl":
            case "salt":
                return rockSalt(id, first, second, cells);
            default:
                throw new IllegalArgumentException("Unknown crystal lattice: " + lattice);
        }
    }

    private static MoleculeStructure diamond(String id, String element, int cells) {

        double a = 4.0 / SQRT3;
        double[][] basis = { { 0.0, 0.0, 0.0 }, { 0.0, 0.5, 0.5 }, { 0.5, 0.0, 0.5 }, { 0.5, 0.5, 0.0 },
            { 0.25, 0.25, 0.25 }, { 0.25, 0.75, 0.75 }, { 0.75, 0.25, 0.75 }, { 0.75, 0.75, 0.25 } };
        MoleculeStructure.Builder builder = MoleculeStructure.builder(id);
        List<float[]> positions = new ArrayList<float[]>();
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                for (int k = 0; k < cells; k++) {
                    for (double[] offset : basis) {
                        addAtom(
                            builder,
                            positions,
                            element,
                            (i + offset[0]) * a,
                            (j + offset[1]) * a,
                            (k + offset[2]) * a);
                    }
                }
            }
        }
        bondNearest(builder, positions, 1.05);
        return builder.build();
    }

    private static MoleculeStructure graphite(String id, String element, int cells, int layers) {
        double a1x = 1.5, a1y = SQRT3 / 2.0;
        double a2x = 1.5, a2y = -SQRT3 / 2.0;
        MoleculeStructure.Builder builder = MoleculeStructure.builder(id);
        List<float[]> positions = new ArrayList<float[]>();
        for (int layer = 0; layer < layers; layer++) {
            double z = layer * GRAPHITE_LAYER_GAP;
            double shift = layer % 2 == 0 ? 0.0 : 1.0;
            for (int i = 0; i < cells; i++) {
                for (int j = 0; j < cells; j++) {
                    double px = shift + i * a1x + j * a2x;
                    double py = i * a1y + j * a2y;
                    addAtom(builder, positions, element, px, py, z);
                    addAtom(builder, positions, element, px + 1.0, py, z);
                }
            }
        }
        bondNearest(builder, positions, 1.05);
        return builder.build();
    }

    private static MoleculeStructure simpleCubic(String id, String element, int cells) {
        MoleculeStructure.Builder builder = MoleculeStructure.builder(id);
        List<float[]> positions = new ArrayList<float[]>();
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                for (int k = 0; k < cells; k++) {
                    addAtom(builder, positions, element, i, j, k);
                }
            }
        }
        bondNearest(builder, positions, 1.05);
        return builder.build();
    }

    private static MoleculeStructure bodyCentredCubic(String id, String element, int cells) {

        double a = 2.0 / SQRT3;
        MoleculeStructure.Builder builder = MoleculeStructure.builder(id);
        List<float[]> positions = new ArrayList<float[]>();
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                for (int k = 0; k < cells; k++) {
                    addAtom(builder, positions, element, i * a, j * a, k * a);
                    addAtom(builder, positions, element, (i + 0.5) * a, (j + 0.5) * a, (k + 0.5) * a);
                }
            }
        }
        bondNearest(builder, positions, 1.05);
        return builder.build();
    }

    private static MoleculeStructure faceCentredCubic(String id, String element, int cells) {

        double a = Math.sqrt(2.0);
        double[][] basis = { { 0.0, 0.0, 0.0 }, { 0.0, 0.5, 0.5 }, { 0.5, 0.0, 0.5 }, { 0.5, 0.5, 0.0 } };
        MoleculeStructure.Builder builder = MoleculeStructure.builder(id);
        List<float[]> positions = new ArrayList<float[]>();
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                for (int k = 0; k < cells; k++) {
                    for (double[] offset : basis) {
                        addAtom(
                            builder,
                            positions,
                            element,
                            (i + offset[0]) * a,
                            (j + offset[1]) * a,
                            (k + offset[2]) * a);
                    }
                }
            }
        }
        bondNearest(builder, positions, 1.05);
        return builder.build();
    }

    private static MoleculeStructure rockSalt(String id, String first, String second, int cells) {

        double a = 2.0;
        double[][] firstBasis = { { 0.0, 0.0, 0.0 }, { 0.0, 0.5, 0.5 }, { 0.5, 0.0, 0.5 }, { 0.5, 0.5, 0.0 } };
        double[][] secondBasis = { { 0.5, 0.0, 0.0 }, { 0.0, 0.5, 0.0 }, { 0.0, 0.0, 0.5 }, { 0.5, 0.5, 0.5 } };
        MoleculeStructure.Builder builder = MoleculeStructure.builder(id);
        List<float[]> positions = new ArrayList<float[]>();
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                for (int k = 0; k < cells; k++) {
                    for (double[] offset : firstBasis) {
                        addAtom(
                            builder,
                            positions,
                            first,
                            (i + offset[0]) * a,
                            (j + offset[1]) * a,
                            (k + offset[2]) * a);
                    }
                    for (double[] offset : secondBasis) {
                        addAtom(
                            builder,
                            positions,
                            second,
                            (i + offset[0]) * a,
                            (j + offset[1]) * a,
                            (k + offset[2]) * a);
                    }
                }
            }
        }
        bondNearest(builder, positions, 1.05);
        return builder.build();
    }

    private static void addTubeAtom(List<TubeAtom> atoms, Set<String> seen, double gx, double gy, double ecx,
        double ecy, double etx, double ety, double circumference) {
        double s = gx * ecx + gy * ecy;
        double t = gx * etx + gy * ety;

        double wrapped = s - Math.floor(s / circumference) * circumference;
        String key = Math.round(wrapped * 10000.0) + ":" + Math.round(t * 10000.0);
        if (!seen.add(key)) return;
        if (atoms.size() >= MAX_ATOMS) {
            throw new IllegalArgumentException("Nanotube is too large (more than " + MAX_ATOMS + " atoms)");
        }
        TubeAtom atom = new TubeAtom();
        atom.gx = gx;
        atom.gy = gy;
        atom.s = s;
        atom.t = t;
        atoms.add(atom);
    }

    private static void addAtom(MoleculeStructure.Builder builder, List<float[]> positions, String element, double x,
        double y, double z) {
        if (positions.size() >= MAX_ATOMS) {
            throw new IllegalArgumentException("Crystal is too large (more than " + MAX_ATOMS + " atoms)");
        }
        builder.addAtom(AtomElement.bySymbol(element), (float) x, (float) y, (float) z);
        positions.add(new float[] { (float) x, (float) y, (float) z });
    }

    private static void bondNearest(MoleculeStructure.Builder builder, List<float[]> positions, double maxDistance) {
        double maxDistanceSq = maxDistance * maxDistance;
        for (int i = 0; i < positions.size(); i++) {
            float[] a = positions.get(i);
            for (int j = i + 1; j < positions.size(); j++) {
                float[] b = positions.get(j);
                double dx = a[0] - b[0];
                double dy = a[1] - b[1];
                double dz = a[2] - b[2];
                if (dx * dx + dy * dy + dz * dz <= maxDistanceSq) {
                    builder.bond(i, j);
                }
            }
        }
    }

    private static String element(List<String> elements, int index, String fallback) {
        if (elements == null || index >= elements.size()) return fallback;
        String value = elements.get(index);
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static double[] permute(double[] value, int permutation) {
        switch (permutation) {
            case 0:
                return new double[] { value[0], value[1], value[2] };
            case 1:
                return new double[] { value[0], value[2], value[1] };
            case 2:
                return new double[] { value[1], value[0], value[2] };
            case 3:
                return new double[] { value[1], value[2], value[0] };
            case 4:
                return new double[] { value[2], value[0], value[1] };
            default:
                return new double[] { value[2], value[1], value[0] };
        }
    }

    private static String positionKey(double x, double y, double z) {
        return Math.round(x * 100000.0) + ":" + Math.round(y * 100000.0) + ":" + Math.round(z * 100000.0);
    }

    private static double cross(double ax, double ay, double bx, double by) {
        return ax * by - ay * bx;
    }

    private static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a == 0 ? 1 : a;
    }

    private static final class TubeAtom {

        double gx, gy;
        double s, t;
    }
}
