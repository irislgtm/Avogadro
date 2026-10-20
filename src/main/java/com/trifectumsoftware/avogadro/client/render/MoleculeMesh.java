package com.trifectumsoftware.avogadro.client.render;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

public final class MoleculeMesh {

    private static final int STRIDE = 6;

    private final float[] data;
    private final int vertexCount;

    MoleculeMesh(float[] data, int floats) {
        this.data = data;
        this.vertexCount = floats / STRIDE;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public void draw() {
        if (vertexCount == 0) return;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_TRIANGLES);
        for (int i = 0; i < data.length; i += STRIDE) {
            tessellator.setColorRGBA_F(data[i + 3], data[i + 4], data[i + 5], 1f);
            tessellator.addVertex(data[i], data[i + 1], data[i + 2]);
        }
        tessellator.draw();
    }
}
