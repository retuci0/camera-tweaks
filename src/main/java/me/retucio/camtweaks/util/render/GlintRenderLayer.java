package me.retucio.camtweaks.util.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import me.retucio.camtweaks.CameraTweaks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** clase modificada de RenderLayer para modificar el color del destello de encantamientos
 * @see me.retucio.camtweaks.module.modules.GlintPlus
 * yoinkeado de: https://github.com/Pepperoni-Jabroni/NoMorePurple/blob/main/src/main/java/pepjebs/no_more_purple/client/GlintRenderLayer.java
 * (y actualizado)
*/

public class GlintRenderLayer extends RenderLayer {

    public static List<RenderLayer> glintColor = newRenderList(GlintRenderLayer::buildGlintRenderLayer);
    public static List<RenderLayer> entityGlintColor = newRenderList(GlintRenderLayer::buildEntityGlintRenderLayer);
    public static List<RenderLayer> armorGlintColor = newRenderList(GlintRenderLayer::buildArmorGlintRenderLayer);
    public static List<RenderLayer> armorEntityGlintColor = newRenderList(GlintRenderLayer::buildArmorEntityGlintRenderLayer);

    public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> map) {
        addGlintTypes(map, glintColor);
        addGlintTypes(map, entityGlintColor);
        addGlintTypes(map, armorGlintColor);
        addGlintTypes(map, armorEntityGlintColor);
    }

    public GlintRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    private static List<RenderLayer> newRenderList(Function<String, RenderLayer> func) {
        ArrayList<RenderLayer> list = new ArrayList<>(DyeColor.values().length);

        for (DyeColor color : DyeColor.values())
            list.add(func.apply(color.name()));

        list.add(func.apply("rainbow"));
        list.add(func.apply("none"));

        return list;
    }

    public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> map, List<RenderLayer> typeList) {
        for(RenderLayer renderType : typeList)
            if (!map.containsKey(renderType))
                map.put(renderType, new BufferAllocator(renderType.getExpectedBufferSize()));
    }

    private static RenderLayer buildGlintRenderLayer(String name) {
        final Identifier res = Identifier.of(CameraTweaks.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");

        return RenderLayer.of("glint_" + name, 256, RenderPipelines.GLINT, MultiPhaseParameters.builder()
                .texturing(RenderPhase.GLINT_TEXTURING)
                .texture(new Texture(res, false))
                .texturing(GLINT_TEXTURING)
                .build(false));
    }

    private static RenderLayer buildEntityGlintRenderLayer(String name) {
        final Identifier res = Identifier.of(CameraTweaks.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");

        return RenderLayer.of("entity_glint_" + name, 256, RenderPipelines.GLINT, MultiPhaseParameters.builder()
                .texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
                .texture(new Texture(res, false))
                .target(ITEM_ENTITY_TARGET)
                .texturing(ENTITY_GLINT_TEXTURING)
                .build(false));
    }


    private static RenderLayer buildArmorGlintRenderLayer(String name) {
        final Identifier res = Identifier.of(CameraTweaks.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");

        return RenderLayer.of("armor_glint_" + name, 256, RenderPipelines.GLINT, MultiPhaseParameters.builder()
                .texturing(RenderPhase.ARMOR_ENTITY_GLINT_TEXTURING)
                .texture(new Texture(res, false))
                .texturing(GLINT_TEXTURING)
                .layering(VIEW_OFFSET_Z_LAYERING)
                .build(false));
    }

    private static RenderLayer buildArmorEntityGlintRenderLayer(String name) {
        final Identifier res = Identifier.of(CameraTweaks.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");

        return RenderLayer.of("armor_entity_glint_" + name, 256, RenderPipelines.GLINT, MultiPhaseParameters.builder()
                .texturing(RenderPhase.ARMOR_ENTITY_GLINT_TEXTURING)
                .texture(new Texture(res, false))
                .texturing(ENTITY_GLINT_TEXTURING)
                .layering(VIEW_OFFSET_Z_LAYERING)
                .build(false));
    }

    @Override
    public void draw(BuiltBuffer buffer) {}

    @Override
    public VertexFormat getVertexFormat() { return VertexFormat.builder().build(); }

    @Override
    public VertexFormat.DrawMode getDrawMode() {
        return null;
    }

    @Override
    public RenderPipeline getRenderPipeline() {
        return null;
    }
}