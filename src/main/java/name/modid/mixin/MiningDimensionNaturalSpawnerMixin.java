package name.modid.mixin;

import name.modid.Lanturn;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public abstract class MiningDimensionNaturalSpawnerMixin {
	@Inject(method = "spawnForChunk", at = @At("HEAD"), cancellable = true)
	private static void lanturn$disableMiningDimensionNaturalSpawns(ServerLevel level, CallbackInfo callback) {
		if (level.dimension().equals(Lanturn.MINING_DIMENSION)) {
			callback.cancel();
		}
	}
}
