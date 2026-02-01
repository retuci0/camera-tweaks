package me.retucio.sputnik.util.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;

import me.retucio.sputnik.Sputnik;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Pipelines {

    static List<RenderPipeline> PIPELINES = new ArrayList<>();

    static final RenderPipeline LINES_PIPELINE = add(RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation(Identifier.of(Sputnik.MOD_ID, "pipeline/lines"))
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.DrawMode.LINES)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build());

    static final RenderPipeline LINES_CULL_PIPELINE = add(RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation(Identifier.of(Sputnik.MOD_ID, "pipeline/lines_cull"))
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.DrawMode.LINES)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(true)
            .build());

    static final RenderPipeline QUADS_PIPELINE = add(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of(Sputnik.MOD_ID, "pipeline/quads"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .build());

    static final RenderPipeline QUADS_CULL_PIPELINE = add(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of(Sputnik.MOD_ID, "pipeline/quads_cull"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .build());

    static final RenderPipeline TEXT_PIPELINE = add(RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
            .withLocation(Identifier.of(Sputnik.MOD_ID, "pipeline/text"))
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.TRIANGLES)
            .withVertexShader(Identifier.of(Sputnik.MOD_ID, "shaders/text.vert"))
            .withFragmentShader(Identifier.of(Sputnik.MOD_ID, "shaders/text.frag"))
            .withSampler("u_Texture")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
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
        ResourceManager resources = MinecraftClient.getInstance().getResourceManager();

        device.precompilePipeline(pipeline, (identifier, shaderType) -> {
            Resource resource = resources.getResource(identifier).get();

            try (InputStream in = resource.getInputStream()) {
                return IOUtils.toString(in, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}