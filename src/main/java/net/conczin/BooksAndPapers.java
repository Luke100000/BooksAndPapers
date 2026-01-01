package net.conczin;

import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.conczin.data.MailboxResource;
import net.conczin.gui.BookUISupplier;

import javax.annotation.Nonnull;


public class BooksAndPapers extends JavaPlugin {
    private static BooksAndPapers instance;

    private ResourceType<EntityStore, MailboxResource> mailbox;


    public BooksAndPapers(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        this.mailbox = this.getEntityStoreRegistry().registerResource(MailboxResource.class, "BooksAndPapersMailboxes", MailboxResource.CODEC);

        this.getCodecRegistry(Interaction.CODEC).register("Books_And_Papers_Mailbox", MailboxInteraction.class, MailboxInteraction.CODEC);

        this.getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("Books_And_Papers_Book", BookUISupplier.class, BookUISupplier.CODEC);
    }

    public static BooksAndPapers getInstance() {
        return instance;
    }

    public ResourceType<EntityStore, MailboxResource> getMailbox() {
        return mailbox;
    }
}