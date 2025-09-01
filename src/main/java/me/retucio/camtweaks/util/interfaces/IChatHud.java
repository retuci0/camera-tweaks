package me.retucio.camtweaks.util.interfaces;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;

import java.util.List;

public interface IChatHud {

    void smegma$add(Text message, int id);

    List<ChatHudLine.Visible> smegma$getVisibleMessages();
}