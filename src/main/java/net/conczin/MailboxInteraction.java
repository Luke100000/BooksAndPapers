package net.conczin;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.conczin.data.MailboxResource;
import net.conczin.gui.MailComposeGui;
import net.conczin.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class MailboxInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<MailboxInteraction> CODEC = BuilderCodec.builder(
                    MailboxInteraction.class, MailboxInteraction::new, SimpleBlockInteraction.CODEC
            )
            .documentation("Retrieves or sends mail.")
            .build();

    @Override
    protected void interactWithBlock(
            @Nonnull World world,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InteractionType type,
            @Nonnull InteractionContext context,
            @Nullable ItemStack itemInHand,
            @Nonnull Vector3i targetBlock,
            @Nonnull CooldownHandler cooldownHandler
    ) {
        Ref<EntityStore> ref = context.getEntity();
        Store<EntityStore> store = ref.getStore();
        UUID uuid = Utils.getUUID(ref);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        PlayerRef playerref = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if (playerref == null) return;

        if (itemInHand != null && Utils.getBookSupplier(itemInHand) != null) {
            // Send mail
            player.getPageManager().openCustomPage(ref, store, new MailComposeGui(playerref));
            playSound(commandBuffer, targetBlock, ref, "SFX_Books_And_Papers_Mailbox_Send");
        } else {
            // Retrieve mail
            MailboxResource mailboxResource = store.getResource(MailboxResource.getResourceType());
            MailboxResource.MailBox mailbox = mailboxResource.getMailbox(uuid);
            mailbox.setPlayerName(player.getDisplayName());
            if (mailbox.hasMail()) {
                ItemStack pop = mailbox.pop();
                if (pop != null) {
                    SimpleItemContainer.addOrDropItemStacks(store, ref, player.getInventory().getCombinedHotbarFirst(), List.of(pop));
                    playSound(commandBuffer, targetBlock, ref, "SFX_Books_And_Papers_Mailbox_Receive");
                }
            } else {
                player.sendMessage(Message.translation("server.interactions.booksAndPapers.mailbox.empty"));
                playSound(commandBuffer, targetBlock, ref, "SFX_Books_And_Papers_Mailbox_Empty");
            }
        }
    }

    private static void playSound(CommandBuffer<EntityStore> commandBuffer, Vector3i targetBlock, Ref<EntityStore> ref, String sound) {
        int soundEventIndex = SoundEvent.getAssetMap().getIndex(sound);
        SoundUtil.playSoundEvent3d(ref, soundEventIndex, targetBlock.x, targetBlock.y, targetBlock.z, commandBuffer);
    }

    @Override
    protected void simulateInteractWithBlock(
            @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
    ) {
        // NOP
    }
}
