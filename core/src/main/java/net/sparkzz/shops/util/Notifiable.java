package net.sparkzz.shops.util;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Helper class for attributes on Notifiable classes
 */
public abstract class Notifiable {

    private final Map<String, Object> attributes = new HashMap<>();

    protected void resetAttributes() {
        attributes.clear();
    }

    /**
     * Maps the provided arguments based on the pattern: ("arg#", "args[#]")
     *
     * @param args the arguments to be mapped
     */
    protected void setArgsAsAttributes(String[] args) {
        for (int i = 0; i < args.length; i++)
            attributes.put("arg" + i, args[i]);
    }

    /**
     * Get the attributes that can be used in translations
     *
     * @return the attributes set by classes extending this class
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Gets a specific attribute from the attributes map
     *
     * @param key the attribute key to be retrieved
     * @return the value mapped to the provided key
     */
    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    /**
     * Sets an attribute
     *
     * @param key the attribute key to be set
     * @param value the attribute value to be mapped to the provided key
     * @return the provided value back to the calling method
     */
    public Object setAttribute(String key, @Nullable Object value) {
        attributes.put(key, value);
        return value;
    }
}
