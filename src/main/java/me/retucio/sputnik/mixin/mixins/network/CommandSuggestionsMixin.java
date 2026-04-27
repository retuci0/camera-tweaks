package me.retucio.sputnik.mixin.mixins.network;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import me.retucio.sputnik.command.CommandManager;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

import static me.retucio.sputnik.Sputnik.mc;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    @Shadow
    private ParseResults<SharedSuggestionProvider> currentParse;

    @Shadow @Final
    private EditBox input;

    @Shadow
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    private boolean keepSuggestions;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    protected abstract void updateUsageInfo(ParseResults<SharedSuggestionProvider> currentParse, Suggestions suggestions);

    @Inject(
            method = "updateCommandInfo",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false),
            cancellable = true
    )
    private void onRefresh(CallbackInfo ci, @Local(name = "reader") StringReader reader) {
        String prefix = CommandManager.INSTANCE.getPrefix();
        int length = prefix.length();

        if (reader.canRead(length) && reader.getString().startsWith(prefix, reader.getCursor())) {
            reader.setCursor(reader.getCursor() + length);

            if (currentParse == null) {
                currentParse = CommandManager.dispatcher.parse(reader, mc.getConnection().getSuggestionsProvider());
            }

            int cursor = input.getCursorPosition();
            if (cursor >= length && (suggestions == null || !keepSuggestions)) {
                pendingSuggestions = CommandManager.dispatcher.getCompletionSuggestions(currentParse, cursor);
                pendingSuggestions.thenAccept(suggestionResult -> {
                    if (pendingSuggestions.isDone()) {
                        updateUsageInfo(currentParse, suggestionResult);
                    }
                });
            }
            ci.cancel();
        }
    }
}