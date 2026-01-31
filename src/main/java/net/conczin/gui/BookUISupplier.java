package net.conczin.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.conczin.utils.RecordCodec;

import javax.annotation.Nonnull;

public record BookUISupplier(
        String style,
        String background
) implements OpenCustomUIInteraction.CustomPageSupplier {
    public static final Codec<BookUISupplier> CODEC = RecordCodec.composite(
            BookUISupplier::new,
            new RecordCodec.Field<>("Style", Codec.STRING, BookUISupplier::style, "Book"),
            new RecordCodec.Field<>("Background", Codec.STRING, BookUISupplier::style, "Book")
    );

    @Nonnull
    @Override
    public CustomUIPage tryCreate(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor, @Nonnull PlayerRef playerRef, InteractionContext context) {
        return new BooksGui(playerRef, context.getTargetBlock(), style, background);
    }
}
