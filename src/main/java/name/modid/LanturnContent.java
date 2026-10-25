package name.modid;

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
	private static final ResourceKey<Block> LANTURN_BLOCK_KEY =
			ResourceKey.create(Registries.BLOCK, Lanturn.id("lantern"));
	private static final ResourceKey<Block> FADING_LIGHT_BLOCK_KEY =
			ResourceKey.create(Registries.BLOCK, Lanturn.id("fading_light"));
	private static final ResourceKey<Item> LANTURN_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, Lanturn.id("lantern"));
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

	public static final Block FADING_LIGHT = Registry.register(
			BuiltInRegistries.BLOCK,
			FADING_LIGHT_BLOCK_KEY,
			new FadingLightBlock(BlockBehaviour.Properties.of()
					.setId(FADING_LIGHT_BLOCK_KEY)
					.noCollision()
					.noOcclusion()
					.replaceable()
					.noLootTable()
					.lightLevel(state -> state.getValue(FadingLightBlock.LIGHT_LEVEL)))
	);

	public static final EntityType<LightPointEntity> LIGHT_POINT_ENTITY = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			LIGHT_POINT_ENTITY_KEY,
			EntityType.Builder.of(LightPointEntity::new, MobCategory.MISC)
					.sized(0.1f, 0.1f)
					.noSummon()
					.clientTrackingRange(4)
					.build(LIGHT_POINT_ENTITY_KEY)
	);

	public static final Block LANTURN_BLOCK = Registry.register(
			BuiltInRegistries.BLOCK,
			LANTURN_BLOCK_KEY,
			new LanturnBlock(BlockBehaviour.Properties.of()
					.setId(LANTURN_BLOCK_KEY)
					.strength(1.0f)
					.noCollision()
					.lightLevel(state -> state.getValue(LIGHT_LEVEL_PROPERTY)))
	);

	public static final Item LANTURN_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			LANTURN_ITEM_KEY,
			new BlockItem(LANTURN_BLOCK, new Item.Properties().setId(LANTURN_ITEM_KEY))
	);

	public static final CreativeModeTab LANTURN_TAB = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			LANTURN_TAB_KEY,
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
					.title(Component.translatable("itemGroup.lanturn"))
					.icon(() -> new ItemStack(LANTURN_ITEM))
					.displayItems((parameters, output) -> output.accept(LANTURN_ITEM))
					.build()
	);

	private LanturnContent() {
	}

	public static void initialize() {
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
