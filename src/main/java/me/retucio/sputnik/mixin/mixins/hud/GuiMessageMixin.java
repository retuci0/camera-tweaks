package me.retucio.sputnik.mixin.mixins.hud;

import com.mojang.authlib.GameProfile;
import me.retucio.sputnik.util.interfaces.IGuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.class)
public class GuiMessageMixin implements IGuiMessage {

    @Shadow @Final
    private Component content;

    @Unique
    private int id;

    @Unique
    private GameProfile sender;

    @Override
    public String sputnik$getText() { return content.toString(); }

    @Override
    public int sputnik$getId() { return id; }

    @Override
    public void sputnik$setId(int id) { this.id = id; }

    @Override
    public GameProfile sputnik$getSender() { return sender; }

    @Override
    public void sputnik$setSender(GameProfile profile) { this.sender = profile; }
}
