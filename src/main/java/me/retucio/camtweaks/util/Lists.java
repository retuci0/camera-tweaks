package me.retucio.camtweaks.util;

import net.minecraft.block.Block;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
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

    public static final List<Color> colorList = new ArrayList<>();

    public static void init() {
        // no consigo traducir el texto. a tomar por culo, se queda en inglés, si no entiendes pues a estudiar
        entityNames = getMapOfLists(entityList,
                entityList.stream().map(entity -> I18n.translate(entity.getTranslationKey())).toList());

        // no hay traducciones para nombres de partículas, y paso de hacerlas yo
        particleNames = getMapOfLists(particleList,
                particleList.stream().map(particle -> Registries.PARTICLE_TYPE.getId(particle).toShortTranslationKey()).toList());

        itemNames = getMapOfLists(itemList,
                itemList.stream().map(item -> I18n.translate(item.getTranslationKey())).toList());

        colorList.addAll(Arrays.asList(RED, ORANGE, YELLOW, LIME, GREEN, CYAN, CELESTE, BLUE, PURPLE, MAGENTA, PINK, LAVENDER, WHITE, SILVER, GRAY, BLACK, BROWN));
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
