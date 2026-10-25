package name.modid;

import java.util.ArrayList;
import java.util.List;

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
	private static final int LIGHT_POINT_COUNT = 150;
	private static final int LIGHT_POINT_SPACING = 5;
	private static final int GRID_MIN_OFFSET = -10;
	private static final int GRID_MAX_OFFSET = 10;
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
			ServerLevel serverLevel = (ServerLevel) level;
			for (BlockPos lightPos : lightPointPositions(pos)) {
				if (level.isInWorldBounds(lightPos) && level.getBlockState(lightPos).isAir()) {
					level.setBlock(lightPos, lightBlock.defaultBlockState()
							.setValue(FadingLightBlock.LIGHT_LEVEL, 0)
							.setValue(FadingLightBlock.FADE_START_LEVEL, LanturnContent.LIGHT_LEVEL)
							.setValue(FadingLightBlock.LIGHT_POINT, true), 3);
					LightPointEntity lightPoint = new LightPointEntity(LanturnContent.LIGHT_POINT_ENTITY, level);
					lightPoint.setPos(lightPos.getX() + 0.5, lightPos.getY() + 0.5, lightPos.getZ() + 0.5);
					lightPoint.setLanternPos(pos);
					lightPoint.setColor(color);
					if (!serverLevel.addFreshEntity(lightPoint)) {
						level.removeBlock(lightPos, false);
					}
				}
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

	private static List<BlockPos> lightPointPositions(BlockPos lanternPos) {
		List<BlockPos> positions = new ArrayList<>(LIGHT_POINT_COUNT);
		for (int x = GRID_MIN_OFFSET; x <= GRID_MAX_OFFSET; x += LIGHT_POINT_SPACING) {
			for (int y = GRID_MIN_Y_OFFSET; y < GRID_MIN_Y_OFFSET + 6 * LIGHT_POINT_SPACING; y += LIGHT_POINT_SPACING) {
				for (int z = GRID_MIN_OFFSET; z <= GRID_MAX_OFFSET; z += LIGHT_POINT_SPACING) {
					positions.add(lanternPos.offset(x, y, z));
				}
			}
		}
		return positions;
	}

	@Override
	public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state,
			BlockEntity blockEntity, ItemStack tool) {
		super.playerDestroy(level, player, pos, state, blockEntity, tool);
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
