package me.retucio.sputnik.util.interfaces;

import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import java.util.List;


public interface IChatComponent {

    void sputnik$add(Component message, int id);

    List<GuiMessage.Line> sputnik$getVisibleMessages();
}