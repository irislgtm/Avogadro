package com.trifectumsoftware.avogadro.molecule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SmilesParser {

    private SmilesParser() {}

    private static final int ITERATIONS = 500;
    private static final Set<String> TWO_LETTER = Collections.unmodifiableSet(
        new HashSet<String>(
            Arrays.asList(
                "He",
                "Li",
                "Na",
                "Mg",
                "Al",
                "Si",
                "Cl",
                "Ar",
                "Ca",
                "Ti",
                "Cr",
                "Mn",
                "Fe",
                "Co",
                "Ni",
                "Cu",
                "Zn",
                "Br",
                "Kr",
                "Ag",
                "Sn",
                "Xe",
                "Au",
                "Pb",
                "Se",
                "Ne",
                "Os",
                "Pu",
                "Po")));
    private static final float BOND = 1.0f;
    private static final float SPRING = 0.12f;
    private static final float REPULSION = 0.10f;
    private static final float H_BOND = 0.9f;

    public static MoleculeStructure parse(String id, String smiles) {
        Graph g = build(id, smiles);
        layout(g);
        return structure(g);
    }

    private static final class Atom {

        String element;
        boolean aromatic;
        boolean bracket;
        int hydrogens;
        float x, y;
    }

    private static final class Bond {

        int a, b, order;
    }

    private static final class Graph {

        String id;
        final List<Atom> atoms = new ArrayList<Atom>();
        final List<Bond> bonds = new ArrayList<Bond>();
    }

    private static void addBond(Graph g, int a, int b, int order) {
        if (a < 0 || b < 0 || a == b) return;
        Bond bond = new Bond();
        bond.a = a;
        bond.b = b;
        bond.order = Math.max(1, Math.min(3, order));
        g.bonds.add(bond);
    }

    private static Graph build(String id, String s) {
        Graph g = new Graph();
        g.id = id;
        int prev = -1;
        int pending = 0;
        int i = 0;
        int n = s.length();
        ArrayDeque<Integer> branch = new ArrayDeque<Integer>();
        Map<Integer, int[]> rings = new HashMap<Integer, int[]>();

        while (i < n) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                i++;
                continue;
            }
            if (c == '(') {
                branch.push(Integer.valueOf(prev));
                i++;
                continue;
            }
            if (c == ')') {
                prev = branch.isEmpty() ? -1
                    : branch.pop()
                        .intValue();
                i++;
                continue;
            }
            if (c == '.') {
                prev = -1;
                i++;
                continue;
            }
            if (c == '@' || c == '>') {
                i++;
                continue;
            }
            if (c == '-' || c == '=' || c == '#' || c == ':' || c == '/' || c == '\\') {
                if (c == '=') {
                    pending = 2;
                } else if (c == '#') {
                    pending = 3;
                } else {
                    pending = 1;
                }
                i++;
                continue;
            }
            if (c == '%') {
                int ring = Integer.parseInt(s.substring(i + 1, i + 3));
                i += 3;
                prev = closeRing(g, rings, ring, prev, pending);
                pending = 0;
                continue;
            }
            if (c >= '0' && c <= '9') {
                int ring = c - '0';
                i++;
                prev = closeRing(g, rings, ring, prev, pending);
                pending = 0;
                continue;
            }

            Atom atom = new Atom();
            if (c == '[') {
                int end = s.indexOf(']', i);
                if (end < 0) end = n - 1;
                parseBracket(s.substring(i + 1, end), atom);
                i = end + 1;
            } else {
                if (i + 1 < n && Character.isUpperCase(c)
                    && Character.isLowerCase(s.charAt(i + 1))
                    && TWO_LETTER.contains(s.substring(i, i + 2))) {
                    atom.element = s.substring(i, i + 2);
                    i += 2;
                } else {
                    atom.element = String.valueOf(c);
                    i++;
                }
                atom.aromatic = Character.isLowerCase(c);
                if (atom.aromatic) {
                    atom.element = atom.element.toUpperCase()
                        .substring(0, 1);
                }
            }

            int idx = g.atoms.size();
            g.atoms.add(atom);
            if (prev >= 0) {
                boolean aromatic = g.atoms.get(prev).aromatic && atom.aromatic;
                addBond(g, prev, idx, pending != 0 ? pending : (aromatic ? 1 : 1));
            }
            prev = idx;
            pending = 0;
        }

        for (int a = 0; a < g.atoms.size(); a++) {
            fillImplicitHydrogens(g, a);
        }
        return g;
    }

    private static int closeRing(Graph g, Map<Integer, int[]> rings, int ring, int prev, int pending) {
        if (prev < 0) return prev;
        int[] open = rings.remove(Integer.valueOf(ring));
        if (open == null) {
            rings.put(Integer.valueOf(ring), new int[] { prev, pending });
            return prev;
        }
        addBond(g, open[0], prev, pending != 0 ? pending : (open[1] != 0 ? open[1] : 1));
        return prev;
    }

    private static void parseBracket(String body, Atom atom) {
        atom.bracket = true;
        int i = 0;
        if (i < body.length() && Character.isLowerCase(body.charAt(i))) {
            atom.aromatic = true;
            atom.element = body.substring(i, i + 1)
                .toUpperCase();
            i++;
        } else if (i < body.length()) {
            int j = i + 1;
            if (j < body.length() && Character.isLowerCase(body.charAt(j))) j++;
            atom.element = body.substring(i, j);
            i = j;
        }
        while (i < body.length()) {
            char c = body.charAt(i);
            if (c == 'H') {
                i++;
                int count = 0;
                while (i < body.length() && Character.isDigit(body.charAt(i))) {
                    count = count * 10 + (body.charAt(i) - '0');
                    i++;
                }
                atom.hydrogens = Math.max(1, count);
            } else {
                i++;
            }
        }
    }

    private static void fillImplicitHydrogens(Graph g, int idx) {
        Atom a = g.atoms.get(idx);
        if (a.bracket) return;
        int valence = valence(a.element, a.aromatic);
        if (valence <= 0) return;
        int used = 0;
        for (Bond b : g.bonds) {
            if (b.a == idx || b.b == idx) used += b.order;
        }
        a.hydrogens = Math.max(0, valence - used);
    }

    private static int valence(String element, boolean aromatic) {
        if (element == null) return 0;
        if (aromatic) {
            if ("C".equals(element) || "N".equals(element) || "P".equals(element) || "B".equals(element)) return 3;
            if ("O".equals(element) || "S".equals(element)) return 2;
            return 0;
        }
        if ("B".equals(element)) return 3;
        if ("C".equals(element) || "Si".equals(element)) return 4;
        if ("N".equals(element) || "P".equals(element)) return 3;
        if ("O".equals(element) || "S".equals(element)) return 2;
        if ("F".equals(element) || "Cl".equals(element) || "Br".equals(element) || "I".equals(element)) return 1;
        return 0;
    }

    private static void layout(Graph g) {
        int n = g.atoms.size();
        if (n == 0) return;
        for (int i = 0; i < n; i++) {
            double angle = 2.0 * Math.PI * i / Math.max(1, n);
            double radius = 0.6 * Math.sqrt(n);
            g.atoms.get(i).x = (float) (Math.cos(angle) * radius);
            g.atoms.get(i).y = (float) (Math.sin(angle) * radius);
        }

        float[] fx = new float[n];
        float[] fy = new float[n];
        for (int iter = 0; iter < ITERATIONS; iter++) {
            Arrays.fill(fx, 0f);
            Arrays.fill(fy, 0f);

            for (Bond b : g.bonds) {
                Atom a = g.atoms.get(b.a), c = g.atoms.get(b.b);
                float dx = a.x - c.x, dy = a.y - c.y;
                float d = (float) Math.sqrt(dx * dx + dy * dy) + 1e-4f;
                float f = (d - BOND) * SPRING;
                float ux = dx / d, uy = dy / d;
                fx[b.a] -= ux * f;
                fy[b.a] -= uy * f;
                fx[b.b] += ux * f;
                fy[b.b] += uy * f;
            }

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    Atom a = g.atoms.get(i), c = g.atoms.get(j);
                    float dx = a.x - c.x, dy = a.y - c.y;
                    float d2 = dx * dx + dy * dy + 1e-3f;
                    float d = (float) Math.sqrt(d2);
                    float f = REPULSION * BOND * BOND / d2;
                    float ux = dx / d, uy = dy / d;
                    fx[i] += ux * f;
                    fy[i] += uy * f;
                    fx[j] -= ux * f;
                    fy[j] -= uy * f;
                }
            }

            float cooling = 1f - (iter / (float) ITERATIONS) * 0.7f;
            for (int i = 0; i < n; i++) {
                g.atoms.get(i).x += clamp(fx[i], 0.2f) * cooling;
                g.atoms.get(i).y += clamp(fy[i], 0.2f) * cooling;
            }
        }

        float cx = 0f, cy = 0f;
        for (Atom a : g.atoms) {
            cx += a.x;
            cy += a.y;
        }
        cx /= n;
        cy /= n;
        for (Atom a : g.atoms) {
            a.x -= cx;
            a.y -= cy;
        }
    }

    private static float clamp(float v, float lim) {
        return Math.max(-lim, Math.min(lim, v));
    }

    private static MoleculeStructure structure(Graph g) {
        MoleculeStructure.Builder b = MoleculeStructure.builder(g.id, "");
        int[] heavyIndex = new int[g.atoms.size()];
        for (int i = 0; i < g.atoms.size(); i++) {
            Atom a = g.atoms.get(i);
            heavyIndex[i] = b.addAtom(AtomElement.bySymbol(a.element), a.x, a.y, 0f);
        }
        for (Bond bond : g.bonds) {
            b.bond(heavyIndex[bond.a], heavyIndex[bond.b], bondType(bond.order));
        }
        for (int i = 0; i < g.atoms.size(); i++) {
            Atom a = g.atoms.get(i);
            if (a.hydrogens <= 0) continue;
            float base = neighborAngle(g, i);
            for (int k = 0; k < a.hydrogens; k++) {
                float spread = (float) Math.toRadians(60.0 * (k - (a.hydrogens - 1) / 2.0));
                float angle = base + spread;
                float hx = a.x + (float) Math.cos(angle) * H_BOND;
                float hy = a.y + (float) Math.sin(angle) * H_BOND;
                int hIndex = b.addAtom(AtomElement.HYDROGEN, hx, hy, 0f);
                b.bond(heavyIndex[i], hIndex, BondType.SINGLE);
            }
        }
        return b.build();
    }

    private static float neighborAngle(Graph g, int idx) {
        Atom a = g.atoms.get(idx);
        float sx = 0f, sy = 0f;
        boolean any = false;
        for (Bond bond : g.bonds) {
            int other = bond.a == idx ? bond.b : bond.b == idx ? bond.a : -1;
            if (other < 0) continue;
            Atom o = g.atoms.get(other);
            float dx = o.x - a.x, dy = o.y - a.y;
            float len = (float) Math.sqrt(dx * dx + dy * dy) + 1e-4f;
            sx += dx / len;
            sy += dy / len;
            any = true;
        }
        if (!any || (Math.abs(sx) < 1e-3f && Math.abs(sy) < 1e-3f)) {
            return (float) (Math.PI / 2.0);
        }
        return (float) Math.atan2(-sy, -sx);
    }

    private static BondType bondType(int order) {
        if (order == 2) return BondType.DOUBLE;
        if (order == 3) return BondType.TRIPLE;
        return BondType.SINGLE;
    }
}
