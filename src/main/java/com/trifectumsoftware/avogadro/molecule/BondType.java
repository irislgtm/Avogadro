package com.trifectumsoftware.avogadro.molecule;

public enum BondType {

    SINGLE(1, 0.15f),
    DOUBLE(2, 0.15f),
    TRIPLE(3, 0.15f),
    QUADRUPLE(4, 0.15f);

    private final int order;
    private final float radius;

    BondType(int order, float radius) {
        this.order = order;
        this.radius = radius;
    }

    public int order() {
        return order;
    }

    public float radius() {
        return radius;
    }
}
