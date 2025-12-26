package net.conczin.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class KeyMapCodec<K, V, M extends Map<K, V>> implements Codec<Map<K, V>>, WrappedCodec<V> {
    private final Codec<V> valueCodec;
    private final KeyCodec<K> keyCodec;
    private final Supplier<M> supplier;
    private final boolean unmodifiable;


    public KeyMapCodec(Codec<V> valueCodec, KeyCodec<K> keyCodec, Supplier<M> supplier, boolean unmodifiable) {
        this.valueCodec = valueCodec;
        this.keyCodec = keyCodec;
        this.supplier = supplier;
        this.unmodifiable = unmodifiable;
    }

    @Override
    public Codec<V> getChildCodec() {
        return this.valueCodec;
    }

    public Map<K, V> decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo info) {
        BsonDocument bsondocument = bsonValue.asDocument();
        if (bsondocument.isEmpty()) {
            return this.unmodifiable ? Collections.emptyMap() : this.supplier.get();
        } else {
            Map<K, V> map = this.supplier.get();

            for (Entry<String, BsonValue> entry : bsondocument.entrySet()) {
                String key = entry.getKey();
                BsonValue value = entry.getValue();
                info.pushKey(key);

                try {
                    map.put(this.keyCodec.decode(key), this.valueCodec.decode(value, info));
                } catch (Exception exception) {
                    throw new CodecException("Failed to decode", value, info, exception);
                } finally {
                    info.popKey();
                }
            }

            if (this.unmodifiable) {
                map = Collections.unmodifiableMap(map);
            }

            return map;
        }
    }

    @Nonnull
    public BsonValue encode(@Nonnull Map<K, V> map, ExtraInfo extraInfo) {
        BsonDocument bsondocument = new BsonDocument();

        for (Entry<K, V> entry : map.entrySet()) {
            BsonValue bsonvalue = this.valueCodec.encode(entry.getValue(), extraInfo);
            if (bsonvalue != null
                    && !bsonvalue.isNull()
                    && (!bsonvalue.isDocument() || !bsonvalue.asDocument().isEmpty())
                    && (!bsonvalue.isArray() || !bsonvalue.asArray().isEmpty())) {
                String key = this.keyCodec.encode(entry.getKey());
                bsondocument.put(key, bsonvalue);
            }
        }

        return bsondocument;
    }

    public Map<K, V> decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
        reader.expect('{');
        reader.consumeWhiteSpace();
        if (reader.tryConsume('}')) {
            return this.unmodifiable ? Collections.emptyMap() : this.supplier.get();
        } else {
            Map<K, V> map = this.supplier.get();

            while (true) {
                String key = reader.readString();
                reader.consumeWhiteSpace();
                reader.expect(':');
                reader.consumeWhiteSpace();
                extraInfo.pushKey(key, reader);

                try {
                    map.put(this.keyCodec.decode(key), this.valueCodec.decodeJson(reader, extraInfo));
                } catch (Exception exception) {
                    throw new CodecException("Failed to decode", reader, extraInfo, exception);
                } finally {
                    extraInfo.popKey();
                }

                reader.consumeWhiteSpace();
                if (reader.tryConsumeOrExpect('}', ',')) {
                    if (this.unmodifiable) {
                        map = Collections.unmodifiableMap(map);
                    }

                    return map;
                }

                reader.consumeWhiteSpace();
            }
        }
    }

    @Nonnull
    @Override
    public Schema toSchema(@Nonnull SchemaContext context) {
        ObjectSchema objectschema = new ObjectSchema();
        objectschema.setTitle("Map");
        Schema schema = context.refDefinition(this.valueCodec);
        objectschema.setAdditionalProperties(schema);
        return objectschema;
    }
}
