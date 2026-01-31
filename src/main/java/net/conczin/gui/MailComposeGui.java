package net.conczin.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.conczin.data.BookData;
import net.conczin.data.MailboxResource;
import net.conczin.utils.RecordCodec;
import net.conczin.utils.Utils;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static net.conczin.data.BookData.METADATA_KEY;


public class MailComposeGui extends CodecDataInteractiveUIPage<MailComposeGui.Data> {
    public MailComposeGui(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Pages/BooksAndPapers/Compose.ui");

        // Build recipient dropdown
        List<DropdownEntryInfo> recipients = new LinkedList<>();
        MailboxResource mailboxResource = ref.getStore().getResource(MailboxResource.getResourceType());
        for (PlayerRef player : Universe.get().getPlayers()) {
            mailboxResource.getMailbox(player.getUuid()).setPlayerName(player.getUsername());
        }
        mailboxResource.getMailboxes().entrySet().stream()
                .sorted(Comparator.comparing(m -> m.getValue().getPlayerName()))
                .forEach(mb -> recipients.add(new DropdownEntryInfo(LocalizableString.fromString(mb.getValue().getPlayerName()), String.valueOf(mb.getKey()))));
        commandBuilder.set("#Recipient.Entries", recipients);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Send", new EventData().append("Action", "Send").append("@Recipient", "#Recipient.Value"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Cancel", EventData.of("Action", "Cancel"));
    }

    public record Data(String recipient, String action) {
        public static final Codec<Data> CODEC = RecordCodec.composite(
                "@Recipient", Codec.STRING, Data::recipient,
                "Action", Codec.STRING, Data::action,
                Data::new
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        if ("Send".equals(data.action)) {
            if (data.recipient != null) {
                Inventory inventory = Utils.getInventory(ref);
                ItemStack itemInHand = inventory.getActiveHotbarItem();
                BookData book = itemInHand != null ? itemInHand.getFromMetadataOrNull(METADATA_KEY, BookData.CODEC) : null;
                if (book != null) {
                    MailboxResource mailboxResource = ref.getStore().getResource(MailboxResource.getResourceType());
                    mailboxResource.push(UUID.fromString(data.recipient), itemInHand);
                    inventory.getHotbar().replaceItemStackInSlot(inventory.getActiveHotbarSlot(), itemInHand, ItemStack.EMPTY);
                }
            }
            close();
        }

        if ("Cancel".equals(data.action)) {
            close();
        }
    }
}