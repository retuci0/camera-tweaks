package me.retucio.sputnik.module.modules.network;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;


/** continúa en:
 * @see ClientCommonNetworkHandlerMixin
 * @see ConfirmScreenMixin
 * @see ServerPackStatusMixin
 * @see ServerConnectorMixin
 * @see ServerDataMixin
 */

// https://github.com/emilyy-dev/bypass-resource-pack
public class RPackBypass extends Module {

    public String TAG_NAME = "bypassTextures";
    public String ENUM_NAME = "BYPASS";
    public Component BYPASS_TEXT = Component.literal("nuh uh");

    public RPackBypass() {
        super("bypassear packs",
                "te permite omitir packs de recursos forzados por servers",
                Category.NETWORK);
    }

    public ServerData.ServerPackStatus getStatus() {
        return ServerData.ServerPackStatus.valueOf(ENUM_NAME);
    }
}