package me.retucio.sputnik.ui.widgets.misc;

import me.retucio.sputnik.ui.widgets.Widget;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class NotificationWidget extends Widget {

    public static int duration = 5000;
    private static final int PADDING = 4;

    private final String title, desc;
    private int index;

    public final long pushTime;
    public final long popTime;

    public NotificationWidget(String title, String desc) {
        super(0, 0,
                Math.max(mc.font.width(title), mc.font.width(desc)) + PADDING * 2,
                mc.font.lineHeight * 2 + PADDING * 3);

        this.title = title;
        this.desc = desc;

        this.x = mc.getWindow().getGuiScaledWidth() - w - 2;
        this.pushTime = System.currentTimeMillis();
        this.popTime = pushTime + duration;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float d) {
        x = mc.getWindow().getGuiScaledWidth() - w - 2;
        y = mc.getWindow().getGuiScaledHeight() - (index + 1) * (h + 2);

        gui.fill(x, y, x + w, y + h, Colors.panelBgColor.getRGB());

        gui.text(mc.font, title, x + PADDING, y + PADDING, -1, true);
        gui.text(mc.font, desc,  x + PADDING, y + PADDING + mc.font.lineHeight + 2, 0xFFAAAAAA, true);

        float progress = (float) (popTime - System.currentTimeMillis()) / duration;
        progress = Math.clamp(progress, 0f, 1f);
        gui.fill(x, y + h - 2, x + (int) (progress * w), y + h, Colors.mainColor.getRGB());
    }

    public void setIndex(int index) {
        this.index = index;
    }
}