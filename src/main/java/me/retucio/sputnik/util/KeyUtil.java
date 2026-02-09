package me.retucio.sputnik.util;

import me.retucio.sputnik.mixin.accessors.KeyBindingAccessor;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.im.InputContext;
import java.util.Locale;

import static me.retucio.sputnik.Sputnik.mc;

// cosas útiles relacionadas con las teclas
public class KeyUtil {

    public static int getKeyAction(int key) {
        return GLFW.glfwGetKey(mc.getWindow().getHandle(), key);
    }

    public static boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(
                mc.getWindow().getHandle(),
                key) != GLFW.GLFW_RELEASE;
    }

    public static boolean isKeyDown(KeyBinding key) {
        return GLFW.glfwGetKey(
                mc.getWindow().getHandle(),
                ((KeyBindingAccessor) key).getBoundKey().getCode()
        ) != GLFW.GLFW_RELEASE;
    }

    public static boolean isShiftDown() {
        return isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)
                || isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }


    public static boolean isCapsLockOn() throws HeadlessException {
        return Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
    }

    public static void pressKey(int key, int action) {
        pressKey(key, action, 0, 0);
    }

    public static void pressKey(int key, int action, int scancode, int modifiers) {
        mc.keyboard.onKey(
                mc.getWindow().getHandle(),
                action,
                new KeyInput(
                        key,
                        scancode,
                        modifiers
                )
        );
    }

    public static String getKeyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "ninguna";

        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();

        // para teclas especiales
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "espacio";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "shift izquierdo";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "shift derecho";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "ctrl izquierdo";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "ctrl derecho";
            case GLFW.GLFW_KEY_LEFT_ALT -> "alt izquierdo";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "alt gr";
            case GLFW.GLFW_KEY_TAB -> "tab";
            case GLFW.GLFW_KEY_ENTER -> "enter";
            case GLFW.GLFW_KEY_BACKSPACE -> "borrar";
            case GLFW.GLFW_KEY_DELETE -> "suprimir";
            case GLFW.GLFW_KEY_UP -> "arriba";
            case GLFW.GLFW_KEY_DOWN -> "abajo";
            case GLFW.GLFW_KEY_LEFT -> "izquierda";
            case GLFW.GLFW_KEY_RIGHT -> "derecha";
            case GLFW.GLFW_KEY_PAGE_UP -> "pág. arriba";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "pág. abajo";
            default -> {
                if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25)
                    yield "F" + (key - GLFW.GLFW_KEY_F1 + 1);  // F1–F25
                yield "tecla " + key;  // fallback
            }
        };
    }


    public static String shiftKey(String c) {
        return shiftKey(c, getLayout());
    }

    public static String shiftKey(String c, KeyboardLayout layout) {
        String uppercase = isCapsLockOn() ? c.toLowerCase() : c.toUpperCase();

        switch (layout) {
            case EN_US -> {
                return switch (c) {
                    case "`" -> "~";
                    case "1" -> "!";
                    case "2" -> "@";
                    case "3" -> "#";
                    case "4" -> "$";
                    case "5" -> "%";
                    case "6" -> "^";
                    case "7" -> "&";
                    case "8" -> "*";
                    case "9" -> "(";
                    case "0" -> ")";
                    case "-" -> "_";
                    case "=" -> "+";
                    case "[" -> "{";
                    case "]" -> "}";
                    case "\\" -> "|";
                    case ";" -> ":";
                    case "'" -> "\"";
                    case "," -> "<";
                    case "." -> ">";
                    case "/" -> "?";
                    default -> uppercase;
                };
            }

            case ES_ES -> {
                return switch (c) {
                    case "º" -> "ª";
                    case "1" -> "!";
                    case "2" -> "\"";
                    case "3" -> "·";
                    case "4" -> "$";
                    case "5" -> "%";
                    case "6" -> "&";
                    case "7" -> "/";
                    case "8" -> "(";
                    case "9" -> ")";
                    case "0" -> "=";
                    case "'" -> "?";
                    case "¡" -> "¿";
                    case "`" -> "^";
                    case "+" -> "*";
                    case "´" -> "¨";
                    case "," -> ";";
                    case "." -> ":";
                    case "-" -> "_";
                    case "<" -> ">";
                    default -> uppercase;
                };
            }
        }

        return uppercase;
    }



    public static KeyboardLayout getLayout() {
        Locale locale = InputContext.getInstance().getLocale();
        if (locale == null) locale = Locale.getDefault();

        String country = locale.getCountry();
        String lang = locale.getLanguage();

        if (country.equals("US") && lang.equals("en")) {
            return KeyboardLayout.EN_US;
        } else if (country.equals("ES") && lang.equals("es")) {
            return KeyboardLayout.ES_ES;
        }

        // fallback
        return KeyboardLayout.EN_US;
    }

    public enum KeyboardLayout {
        EN_US,
        ES_ES;
    }
}
