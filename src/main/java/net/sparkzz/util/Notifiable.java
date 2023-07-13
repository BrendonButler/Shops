package net.sparkzz.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Notifiable {

    private final Map<String, Optional<Object>> attributes = new HashMap<>();

    protected void resetAttributes() {
        attributes.clear();
    }

    protected void setArgsAsAttributes(String[] args) {
        for (int i = 0; i < args.length; i++)
            attributes.put("args" + i, Optional.of(args[i]));
    }

    public Map<String, Optional<Object>> getAttributes() {
        return attributes;
    }

    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    public Object setAttribute(String key, Object value) {
        attributes.put(key, Optional.of(value));
        return value;
    }
}
