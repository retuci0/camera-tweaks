package me.retucio.camtweaks.util.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.retucio.camtweaks.CameraTweaks;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.OptionalDouble;
import java.util.function.Function;


public class Pipelines {

    static final RenderPipeline GLOBAL_LINES_PIPELINE = RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation(Identifier.of(CameraTweaks.MOD_ID, "pipeline/global_lines"))
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.LINES)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    static final RenderPipeline GLOBAL_QUADS_PIPELINE = RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation("pipeline/global_fill_pipeline")
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    static final Function<Double, RenderLayer> GLOBAL_LINES_LAYER = Util.memoize(lineWidth -> {
        RenderPhase.LineWidth lineState = new RenderPhase.LineWidth(OptionalDouble.of(lineWidth));
        RenderLayer.MultiPhaseParameters compositeState = RenderLayer.MultiPhaseParameters.builder()
                .lineWidth(lineState)
                .build(false);
        return RenderLayer.of("global_lines", RenderLayer.DEFAULT_BUFFER_SIZE, GLOBAL_LINES_PIPELINE, compositeState);
    });




    public static RenderLayer getGlobalLinesLayer(double lineWidth) {
        return GLOBAL_LINES_LAYER.apply(lineWidth);
    }
}