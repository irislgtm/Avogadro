package com.trifectumsoftware.avogadro.molecule;

/**
 * Minimal immutable 3-component vector. Replaces JOML's Vector3f, which is not available on 1.7.10.
 */
public final class Vec3 {

    private final float x;
    private final float y;
    private final float z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }
}
