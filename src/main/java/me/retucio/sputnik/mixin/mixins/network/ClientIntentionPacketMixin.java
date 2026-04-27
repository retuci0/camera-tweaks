package me.retucio.sputnik.mixin.mixins.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.network.BungeecordSpoofer;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.retucio.sputnik.Sputnik.mc;


@Mixin(ClientIntentionPacket.class)
public abstract class ClientIntentionPacketMixin {

    @Shadow @Mutable @Final
    private String hostName;

    @Unique
    private final Gson gson = new Gson();

    @Unique
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Shadow
    public abstract ClientIntent intention();

    @SuppressWarnings("deprecation")
    @Inject(method = "<init>(ILjava/lang/String;ILnet/minecraft/network/protocol/handshake/ClientIntent;)V", at = @At("RETURN"))
    private void onHandshakeC2SPacket(int protocolVersion, String hostName, int port, ClientIntent intention, CallbackInfo ci) {
        BungeecordSpoofer spoofer = ModuleManager.INSTANCE.getModuleByClass(BungeecordSpoofer.class);
        if (!spoofer.isEnabled()) return;

        if (this.intention() != ClientIntent.LOGIN) return;

        final String[] spoofedUUID = {Sputnik.mc.getUser().getProfileId().toString()};
        String URL = "https://api.mojang.com/users/profiles/minecraft/" + mc.getUser().getName();

        executor.execute(() -> {
            try {
                HttpURLConnection req = (HttpURLConnection) new URL(URL).openConnection();
                req.setRequestMethod("GET");
                req.setConnectTimeout(5000);
                req.setReadTimeout(5000);

                if (req.getResponseCode() != 200) {
                    this.hostName += "\u0000" + spoofer.address.getValue() + "\u0000" + spoofedUUID[0];
                    return;
                }

                JsonObject obj = JsonParser.parseReader(new InputStreamReader(req.getInputStream())).getAsJsonObject();

                if (obj.has("id"))
                    spoofedUUID[0] = obj.get("id").getAsString();

                this.hostName += "\u0000" + spoofer.address.getValue() + "\u0000" + spoofedUUID[0];

            } catch (Exception e) {
                e.printStackTrace();
                this.hostName += "\u0000" + spoofer.address.getValue() + "\u0000" + spoofedUUID[0];
            }
        });
    }


}
