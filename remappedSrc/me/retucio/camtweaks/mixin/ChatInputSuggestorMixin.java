package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import me.retucio.camtweaks.command.CommandManager;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {

    @Shadow
    private ParseResults<CommandSource> parse;

    @Shadow @Final
    TextFieldWidget textField;

    @Shadow
    private ChatInputSuggestor.SuggestionWindow window;

    @Shadow
    boolean completingSuggestions;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow protected abstract void showCommandSuggestions();

    @Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false), cancellable = true)
    private void onRefresh(CallbackInfo ci, @Local StringReader reader) {
        String prefix = CommandManager.INSTANCE.getPrefix();
        int length = prefix.length();

        if (reader.canRead(length) && reader.getString().startsWith(prefix, reader.getCursor())) {
            reader.setCursor(reader.getCursor() + length);

            if (parse == null) this.parse = CommandManager.dispatcher.parse(reader, mc.getNetworkHandler().getCommandSource());

            int cursor = textField.getCursor();
            if (cursor >= length && (window == null || !completingSuggestions)) {
                pendingSuggestions = CommandManager.dispatcher.getCompletionSuggestions(parse, cursor);
                pendingSuggestions.thenRun(() -> {
                    if (pendingSuggestions.isDone()) showCommandSuggestions();
                });
            }
            ci.cancel();
        }
    }
}
