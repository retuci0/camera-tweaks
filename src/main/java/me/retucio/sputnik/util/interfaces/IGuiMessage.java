package me.retucio.sputnik.util.interfaces;

import com.mojang.authlib.GameProfile;

public interface IGuiMessage {

    String sputnik$getText();

    int sputnik$getId();
    void sputnik$setId(int id);

    GameProfile sputnik$getSender();
    void sputnik$setSender(GameProfile profile);
}