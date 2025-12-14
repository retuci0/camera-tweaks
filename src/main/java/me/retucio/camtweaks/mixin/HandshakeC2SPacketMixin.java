package me.retucio.camtweaks.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.BungeecordSpoofer;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(HandshakeC2SPacket.class)
public abstract class HandshakeC2SPacketMixin {

    @Shadow @Mutable @Final
    private String address;

    @Unique
    private final Gson gson = new Gson();

    @Unique
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Shadow
    public abstract ConnectionIntent intendedState();

    @SuppressWarnings("deprecation")
    @Inject(method = "<init>(ILjava/lang/String;ILnet/minecraft/network/packet/c2s/handshake/ConnectionIntent;)V", at = @At("RETURN"))
    private void onHandshakeC2SPacket(int i, String string, int j, ConnectionIntent connectionIntent, CallbackInfo ci) {
        BungeecordSpoofer spoofer = ModuleManager.INSTANCE.getModuleByClass(BungeecordSpoofer.class);
        if (!spoofer.isEnabled()) return;

        if (this.intendedState() != ConnectionIntent.LOGIN) return;

        final String[] spoofedUUID = {CameraTweaks.mc.getSession().getUuidOrNull().toString()};
        String URL = "https://api.mojang.com/users/profiles/minecraft/" + mc.getSession().getUsername();

        executor.execute(() -> {
            try {
                HttpURLConnection req = (HttpURLConnection) new URL(URL).openConnection();
                req.setRequestMethod("GET");
                req.setConnectTimeout(5000);
                req.setReadTimeout(5000);

                if (req.getResponseCode() != 200) {
                    this.address += "\u0000" + spoofer.address.getValue() + "\u0000" + spoofedUUID[0];
                    return;
                }

                JsonObject obj = JsonParser.parseReader(new InputStreamReader(req.getInputStream())).getAsJsonObject();

                if (obj.has("id"))
                    spoofedUUID[0] = obj.get("id").getAsString();

                this.address += "\u0000" + spoofer.address.getValue() + "\u0000" + spoofedUUID[0];

            } catch (Exception e) {
                e.printStackTrace();
                this.address += "\u0000" + spoofer.address.getValue() + "\u0000" + spoofedUUID[0];
            }
        });
    }


}
