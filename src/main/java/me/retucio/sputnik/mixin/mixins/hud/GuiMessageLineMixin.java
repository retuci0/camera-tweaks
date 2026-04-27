package me.retucio.sputnik.mixin.mixins.hud;

import com.mojang.authlib.GameProfile;
import me.retucio.sputnik.util.interfaces.IGuiMessageLine;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.Line.class)
public class GuiMessageLineMixin implements IGuiMessageLine {

    @Shadow @Final
    private FormattedCharSequence content;

    @Unique
    private int id;

    @Unique
    private GameProfile sender;

    @Unique
    private boolean startOfEntry;

    @Override
    public String sputnik$getText() {
        StringBuilder sb = new StringBuilder();

        content.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }

    @Override
    public int sputnik$getId() { return id; }

    @Override
    public void sputnik$setId(int id) { this.id = id; }

    @Override
    public GameProfile sputnik$getSender() { return sender; }

    @Override
    public void sputnik$setSender(GameProfile profile) { this.sender = profile; }

    @Override
    public boolean sputnik$isStartOfEntry() { return startOfEntry; }

    @Override
    public void sputnik$setStartOfEntry(boolean start) { this.startOfEntry = start; }
}