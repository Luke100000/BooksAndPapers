package net.conczin;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.conczin.data.BookData;
import net.conczin.gui.BookUISupplier;
import net.conczin.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.conczin.data.BookData.METADATA_KEY;

public class LecternInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<LecternInteraction> CODEC = BuilderCodec.builder(
                    LecternInteraction.class, LecternInteraction::new, SimpleBlockInteraction.CODEC
            )
            .documentation("Reads book from lectern on secondary interaction.")
            .build();

    @Override
    protected void interactWithBlock(
            @Nonnull World world,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InteractionType type,
            @Nonnull InteractionContext context,
            @Nullable ItemStack itemInHand,
            @Nonnull Vector3i targetPosition,
            @Nonnull CooldownHandler cooldownHandler
    ) {
        Ref<EntityStore> ref = context.getEntity();
        Store<EntityStore> store = ref.getStore();
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        PlayerRef playerref = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if (playerref == null) return;
        BlockPosition targetBlock = context.getTargetBlock();
        if (targetBlock == null) return;

        // Fetch book if item appears to be one
        BookData bookInHand = null;
        if (itemInHand != null && getCustomPageSupplier(itemInHand) != null) {
            bookInHand = itemInHand.getFromMetadataOrDefault(METADATA_KEY, BookData.CODEC);
        }

        // Fetch book in lectern if appears to be one
        BookData bookInLectern = null;
        BookUISupplier pageSupplier = null;
        ItemStack itemInLectern = Utils.getItemFromContainer(world, targetBlock, 0);
        if (itemInLectern != null) {
            pageSupplier = getCustomPageSupplier(itemInLectern);
            if (pageSupplier != null) {
                bookInLectern = itemInLectern.getFromMetadataOrDefault(METADATA_KEY, BookData.CODEC);
            }
        }

        if (bookInHand == null) {
            if (bookInLectern != null) {
                // Read the book from the lectern
                CustomUIPage customUIPage = pageSupplier.tryCreate(ref, commandBuffer, playerref, context);
                PageManager pagemanager = player.getPageManager();
                pagemanager.openCustomPage(ref, store, customUIPage);
                playSound(commandBuffer, targetPosition, ref, "SFX_Books_And_Papers_Open");
            } else {
                // The lectern is empty
                player.sendMessage(Message.translation("server.interactions.booksAndPapers.lectern.empty"));
            }
        } else {
            if (bookInLectern == null) {
                // Place book in the lectern
                byte activeHotbarSlot = player.getInventory().getActiveHotbarSlot();
                ItemContainerState inventoryState = Utils.getInventory(world, targetBlock);
                if (inventoryState == null) return;
                ItemContainer inventory = inventoryState.getItemContainer();
                if (inventory != null && player.getInventory().getHotbar().moveItemStackFromSlot(activeHotbarSlot, inventory).succeeded()) {
                    playSound(commandBuffer, targetPosition, ref, "SFX_Books_And_Papers_Place");
                }
            } else {
                // Copy book if empty
                if (bookInHand.getOrCreatePage(0).content.isEmpty()) {
                    Utils.setData(ref, null, METADATA_KEY, BookData.CODEC, bookInLectern);
                    playSound(commandBuffer, targetPosition, ref, "SFX_Books_And_Papers_Open");
                    player.sendMessage(Message.translation("server.interactions.booksAndPapers.lectern.copy_success"));
                } else {
                    player.sendMessage(Message.translation("server.interactions.booksAndPapers.lectern.copy_fail"));
                }
            }
        }
    }

    private static BookUISupplier getCustomPageSupplier(ItemStack itemStack) {
        String rootInteraction = itemStack.getItem().getInteractions().get(InteractionType.Secondary);
        RootInteraction asset = RootInteraction.getAssetMap().getAsset(rootInteraction);
        if (asset == null) return null;
        for (String interactionId : asset.getInteractionIds()) {
            Interaction interaction = Interaction.getAssetMap().getAsset(interactionId);
            if (interaction instanceof OpenCustomUIInteraction openCustomUIInteraction) {
                if (Utils.get(openCustomUIInteraction, "customPageSupplier", OpenCustomUIInteraction.CustomPageSupplier.class) instanceof BookUISupplier supplier) {
                    return supplier;
                }
            }
        }
        return null;
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
