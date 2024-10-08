package red.jackf.jsst.impl;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jsst.impl.feature.portablecrafting.PortableCrafting;

public class JSST implements ModInitializer {
	public static final String MOD_ID = "jsst";
	public static ResourceLocation id(String path) {
		//? if <=1.20.6 {
		/*return new ResourceLocation(MOD_ID, path);
		*///?} else
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static Logger getLogger(String... path) {
		if (path == null || path.length == 0) return LoggerFactory.getLogger(MOD_ID);
		String delimiter = "/";

		return LoggerFactory.getLogger(MOD_ID + delimiter + String.join(delimiter, path));
	}
	public static final Logger LOGGER = getLogger();

	@Override
	public void onInitialize() {
		PortableCrafting.setup();
	}
}