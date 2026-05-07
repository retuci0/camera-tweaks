package me.retucio.sputnik.util;


import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.*;
import java.util.List;

import static me.retucio.sputnik.util.Colors.*;

public class Lists {

    public static final List<EntityType<?>> entityList = BuiltInRegistries.ENTITY_TYPE.stream().toList();
    public static Map<EntityType<?>, String> entityNames;

    public static final List<ParticleType<?>> particleList = BuiltInRegistries.PARTICLE_TYPE.stream().toList();
    public static Map<ParticleType<?>, String> particleNames;

    public static final List<Item> itemList = BuiltInRegistries.ITEM.stream().toList();
    public static Map<Item, String> itemNames;

    public static final List<Block> blockList = BuiltInRegistries.BLOCK.stream().toList();
    public static Map<Block, String> blockNames;

    public static final List<SoundEvent> soundList = BuiltInRegistries.SOUND_EVENT.stream().toList();
    public static Map<SoundEvent, String> soundNames;

    public static final List<MenuType<?>> screenList = BuiltInRegistries.MENU.stream().toList();
    public static Map<MenuType<?>, String> screenNames;

    public static final List<Color> colorList = new ArrayList<>();
    public static final List<String> fontList = new ArrayList<>();

    // intentar traducir nombres y fallar miserablemente
    public static void init() {
        entityNames = getMapOfLists(entityList,
                entityList.stream().map(entity -> Component.translatable(
                        entity.getDescriptionId()
                ).getString()).toList());

        particleNames = getMapOfLists(particleList,
                particleList.stream().map(particle -> Component.translatable(
                        BuiltInRegistries.PARTICLE_TYPE.getKey(particle).toLanguageKey()
                ).getString()).toList());

        itemNames = getMapOfLists(itemList,
                itemList.stream().map(item -> Component.translatable(
                        item.getDescriptionId()
                ).getString()).toList());

        blockNames = getMapOfLists(blockList,
                blockList.stream().map(block -> Component.translatable(
                        block.getDescriptionId()
                ).getString()).toList());

        soundNames = getMapOfLists(soundList,
                soundList.stream().map(sound -> Component.translatable(
                        sound.location().toShortLanguageKey()
                ).getString()).toList());
        soundNames.replace(SoundEvents.EMPTY, "NINGUNO");

        screenNames = getMapOfLists(screenList,
                screenList.stream().map(screen -> Component.translatable(
                        BuiltInRegistries.MENU.getKey(screen).toLanguageKey()
                ).getString()).toList());

        colorList.addAll(Arrays.asList(
                RED, ORANGE, YELLOW, LIME, GREEN, CYAN,
                CELESTE, BLUE, PURPLE, MAGENTA, PINK,
                LAVENDER, WHITE, SILVER, GRAY, BLACK, BROWN
        ));

        fontList.addAll(List.of(
                "bahnschrift", "calibri", "cantarell", "comic-sans-ms",
                "deja-vu-sans-mono", "new-times-roman", "roboto", "segoe-ui",
                "ubuntu"
        ));
    }

    public static <T> Map<T, Boolean> allTrue(List<T> options) {
        Map<T, Boolean> map = new HashMap<>();
        for (T option : options) map.put(option, true);
        return map;
    }

    @SafeVarargs
    public static <T> Map<T, Boolean> allTrueExcept(List<T> options, T... exceptions) {
        Map<T, Boolean> map = allFalse(options);
        for (T exception : exceptions) map.replace(exception, false);
        return map;
    }

    public static <T> Map<T, Boolean> allFalse(List<T> options) {
        Map<T, Boolean> map = new HashMap<>();
        for (T option : options) map.put(option, false);
        return map;
    }

    @SafeVarargs
    public static <T> Map<T, Boolean> allFalseExcept(List<T> options, T... exceptions) {
        Map<T, Boolean> map = allFalse(options);
        for (T exception : exceptions) map.replace(exception, true);
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
