package me.retucio.sputnik.util.render;

import com.mojang.blaze3d.platform.NativeImage;
import me.retucio.sputnik.Sputnik;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.util.ArrayList;
import java.util.List;


public class Textures {

    public static final Minecraft mc = Minecraft.getInstance();

    public static final List<Identifier> textures = new ArrayList<>();

    public static final Identifier SEARCH_BAR_FILTER = texture("gui/filter_icon");

    public static Identifier texture(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "textures/" + path + ".png");
        textures.add(id);
        return id;
    }

    public static boolean register(Identifier id) {
        if (!textures.contains(id)) textures.add(id);
        try {
            Resource resource = mc.getResourceManager().getResource(id).orElseThrow();
            NativeImage image = NativeImage.read(resource.open());
            DynamicTexture texture = new DynamicTexture(null, image);
            mc.getTextureManager().register(id, texture);
            return true;
        } catch (Exception e) {
            Sputnik.LOGGER.error("Failed to load texture: {}", id, e);
            return false;
        }
    }

    public static void init() {
        for (Identifier id : textures) {
            register(id);
        }
    }
}
