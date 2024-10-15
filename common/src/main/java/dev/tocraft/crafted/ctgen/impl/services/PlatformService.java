package dev.tocraft.crafted.ctgen.impl.services;

import java.util.ServiceLoader;

public interface PlatformService {
    static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed loading service " + clazz.getName()));
    }
}
