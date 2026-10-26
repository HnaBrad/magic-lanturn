package name.modid;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class LanturnContent {
	private static final ResourceKey<CreativeModeTab> LANTURN_TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Lanturn.id("items"));

	public static final int LIGHT_LEVEL = 15;
	public static final int FADE_IN_DURATION_TICKS = 300;
	public static final int LIGHT_POINT_FADE_IN_WINDOW_TICKS = 600;
	public static final int LIGHT_POINT_FADE_IN_MAX_TICKS = 120;
	public static final int FADE_OUT_DURATION_TICKS = 200;
	public static final IntegerProperty LIGHT_LEVEL_PROPERTY = IntegerProperty.create("light_level", 0, LIGHT_LEVEL);

	public static final Map<LanturnColor, Block> FADING_LIGHTS = registerFadingLights();
	public static final Block FADING_LIGHT = FADING_LIGHTS.get(LanturnColor.WHITE);

	public static final Map<LanturnColor, Block> LANTURN_BLOCKS = registerLanturnBlocks();
	public static final Block LANTURN_BLOCK = LANTURN_BLOCKS.get(LanturnColor.WHITE);

	public static final Map<LanturnColor, Item> LANTURN_ITEMS = registerLanturnItems();
	public static final Item LANTURN_ITEM = LANTURN_ITEMS.get(LanturnColor.WHITE);

	public static final Block MINING_PORTAL_BLOCK = registerPortalBlock("mining_dimension_portal",
			Lanturn.MINING_DIMENSION, "message.lanturn.portal.mining_unavailable");
	public static final Item MINING_PORTAL_ITEM = registerPortalItem("mining_dimension_portal",
			MINING_PORTAL_BLOCK);
	public static final Block VOID_PORTAL_BLOCK = registerPortalBlock("void_dimension_portal",
			Lanturn.VOID_DIMENSION, "message.lanturn.portal.void_unavailable");
	public static final Item VOID_PORTAL_ITEM = registerPortalItem("void_dimension_portal", VOID_PORTAL_BLOCK);

	public static final CreativeModeTab LANTURN_TAB = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			LANTURN_TAB_KEY,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
					.title(Component.translatable("itemGroup.lanturn"))
					.icon(() -> new ItemStack(LANTURN_ITEM))
					.displayItems((parameters, output) -> {
						LANTURN_ITEMS.values().forEach(output::accept);
						output.accept(MINING_PORTAL_ITEM);
						output.accept(VOID_PORTAL_ITEM);
					})
					.build()
	);

	private LanturnContent() {
	}

	public static void initialize() {
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Lanturn.id("flat_mining"), MiningChunkGenerator.CODEC);
		ServerTickEvents.START_LEVEL_TICK.register(level -> {
			if (level.dimension().equals(Lanturn.MINING_DIMENSION)) {
				MiningChunkGenerator.freezeTime(level);
			}
		});
	}

	private static Map<LanturnColor, Block> registerFadingLights() {
		Map<LanturnColor, Block> blocks = new EnumMap<>(LanturnColor.class);
		for (LanturnColor color : LanturnColor.values()) {
			ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Lanturn.id(color.lightId()));
			Block block = new FadingLightBlock(BlockBehaviour.Properties.of()
					.setId(key)
					.noCollision()
					.noOcclusion()
					.replaceable()
					.noLootTable()
					.lightLevel(state -> state.getValue(FadingLightBlock.LIGHT_LEVEL)));
			blocks.put(color, Registry.register(BuiltInRegistries.BLOCK, key, block));
		}
		return Collections.unmodifiableMap(blocks);
	}

	private static Map<LanturnColor, Block> registerLanturnBlocks() {
		Map<LanturnColor, Block> blocks = new EnumMap<>(LanturnColor.class);
		for (LanturnColor color : LanturnColor.values()) {
			ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Lanturn.id(color.lanternId()));
			Block block = new LanturnBlock(BlockBehaviour.Properties.of()
					.setId(key)
					.strength(1.0f)
					.noCollision()
					.lightLevel(state -> state.getValue(LIGHT_LEVEL_PROPERTY)), color);
			blocks.put(color, Registry.register(BuiltInRegistries.BLOCK, key, block));
		}
		return Collections.unmodifiableMap(blocks);
	}

	private static Map<LanturnColor, Item> registerLanturnItems() {
		Map<LanturnColor, Item> items = new EnumMap<>(LanturnColor.class);
		for (LanturnColor color : LanturnColor.values()) {
			ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Lanturn.id(color.lanternId()));
			Item item = new BlockItem(LANTURN_BLOCKS.get(color), new Item.Properties().setId(key));
			items.put(color, Registry.register(BuiltInRegistries.ITEM, key, item));
		}
		return Collections.unmodifiableMap(items);
	}

	private static Block registerPortalBlock(String path, ResourceKey<Level> dimension,
			String unavailableMessageKey) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Lanturn.id(path));
		Block block = new DimensionPortalBlock(BlockBehaviour.Properties.of()
				.setId(key)
				.strength(3.0f),
				dimension, unavailableMessageKey);
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	private static Item registerPortalItem(String path, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Lanturn.id(path));
		return Registry.register(BuiltInRegistries.ITEM, key,
				new BlockItem(block, new Item.Properties().setId(key)));
	}

	static int ticksUntilNextFadeInLevel(int nextLevel) {
		return ticksUntilNextFadeInLevel(FADE_IN_DURATION_TICKS, nextLevel);
	}

	private static int ticksUntilNextFadeInLevel(int durationTicks, int nextLevel) {
		int ticksPerLevel = durationTicks / LIGHT_LEVEL;
		return nextLevel == LIGHT_LEVEL
				? durationTicks - (LIGHT_LEVEL - 1) * ticksPerLevel
				: ticksPerLevel;
	}

	static int ticksUntilNextFadeOutLevel(int startingLevel, int currentLevel) {
		int ticksPerLevel = FADE_OUT_DURATION_TICKS / startingLevel;
		return currentLevel == 1
				? FADE_OUT_DURATION_TICKS - (startingLevel - 1) * ticksPerLevel
				: ticksPerLevel;
	}
}
