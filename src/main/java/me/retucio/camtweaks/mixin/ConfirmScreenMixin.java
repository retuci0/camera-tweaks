package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.RPackBypass;
import me.retucio.camtweaks.util.interfaces.IConfirmScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmScreen.class)
public abstract class ConfirmScreenMixin extends Screen implements IConfirmScreen {

    @Shadow protected DirectionalLayoutWidget layout;
    @Unique private Runnable bypassAction = null;

    protected ConfirmScreenMixin(final Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ConfirmScreen;addButtons(Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;)V", shift = At.Shift.AFTER))
    protected void addBypassButton(CallbackInfo ci) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (this.bypassAction != null && bypassPack.isEnabled())
            this.layout.add(ButtonWidget.builder(bypassPack.BYPASS_TEXT, $ -> this.bypassAction.run()).build());
    }

    @Override
    public void smegma$setBypassAction(Runnable bypass) {
        this.bypassAction = bypass;
    }
}