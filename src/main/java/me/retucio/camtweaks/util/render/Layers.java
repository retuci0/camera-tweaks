package me.retucio.camtweaks.util.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;

import static me.retucio.camtweaks.util.render.Pipelines.GLOBAL_LINES_PIPELINE;
import static me.retucio.camtweaks.util.render.Pipelines.GLOBAL_QUADS_PIPELINE;

public class Layers {

    private static final RenderLayer GLOBAL_QUADS;
    private static final RenderLayer GLOBAL_LINES;

    public static RenderLayer getGlobalLines() {
        return GLOBAL_LINES;
    }
    public static RenderLayer getGlobalQuads() {
        return GLOBAL_QUADS;
    }

    static {
        GLOBAL_QUADS = RenderLayer.of("global_fill", RenderSetup.builder(GLOBAL_QUADS_PIPELINE).build());
        GLOBAL_LINES = RenderLayer.of("global_lines", RenderSetup.builder(GLOBAL_LINES_PIPELINE).build());
    }
}