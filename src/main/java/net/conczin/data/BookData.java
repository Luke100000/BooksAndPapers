package net.conczin.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import net.conczin.utils.ListCodec;

import java.util.LinkedList;
import java.util.List;


public final class BookData {
    public static final String METADATA_KEY = "BookAndPapers_BookData";

    public static final BuilderCodec<BookData> CODEC = BuilderCodec.builder(BookData.class, BookData::new)
            .appendInherited(
                    new KeyedCodec<>("Title", Codec.STRING),
                    (o, v) -> o.title = v,
                    o -> o.title,
                    (o, p) -> o.title = p.title)
            .add()
            .appendInherited(
                    new KeyedCodec<>("Pages", new ListCodec<>(Page.CODEC)),
                    (o, v) -> o.pages = v,
                    o -> o.pages,
                    (o, p) -> o.pages = p.pages)
            .add()
            .appendInherited(
                    new KeyedCodec<>("Author", Codec.STRING),
                    (o, v) -> o.author = v,
                    o -> o.author,
                    (o, p) -> o.author = p.author)
            .add()
            .appendInherited(
                    new KeyedCodec<>("Signed", Codec.BOOLEAN),
                    (o, v) -> o.signed = v,
                    o -> o.signed,
                    (o, p) -> o.signed = p.signed)
            .add()
            .build();

    public String title = "";
    public List<Page> pages = new LinkedList<>();
    public String author = "";
    public boolean signed = false;

    public Page getOrCreatePage(int page) {
        page = Math.max(0, page);
        while (pages.size() <= page) {
            pages.add(new Page());
        }
        return pages.get(page);
    }

    public void trim() {
        while (!pages.isEmpty() && pages.getFirst().content.isEmpty() && pages.getFirst().title.isEmpty()) {
            pages.removeFirst();
        }
        if (pages.isEmpty()) {
            pages.add(new Page());
        }
    }

    public static final class Page {
        public static final BuilderCodec<Page> CODEC = BuilderCodec.builder(Page.class, Page::new)
                .appendInherited(
                        new KeyedCodec<>("Title", Codec.STRING),
                        (o, v) -> o.title = v,
                        o -> o.title,
                        (o, p) -> o.title = p.title)
                .add()
                .appendInherited(
                        new KeyedCodec<>("Content", Codec.STRING),
                        (o, v) -> o.content = v,
                        o -> o.content,
                        (o, p) -> o.content = p.content)
                .add()
                .build();

        public String title = "";
        public String content = "";
    }
}
