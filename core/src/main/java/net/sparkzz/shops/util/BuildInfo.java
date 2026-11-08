package net.sparkzz.shops.util;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public record BuildInfo(String commit, String build) {

    public static final String UNKNOWN_COMMIT = "local";
    public static final String UNKNOWN_BUILD = "dev";
    public static final BuildInfo UNKNOWN = new BuildInfo(UNKNOWN_COMMIT, UNKNOWN_BUILD);

    private static final Attributes.Name COMMIT = new Attributes.Name("Shops-Commit");
    private static final Attributes.Name BUILD = new Attributes.Name("Shops-Build");

    public static BuildInfo from(@Nullable Manifest manifest) {
        if (manifest == null) return UNKNOWN;

        Attributes main = manifest.getMainAttributes();

        return new BuildInfo(
                valueOr(main.getValue(COMMIT), UNKNOWN_COMMIT),
                valueOr(main.getValue(BUILD), UNKNOWN_BUILD)
        );
    }

    public static BuildInfo load(@Nullable File jar) {
        if (jar == null || !jar.isFile()) return UNKNOWN;

        try (JarFile jarFile = new JarFile(jar)) {
            return from(jarFile.getManifest());
        } catch (IOException e) {
            return UNKNOWN;
        }
    }

    private static String valueOr(@Nullable String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
