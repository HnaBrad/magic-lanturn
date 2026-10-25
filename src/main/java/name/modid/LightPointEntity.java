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
	private int fadeOutStart;
	private int fadeOutDuration;

	public LightPointEntity(EntityType<? extends LightPointEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
		RandomSource random = level.getRandom();
		fadeInDuration = random.nextInt(40, 121);
		fadeOutDuration = random.nextInt(40, 121);
		fadeInStart = random.nextInt(LanturnContent.LIGHT_POINT_LIFETIME_TICKS
				- fadeInDuration - fadeOutDuration + 1);
		int earliestFadeOut = fadeInStart + fadeInDuration;
		int latestFadeOut = LanturnContent.LIGHT_POINT_LIFETIME_TICKS - fadeOutDuration;
		fadeOutStart = random.nextInt(earliestFadeOut, latestFadeOut + 1);
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
		BlockPos lightPos = blockPosition();
		BlockState lightState = level().getBlockState(lightPos);
		if (lightState.is(LanturnContent.FADING_LIGHT) && lightState.getValue(FadingLightBlock.LIGHT_POINT)) {
			int lightLevel = getLightLevelForAge();
			if (lightLevel == 0) {
				level().removeBlock(lightPos, false);
			} else if (lightState.getValue(FadingLightBlock.LIGHT_LEVEL) != lightLevel) {
				level().setBlock(lightPos, lightState.setValue(FadingLightBlock.LIGHT_LEVEL, lightLevel), 3);
			}
		}

		if (age >= LanturnContent.LIGHT_POINT_LIFETIME_TICKS) {
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
		if (age < fadeOutStart) {
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
		output.putInt("FadeOutStart", fadeOutStart);
		output.putInt("FadeOutDuration", fadeOutDuration);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		age = Math.max(0, input.getIntOr("Age", 0));
		fadeInDuration = Math.max(1, input.getIntOr("FadeInDuration", 80));
		fadeOutDuration = Math.max(1, input.getIntOr("FadeOutDuration", 80));
		fadeInStart = Math.max(0, input.getIntOr("FadeInStart", 0));
		fadeOutStart = Math.max(fadeInStart + fadeInDuration, input.getIntOr("FadeOutStart", 400));
	}
}
