package me.retucio.camtweaks.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

// al llamar un evento, se guarda en el bus de eventos, de donde luego se puede detectar si ese evento ha sucedido
public class EventBus {
    private final Map<Class<?>, List<ListenerMethod>> listeners = new HashMap<>();

    private record ListenerMethod(Object instance, Method method) {}

    public void register(Object listener) {
        Class<?> targetClass;

        if (listener instanceof Class<?> clazz) {
            targetClass = clazz; // "listeners" estáticos
            listener = null;
        } else {
            targetClass = listener.getClass();
        }

        for (Method method : targetClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> eventType = method.getParameterTypes()[0];
            method.setAccessible(true);

            if (listener == null && !Modifier.isStatic(method.getModifiers())) continue;

            listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                    .add(new ListenerMethod(listener, method));
        }
    }

    public void subscribe(Object listener) { register(listener); }

    public void unregister(Object listener) {
        for (List<ListenerMethod> list : listeners.values()) {
            list.removeIf(lm -> lm.instance == listener);
        }
    }

    public void unsubscribe(Object listener) { unregister(listener); }

    public <T extends Event> T post(T event) {
        List<ListenerMethod> list = listeners.get(event.getClass());
        if (list != null) {
            for (ListenerMethod lm : new ArrayList<>(list)) {  // copiar para evitar CME
                try {
                    lm.method.invoke(lm.instance, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return event;
    }

    public boolean isRegistered(Object listener) {
        for (List<ListenerMethod> list : listeners.values()) {
            for (ListenerMethod lm : list) {
                if (lm.instance == listener) {
                    return true;
                }
            }
        }
        return false;
    }
}