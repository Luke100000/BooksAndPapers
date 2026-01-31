package net.conczin.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.conczin.Parser;
import net.conczin.data.BookData;
import net.conczin.utils.RecordCodec;
import net.conczin.utils.Utils;

import javax.annotation.Nonnull;

import static net.conczin.data.BookData.METADATA_KEY;


public class BooksGui extends CodecDataInteractiveUIPage<BooksGui.Data> {
    private boolean editMode = true;
    private int page = 0;

    private final BlockPosition block;
    private final String style;
    private final String background;

    public BooksGui(@Nonnull PlayerRef playerRef, BlockPosition block, String style, String background) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);

        this.block = block;
        this.style = style;
        this.background = background;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Pages/BooksAndPapers/Styles/" + style + ".ui");

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Title", EventData.of("@Title", "#Title.Value"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Content", EventData.of("@Content", "#Content.Value"), false);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Previous", EventData.of("Action", "Previous"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Next", EventData.of("Action", "Next"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Sign", EventData.of("Action", "Sign"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Edit", EventData.of("Action", "Edit"));

        this.buildList(ref, commandBuilder);
    }

    private void buildList(Ref<EntityStore> ref, UICommandBuilder commandBuilder) {
        BookData book = Utils.getData(ref, block, METADATA_KEY, BookData.CODEC);
        editMode = editMode && !book.signed;

        // Clamp page
        if (!editMode) {
            page = Math.min(page, book.pages.size() - 1);
        }

        // Settings
        commandBuilder.set("#Background.Background", "Common/" + background + ".png");

        // Content
        BookData.Page page = book.getOrCreatePage(this.page);
        if (editMode) {
            commandBuilder.set("#Title.Value", page.title);
            commandBuilder.set("#Content.Value", page.content);
        } else {
            commandBuilder.set("#TitleLabel.TextSpans", Parser.parse(page.title));
            commandBuilder.set("#ContentLabel.TextSpans", Parser.parse(page.content));
        }

        commandBuilder.set("#Title.Visible", editMode);
        commandBuilder.set("#TitleLabel.Visible", !editMode);

        commandBuilder.set("#Content.Visible", editMode);
        commandBuilder.set("#ContentLabel.Visible", !editMode);

        commandBuilder.set("#PageNumber.Text", String.format("%d/%d", this.page + 1, book.pages.size()));

        commandBuilder.set("#Edit.Visible", !book.signed);
        commandBuilder.set("#Sign.Visible", !book.signed);
    }

    private void rebuildPage(Ref<EntityStore> ref) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        this.buildList(ref, commandBuilder);
        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    public record Data(String title, String content, String action) {
        public static final Codec<Data> CODEC = RecordCodec.composite(
                "@Title", Codec.STRING, Data::title,
                "@Content", Codec.STRING, Data::content,
                "Action", Codec.STRING, Data::action,
                Data::new
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        if (data.title != null || data.content != null) {
            saveBook(ref, data.title, data.content);
        }

        if ("Previous".equals(data.action)) {
            page = Math.max(0, page - 1);
            rebuildPage(ref);
        }

        if ("Next".equals(data.action)) {
            page = page + 1;
            rebuildPage(ref);
        }

        if ("Edit".equals(data.action)) {
            editMode = !editMode;
            rebuildPage(ref);
        }

        if ("Sign".equals(data.action)) {
            Player player = store.getComponent(ref, Player.getComponentType());
            assert player != null;
            player.getPageManager().openCustomPage(ref, store, new BookSignGui(playerRef, block));
        }
    }

    private void saveBook(Ref<EntityStore> ref, String title, String content) {
        BookData book = Utils.getData(ref, block, METADATA_KEY, BookData.CODEC);
        if (book.signed) return;

        BookData.Page page = book.getOrCreatePage(this.page);
        page.title = title == null ? page.title : title;
        page.content = content == null ? page.content : content;

        Utils.setData(ref, block, METADATA_KEY, BookData.CODEC, book);
    }
}