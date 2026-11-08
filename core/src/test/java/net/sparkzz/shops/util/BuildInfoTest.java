package net.sparkzz.shops.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Build Info")
class BuildInfoTest {

    private static final String COMMIT = "Shops-Commit";
    private static final String BUILD = "Shops-Build";

    @Nested
    @DisplayName("from(Manifest)")
    class FromManifest {

        @Test
        @DisplayName("reads both values")
        void readsBothValues() {
            assertEquals(new BuildInfo("a1b2c3d", "312"), BuildInfo.from(manifest(Map.of(COMMIT, "a1b2c3d", BUILD, "312"))));
        }

        @Test
        @DisplayName("null manifest gives the placeholders")
        void nullManifest() {
            assertEquals(BuildInfo.UNKNOWN, BuildInfo.from(null));
        }

        @Test
        @DisplayName("missing attributes give the placeholders")
        void missingAttributes() {
            assertEquals(BuildInfo.UNKNOWN, BuildInfo.from(manifest(Map.of())));
        }

        @Test
        @DisplayName("blank values give the placeholders")
        void blankValues() {
            assertEquals(BuildInfo.UNKNOWN, BuildInfo.from(manifest(Map.of(COMMIT, "", BUILD, "   "))));
        }

        @Test
        @DisplayName("values are trimmed")
        void trimsValues() {
            assertEquals(new BuildInfo("a1b2c3d", "312"), BuildInfo.from(manifest(Map.of(COMMIT, " a1b2c3d ", BUILD, " 312 "))));
        }

        @Test
        @DisplayName("only the commit present")
        void onlyCommit() {
            assertEquals(new BuildInfo("a1b2c3d", BuildInfo.UNKNOWN_BUILD), BuildInfo.from(manifest(Map.of(COMMIT, "a1b2c3d"))));
        }

        @Test
        @DisplayName("only the build number present")
        void onlyBuild() {
            assertEquals(new BuildInfo(BuildInfo.UNKNOWN_COMMIT, "312"), BuildInfo.from(manifest(Map.of(BUILD, "312"))));
        }
    }

    @Nested
    @DisplayName("load(File)")
    class Load {

        @TempDir
        Path dir;

        @Test
        @DisplayName("reads the values from a real JAR")
        void readsJar() throws IOException {
            File jar = writeJar(dir.resolve("shops.jar"), Map.of(COMMIT, "a1b2c3d", BUILD, "312"));

            assertEquals(new BuildInfo("a1b2c3d", "312"), BuildInfo.load(jar));
        }

        @Test
        @DisplayName("JAR without the attributes gives the placeholders")
        void jarWithoutAttributes() throws IOException {
            File jar = writeJar(dir.resolve("shops.jar"), Map.of());

            assertEquals(BuildInfo.UNKNOWN, BuildInfo.load(jar));
        }

        @Test
        @DisplayName("null file gives the placeholders")
        void nullFile() {
            assertEquals(BuildInfo.UNKNOWN, BuildInfo.load(null));
        }

        @Test
        @DisplayName("missing file gives the placeholders")
        void missingFile() {
            assertEquals(BuildInfo.UNKNOWN, BuildInfo.load(dir.resolve("missing.jar").toFile()));
        }

        @Test
        @DisplayName("directory gives the placeholders")
        void directory() {
            assertEquals(BuildInfo.UNKNOWN, BuildInfo.load(dir.toFile()));
        }

        @Test
        @DisplayName("file that isn't a JAR gives the placeholders")
        void notAJar() throws IOException {
            Path file = Files.writeString(dir.resolve("shops.jar"), "not a jar");

            assertEquals(BuildInfo.UNKNOWN, BuildInfo.load(file.toFile()));
        }
    }

    /**
     * Builds a manifest with the given main attributes, plus the Manifest-Version that a JAR manifest needs to be
     * written at all
     */
    private static Manifest manifest(Map<String, String> attributes) {
        Manifest manifest = new Manifest();
        Attributes main = manifest.getMainAttributes();

        main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.forEach(main::putValue);

        return manifest;
    }

    /**
     * Writes an otherwise empty JAR with the given main manifest attributes
     */
    private static File writeJar(Path path, Map<String, String> attributes) throws IOException {
        try (OutputStream out = Files.newOutputStream(path);
             JarOutputStream ignored = new JarOutputStream(out, manifest(attributes))) {
            // The constructor writes the manifest; nothing else is needed
        }

        return path.toFile();
    }
}
