package com.trifectumsoftware.avogadro.client.render;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.molecule.MoleculeStructure;
import com.trifectumsoftware.avogadro.molecule.Vec3;

/**
 * Draws a molecule with 1.7.10's immediate mode Tessellator. Geometry is emitted in model space and transformed by
 * the fixed function pipeline (the current model view matrix), and a single directional light is baked into the
 * vertex colours so no GL lighting state is needed.
 */
public final class MoleculeRenderer {

    private static final int SPHERE_SLICES = 14;
    private static final int SPHERE_STACKS = 10;
    private static final int CYL_SEGMENTS = 10;

    private static final float LIGHT_X = -0.577f;
    private static final float LIGHT_Y = 0.577f;
    private static final float LIGHT_Z = -0.577f;
    private static final float AMBIENT = 0.35f;

    private static final float BOND_R = 0xCC / 255f;
    private static final float BOND_G = 0xCC / 255f;
    private static final float BOND_B = 0xCC / 255f;

    private static final float SPHERE_SCALE = 0.9f;
    private static final float SINGLE_BOND_RADIUS = 0.15f;
    private static final float MULTI_BOND_RADIUS = 0.10f;
    private static final float MULTI_BOND_GAP = 0.14f;

    private MoleculeRenderer() {}

    public static void render(MoleculeStructure mol, float x, float y, float size, float angle) {
        if (mol == null) return;
        List<MoleculeStructure.Atom> atoms = mol.atoms();
        List<MoleculeStructure.Bond> bonds = mol.bonds();
        if (atoms.isEmpty()) return;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        GL11.glPushMatrix();

        GL11.glTranslatef(x + size / 2f, y + size / 2f, 500f);

        float radius = mol.radius();
        float scale = (size * 0.45f) / (radius > 0.001f ? radius : 1f);
        GL11.glScalef(scale, -scale, scale);

        GL11.glRotatef(15f, 1f, 0f, 0f);
        GL11.glRotatef(angle, 0f, 1f, 0f);

        Vec3 center = mol.center();
        GL11.glTranslatef(-center.x(), -center.y(), -center.z());

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        boolean cubic = Config.cubicAtoms;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_TRIANGLES);
        for (MoleculeStructure.Atom atom : atoms) {
            Vec3 p = atom.position();
            int color = atom.element()
                .color();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            if (cubic) {
                cube(
                    tessellator,
                    p.x(),
                    p.y(),
                    p.z(),
                    atom.element()
                        .radius() * SPHERE_SCALE,
                    r,
                    g,
                    b);
            } else {
                sphere(
                    tessellator,
                    p.x(),
                    p.y(),
                    p.z(),
                    atom.element()
                        .radius() * SPHERE_SCALE,
                    r,
                    g,
                    b);
            }
        }
        for (MoleculeStructure.Bond bond : bonds) {
            Vec3 from = atoms.get(bond.from())
                .position();
            Vec3 to = atoms.get(bond.to())
                .position();
            bond(
                tessellator,
                from,
                to,
                bond.type()
                    .order(),
                cubic);
        }
        tessellator.draw();

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static float[] perpDir(float ax, float ay, float az) {
        float axa = Math.abs(ax), aya = Math.abs(ay), aza = Math.abs(az);
        if (axa <= aya && axa <= aza) {
            float il = 1f / (float) Math.sqrt(az * az + ay * ay);
            return new float[] { 0f, az * il, -ay * il };
        } else if (aya <= aza) {
            float il = 1f / (float) Math.sqrt(az * az + ax * ax);
            return new float[] { az * il, 0f, -ax * il };
        } else {
            float il = 1f / (float) Math.sqrt(ay * ay + ax * ax);
            return new float[] { -ay * il, ax * il, 0f };
        }
    }

