package com.trifectumsoftware.avogadro.molecule;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MoleculeStructure {

    private final String id;
    private final String formula;
    private final List<Atom> atoms;
    private final List<Bond> bonds;

    private volatile Vector3f centerCache;
    private volatile Float radiusCache;

    private MoleculeStructure(String id, String formula, List<Atom> atoms, List<Bond> bonds) {
        this.id = id;
        this.formula = formula;
        this.atoms = Collections.unmodifiableList(atoms);
        this.bonds = Collections.unmodifiableList(bonds);
    }

    public String id() {
        return id;
    }

    public String formula() {
        return formula;
    }

    public List<Atom> atoms() {
        return atoms;
    }

    public List<Bond> bonds() {
        return bonds;
    }

    public Vector3f center() {
        Vector3f c = centerCache;
        if (c == null) {
            float cx = 0f, cy = 0f, cz = 0f;
            for (Atom a : atoms) {
                cx += a.position().x();
                cy += a.position().y();
                cz += a.position().z();
            }
            int n = atoms.size();
            centerCache = c = new Vector3f(cx / n, cy / n, cz / n);
        }
        return c;
    }

    public float radius() {
        Float r = radiusCache;
        if (r == null) {
            Vector3f c = center();
            float max = 0f;
            for (Atom a : atoms) {
                Vector3f p = a.position();
                float dx = p.x() - c.x();
                float dy = p.y() - c.y();
                float dz = p.z() - c.z();
                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) + a.element().radius();
                if (dist > max) max = dist;
            }
            radiusCache = r = max;
        }
        return r;
    }

    public record Atom(AtomElement element, Vector3f position) {
    }

    public record Bond(int from, int to, BondType type) {
    }

    public static Builder builder(String id, String formula) {
        return new Builder(id, formula);
    }

    public static final class Builder {
        private final String id;
        private final String formula;
        private final List<Atom> atoms = new ArrayList<>();
        private final List<Bond> bonds = new ArrayList<>();

        private Builder(String id, String formula) {
            this.id = id;
            this.formula = formula;
        }

        public int addAtom(AtomElement element, float x, float y, float z) {
            atoms.add(new Atom(element, new Vector3f(x, y, z)));
            return atoms.size() - 1;
        }

        public float[] peekPosition(int idx) {
            Atom a = atoms.get(idx);
            return new float[]{a.position().x(), a.position().y(), a.position().z()};
        }

        public Builder bond(int from, int to) {
            bonds.add(new Bond(from, to, BondType.SINGLE));
            return this;
        }

        public Builder bond(int from, int to, BondType type) {
            bonds.add(new Bond(from, to, type));
            return this;
        }

        public Builder autoBond() {
            for (int i = 0; i < atoms.size(); i++) {
                Vector3f pi = atoms.get(i).position();
                float ri = atoms.get(i).element().radius();
                for (int j = i + 1; j < atoms.size(); j++) {
                    Vector3f pj = atoms.get(j).position();
                    float rj = atoms.get(j).element().radius();
                    float dx = pi.x() - pj.x(), dy = pi.y() - pj.y(), dz = pi.z() - pj.z();
                    float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist < (ri + rj) * 1.3f) {
                        bonds.add(new Bond(i, j, BondType.SINGLE));
                    }
                }
            }
            return this;
        }

        public MoleculeStructure build() {
            return new MoleculeStructure(id, formula, List.copyOf(atoms), List.copyOf(bonds));
        }
    }
}
