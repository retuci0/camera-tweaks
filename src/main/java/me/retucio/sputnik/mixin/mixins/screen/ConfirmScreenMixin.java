package me.retucio.sputnik.mixin.mixins.screen;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.network.RPackBypass;
import me.retucio.sputnik.util.interfaces.IConfirmScreen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ConfirmScreen.class)
public abstract class ConfirmScreenMixin extends Screen implements IConfirmScreen {

    @Final @Shadow protected LinearLayout layout;
    @Unique private Runnable bypassAction = null;

    protected ConfirmScreenMixin(final Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ConfirmScreen;addButtons(Lnet/minecraft/client/gui/layouts/LinearLayout;)V", shift = At.Shift.AFTER))
    protected void addBypassButton(CallbackInfo ci) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (bypassAction != null && bypassPack.isEnabled()) {
            layout.addChild(Button.builder(bypassPack.BYPASS_TEXT, button -> this.bypassAction.run()).build());
        }
    }

    @Override
    public void sputnik$setBypassAction(Runnable bypass) {
        this.bypassAction = bypass;
    }
}