package name.modid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public final class LanturnBlock extends Block {
	private final LanturnColor color;
	private final Block lightBlock;
	private static final int LIGHT_POINT_COUNT = 1000;
	private static final int GRID_Y_LAYERS = 12;
	private static final int GRID_AREA_SIZE = 50;
	private static final int GRID_MIN_OFFSET = -GRID_AREA_SIZE / 2;
	private static final int GRID_MIN_Y_OFFSET = -12;

	public LanturnBlock(BlockBehaviour.Properties properties, LanturnColor color) {
		super(properties);
		this.color = color;
		this.lightBlock = LanturnContent.FADING_LIGHTS.get(color);
		registerDefaultState(defaultBlockState().setValue(LanturnContent.LIGHT_LEVEL_PROPERTY, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LanturnContent.LIGHT_LEVEL_PROPERTY);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (!level.isClientSide() && !oldState.is(this)) {
			level.scheduleTick(pos, this, LanturnContent.ticksUntilNextFadeInLevel(1));
			for (BlockPos lightPos : lightPointPositions(pos, level.getRandom())) {
				if (!level.isInWorldBounds(lightPos) || !level.getBlockState(lightPos).isAir()) {
					continue;
				}
				level.setBlock(lightPos, lightBlock.defaultBlockState()
						.setValue(FadingLightBlock.LIGHT_LEVEL, 0)
						.setValue(FadingLightBlock.FADE_START_LEVEL, LanturnContent.LIGHT_LEVEL)
						.setValue(FadingLightBlock.LIGHT_POINT, true), 3);
				level.scheduleTick(lightPos, lightBlock,
						level.getRandom().nextInt(LanturnContent.LIGHT_POINT_FADE_IN_WINDOW_TICKS
								- LanturnContent.LIGHT_POINT_FADE_IN_MAX_TICKS + 1));
			}
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int lightLevel = state.getValue(LanturnContent.LIGHT_LEVEL_PROPERTY);
		if (lightLevel >= LanturnContent.LIGHT_LEVEL) {
			return;
		}

		int nextLightLevel = lightLevel + 1;
		level.setBlock(pos, state.setValue(LanturnContent.LIGHT_LEVEL_PROPERTY, nextLightLevel), 3);
		if (nextLightLevel < LanturnContent.LIGHT_LEVEL) {
			level.scheduleTick(pos, this, LanturnContent.ticksUntilNextFadeInLevel(nextLightLevel + 1));
		}
	}

	private static List<BlockPos> lightPointPositions(BlockPos lanternPos, RandomSource random) {
		List<BlockPos> positions = new ArrayList<>(LIGHT_POINT_COUNT);
		for (int layer = 0; layer < GRID_Y_LAYERS; layer++) {
			int y = GRID_MIN_Y_OFFSET + layer * 5;
			int pointsInLayer = LIGHT_POINT_COUNT / GRID_Y_LAYERS
					+ (layer < LIGHT_POINT_COUNT % GRID_Y_LAYERS ? 1 : 0);
			Set<BlockPos> layerPositions = new HashSet<>(pointsInLayer);
			while (layerPositions.size() < pointsInLayer) {
				int x = GRID_MIN_OFFSET + random.nextInt(GRID_AREA_SIZE);
				int z = GRID_MIN_OFFSET + random.nextInt(GRID_AREA_SIZE);
				layerPositions.add(lanternPos.offset(x, y, z));
			}
			positions.addAll(layerPositions);
		}
		return positions;
	}

	@Override
	public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state,
			BlockEntity blockEntity, ItemStack tool) {
		super.playerDestroy(level, player, pos, state, blockEntity, tool);
		RandomSource random = level.getRandom();
		for (int layer = 0; layer < GRID_Y_LAYERS; layer++) {
			int y = GRID_MIN_Y_OFFSET + layer * 5;
			for (int x = GRID_MIN_OFFSET; x < GRID_MIN_OFFSET + GRID_AREA_SIZE; x++) {
				for (int z = GRID_MIN_OFFSET; z < GRID_MIN_OFFSET + GRID_AREA_SIZE; z++) {
					BlockPos lightPos = pos.offset(x, y, z);
					BlockState lightState = level.getBlockState(lightPos);
					if (lightState.is(lightBlock) && lightState.getValue(FadingLightBlock.LIGHT_POINT)) {
						int lightLevel = lightState.getValue(FadingLightBlock.LIGHT_LEVEL);
						level.setBlock(lightPos, lightState
								.setValue(FadingLightBlock.FADING_OUT, true)
								.setValue(FadingLightBlock.FADE_START_LEVEL,
										Math.max(1, lightLevel)), 3);
						if (lightLevel == LanturnContent.LIGHT_LEVEL) {
							level.scheduleTick(lightPos, lightBlock,
									LanturnContent.ticksUntilNextFadeOutLevel(lightLevel, lightLevel));
						}
					}
				}
			}
		}

		int lightLevel = state.getValue(LanturnContent.LIGHT_LEVEL_PROPERTY);
		if (lightLevel > 0) {
			level.setBlock(pos, lightBlock.defaultBlockState()
					.setValue(FadingLightBlock.LIGHT_LEVEL, lightLevel)
					.setValue(FadingLightBlock.FADE_START_LEVEL, lightLevel), 3);
			level.scheduleTick(pos, lightBlock,
					LanturnContent.ticksUntilNextFadeOutLevel(lightLevel, lightLevel));
		}
	}
}
