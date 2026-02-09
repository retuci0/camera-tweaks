package me.retucio.sputnik.module.modules.network;

import me.retucio.sputnik.mixin.mixins.network.ClientCommonNetworkHandlerMixin;
import me.retucio.sputnik.mixin.mixins.network.ResourcePackPolicyMixin;
import me.retucio.sputnik.mixin.mixins.network.ServerConnectorMixin;
import me.retucio.sputnik.mixin.mixins.network.ServerInfoMixin;
import me.retucio.sputnik.mixin.mixins.screen.ConfirmScreenMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;


/** continúa en:
 * @see ClientCommonNetworkHandlerMixin
 * @see ConfirmScreenMixin
 * @see ResourcePackPolicyMixin
 * @see ServerConnectorMixin
 * @see ServerInfoMixin
 */

// https://github.com/emilyy-dev/bypass-resource-pack
public class RPackBypass extends Module {

    public String TAG_NAME = "bypassTextures";
    public String ENUM_NAME = "BYPASS";
    public Text BYPASS_TEXT = Text.literal("nuh uh");

    public RPackBypass() {
        super("bypassear packs",
                "te permite omitir packs de recursos forzados por servers",
                Category.NETWORK);
    }

    public ServerInfo.ResourcePackPolicy getPolicy() {
        return ServerInfo.ResourcePackPolicy.valueOf(ENUM_NAME);
    }
}