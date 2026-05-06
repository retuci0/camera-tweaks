package me.retucio.sputnik.util.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.module.modules.render.GlintPlus;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/** utilidad para crear RenderTypes de glint con color personalizado
 * @see GlintPlus
 * yoinkeado de: https://github.com/Pepperoni-Jabroni/NoMorePurple/blob/main/src/main/java/pepjebs/no_more_purple/client/GlintRenderLayer.java
 * (y actualizado)
 */

public class GlintRenderType {

    public static final List<RenderType> glintColor = newRenderList(GlintRenderType::buildGlintRenderType);
    public static final List<RenderType> entityGlintColor = newRenderList(GlintRenderType::buildEntityGlintRenderType);
    public static final List<RenderType> armorEntityGlintColor = newRenderList(GlintRenderType::buildArmorEntityGlintRenderType);
    public static final List<RenderType> glintTranslucentColor = newRenderList(GlintRenderType::buildGlintTranslucentRenderType);

    public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map) {
        addGlintTypes(map, glintColor);
        addGlintTypes(map, glintTranslucentColor);
        addGlintTypes(map, entityGlintColor);
        addGlintTypes(map, armorEntityGlintColor);
    }

    private static List<RenderType> newRenderList(Function<String, RenderType> func) {
        ArrayList<RenderType> list = new ArrayList<>(DyeColor.values().length + 2);

        for (DyeColor color : DyeColor.values())
            list.add(func.apply(color.name()));

        list.add(func.apply("rainbow"));
        list.add(func.apply("none"));

        return list;
    }

    public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, List<RenderType> typeList) {
        for (RenderType renderType : typeList)
            if (!map.containsKey(renderType))
                map.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
    }

    private static RenderType buildGlintRenderType(String name) {
        final Identifier res = Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");
        return RenderType.create(
                "glint_" + name,
                RenderSetup.builder(RenderPipelines.GLINT)
                        .withTexture("Sampler0", res)
                        .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                        .createRenderSetup()
        );
    }

    private static RenderType buildGlintTranslucentRenderType(String name) {
        final Identifier res = Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");
        return RenderType.create(
                "glint_translucent_" + name,
                RenderSetup.builder(RenderPipelines.GLINT)
                        .withTexture("Sampler0", res)
                        .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                        .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                        .createRenderSetup()
        );
    }

    private static RenderType buildEntityGlintRenderType(String name) {
        final Identifier res = Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");
        return RenderType.create(
                "entity_glint_" + name,
                RenderSetup.builder(RenderPipelines.GLINT)
                        .withTexture("Sampler0", res)
                        .setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
                        .createRenderSetup()
        );
    }

    private static RenderType buildArmorEntityGlintRenderType(String name) {
        final Identifier res = Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "textures/misc/glint_" + name.toLowerCase() + ".png");
        return RenderType.create(
                "armor_glint_" + name,
                RenderSetup.builder(RenderPipelines.GLINT)
                        .withTexture("Sampler0", res)
                        .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .createRenderSetup()
        );
    }
}