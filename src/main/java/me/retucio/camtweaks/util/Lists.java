package me.retucio.camtweaks.util;

import com.ibm.icu.impl.locale.XCldrStub;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.Sound;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.packet.PacketType;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.*;
import java.util.List;

import static me.retucio.camtweaks.util.Colors.*;

import static me.retucio.camtweaks.CameraTweaks.mc;

public class Lists {

    public static final List<EntityType<?>> entityList = Registries.ENTITY_TYPE.stream().toList();
    public static Map<EntityType<?>, String> entityNames;

    public static final List<ParticleType<?>> particleList = Registries.PARTICLE_TYPE.stream().toList();
    public static Map<ParticleType<?>, String> particleNames;

    public static final List<Item> itemList = Registries.ITEM.stream().toList();
    public static Map<Item, String> itemNames;

    public static final List<Block> blockList = Registries.BLOCK.stream().toList();
    public static Map<Block, String> blockNames;

    public static final List<SoundEvent> soundList = Registries.SOUND_EVENT.stream().toList();
    public static Map<SoundEvent, String> soundNames;

    public static final List<ScreenHandlerType<?>> screenList = Registries.SCREEN_HANDLER.stream().toList();
    public static Map<ScreenHandlerType<?>, String> screenNames;

    public static final List<Color> colorList = new ArrayList<>();

    // intentar traducir nombres y fallar miserablemente
    public static void init() {
        entityNames = getMapOfLists(entityList,
                entityList.stream().map(entity -> Text.translatable(entity.getTranslationKey()).getString()).toList());

        particleNames = getMapOfLists(particleList,
                particleList.stream().map(particle -> Text.translatable(
                        Registries.PARTICLE_TYPE.getId(particle).toShortTranslationKey()).getString()
                ).toList());

        itemNames = getMapOfLists(itemList,
                itemList.stream().map(item -> Text.translatable(
                        item.getTranslationKey()).getString()
                ).toList());

        blockNames = getMapOfLists(blockList,
                blockList.stream().map(block -> Text.translatable(
                        block.getTranslationKey()).getString()
                ).toList());

        soundNames = getMapOfLists(soundList,
                soundList.stream().map(sound -> Text.translatable(
                        sound.id().toShortTranslationKey()).getString()
                ).toList());

        screenNames = getMapOfLists(screenList,
                screenList.stream().map(screen -> Text.translatable(
                        Registries.SCREEN_HANDLER.getId(screen).toShortTranslationKey()).getString()
                ).toList());

        colorList.addAll(Arrays.asList(
                RED, ORANGE, YELLOW, LIME, GREEN, CYAN, CELESTE, BLUE, PURPLE, MAGENTA, PINK, LAVENDER, WHITE, SILVER, GRAY, BLACK, BROWN));
    }

    public static <T> Map<T, Boolean> allTrue(List<T> options) {
        Map<T, Boolean> map = new HashMap<>();
        for (T option : options) map.put(option, true);
        return map;
    }

    public static <T> Map<T, Boolean> allFalse(List<T> options) {
        Map<T, Boolean> map = new HashMap<>();
        for (T option : options) map.put(option, false);
        return map;
    }

    public static <T> Map<T, String> getMapOfLists(List<T> options, List<String> names) {
        Map<T, String> result = new HashMap<>();

        // no exceder los límites de ninguna de las dos listas
        int size = Math.min(options.size(), names.size());

        for (int i = 0; i < size; i++)
            result.put(options.get(i), names.get(i));

        return result;
    }
}
