package net.conczin.utils;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Field;
import java.util.UUID;

public class Utils {
    public static UUID getUUID(Ref<EntityStore> ref) {
        UUIDComponent uuidComponent = ref.getStore().getComponent(ref, UUIDComponent.getComponentType());
        assert uuidComponent != null;
        return uuidComponent.getUuid();
    }

    public static <T> void setData(Ref<EntityStore> ref, BlockPosition block, String field, BuilderCodec<T> codec, T data) {
        if (block == null) {
            Inventory inventory = getInventory(ref);
            ItemStack itemInHand = inventory.getActiveHotbarItem();
            if (itemInHand != null) {
                ItemStack newItemInHand = itemInHand.withMetadata(field, codec, data);
                inventory.getHotbar().replaceItemStackInSlot(inventory.getActiveHotbarSlot(), itemInHand, newItemInHand);
            }
        } else {
            World world = ref.getStore().getExternalData().getWorld();
            ItemStack stack = getItemFromContainer(world, block, 0);
            if (stack != null) {
                ItemStack newStack = stack.withMetadata(field, codec, data);
                ItemContainerState inventory = getInventory(world, block);
                if (inventory != null) {
                    inventory.getItemContainer().setItemStackForSlot((short) 0, newStack);
                }
            }
        }
    }

    public static <T> T getData(Ref<EntityStore> ref, BlockPosition block, String field, BuilderCodec<T> codec) {
        ItemStack stack;
        if (block == null) {
            Inventory inventory = getInventory(ref);
            stack = inventory.getActiveHotbarItem();
        } else {
            World world = ref.getStore().getExternalData().getWorld();
            stack = getItemFromContainer(world, block, 0);
        }
        if (stack != null) {
            return stack.getFromMetadataOrDefault(field, codec);
        }
        return codec.getDefaultValue();
    }

    public static Inventory getInventory(Ref<EntityStore> ref) {
        Player player = ref.getStore().getComponent(ref, Player.getComponentType());
        assert player != null;
        return player.getInventory();
    }


    public static <T> T get(Object target, String fieldName, Class<T> type) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object value = f.get(target);
            return type.cast(value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static ItemContainerState getInventory(World world, BlockPosition targetBlock) {
        // noinspection deprecation
        if (world.getState(targetBlock.x, targetBlock.y, targetBlock.z, true) instanceof ItemContainerState itemcontainerstate) {
            return itemcontainerstate;
        }
        return null;
    }

    public static ItemStack getItemFromContainer(World world, BlockPosition targetBlock, int slot) {
        ItemContainerState inventory = getInventory(world, targetBlock);
        if (inventory != null) {
            return inventory.getItemContainer().getItemStack((short) slot);
        }
        return null;
    }
}
