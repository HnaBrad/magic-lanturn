package name.modid.mixin;

import java.util.List;

import name.modid.Lanturn;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public abstract class MiningDimensionNaturalSpawnerMixin {
	@Inject(method = "spawnForChunk", at = @At("HEAD"), cancellable = true)
	private static void lanturn$disableMiningDimensionNaturalSpawns(ServerLevel level, LevelChunk chunk,
			NaturalSpawner.SpawnState state, List<MobCategory> spawningCategories, CallbackInfo callback) {
		if (level.dimension().equals(Lanturn.MINING_DIMENSION)) {
			callback.cancel();
		}
	}
}
