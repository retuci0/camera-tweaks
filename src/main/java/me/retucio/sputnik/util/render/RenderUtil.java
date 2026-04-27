package me.retucio.sputnik.util.render;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.awt.*;

// literalmente robado de https://github.com/mioclient/oyvey-ported/ (perdón)
public class RenderUtil {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final Tesselator tessellator = Tesselator.getInstance();


    /* líneas */

    public static void drawLine(PoseStack matrices, Vec3 from, Vec3 to, Color color, float lineWidth) {
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
        Vec3 cam = mc.gameRenderer.getMainCamera().position();

        buffer.addVertex(matrices.last(), (float) (from.x - cam.x), (float) (from.y - cam.y), (float) (from.z - cam.z)).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), (float) (to.x - cam.x), (float) (to.y - cam.y), (float) (to.z - cam.z)).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        SputnikRenderTypes.lines().draw(buffer.buildOrThrow());
    }

    // https://github.com/TheF1xer/GateClient-1.12.2/blob/main/src/main/java/me/thef1xer/gateclient/util/RenderUtil.java
    public static void drawTracer(PoseStack matrices, Vec3 pos, Color color, float lineWidth) {
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
        Camera cam = mc.gameRenderer.getMainCamera();

        Vector3f cameraVector = new Vector3f(0, 0, 1)
                .rotateX((float) Math.toRadians(cam.xRot()))
                .rotateY((float) -Math.toRadians(cam.yRot()));

        buffer.addVertex(matrices.last(), cameraVector.x, cameraVector.y, cameraVector.z).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), (float) (pos.x - cam.position().x), (float) (pos.y - cam.position().y), (float) (pos.z - cam.position().z)).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        SputnikRenderTypes.lines().draw(buffer.buildOrThrow());
    }

    public static void drawVector(PoseStack matrices, Vector3f start, Vec3 direction, Color c, float lineWidth) {
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
        PoseStack.Pose pose = matrices.last();

        int color = c.getRGB();
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = ((color >>  0) & 0xFF) / 255f;

        float endX = start.x() + (float) direction.x;
        float endY = start.y() + (float) direction.y;
        float endZ = start.z() + (float) direction.z;

        buffer.addVertex(pose.pose(), start.x(), start.y(), start.z())
                .setColor(r, g, b, a)
                .setNormal(pose, (float) direction.x, (float) direction.y, (float) direction.z)
                .setLineWidth(lineWidth);

        buffer.addVertex(pose.pose(), endX, endY, endZ)
                .setColor(r, g, b, a)
                .setNormal(pose, (float) direction.x, (float) direction.y, (float) direction.z)
                .setLineWidth(lineWidth);

        SputnikRenderTypes.lines().draw(buffer.buildOrThrow());
    }


    /* caras */

    public static void drawFilledRect(PoseStack matrices, float x, float y, float width, float height, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        matrices.pushPose();
        matrices.translate(0, 0, 0);

        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        buffer.addVertex(matrices.last().pose(), x, y + height, 0).setColor(r, g, b, a);
        buffer.addVertex(matrices.last().pose(), x + width, y + height, 0).setColor(r, g, b, a);
        buffer.addVertex(matrices.last().pose(), x + width, y, 0).setColor(r, g, b, a);
        buffer.addVertex(matrices.last().pose(), x, y, 0).setColor(r, g, b, a);

        SputnikRenderTypes.quads().draw(buffer.buildOrThrow());
        matrices.popPose();
    }

    public static void drawBlockFaceOutlines(PoseStack matrices, BlockPos pos, Direction face, Color color, float lineWidth, boolean cull) {
        if (lineWidth <= 0) return;
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();

        float minX = (float) (pos.getX() - cameraPos.x);
        float minY = (float) (pos.getY() - cameraPos.y);
        float minZ = (float) (pos.getZ() - cameraPos.z);
        float maxX = minX + 1;
        float maxY = minY + 1;
        float maxZ = minZ + 1;

        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);

        switch (face) {
            case UP:
                buffer.addVertex(matrices.last(), minX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), minX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                break;

            case DOWN:
                buffer.addVertex(matrices.last(), minX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), minX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                break;

            case EAST:
                buffer.addVertex(matrices.last(), maxX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                break;

            case WEST:
                buffer.addVertex(matrices.last(), minX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), minX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), minX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), minX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                break;

            case NORTH:
                buffer.addVertex(matrices.last(), minX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), minX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                break;

            case SOUTH:
                buffer.addVertex(matrices.last(), minX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), maxX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), maxX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                buffer.addVertex(matrices.last(), minX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
                buffer.addVertex(matrices.last(), minX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

                break;
        }

        if (cull) SputnikRenderTypes.linesCull().draw(buffer.buildOrThrow());
        else SputnikRenderTypes.lines().draw(buffer.buildOrThrow());
    }

    // expand es para evitar z-fighting
    public static void drawBlockFaceFilled(PoseStack matrices, BlockPos pos, Direction face, Color color, float expand, boolean cull) {
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();

        float minX = (float) (pos.getX() - cameraPos.x);
        float minY = (float) (pos.getY() - cameraPos.y);
        float minZ = (float) (pos.getZ() - cameraPos.z);
        float maxX = minX + 1;
        float maxY = minY + 1;
        float maxZ = minZ + 1;

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        if (expand != 0) {
            switch (face) {
                case DOWN: minY -= expand; maxY = minY + 0.001f; break;
                case UP: minY = maxY; maxY += expand; break;
                case NORTH: minZ -= expand; maxZ = minZ + 0.001f; break;
                case SOUTH: minZ = maxZ; maxZ += expand; break;
                case WEST: minX -= expand; maxX = minX + 0.001f; break;
                case EAST: minX = maxX; maxX += expand; break;
            }
        }

        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        switch (face) {
            case DOWN:
                buffer.addVertex(matrices.last().pose(), minX, minY, minZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, minY, minZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, minY, maxZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), minX, minY, maxZ).setColor(r, g, b, a);
                break;

            case UP:
                buffer.addVertex(matrices.last().pose(), minX, maxY, minZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), minX, maxY, maxZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, maxY, minZ).setColor(r, g, b, a);
                break;

            case NORTH:
                buffer.addVertex(matrices.last().pose(), minX, minY, minZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), minX, maxY, minZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, maxY, minZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, minY, minZ).setColor(r, g, b, a);
                break;

            case SOUTH:
                buffer.addVertex(matrices.last().pose(), minX, minY, maxZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, minY, maxZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), minX, maxY, maxZ).setColor(r, g, b, a);
                break;

            case WEST:
                buffer.addVertex(matrices.last().pose(), minX, minY, minZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), minX, minY, maxZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), minX, maxY, maxZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), minX, maxY, minZ).setColor(r, g, b, a);
                break;

            case EAST:
                buffer.addVertex(matrices.last().pose(), maxX, minY, minZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, maxY, minZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a);
                buffer.addVertex(matrices.last().pose(), maxX, minY, maxZ).setColor(r, g, b, a);
                break;
        }

        if (cull) SputnikRenderTypes.quadsCull().draw(buffer.buildOrThrow());
        else SputnikRenderTypes.quads().draw(buffer.buildOrThrow());
    }


    public static void drawTriangle(PoseStack matrices, float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrices.last().pose(), x1, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(matrices.last().pose(), x2, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrices.last().pose(), x3, y3, 0).setColor(r, g, b, a);
        SputnikRenderTypes.quads().draw(buffer.buildOrThrow());
    }

    // cajas

    public static void drawOutlineBox(PoseStack matrices, AABB box, Color color, float lineWidth, boolean cull) {
        if (lineWidth <= 0) return;
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();

        float minX = (float) (box.minX - cameraPos.x);
        float minY = (float) (box.minY - cameraPos.y);
        float minZ = (float) (box.minZ - cameraPos.z);
        float maxX = (float) (box.maxX - cameraPos.x);
        float maxY = (float) (box.maxY - cameraPos.y);
        float maxZ = (float) (box.maxZ - cameraPos.z);

        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);

        // parte inferior
        buffer.addVertex(matrices.last(), minX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), maxX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        buffer.addVertex(matrices.last(), maxX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), maxX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        buffer.addVertex(matrices.last(), maxX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), minX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        buffer.addVertex(matrices.last(), minX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), minX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        // parte superior
        buffer.addVertex(matrices.last(), minX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), maxX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        buffer.addVertex(matrices.last(), maxX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), maxX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        buffer.addVertex(matrices.last(), maxX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), minX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        buffer.addVertex(matrices.last(), minX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), minX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        // líneas verticales
        buffer.addVertex(matrices.last(), minX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), minX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        buffer.addVertex(matrices.last(), maxX, minY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), maxX, maxY, minZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        buffer.addVertex(matrices.last(), maxX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), maxX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        buffer.addVertex(matrices.last(), minX, minY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);
        buffer.addVertex(matrices.last(), minX, maxY, maxZ).setColor(color.getRGB()).setNormal(-1, -1, -1).setLineWidth(lineWidth);

        if (cull) SputnikRenderTypes.linesCull().draw(buffer.buildOrThrow());
        else SputnikRenderTypes.lines().draw(buffer.buildOrThrow());
    }

    public static void drawFilledBox(PoseStack matrices, AABB box, Color color, boolean cull) {
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();

        float minX = (float) (box.minX - cameraPos.x);
        float minY = (float) (box.minY - cameraPos.y);
        float minZ = (float) (box.minZ - cameraPos.z);
        float maxX = (float) (box.maxX - cameraPos.x);
        float maxY = (float) (box.maxY - cameraPos.y);
        float maxZ = (float) (box.maxZ - cameraPos.z);

        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        buffer.addVertex(matrices.last().pose(), minX, minY, minZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, minY, minZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, minY, maxZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), minX, minY, maxZ).setColor(color.getRGB());

        buffer.addVertex(matrices.last().pose(), minX, maxY, minZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), minX, maxY, maxZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, maxY, maxZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, maxY, minZ).setColor(color.getRGB());

        buffer.addVertex(matrices.last().pose(), minX, minY, minZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), minX, maxY, minZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, maxY, minZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, minY, minZ).setColor(color.getRGB());

        buffer.addVertex(matrices.last().pose(), maxX, minY, minZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, maxY, minZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, maxY, maxZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, minY, maxZ).setColor(color.getRGB());

        buffer.addVertex(matrices.last().pose(), minX, minY, maxZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, minY, maxZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), maxX, maxY, maxZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), minX, maxY, maxZ).setColor(color.getRGB());

        buffer.addVertex(matrices.last().pose(), minX, minY, minZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), minX, minY, maxZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), minX, maxY, maxZ).setColor(color.getRGB());
        buffer.addVertex(matrices.last().pose(), minX, maxY, minZ).setColor(color.getRGB());

        if (cull) SputnikRenderTypes.quadsCull().draw(buffer.buildOrThrow());
        else SputnikRenderTypes.quads().draw(buffer.buildOrThrow());
    }


    // formas custom

    public static void drawVoxelShapeOutline(PoseStack matrices, VoxelShape voxelShape, BlockPos blockPos, Color color, float lineWidth, boolean cull) {
        if (voxelShape.isEmpty()) return;

        voxelShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            AABB box = new AABB(
                    blockPos.getX() + minX, blockPos.getY() + minY, blockPos.getZ() + minZ,
                    blockPos.getX() + maxX, blockPos.getY() + maxY, blockPos.getZ() + maxZ
            );

            drawOutlineBox(matrices, box, color, lineWidth, cull);
        });
    }

    public static void drawVoxelShapeFilled(PoseStack matrices, VoxelShape voxelShape, BlockPos pos, Color color, boolean cull) {
        if (voxelShape.isEmpty()) return;

        voxelShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            AABB box = new AABB(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            );

            drawFilledBox(matrices, box, color, cull);
        });
    }


    // bloques

    public static void drawBlockOutline(PoseStack matrices, BlockPos pos, Color color, float lineWidth, boolean cull) {
        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        drawOutlineBox(matrices, box, color, lineWidth, cull);
    }

    public static void drawBlockFilled(PoseStack matrices, BlockPos pos, Color color, boolean cull) {
        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        drawFilledBox(matrices, box, color, cull);
    }




    public static PoseStack matrixFrom(Vec3 pos) {
        PoseStack matrices = new PoseStack();

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.position();

        matrices.mulPose(Axis.XP.rotationDegrees(camera.yRot()));
        matrices.mulPose(Axis.YP.rotationDegrees(camera.xRot() + 180.0f));

        matrices.translate(
                pos.x() - camPos.x(),
                pos.y() - camPos.y(),
                pos.z() - camPos.z()
        );

        return matrices;
    }
}
