package name.modid;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class LightPointEntity extends Entity {
	private int age;
	private int fadeInStart;
	private int fadeInDuration;
	private int fadeOutStart = -1;
	private int fadeOutDuration;
	private BlockPos lanternPos;
	private LanturnColor color = LanturnColor.ORANGE;

	public LightPointEntity(EntityType<? extends LightPointEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
		RandomSource random = level.getRandom();
		fadeInDuration = random.nextInt(
				LanturnContent.LIGHT_POINT_FADE_IN_MIN_TICKS,
				LanturnContent.LIGHT_POINT_FADE_IN_MAX_TICKS + 1);
		fadeOutDuration = random.nextInt(
				LanturnContent.LIGHT_POINT_FADE_OUT_MIN_TICKS,
				LanturnContent.LIGHT_POINT_FADE_OUT_MAX_TICKS + 1);
		fadeInStart = random.nextInt(LanturnContent.LIGHT_POINT_FADE_IN_WINDOW_TICKS
				- fadeInDuration + 1);
	}

	public void setLanternPos(BlockPos lanternPos) {
		this.lanternPos = lanternPos.immutable();
	}

	public void setColor(LanturnColor color) {
		this.color = color;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			return;
		}

		age++;
		if (lanternPos != null && fadeOutStart < 0
				&& !level().getBlockState(lanternPos).is(LanturnContent.LANTURN_BLOCKS.get(color))) {
			fadeOutStart = Math.max(age, fadeInStart + fadeInDuration);
		}

		BlockPos lightPos = blockPosition();
		BlockState lightState = level().getBlockState(lightPos);
		var lightBlock = LanturnContent.FADING_LIGHTS.get(color);
		int lightLevel = getLightLevelForAge();
		if (lightState.is(lightBlock) && lightState.getValue(FadingLightBlock.LIGHT_POINT)) {
			if (lightLevel == 0) {
				level().removeBlock(lightPos, false);
			} else if (lightState.getValue(FadingLightBlock.LIGHT_LEVEL) != lightLevel) {
				level().setBlock(lightPos, lightState.setValue(FadingLightBlock.LIGHT_LEVEL, lightLevel), 3);
			}
		} else if (lightState.isAir() && lightLevel > 0) {
			level().setBlock(lightPos, lightBlock.defaultBlockState()
					.setValue(FadingLightBlock.LIGHT_LEVEL, lightLevel)
					.setValue(FadingLightBlock.FADE_START_LEVEL, LanturnContent.LIGHT_LEVEL)
					.setValue(FadingLightBlock.LIGHT_POINT, true), 3);
		}

		if (fadeOutStart >= 0 && age >= fadeOutStart + fadeOutDuration) {
			discard();
		}
	}

	private int getLightLevelForAge() {
		if (age < fadeInStart) {
			return 0;
		}
		if (age < fadeInStart + fadeInDuration) {
			return (age - fadeInStart) * LanturnContent.LIGHT_LEVEL / fadeInDuration;
		}
		if (fadeOutStart < 0 || age < fadeOutStart) {
			return LanturnContent.LIGHT_LEVEL;
		}
		return Math.max(0, LanturnContent.LIGHT_LEVEL
				- (age - fadeOutStart) * LanturnContent.LIGHT_LEVEL / fadeOutDuration);
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith(Entity entity) {
		return false;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("Age", age);
		output.putInt("FadeInStart", fadeInStart);
		output.putInt("FadeInDuration", fadeInDuration);
		output.putInt("FadeOutStart", Math.max(0, fadeOutStart));
		output.putInt("FadeOutDuration", fadeOutDuration);
		output.putString("Color", color.id());
		output.putBoolean("HasLanternPos", lanternPos != null);
		if (lanternPos != null) {
			output.putInt("LanternX", lanternPos.getX());
			output.putInt("LanternY", lanternPos.getY());
			output.putInt("LanternZ", lanternPos.getZ());
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		age = Math.max(0, input.getIntOr("Age", 0));
		fadeInDuration = Math.max(1, input.getIntOr("FadeInDuration", 80));
		fadeOutDuration = Math.max(1, input.getIntOr("FadeOutDuration", 80));
		fadeInStart = Math.max(0, input.getIntOr("FadeInStart", 0));
		int savedFadeOutStart = input.getIntOr("FadeOutStart", 0);
		fadeOutStart = savedFadeOutStart > 0
				? Math.max(fadeInStart + fadeInDuration, savedFadeOutStart)
				: -1;
		color = LanturnColor.byId(input.getStringOr("Color", "orange"));
		if (input.getBooleanOr("HasLanternPos", false)) {
			lanternPos = new BlockPos(
					input.getIntOr("LanternX", 0),
					input.getIntOr("LanternY", 0),
					input.getIntOr("LanternZ", 0));
		}
	}
}
