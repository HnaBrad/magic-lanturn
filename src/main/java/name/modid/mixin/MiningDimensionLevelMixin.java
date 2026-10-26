package name.modid.mixin;

import name.modid.Lanturn;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class MiningDimensionLevelMixin {
	@Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
	private void lanturn$disableMiningDimensionRain(CallbackInfoReturnable<Boolean> callback) {
		if (((Level) (Object) this).dimension().equals(Lanturn.MINING_DIMENSION)) {
			callback.setReturnValue(false);
		}
	}

	@Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
	private void lanturn$disableMiningDimensionThunder(CallbackInfoReturnable<Boolean> callback) {
		if (((Level) (Object) this).dimension().equals(Lanturn.MINING_DIMENSION)) {
			callback.setReturnValue(false);
		}
	}
}
