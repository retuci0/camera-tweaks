package me.retucio.camtweaks.util.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.module.modules.render.GlintPlus;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** clase modificada de RenderLayer para modificar el color del destello de encantamientos
 * @see GlintPlus
 * yoinkeado de: https://github.com/Pepperoni-Jabroni/NoMorePurple/blob/main/src/main/java/pepjebs/no_more_purple/client/GlintRenderLayer.java
 * (y actualizado)
 */

public class GlintRenderLayer extends RenderLayer {

    public static List<RenderLayer> glintColor = newRenderList(GlintRenderLayer::buildGlintRenderLayer);
    public static List<RenderLayer> entityGlintColor = newRenderList(GlintRenderLayer::buildEntityGlintRenderLayer);
    public static List<RenderLayer> armorEntityGlintColor = newRenderList(GlintRenderLayer::buildArmorEntityGlintRenderLayer);

    public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> map) {
        addGlintTypes(map, glintColor);
        addGlintTypes(map, entityGlintColor);
        addGlintTypes(map, armorEntityGlintColor);
    }

    public GlintRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, RenderSetup.builder(RenderPipeline.builder().build()).build());
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

        return RenderLayer.of("glint_" + name, RenderSetup.builder(
                        RenderPipeline.builder()
                                .withCull(false)
                                .withDepthTestFunction(DepthTestFunction.EQUAL_DEPTH_TEST)
                                .withBlend(BlendFunction.GLINT)
                                .withLocation(RenderPipelines.GLINT.getLocation())
                                .withVertexShader(RenderPipelines.GLINT.getVertexShader())
                                .withFragmentShader(RenderPipelines.GLINT.getFragmentShader())
                                .withVertexFormat(RenderPipelines.GLINT.getVertexFormat(), RenderPipelines.GLINT.getVertexFormatMode())
                                .build())
                .texture("glint_" + name, res)
                .textureTransform(TextureTransform.GLINT_TEXTURING)
                .translucent()
                .build());

    }

    private static RenderLayer buildEntityGlintRenderLayer(String name) {
        final Identifier res = Identifier.of(CameraTweaks.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");

        return RenderLayer.of("entity_glint_" + name, RenderSetup.builder(
                        RenderPipeline.builder()
                                .withCull(true)
                                .withDepthTestFunction(DepthTestFunction.EQUAL_DEPTH_TEST)
                                .withBlend(BlendFunction.GLINT)
                                .withLocation(RenderPipelines.GLINT.getLocation())
                                .withVertexShader(RenderPipelines.GLINT.getVertexShader())
                                .withFragmentShader(RenderPipelines.GLINT.getFragmentShader())
                                .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
                                .build())
                .texture("entity_glint_" + name, res)
                .textureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
                .outputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                .translucent()
                .build());
    }


    private static RenderLayer buildArmorEntityGlintRenderLayer(String name) {
        final Identifier res = Identifier.of(CameraTweaks.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");

        return RenderLayer.of("armor_glint_" + name, RenderSetup.builder(
                        RenderPipeline.builder()
                                .withCull(false)
                                .withDepthTestFunction(DepthTestFunction.EQUAL_DEPTH_TEST)
                                .withLocation(RenderPipelines.GLINT.getLocation())
                                .withVertexShader(RenderPipelines.GLINT.getVertexShader())
                                .withFragmentShader(RenderPipelines.GLINT.getFragmentShader())
                                .withVertexFormat(RenderPipelines.GLINT.getVertexFormat(), RenderPipelines.GLINT.getVertexFormatMode())
                                .build())
                .texture("armor_glint_" + name, res)
                .textureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
                .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .translucent()
                .build());
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