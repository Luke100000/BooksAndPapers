package net.conczin.data;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.conczin.BooksAndPapers;
import net.conczin.utils.ListCodec;

import javax.annotation.Nullable;
import java.util.*;

public class Mailbox implements Resource<EntityStore> {
    public static final BuilderCodec<Mailbox> CODEC = BuilderCodec.builder(
                    Mailbox.class, Mailbox::new
            )
            .append(
                    new KeyedCodec<>(
                            "Inbox",
                            new MapCodec<>(
                                    new ListCodec<>(ItemStack.CODEC),
                                    HashMap::new
                            ),
                            true
                    ),
                    (o, map) -> {
                        if (map != null) {
                            map.forEach((k, v) ->
                                    o.inbox.put(UUID.fromString(k), new LinkedList<>(v))
                            );
                        }
                    },
                    o -> {
                        Map<String, List<ItemStack>> out = new HashMap<>();
                        o.inbox.forEach((uuid, inner) ->
                                out.put(uuid.toString(), inner)
                        );
                        return out;
                    }
            )
            .add()
            .build();


    private final Map<UUID, List<ItemStack>> inbox = new HashMap<>();

    public static ResourceType<EntityStore, Mailbox> getResourceType() {
        return BooksAndPapers.getInstance().getMailbox();
    }

    public Mailbox() {
    }

    public Mailbox(Mailbox other) {
        other.inbox.forEach((k, v) ->
                this.inbox.put(k, new LinkedList<>(v))
        );
    }

    public void push(UUID playerUuid, ItemStack item) {
        inbox.computeIfAbsent(playerUuid, _ -> new LinkedList<>()).add(item);
    }

    public ItemStack pop(UUID playerUuid) {
        List<ItemStack> items = inbox.get(playerUuid);
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.removeFirst();
    }

    public boolean has(UUID playerUuid) {
        List<ItemStack> items = inbox.get(playerUuid);
        return items != null && !items.isEmpty();
    }

    @Nullable
    @Override
    public Resource<EntityStore> clone() {
        return new Mailbox(this);
    }
}
