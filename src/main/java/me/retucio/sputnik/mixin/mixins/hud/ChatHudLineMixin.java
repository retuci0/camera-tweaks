package me.retucio.sputnik.mixin.mixins.hud;

import com.mojang.authlib.GameProfile;
import me.retucio.sputnik.util.interfaces.IChatHudLine;
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
