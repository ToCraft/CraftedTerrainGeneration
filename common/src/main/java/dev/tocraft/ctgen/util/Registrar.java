package dev.tocraft.ctgen.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class Registrar<T> {
    private final Map<ResourceLocation, T> VALUE_BY_ID = new HashMap<>();
    private final Map<T, ResourceLocation> ID_BY_VALUE = new IdentityHashMap<>();

    public void clear() {
        VALUE_BY_ID.clear();
        ID_BY_VALUE.clear();
    }

    public <V> void register(ResourceLocation id, T obj) {
        VALUE_BY_ID.put(id, obj);
        ID_BY_VALUE.put(obj, id);
    }

    @Nullable
    public T get(ResourceLocation id) {
        return VALUE_BY_ID.get(id);
    }

    @Nullable
    public ResourceLocation getId(T value) {
        return ID_BY_VALUE.get(value);
    }

    @NotNull
    public Codec<T> byNameCodec() {
        return ResourceLocation.CODEC.flatXmap(
                id -> Optional.ofNullable(get(id))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> String.format("Unknown key: %s", id))),
                c -> Optional.ofNullable(getId(c))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> String.format("Unknown element: %s", c)))
        );
    }
}
