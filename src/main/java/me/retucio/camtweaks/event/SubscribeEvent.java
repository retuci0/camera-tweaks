package me.retucio.camtweaks.event;

import java.lang.annotation.*;

// anotación para registar un "listener", que escucha cuando se llama un evento
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {}