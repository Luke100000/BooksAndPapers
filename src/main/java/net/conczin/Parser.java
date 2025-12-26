package net.conczin;

import com.hypixel.hytale.server.core.Message;

import java.util.LinkedList;
import java.util.List;

public class Parser {
    public static Message parse(String input) {
        String color = null;
        boolean bold = false;
        boolean italic = false;
        boolean monospace = false;

        List<Message> messages = new LinkedList<>();

        int i = 0;
        int n = input.length();
        int last = 0;

        while (i < n) {
            char c = input.charAt(i);

            // New tag
            if (c == '<') {
                if (i > last) {
                    String chunk = input.substring(last, i);
                    messages.add(getMessage(chunk, color, bold, italic, monospace));
                }

                int end = input.indexOf('>', i);
                if (end < 0) break;

                String tag = input.substring(i + 1, end);
                i = end + 1;
                last = i;

                boolean closing = tag.startsWith("/");
                if (closing) tag = tag.substring(1);

                if (tag.equals("b")) {
                    bold = !closing;
                } else if (tag.equals("i")) {
                    italic = !closing;
                } else if (tag.equals("m")) {
                    monospace = !closing;
                } else if (tag.startsWith("color")) {
                    if (!closing) {
                        int p = tag.indexOf("is=\"");
                        if (p != -1) {
                            int q = tag.indexOf('"', p + 4);
                            if (q != -1) color = tag.substring(p + 4, q);
                        }
                    } else {
                        color = null;
                    }
                }

                continue;
            }

            i++;
        }

        if (i > last) {
            String chunk = input.substring(last, i);
            messages.add(getMessage(chunk, color, bold, italic, monospace));
        }

        return Message.join(messages.toArray(new Message[0]));
    }

    private static Message getMessage(String chunk, String color, boolean bold, boolean italic, boolean monospace) {
        Message m = Message.raw(chunk);
        if (color != null) m = m.color(color);
        if (bold) m = m.bold(true);
        if (italic) m = m.italic(true);
        if (monospace) m = m.monospace(true);
        return m;
    }
}
