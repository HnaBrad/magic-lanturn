package name.modid;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FadingLightBlock extends Block {
	public static final IntegerProperty LIGHT_LEVEL = LanturnContent.LIGHT_LEVEL_PROPERTY;
	public static final IntegerProperty FADE_START_LEVEL = IntegerProperty.create("fade_start_level", 1, LanturnContent.LIGHT_LEVEL);
	public static final BooleanProperty LIGHT_POINT = BooleanProperty.create("light_point");

	public FadingLightBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(LIGHT_LEVEL, 0)
				.setValue(FADE_START_LEVEL, 1)
				.setValue(LIGHT_POINT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIGHT_LEVEL, FADE_START_LEVEL, LIGHT_POINT);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (level.isClientSide()) {
			return;
		}
		if (state.getValue(LIGHT_POINT)) {
			return;
		}
		int lightLevel = state.getValue(LIGHT_LEVEL);
		if (lightLevel == 0) {
			level.removeBlock(pos, false);
		} else {
			level.scheduleTick(pos, this, LanturnContent.ticksUntilNextFadeOutLevel(
					state.getValue(FADE_START_LEVEL), lightLevel));
		}
	}

	@Override
	protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
			BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState state, net.minecraft.world.level.BlockGetter level,
			BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int lightLevel = state.getValue(LIGHT_LEVEL);
		if (lightLevel <= 1) {
			level.removeBlock(pos, false);
			return;
		}

		int nextLightLevel = lightLevel - 1;
		level.setBlock(pos, state.setValue(LIGHT_LEVEL, nextLightLevel), 3);
		level.scheduleTick(pos, this, LanturnContent.ticksUntilNextFadeOutLevel(
				state.getValue(FADE_START_LEVEL), nextLightLevel));
	}
}
