package name.modid;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lanturn implements ModInitializer {
	public static final String MOD_ID = "lanturn";
	public static final ResourceKey<Level> MINING_DIMENSION =
			ResourceKey.create(Registries.DIMENSION, id("mining_dimension"));
	public static final ResourceKey<Level> VOID_DIMENSION =
			ResourceKey.create(Registries.DIMENSION, id("void_dimension"));

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LanturnContent.initialize();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
