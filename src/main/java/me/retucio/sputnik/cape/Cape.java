package me.retucio.sputnik.cape;

import me.retucio.sputnik.Sputnik;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;


public class Cape {

    private final Minecraft mc = Minecraft.getInstance();

    private final String name;
    private final Identifier id;
    private NativeImage img;
    private DynamicTexture texture;

    public Cape(String name) {
        this.name = name;
        this.id = Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "textures/capes/" + name + ".png");
    }

    public void load() {
        this.img = loadTexture(this.id);
    }

    public NativeImage loadTexture(Identifier id) {
        try (InputStream inputStream = mc.getResourceManager().getResource(id).get().open()) {
            return NativeImage.read(inputStream);
        } catch (IOException e) {
            Sputnik.LOGGER.error("ups: {}", e.getMessage());
            return null;
        }
    }

    public void register() {
        texture = new DynamicTexture(null, img);
        mc.getTextureManager().register(id, texture);
        img = null;
    }

    public String getName() {
        return name;
    }

    public Identifier getId() {
        return id;
    }

    public DynamicTexture getTexture() {
        return texture;
    }

    @Override
    public String toString() {
        return name.replace('-', ' ');
    }
}