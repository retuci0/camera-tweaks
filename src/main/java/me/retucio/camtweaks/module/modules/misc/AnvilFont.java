package me.retucio.camtweaks.module.modules.misc;

import me.retucio.camtweaks.mixin.accessor.AnvilScreenAccessor;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.EnumSetting;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;

import me.retucio.camtweaks.module.modules.misc.AnvilFont.CustomFont.CharMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AnvilFont extends Module {

    public EnumSetting<FontMode> fontMode = addSetting(new EnumSetting<>("tipografía", "qué tipografía utilizar",
            FontMode.class, FontMode.NORMAL));

    String text;

    private static final List<CustomFont> fonts = Arrays.asList(
            new CustomFont(  // normal
                    CharMap.range('!', 65281, 95)),

            new CustomFont(  // mayúsculas
                    CharMap.single('a', 'ᴀ'), CharMap.single('b', 'ʙ'), CharMap.range('c', 0x1d04, 2), CharMap.single('e', 'ᴇ'),
                    CharMap.single('f', 'ꜰ'), CharMap.single('g', 'ɢ'), CharMap.single('h', 'ʜ'), CharMap.single('i', 'ɪ'),
                    CharMap.range('j', 0x1D0a, 2), CharMap.single('l', 'ʟ'), CharMap.single('m', 'ᴍ'), CharMap.single('n', 'ɴ'),
                    CharMap.single('o', 'ᴏ'), CharMap.single('p', 'ᴩ'), CharMap.single('r', 'ʀ'), CharMap.single('s', 'ꜱ'),
                    CharMap.range('t', 0x1D1b, 2), CharMap.range('v', 0x1d20, 2), CharMap.single('z', 'ᴢ'),
                    CharMap.single('A', 'ᴀ'), CharMap.single('B', 'ʙ'), CharMap.range('C', 0x1d04, 2), CharMap.single('E', 'ᴇ'),
                    CharMap.single('F', 'ꜰ'), CharMap.single('G', 'ɢ'), CharMap.single('H', 'ʜ'), CharMap.single('I', 'ɪ'),
                    CharMap.range('J', 0x1D0a, 2), CharMap.single('L', 'ʟ'), CharMap.single('M', 'ᴍ'), CharMap.single('N', 'ɴ'),
                    CharMap.single('O', 'ᴏ'), CharMap.single('P', 'ᴩ'), CharMap.single('R', 'ʀ'), CharMap.single('S', 'ꜱ'),
                    CharMap.range('T', 0x1D1b, 2), CharMap.range('V', 0x1d20, 2), CharMap.single('z', 'ᴢ')),

            new CustomFont( // redondo
                    CharMap.range('1', 9313, 9),
                    CharMap.range('A', 9398, 26),
                    CharMap.range('a', 9424, 26)),

            new CustomFont(  // to raro
                    CharMap.single('a', 'Λ'), CharMap.single('c', 'ᑕ'), CharMap.single('e', 'Σ'), CharMap.single('h', 'Ή'),
                    CharMap.single('l', 'ᒪ'), CharMap.single('n', 'П'), CharMap.single('o', 'Ө'), CharMap.single('r', 'Я'),
                    CharMap.single('s', 'Ƨ'), CharMap.single('t', 'Ƭ'), CharMap.single('u', 'Ц'), CharMap.single('w', 'Щ'),
                    CharMap.single('A', 'Λ'), CharMap.single('C', 'ᑕ'), CharMap.single('E', 'Σ'), CharMap.single('H', 'Ή'),
                    CharMap.single('L', 'ᒪ'), CharMap.single('N', 'П'), CharMap.single('O', 'Ө'), CharMap.single('R', 'Я'),
                    CharMap.single('S', 'Ƨ'), CharMap.single('T', 'Ƭ'), CharMap.single('U', 'Ц'), CharMap.single('W', 'Щ')),

            new CustomFont(  // griego
                    CharMap.single('a', 'α'), CharMap.single('b', 'в'), CharMap.single('d', '∂'), CharMap.single('e', 'є'),
                    CharMap.single('f', 'ƒ'), CharMap.single('h', 'н'), CharMap.single('i', 'ι'), CharMap.single('j', 'נ'),
                    CharMap.single('k', 'к'), CharMap.single('l', 'ℓ'), CharMap.single('m', 'м'), CharMap.single('n', 'η'),
                    CharMap.single('o', 'σ'), CharMap.single('p', 'ρ'), CharMap.single('r', 'я'), CharMap.single('s', 'ѕ'),
                    CharMap.single('t', 'т'), CharMap.single('u', 'υ'), CharMap.single('v', 'ν'), CharMap.single('w', 'ω'),
                    CharMap.single('x', 'χ'), CharMap.single('y', 'у'),
                    CharMap.single('A', 'α'), CharMap.single('B', 'в'), CharMap.single('D', '∂'), CharMap.single('E', 'є'),
                    CharMap.single('F', 'ƒ'), CharMap.single('H', 'н'), CharMap.single('I', 'ι'), CharMap.single('J', 'נ'),
                    CharMap.single('K', 'к'), CharMap.single('L', 'ℓ'), CharMap.single('M', 'м'), CharMap.single('N', 'η'),
                    CharMap.single('O', 'σ'), CharMap.single('P', 'ρ'), CharMap.single('R', 'я'), CharMap.single('S', 'ѕ'),
                    CharMap.single('T', 'т'), CharMap.single('U', 'υ'), CharMap.single('V', 'ν'), CharMap.single('W', 'ω'),
                    CharMap.single('X', 'χ'), CharMap.single('Y', 'у'))
    );

    public AnvilFont() {
        super("yunques con pluma",
                "te permite tipografías distintas al renombrar items en un yunque",
                Category.MISC);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen == null) return;

        if (mc.currentScreen instanceof AnvilScreen) {
            TextFieldWidget nameField = ((AnvilScreenAccessor) mc.currentScreen).getNameField();

            if (fontMode.getValue() != FontMode.NORMAL)
                text = fonts.get(fontMode.getIndex()).replace(nameField.getText());
            else
                text = nameField.getText();

            if (!text.equals(nameField.getText()))
                nameField.setText(text);

            ((AnvilScreenAccessor) mc.currentScreen).setNameField(nameField);
        }
    }


    static class CustomFont {

        private final HashMap<Character, Character> allMaps = new HashMap<>();

        public CustomFont(CharMap... maps) {
            for (CharMap map : maps)
                allMaps.putAll(map.getMap());
        }

        public String replace(String startString) {
            for (Map.Entry<Character, Character> e : allMaps.entrySet())
                startString = startString.replace(e.getKey(), e.getValue());
            return startString;
        }


        static class CharMap {

            private final HashMap<Character, Character> map = new HashMap<>();

            private CharMap(char... mappings) {
                for (int i = 0; i < mappings.length - 1; i += 2)
                    map.put(mappings[i], mappings[i + 1]);
            }

            public static CharMap single(char from, char to) {
                return new CharMap(from, to);
            }

            public static CharMap range(char start, int start1, int amount) {
                char[] chars = new char[amount * 2];
                for (int i = 0; i < amount; i++) {
                    chars[i * 2] = (char) (start + i);
                    chars[i * 2 + 1] = (char) (start1 + i);
                }
                return new CharMap(chars);
            }

            public HashMap<Character, Character> getMap() {
                return map;
            }
        }
    }

    public enum FontMode {
        NORMAL("normal"),
        UPPERCASE("mayúsculas"),
        CIRCLED("redondo"),
        CURSED("to raro"),
        GREEK("griego");

        private final String name;
        FontMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
