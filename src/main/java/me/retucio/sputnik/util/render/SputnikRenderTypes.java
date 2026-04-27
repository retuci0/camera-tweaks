package me.retucio.sputnik.util.render;

import me.retucio.sputnik.Sputnik;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import static me.retucio.sputnik.util.render.Pipelines.*;


public class SputnikRenderTypes {

    private static final RenderType LINES;
    private static final RenderType LINES_CULL;

    private static final RenderType QUADS;
    private static final RenderType QUADS_CULL;

    private static final RenderType TEXT;


    public static RenderType lines() {
        return LINES;
    }

    public static RenderType linesCull() {
        return LINES_CULL;
    }

    public static RenderType quads() {
        return QUADS;
    }

    public static RenderType quadsCull() {
        return QUADS_CULL;
    }

    public static RenderType text() {
        return TEXT;
    }

    static {
        LINES = RenderType.create(Sputnik.MOD_ID + "_lines", RenderSetup.builder(LINES_PIPELINE).createRenderSetup());
        LINES_CULL = RenderType.create(Sputnik.MOD_ID + "_lines_cull", RenderSetup.builder(LINES_CULL_PIPELINE).createRenderSetup());

        QUADS = RenderType.create(Sputnik.MOD_ID + "_quads", RenderSetup.builder(QUADS_PIPELINE).createRenderSetup());
        QUADS_CULL = RenderType.create(Sputnik.MOD_ID + "_quads_cull", RenderSetup.builder(QUADS_CULL_PIPELINE).createRenderSetup());

        TEXT = RenderType.create(Sputnik.MOD_ID + "_text", RenderSetup.builder(TEXT_PIPELINE).createRenderSetup());
    }

}