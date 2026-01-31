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
import net.conczin.data.BookData;
import net.conczin.utils.RecordCodec;
import net.conczin.utils.Utils;

import javax.annotation.Nonnull;
import java.util.Objects;

import static net.conczin.data.BookData.METADATA_KEY;


public class BookSignGui extends CodecDataInteractiveUIPage<BookSignGui.Data> {
    private final BlockPosition block;

    public BookSignGui(@Nonnull PlayerRef playerRef, BlockPosition block) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);

        this.block = block;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Pages/BooksAndPapers/BookSign.ui");

        String author = Objects.requireNonNull(ref.getStore().getComponent(ref, Player.getComponentType())).getDisplayName();
        commandBuilder.set("#TitleField.Value", "");
        commandBuilder.set("#Author.Value", author);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Sign", new EventData().append("Action", "Sign").append("@Title", "#TitleField.Value").append("@Author", "#Author.Value"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Cancel", EventData.of("Action", "Cancel"));
    }

    public record Data(String title, String author, String action) {
        public static final Codec<Data> CODEC = RecordCodec.composite(
                "@Title", Codec.STRING, Data::title,
                "@Author", Codec.STRING, Data::author,
                "Action", Codec.STRING, Data::action,
                Data::new
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        if ("Sign".equals(data.action)) {
            sign(ref, data.title, data.author);
            close();
        }

        if ("Cancel".equals(data.action)) {
            close();
        }
    }

    private void sign(Ref<EntityStore> ref, String title, String author) {
        BookData book = Utils.getData(ref, block, METADATA_KEY, BookData.CODEC);
        if (book.signed) return;

        book.author = author;
        book.title = title;
        book.signed = true;
        book.trim();

        Utils.setData(ref, block, METADATA_KEY, BookData.CODEC, book);
    }
}