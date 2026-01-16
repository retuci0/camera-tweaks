package me.retucio.sputnik.util.render;

import me.retucio.sputnik.Sputnik;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Textures {

    public static final MinecraftClient mc = MinecraftClient.getInstance();

    public static final List<Identifier> textures = new ArrayList<>();

    public static final Identifier SEARCH_BAR_FILTER = texture("gui/filter_icon");

    public static Identifier texture(String path) {
        Identifier id = Identifier.of(Sputnik.MOD_ID, "textures/" + path + ".png");
        textures.add(id);
        return id;
    }

    public static boolean register(Identifier id) {
        if (!textures.contains(id)) textures.add(id);
        if (mc.getTextureManager() != null) {
            try {
                Resource resource = mc.getResourceManager().getResource(id).orElseThrow();
                NativeImage image = NativeImage.read(resource.getInputStream());
                NativeImageBackedTexture texture = new NativeImageBackedTexture(null, image);
                mc.getTextureManager().registerTexture(id, texture);
                return true;
            } catch (Exception e) {
                Sputnik.LOGGER.error("Failed to load texture: {}", id, e);
                return false;
            }
        }
        return false;
    }

    public static void registerAll() {
        for (Identifier id : textures) {
            register(id);
        }
    }
}
