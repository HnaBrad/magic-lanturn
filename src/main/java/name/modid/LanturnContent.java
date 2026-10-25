package name.modid;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class LanturnContent {
	private static final ResourceKey<CreativeModeTab> LANTURN_TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Lanturn.id("items"));
	private static final ResourceKey<EntityType<?>> LIGHT_POINT_ENTITY_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, Lanturn.id("light_point"));

	public static final int LIGHT_LEVEL = 15;
	public static final int FADE_IN_DURATION_TICKS = 300;
	public static final int LIGHT_POINT_FADE_IN_WINDOW_TICKS = 600;
	public static final int LIGHT_POINT_FADE_IN_MIN_TICKS = 40;
	public static final int LIGHT_POINT_FADE_IN_MAX_TICKS = 120;
	public static final int LIGHT_POINT_FADE_OUT_MIN_TICKS = 40;
	public static final int LIGHT_POINT_FADE_OUT_MAX_TICKS = 120;
	public static final int FADE_OUT_DURATION_TICKS = 200;
	public static final IntegerProperty LIGHT_LEVEL_PROPERTY = IntegerProperty.create("light_level", 0, LIGHT_LEVEL);

	public static final Map<LanturnColor, Block> FADING_LIGHTS = registerFadingLights();
	public static final Block FADING_LIGHT = FADING_LIGHTS.get(LanturnColor.WHITE);

	public static final EntityType<LightPointEntity> LIGHT_POINT_ENTITY = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			LIGHT_POINT_ENTITY_KEY,
			EntityType.Builder.of(LightPointEntity::new, MobCategory.MISC)
					.sized(0.1f, 0.1f)
					.noSummon()
					.clientTrackingRange(4)
					.build(LIGHT_POINT_ENTITY_KEY)
	);

	public static final Map<LanturnColor, Block> LANTURN_BLOCKS = registerLanturnBlocks();
	public static final Block LANTURN_BLOCK = LANTURN_BLOCKS.get(LanturnColor.WHITE);

	public static final Map<LanturnColor, Item> LANTURN_ITEMS = registerLanturnItems();
	public static final Item LANTURN_ITEM = LANTURN_ITEMS.get(LanturnColor.WHITE);

	public static final CreativeModeTab LANTURN_TAB = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			LANTURN_TAB_KEY,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
					.title(Component.translatable("itemGroup.lanturn"))
					.icon(() -> new ItemStack(LANTURN_ITEM))
					.displayItems((parameters, output) -> LANTURN_ITEMS.values().forEach(output::accept))
					.build()
	);

	private LanturnContent() {
	}

	public static void initialize() {
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
