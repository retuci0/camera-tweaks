package me.retucio.camtweaks.module.modules.network;

import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;


/** continúa en:
 * @see me.retucio.camtweaks.mixin.ClientCommonNetworkHandlerMixin
 * @see me.retucio.camtweaks.mixin.ConfirmScreenMixin
 * @see me.retucio.camtweaks.mixin.ResourcePackPolicyMixin
 * @see me.retucio.camtweaks.mixin.ServerConnectorMixin
 * @see me.retucio.camtweaks.mixin.ServerInfoMixin
 */

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