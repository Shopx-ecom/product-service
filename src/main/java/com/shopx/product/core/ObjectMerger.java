package com.shopx.product.core;

import java.lang.reflect.Field;
import java.util.Map;

public class ObjectMerger {
    private static Field getSuperClassField(String key, Class<?> superClass) {
        if (superClass == null) {
            return null;
        }
        try {
            return superClass.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            return getSuperClassField(key, superClass.getSuperclass());
        }
    }

    public static <T> T mergeObjects(T original, Map<String, Object> updates) {
        try {
            Class<?> clazz = original.getClass();
            Class<?> superClazz = clazz.getSuperclass();
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                Field field = null;
                Field superClassField = null;
                try {
                    field = clazz.getDeclaredField(entry.getKey());
                } catch (NoSuchFieldException e) {
                    superClassField = getSuperClassField(entry.getKey(), superClazz);
                    if (superClassField == null) {
                        continue;
                    }
                }

                if (field != null) {
                    field.setAccessible(true);
                    field.set(original, entry.getValue());
                }
                if (superClassField != null) {
                    superClassField.setAccessible(true);
                    superClassField.set(original, entry.getValue());
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error merging objects", e);
        }
        return original;
    }
}
