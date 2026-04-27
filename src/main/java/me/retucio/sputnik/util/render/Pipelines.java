package me.retucio.sputnik.util.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import me.retucio.sputnik.Sputnik;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Pipelines {

    static List<RenderPipeline> PIPELINES = new ArrayList<>();

    static final RenderPipeline LINES_PIPELINE = add(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "pipeline/lines"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
//            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//            .withDepthWrite(false)
            .withCull(false)
            .build());

    static final RenderPipeline LINES_CULL_PIPELINE = add(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "pipeline/lines_cull"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
//            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
//            .withDepthWrite(false)
            .withCull(true)
            .build());

    static final RenderPipeline QUADS_PIPELINE = add(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "pipeline/quads"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
//            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//            .withBlend(BlendFunction.TRANSLUCENT)
//            .withDepthWrite(false)
            .withCull(false)
            .build());

    static final RenderPipeline QUADS_CULL_PIPELINE = add(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "pipeline/quads_cull"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
//            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
//            .withBlend(BlendFunction.TRANSLUCENT)
//            .withDepthWrite(false)
            .withCull(false)
            .build());

    static final RenderPipeline TEXT_PIPELINE = add(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "pipeline/text"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLES)
            .withVertexShader(Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "shaders/text.vert"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "shaders/text.frag"))
            .withSampler("u_Texture")
//            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//            .withDepthWrite(false)
//            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build());

    public static void init() {
        for (RenderPipeline pipeline : PIPELINES) {
            precompile(pipeline);
        }
    }

    private static RenderPipeline add(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    public static void precompile(RenderPipeline pipeline) {
        GpuDevice device = RenderSystem.getDevice();
        ResourceManager resources = Minecraft.getInstance().getResourceManager();

        device.precompilePipeline(pipeline, (identifier, shaderType) -> {
            Resource resource = resources.getResource(identifier).get();

            try (InputStream is = resource.open()) {
                return IOUtils.toString(is, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}