    private static void sphere(Tessellator t, float cx, float cy, float cz, float radius, float r, float g, float b) {
        for (int stack = 0; stack < SPHERE_STACKS; stack++) {
            float phi1 = (float) (Math.PI * stack / SPHERE_STACKS);
            float phi2 = (float) (Math.PI * (stack + 1) / SPHERE_STACKS);
            float sP1 = (float) Math.sin(phi1), cP1 = (float) Math.cos(phi1);
            float sP2 = (float) Math.sin(phi2), cP2 = (float) Math.cos(phi2);

            for (int slice = 0; slice < SPHERE_SLICES; slice++) {
                float th1 = (float) (2.0 * Math.PI * slice / SPHERE_SLICES);
                float th2 = (float) (2.0 * Math.PI * (slice + 1) / SPHERE_SLICES);
                float sT1 = (float) Math.sin(th1), cT1 = (float) Math.cos(th1);
                float sT2 = (float) Math.sin(th2), cT2 = (float) Math.cos(th2);

                float n1x = sP1 * cT1, n1y = cP1, n1z = sP1 * sT1;
                float n2x = sP2 * cT1, n2y = cP2, n2z = sP2 * sT1;
                float n3x = sP2 * cT2, n3y = cP2, n3z = sP2 * sT2;
                float n4x = sP1 * cT2, n4y = cP1, n4z = sP1 * sT2;

                float p1x = cx + n1x * radius, p1y = cy + n1y * radius, p1z = cz + n1z * radius;
                float p2x = cx + n2x * radius, p2y = cy + n2y * radius, p2z = cz + n2z * radius;
                float p3x = cx + n3x * radius, p3y = cy + n3y * radius, p3z = cz + n3z * radius;
                float p4x = cx + n4x * radius, p4y = cy + n4y * radius, p4z = cz + n4z * radius;

                emit(t, p1x, p1y, p1z, n1x, n1y, n1z, r, g, b);
                emit(t, p2x, p2y, p2z, n2x, n2y, n2z, r, g, b);
                emit(t, p3x, p3y, p3z, n3x, n3y, n3z, r, g, b);

                emit(t, p1x, p1y, p1z, n1x, n1y, n1z, r, g, b);
                emit(t, p3x, p3y, p3z, n3x, n3y, n3z, r, g, b);
                emit(t, p4x, p4y, p4z, n4x, n4y, n4z, r, g, b);
            }
        }
    }

    private static void cube(Tessellator t, float cx, float cy, float cz, float radius, float r, float g, float b) {
        float x0 = cx - radius, x1 = cx + radius;
        float y0 = cy - radius, y1 = cy + radius;
        float z0 = cz - radius, z1 = cz + radius;
        quad(t, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, 1f, 0f, 0f, r, g, b);
        quad(t, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, -1f, 0f, 0f, r, g, b);
        quad(t, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0f, 1f, 0f, r, g, b);
        quad(t, x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0, 0f, -1f, 0f, r, g, b);
        quad(t, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0f, 0f, 1f, r, g, b);
        quad(t, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, 0f, 0f, -1f, r, g, b);
    }

    private static void quad(Tessellator t, float ax, float ay, float az, float bx, float by, float bz, float cx,
        float cy, float cz, float dx, float dy, float dz, float nx, float ny, float nz, float r, float g, float b) {
        emit(t, ax, ay, az, nx, ny, nz, r, g, b);
        emit(t, bx, by, bz, nx, ny, nz, r, g, b);
        emit(t, cx, cy, cz, nx, ny, nz, r, g, b);
        emit(t, ax, ay, az, nx, ny, nz, r, g, b);
        emit(t, cx, cy, cz, nx, ny, nz, r, g, b);
        emit(t, dx, dy, dz, nx, ny, nz, r, g, b);
    }

    private static void bond(Tessellator t, Vec3 from, Vec3 to, int order, boolean cubic) {
        if (order <= 1) {
            cylinder(t, from, to, SINGLE_BOND_RADIUS, cubic);
            return;
        }
        float dx = to.x() - from.x();
        float dy = to.y() - from.y();
        float dz = to.z() - from.z();
        float[] perp = perpDir(dx, dy, dz);
        float px = perp[0];
        float py = perp[1];
        float pz = perp[2];
        if (order == 2) {
            cylinderOffset(
                t,
                from,
                to,
                px * MULTI_BOND_GAP,
                py * MULTI_BOND_GAP,
                pz * MULTI_BOND_GAP,
                MULTI_BOND_RADIUS,
                cubic);
            cylinderOffset(
                t,
                from,
                to,
                -px * MULTI_BOND_GAP,
                -py * MULTI_BOND_GAP,
                -pz * MULTI_BOND_GAP,
                MULTI_BOND_RADIUS,
                cubic);
        } else {
            cylinder(t, from, to, MULTI_BOND_RADIUS, cubic);
            cylinderOffset(
                t,
                from,
                to,
                px * MULTI_BOND_GAP * 2f,
                py * MULTI_BOND_GAP * 2f,
                pz * MULTI_BOND_GAP * 2f,
                MULTI_BOND_RADIUS,
                cubic);
            cylinderOffset(
                t,
                from,
                to,
                -px * MULTI_BOND_GAP * 2f,
                -py * MULTI_BOND_GAP * 2f,
                -pz * MULTI_BOND_GAP * 2f,
                MULTI_BOND_RADIUS,
                cubic);
        }
    }

    private static void cylinderOffset(Tessellator t, Vec3 from, Vec3 to, float ox, float oy, float oz, float radius,
        boolean cubic) {
        cylinderLine(
            t,
            from.x() + ox,
            from.y() + oy,
            from.z() + oz,
            to.x() + ox,
            to.y() + oy,
            to.z() + oz,
            radius,
            cubic);
    }

    private static void cylinder(Tessellator t, Vec3 from, Vec3 to, float radius, boolean cubic) {
        cylinderLine(t, from.x(), from.y(), from.z(), to.x(), to.y(), to.z(), radius, cubic);
    }

