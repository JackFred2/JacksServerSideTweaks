package red.jackf.jsst.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.api.SyntaxError;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import red.jackf.jsst.JSST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JSSTConfig {
    @Comment("Right clicking with a crafting table in your hand opens the screen, without needing to place it")
    public PortableCrafting portableCrafting = new PortableCrafting();

    public static class PortableCrafting {
        public boolean enabled = true;

        @Comment("Valid values: always, sneak_only")
        public Mode mode = Mode.always;

        @Comment("IDs of items that count as crafting tables")
        public Set<ResourceLocation> items = new HashSet<>(List.of(new ResourceLocation("minecraft:crafting_table")));

        public enum Mode {
            always,
            sneak_only
        }
    }

    @Comment("Right clicking walls with a stick lets you change their appearance")
    public WallEditing wallEditing = new WallEditing();

    public static class WallEditing {
        public boolean enabled = true;
    }

    @Comment("Right clicking with a shulker box will open it.")
    public PortableShulkerBox portableShulkerBox = new PortableShulkerBox();

    public static class PortableShulkerBox {
        public boolean enabled = true;

        @Comment("Valid values: always, sneak_only")
        public PortableShulkerBox.Mode mode = PortableShulkerBox.Mode.always;

        public enum Mode {
            always,
            sneak_only
        }
    }

    public static class Handler {
        private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("jsst.json5");

        private JSSTConfig instance = null;

        public JSSTConfig get() {
            if (instance == null) load();
            return instance;
        }

        public void load() {
            if (Files.exists(PATH)) {
                try {
                    var json = JSSTJankson.INSTANCE.load(PATH.toFile());
                    instance = JSSTJankson.INSTANCE.fromJson(json, JSSTConfig.class);
                } catch (IOException e) {
                    JSST.LOGGER.error("Couldn't read the config file", e);
                    instance = new JSSTConfig();
                } catch (SyntaxError e) {
                    JSST.LOGGER.error(e.getMessage());
                    JSST.LOGGER.error(e.getLineMessage());
                    instance = new JSSTConfig();
                }
                JSST.LOGGER.info("Loaded config.");
            } else {
                instance = new JSSTConfig();
            }
            save();
        }

        public void save() {
            var config = get();
            var json = JSSTJankson.INSTANCE.toJson(config);
            try {
                Files.writeString(PATH, json.toJson(JSSTJankson.GRAMMAR));
            } catch (IOException e) {
                JSST.LOGGER.error("Couldn't save the config file", e);
            }
        }
    }
}
