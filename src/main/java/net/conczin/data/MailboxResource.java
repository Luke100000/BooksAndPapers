package net.conczin.data;

import com.hypixel.hytale.codec.Codec;
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

public class MailboxResource implements Resource<EntityStore> {
    public static class MailBox {
        private final List<ItemStack> mails;
        private String playerName;

        public static final BuilderCodec<MailBox> CODEC = BuilderCodec.builder(
                        MailBox.class, MailBox::new
                )
                .append(
                        new KeyedCodec<>(
                                "Mails",
                                new ListCodec<>(ItemStack.CODEC),
                                true
                        ),
                        (o, list) -> {
                            if (list != null) {
                                o.mails.addAll(list);
                            }
                        },
                        o -> o.mails
                )
                .add()
                .append(
                        new KeyedCodec<>(
                                "PlayerName",
                                Codec.STRING,
                                false
                        ),
                        (o, name) -> o.playerName = name,
                        o -> o.playerName
                )
                .add()
                .build();

        public MailBox() {
            this.mails = new LinkedList<>();
            this.playerName = "";
        }

        public MailBox(MailBox other) {
            this.mails = new LinkedList<>(other.mails);
            this.playerName = other.playerName;
        }

        public void push(ItemStack item) {
            mails.add(item);
        }

        public ItemStack pop() {
            if (mails.isEmpty()) {
                return null;
            }
            return mails.removeFirst();
        }

        public boolean hasMail() {
            return !mails.isEmpty();
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }
    }

    public static final BuilderCodec<MailboxResource> CODEC = BuilderCodec.builder(
                    MailboxResource.class, MailboxResource::new
            )
            .append(
                    new KeyedCodec<>(
                            "Mailboxes",
                            new MapCodec<>(
                                    MailBox.CODEC,
                                    HashMap::new
                            ),
                            true
                    ),
                    (o, map) -> {
                        if (map != null) {
                            map.forEach((k, v) ->
                                    o.mailboxes.put(UUID.fromString(k), new MailBox(v))
                            );
                        }
                    },
                    o -> {
                        Map<String, MailBox> out = new HashMap<>();
                        o.mailboxes.forEach((uuid, mailbox) ->
                                out.put(uuid.toString(), mailbox)
                        );
                        return out;
                    }
            )
            .add()
            .build();


    private final Map<UUID, MailBox> mailboxes = new HashMap<>();

    public static ResourceType<EntityStore, MailboxResource> getResourceType() {
        return BooksAndPapers.getInstance().getMailbox();
    }

    public MailboxResource() {
    }

    public MailboxResource(MailboxResource other) {
        other.mailboxes.forEach((k, v) -> this.mailboxes.put(k, new MailBox(v)));
    }

    public void push(UUID playerUuid, ItemStack item) {
        mailboxes.computeIfAbsent(playerUuid, _ -> new MailBox()).push(item);
    }

    public MailBox getMailbox(UUID playerUuid) {
        return mailboxes.computeIfAbsent(playerUuid, _ -> new MailBox());
    }

    public Map<UUID, MailBox> getMailboxes() {
        return mailboxes;
    }

    @Nullable
    @Override
    public Resource<EntityStore> clone() {
        return new MailboxResource(this);
    }
}