    private static void cylinderLine(Tessellator t, float fx, float fy, float fz, float tx, float ty, float tz,
        float radius, boolean square) {
        float dx = tx - fx, dy = ty - fy, dz = tz - fz;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.0001f) return;

        float nx = dx / len, ny = dy / len, nz = dz / len;

        float ax = Math.abs(nx), ay = Math.abs(ny), az = Math.abs(nz);
        float refx, refy, refz;
        if (ax <= ay && ax <= az) {
            refx = 1f;
            refy = 0f;
            refz = 0f;
        } else if (ay <= az) {
            refx = 0f;
            refy = 1f;
            refz = 0f;
        } else {
            refx = 0f;
            refy = 0f;
            refz = 1f;
        }

        float ux = ny * refz - nz * refy;
        float uy = nz * refx - nx * refz;
        float uz = nx * refy - ny * refx;
        float ul = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul;
        uy /= ul;
        uz /= ul;

        float rx = ny * uz - nz * uy;
        float ry = nz * ux - nx * uz;
        float rz = nx * uy - ny * ux;

        int segments = square ? 4 : CYL_SEGMENTS;
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (2.0 * Math.PI * i / segments);
            float a2 = (float) (2.0 * Math.PI * (i + 1) / segments);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float c2 = (float) Math.cos(a2), s2 = (float) Math.sin(a2);

            float nn1x = rx * c1 + ux * s1, nn1y = ry * c1 + uy * s1, nn1z = rz * c1 + uz * s1;
            float nn1l = (float) Math.sqrt(nn1x * nn1x + nn1y * nn1y + nn1z * nn1z);
            nn1x /= nn1l;
            nn1y /= nn1l;
            nn1z /= nn1l;

            float nn2x = rx * c2 + ux * s2, nn2y = ry * c2 + uy * s2, nn2z = rz * c2 + uz * s2;
            float nn2l = (float) Math.sqrt(nn2x * nn2x + nn2y * nn2y + nn2z * nn2z);
            nn2x /= nn2l;
            nn2y /= nn2l;
            nn2z /= nn2l;

            float f1x = nn1x, f1y = nn1y, f1z = nn1z;
            float f2x = nn2x, f2y = nn2y, f2z = nn2z;
            if (square) {
                float am = (a1 + a2) * 0.5f;
                float cm = (float) Math.cos(am), sm = (float) Math.sin(am);
                float qx = rx * cm + ux * sm, qy = ry * cm + uy * sm, qz = rz * cm + uz * sm;
                float ql = (float) Math.sqrt(qx * qx + qy * qy + qz * qz);
                f1x = f2x = qx / ql;
                f1y = f2y = qy / ql;
                f1z = f2z = qz / ql;
            }

            float p1ax = fx + (rx * c1 + ux * s1) * radius;
            float p1ay = fy + (ry * c1 + uy * s1) * radius;
            float p1az = fz + (rz * c1 + uz * s1) * radius;
            float p1bx = fx + (rx * c2 + ux * s2) * radius;
            float p1by = fy + (ry * c2 + uy * s2) * radius;
            float p1bz = fz + (rz * c2 + uz * s2) * radius;
            float p2ax = tx + (rx * c1 + ux * s1) * radius;
            float p2ay = ty + (ry * c1 + uy * s1) * radius;
            float p2az = tz + (rz * c1 + uz * s1) * radius;
            float p2bx = tx + (rx * c2 + ux * s2) * radius;
            float p2by = ty + (ry * c2 + uy * s2) * radius;
            float p2bz = tz + (rz * c2 + uz * s2) * radius;

            emit(t, p1ax, p1ay, p1az, f1x, f1y, f1z, BOND_R, BOND_G, BOND_B);
            emit(t, p2ax, p2ay, p2az, f1x, f1y, f1z, BOND_R, BOND_G, BOND_B);
            emit(t, p2bx, p2by, p2bz, f2x, f2y, f2z, BOND_R, BOND_G, BOND_B);

            emit(t, p1ax, p1ay, p1az, f1x, f1y, f1z, BOND_R, BOND_G, BOND_B);
            emit(t, p2bx, p2by, p2bz, f2x, f2y, f2z, BOND_R, BOND_G, BOND_B);
            emit(t, p1bx, p1by, p1bz, f2x, f2y, f2z, BOND_R, BOND_G, BOND_B);
        }
    }

    private static void emit(Tessellator t, float px, float py, float pz, float nx, float ny, float nz, float r,
        float g, float b) {
        float diff = Math.max(0f, nx * LIGHT_X + ny * LIGHT_Y + nz * LIGHT_Z);
        float brightness = AMBIENT + (1f - AMBIENT) * diff;
        t.setColorRGBA_F(r * brightness, g * brightness, b * brightness, 1f);
        t.addVertex(px, py, pz);
    }
}
