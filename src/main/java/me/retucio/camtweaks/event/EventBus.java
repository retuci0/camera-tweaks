package me.retucio.camtweaks.event;

import java.lang.reflect.Method;
import java.util.*;

// al llamar un evento, se guarda en el bus de eventos, de donde luego se puede detectar si ese evento ha sucedido
public class EventBus {
    private final Map<Class<?>, List<ListenerMethod>> listeners = new HashMap<>();

    private record ListenerMethod(Object instance, Method method) {}

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                if (method.getParameterCount() != 1) continue; // must take 1 param
                Class<?> eventType = method.getParameterTypes()[0];
                method.setAccessible(true);

                listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                        .add(new ListenerMethod(listener, method));
            }
        }
    }

    public void unregister(Object listener) {
        for (List<ListenerMethod> list : listeners.values()) {
            list.removeIf(lm -> lm.instance == listener);
        }
    }

    public <T extends Event> T post(T event) {
        List<ListenerMethod> list = listeners.get(event.getClass());
        if (list != null) {
            for (ListenerMethod lm : list) {
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
