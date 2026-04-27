package me.retucio.sputnik.module.modules.misc;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.DisconnectEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.module.setting.settings.StringSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
import net.minecraft.client.StringSplitter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.function.Predicate;


public class BookBot extends Module {

    private final SettingGroup sgBook = addSg(new SettingGroup("libro", true));

    private final NumberSetting delay = sgGeneral.add(new NumberSetting(
            "delay",
            "delay entre la escritura de libros",
            20,
            1,
            200,
            1
    ));

    private final BooleanSetting toggleOnDisconnect = sgGeneral.add(new BooleanSetting(
            "desactivar al desconectarse",
            ".",
            true
    ));

    private final StringSetting name = sgBook.add(new StringSetting(
            "nombre",
            "nombre a usar para los libros",
            "mortadela y filetón",
            100
    ));

    private final BooleanSetting count = sgBook.add(new BooleanSetting(
            "contar",
            "contar los libros y añadir el número al final del nombre",
            true
    ));

    private final NumberSetting pages = sgBook.add(new NumberSetting(
            "páginas",
            "número de páginas a llenar con mierda",
            50,
            1,
            100,
            1
    ));

    private final BooleanSetting sign = sgBook.add(new BooleanSetting(
            "firmar",
            "firmar el libro",
            true
    ));

    private final BooleanSetting onlyAscii = sgBook.add(new BooleanSetting(
            "solo ascii",
            "usar solamente caracteres ascii",
            false
    ));

    private int delayTimer, bookCount;
    private Random random;

    public BookBot() {
        super("j. k. rowling", "rellena libros con caca", Category.MISC);
    }

    @Override
    public void onEnable() {
        random = new Random();
        delayTimer = delay.getIntValue();
        bookCount = 0;
        super.onEnable();
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        Predicate<ItemStack> bookPredicate = stack -> {
            WritableBookContent component = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
            return stack.getItem() == Items.WRITABLE_BOOK && (component == null || component.pages().isEmpty());
        };

        ItemStack book = InventoryUtil.findStack(bookPredicate);
        if (book == null) {
            ChatUtil.warn("no se encontró ningún libro");
            toggle();
            return;
        }

        if (mc.player.getMainHandItem() != book) {
            InventoryUtil.swapWithHotbar(
                    mc.player.getInventory().findSlotMatchingItem(book),
                    mc.player.getInventory().getSelectedSlot()
            );
        }

        if (delayTimer > 0) {
            delayTimer--;
            return;
        }

        delayTimer = delay.getIntValue();

        int origin = onlyAscii.getValue() ? 0x21 : 0x0800;
        int bound = onlyAscii.getValue() ? 0x7E : 0x10FFFF;

        writeBook(random.ints(origin, bound)
            .filter(i -> !Character.isWhitespace(i)
                    && i != '\r' && i != '\n')
            .iterator()
        );
    }

    @EventListener
    private void onDisconnect(DisconnectEvent event) {
        if (toggleOnDisconnect.getValue()) {
            toggle();
        }
    }

    private void writeBook(PrimitiveIterator.OfInt chars) {
        ArrayList<String> pages = new ArrayList<>();
        ArrayList<Filterable<Component>> filteredPages = new ArrayList<>();
        int maxPages = this.pages.getIntValue();

        StringSplitter.WidthProvider widthRetriever = mc.font.getSplitter().widthProvider;
        int pageIndex = 0;
        int lineIndex = 0;
        final StringBuilder page = new StringBuilder();
        float lineWidth = 0;

        while (chars.hasNext() && pageIndex < maxPages) {
            int c = chars.nextInt();

            if (c == '\r' || c == '\n') {
                page.append('\n');
                lineWidth = 0;
                lineIndex++;
            } else {
                float charWidth = widthRetriever.getWidth(c, Style.EMPTY);

                if (lineWidth + charWidth > 114f) {
                    page.append('\n');
                    lineWidth = charWidth;
                    lineIndex++;
                    if (lineIndex != 14) page.appendCodePoint(c);
                } else if (lineWidth == 0f && c == ' ') {
                    continue;
                } else {
                    lineWidth += charWidth;
                    page.appendCodePoint(c);
                }
            }

            if (lineIndex == 14) {
                filteredPages.add(Filterable.passThrough(Component.nullToEmpty(page.toString())));
                pages.add(page.toString());
                page.setLength(0);
                pageIndex++;
                lineIndex = 0;
                lineWidth = 0;
            }
        }

        if (!page.isEmpty() && pageIndex < maxPages) {
            filteredPages.add(Filterable.passThrough(Component.nullToEmpty(page.toString())));
            pages.add(page.toString());
        }

        if (bookCount > 0) {
            ChatUtil.info("creado libro n. " + bookCount);
        }

        createBook(pages, filteredPages);
    }

    private void createBook(ArrayList<String> pages, ArrayList<Filterable<Component>> filteredPages) {
        String title = name.getValue();
        if (count.getValue() && bookCount != 0) title += " #" + bookCount;

        mc.player.getMainHandItem().set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(title),
                mc.player.getGameProfile().name(),
                0,
                filteredPages,
                true
        ));

        mc.player.connection.send(new ServerboundEditBookPacket(
                mc.player.getInventory().getSelectedSlot(),
                pages,
                sign.getValue()
                        ? Optional.of(title)
                        : Optional.empty()
        ));

        bookCount++;
    }
}