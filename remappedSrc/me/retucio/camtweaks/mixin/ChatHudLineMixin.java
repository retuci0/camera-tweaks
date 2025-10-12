package me.retucio.camtweaks.mixin;

import com.mojang.authlib.GameProfile;
import me.retucio.camtweaks.util.interfaces.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.class)
public class ChatHudLineMixin implements IChatHudLine {

    @Shadow @Final
    private Text content;

    @Unique
    private int id;

    @Unique
    private GameProfile sender;

    @Override
    public String smegma$getText() { return content.toString(); }

    @Override
    public int smegma$getId() { return id; }

    @Override
    public void smegma$setId(int id) { this.id = id; }

    @Override
    public GameProfile smegma$getSender() { return sender; }

    @Override
    public void smegma$setSender(GameProfile profile) { this.sender = profile; }
}
