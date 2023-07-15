package net.sparkzz.util;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Notifiable {

    private final Map<String, Object> attributes = new HashMap<>();

    protected void resetAttributes() {
        attributes.clear();
    }

    protected void setArgsAsAttributes(String[] args) {
        for (int i = 0; i < args.length; i++)
            attributes.put("arg" + i, args[i]);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    public Object setAttribute(String key, @Nullable Object value) {
        attributes.put(key, value);
        return value;
    }
}
