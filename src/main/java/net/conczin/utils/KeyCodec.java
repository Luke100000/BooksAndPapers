package net.conczin.utils;

import java.util.UUID;

public interface KeyCodec<K> {
    String encode(K key);

    K decode(String key);

    KeyCodec<UUID> UUID_KEY_CODEC = new KeyCodec<>() {
        @Override
        public String encode(UUID key) {
            return key.toString();
        }

        @Override
        public UUID decode(String key) {
            return UUID.fromString(key);
        }
    };
}
