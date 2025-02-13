package red.jackf.jsst.impl;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.feature.bannerwriter.BannerWriter;
import red.jackf.jsst.impl.feature.beaconenhancement.BeaconEnhancement;
import red.jackf.jsst.impl.feature.campfiretimers.CampfireTimers;
import red.jackf.jsst.impl.feature.extrahighlights.ExtraHighlights;
import red.jackf.jsst.impl.feature.itemeditor.ItemEditor;
import red.jackf.jsst.impl.feature.mapeditor.MapEditor;
import red.jackf.jsst.impl.feature.portablecrafting.PortableCrafting;
import red.jackf.jsst.impl.feature.saplingreplant.SaplingReplant;
import red.jackf.jsst.impl.utils.Scheduler;
import red.jackf.jsst.impl.utils.sgui.labels.LabelMaps;

public class JSST implements ModInitializer {
	public static final String MOD_ID = "jsst";
	public static ResourceLocation id(String path) {
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
		JSSTConfig.loadAndVerify();

		LabelMaps.touch();

		Scheduler.setup();

		BannerWriter.setup();
		BeaconEnhancement.setup();
		CampfireTimers.setup();
		ExtraHighlights.setup();
		ItemEditor.setup();
		MapEditor.setup();
		PortableCrafting.setup();
		SaplingReplant.setup();
	}
}