package me.retucio.sputnik.module.modules.client;

import dev.firstdark.rpc.DiscordRpc;
import dev.firstdark.rpc.enums.ActivityType;
import dev.firstdark.rpc.enums.ErrorCode;
import dev.firstdark.rpc.exceptions.UnsupportedOsType;
import dev.firstdark.rpc.handlers.RPCEventHandler;
import dev.firstdark.rpc.models.DiscordRichPresence;
import dev.firstdark.rpc.models.User;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.Setting;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.StringSetting;
import me.retucio.sputnik.util.ChatUtil;


/**
 * @author retucio
 */

public class DiscordRPC extends Module {

    private final SettingGroup sgImage = addSg(new SettingGroup("imágenes", false));
    private final SettingGroup sgButton = addSg(new SettingGroup("botón", false));


    // general

    private final StringSetting details = sgGeneral.add(new StringSetting(
            "detalles", "título",
            "usando el mod de putas de retucio",
            40
    ));

    private final StringSetting state = sgGeneral.add(new StringSetting(
            "estado", "subtítulo",
            "(se lo está pasando en grande)",
            40
    ));

    private final StringSetting startTimestamp = sgGeneral.add(new StringSetting(
            "sello de tiempo", "sello de tiempo del comienzo de la actividad, siguiendo el unix epoch (dejar vacío para ahora)",
            "",
            20
    ));

    private final EnumSetting<ActivityTypes> activityType = sgGeneral.add(new EnumSetting<>(
            "tipo de actividad", "qué tipo de actividad se está llevando a cabo",
            ActivityTypes.class, ActivityTypes.PLAYING
    ));

    private final StringSetting appId = sgGeneral.add(new StringSetting(
            "id del bot", "id del bot a usar (obtener de discord.dev)",
            "1314254766150258729", 20
    ));


    // imágenes

    private final StringSetting largeImageKey = sgImage.add(new StringSetting(
            "imagen grande", "clave de la imagen grande",
            "pengiun",
            40
    ));

    private final StringSetting largeImageText = sgImage.add(new StringSetting(
            "texto de la imagen grande", "texto mostrado junto a la imagen grande",
            "pengüino",
            40
    ));

    private final StringSetting smallImageKey = sgImage.add(new StringSetting(
            "imagen pequeña", "clave de la imagen pequeña",
            "pengiun",
            40
    ));

    private final StringSetting smallImageText = sgImage.add(new StringSetting(
            "texto de la imagen pequeña", "texto mostrado junto a la imagen pequeña",
            "pengüino",
            40
    ));


    // botón

    private final StringSetting buttonText = sgButton.add(new StringSetting(
            "texto del botón", "texto a mostrar en el botón",
            "hola",
            20
    ));

    private final StringSetting buttonUrl = sgButton.add(new StringSetting(
            "url del botón", "rickroll en gran 26 :wilted_rose:",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            20
    ));


    // crear el rpc
    private boolean initialized;
    DiscordRpc rpc = new DiscordRpc();
    RPCEventHandler handler = new RPCEventHandler() {
        @Override
        public void ready(User user) {
            rpc.updatePresence(getPresence());
        }

        @Override
        public void disconnected(ErrorCode errorCode, String message) {
            ChatUtil.warn("desconectado: " + errorCode + " - " + message);
            toggle();
        }

        @Override
        public void errored(ErrorCode errorCode, String message) {
            ChatUtil.error("error: " + errorCode + " - " + message);
            toggle();
        }
    };


    public DiscordRPC() {
        super("discordRPC",
                "personaliza la actividad de tu perfil en dishkor",
                Category.CLIENT);

        rpc.setDebugMode(false);

        for (Setting<?> s : getSettings()) {
            s.onUpdate(v -> update());
        }

        appId.onUpdate(v -> reload());
    }


    @Override
    public void onEnable() {
        String token = appId.getValue();
        if (token == null || token.trim().isEmpty()) {
            ChatUtil.error("configura el token del bot primero");
            toggle();
            return;
        }

        try {
            rpc.init(token, handler, false);
            initialized = true;
        } catch (UnsupportedOsType e) {
            ChatUtil.error("sistema operativo no soportado");
            toggle();
        } catch (Exception e) {
            ChatUtil.error("error al inicializar rpc: " + e.getMessage());
            toggle();
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (initialized) {
            rpc.shutdown();
            initialized = false;
        }
        super.onDisable();
    }

    private DiscordRichPresence getPresence() {
        return DiscordRichPresence.builder()
                .details(details.getValue())
                .state(state.getValue())
                .largeImageKey(largeImageKey.getValue())
                .largeImageText(largeImageText.getValue())
                .smallImageKey(smallImageKey.getValue())
                .smallImageText(smallImageText.getValue())
                .startTimestamp(getTimestamp())
                .activityType(activityType.getValue().getActivityType())
                .button(DiscordRichPresence.RPCButton.of(
                        buttonText.getValue(),
                        buttonUrl.getValue())
                ).build();
    }

    private void update() {
        if (initialized && isEnabled()) {
            try {
                rpc.updatePresence(getPresence());
            } catch (Exception e) {
                ChatUtil.error("error al actualizar rpc: " + e.getMessage());
            }
        }
    }

    private void reload() {
        if (initialized && isEnabled()) {
            rpc.shutdown();
            try {
                rpc.init(appId.getValue(), handler, false);
            } catch (UnsupportedOsType e) {
                ChatUtil.error(e.getMessage());
            }
        }
    }

    private long getTimestamp() {
        long timestamp;
        if (startTimestamp.getValue().trim().isEmpty()) {
            timestamp = System.currentTimeMillis();
        } else {
            try {
                timestamp = Long.parseLong(startTimestamp.getValue());
            } catch (NumberFormatException e) {
                ChatUtil.error("introduce un NÚMERO (o déjalo vacío)");
                timestamp = System.currentTimeMillis();
            }
        }

        return timestamp / 1000;
    }

    private enum ActivityTypes {
        PLAYING("jugando"),
        STREAMING("streameando"),
        LISTENING("escuchando"),
        WATCHING("viendo"),
        CUSTOM("custom"),
        COMPETING("compitiendo");

        private final String name;
        ActivityTypes(String name) { this.name = name; }
        @Override public String toString() { return name; }
        private ActivityType getActivityType() { return ActivityType.values()[ordinal()]; }
    }
}
