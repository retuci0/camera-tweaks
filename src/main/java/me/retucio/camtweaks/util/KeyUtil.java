package me.retucio.camtweaks.util;

import me.retucio.camtweaks.CameraTweaks;
import org.lwjgl.glfw.GLFW;

// cosas útiles relacionadas a las teclas
public class KeyUtil {

    public static boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(
                CameraTweaks.mc.getWindow().getHandle(),
                key) != GLFW.GLFW_RELEASE;
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
                default -> { // F1–F25
                    if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25)
                        yield "F" + (key - GLFW.GLFW_KEY_F1 + 1);
                    yield "tecla " + key;
                }
            };
        }
}
