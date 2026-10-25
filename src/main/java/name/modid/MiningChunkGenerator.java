package name.modid;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.clock.ServerClockManager;

public final class MiningChunkGenerator extends ChunkGenerator {
	public static final MapCodec<MiningChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource))
			.apply(instance, MiningChunkGenerator::new));

	private static final int MIN_Y = -64;
	private static final int GEN_DEPTH = 384;
	private static final int SURFACE_Y = 128;

	public MiningChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource, MiningChunkGenerator::oreOnlyGenerationSettings);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets,
			RandomState randomState, long seed) {
		return ChunkGeneratorStructureState.createForFlat(
				randomState, seed, getOrigin(randomState), biomeSource, Stream.empty());
	}

	@Override
	public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState,
			StructureManager structureManager, net.minecraft.world.level.biome.BiomeManager biomeManager,
			WorldGenRegion region, Set<Holder<Biome>> possibleBiomes) {
		ChunkPos chunkPos = chunk.getPos();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
			for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
				for (int y = MIN_Y; y <= SURFACE_Y; y++) {
					pos.set(x, y, z);
					chunk.setBlockState(pos, blockAt(y), 0);
				}
			}
		}
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion region) {
	}

	@Override
	public int getGenDepth() {
		return GEN_DEPTH;
	}

	@Override
	public int getSeaLevel() {
		return MIN_Y;
	}

	@Override
	public int getMinY() {
		return MIN_Y;
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level,
			RandomState randomState) {
		return SURFACE_Y + 1;
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
		BlockState[] states = new BlockState[level.getHeight()];
		Arrays.fill(states, Blocks.AIR.defaultBlockState());
		for (int y = MIN_Y; y <= SURFACE_Y; y++) {
			states[y - level.getMinY()] = blockAt(y);
		}
		return new NoiseColumn(level.getMinY(), states);
	}

	@Override
	public void addDebugScreenInfo(java.util.List<String> lines, RandomState randomState, BlockPos pos,
			net.minecraft.world.level.levelgen.densityfunction.SamplerContext samplerContext) {
	}

	private static BlockState blockAt(int y) {
		if (y == MIN_Y) {
			return Blocks.BEDROCK.defaultBlockState();
		}
		if (y < 0) {
			return Blocks.DEEPSLATE.defaultBlockState();
		}
		if (y < SURFACE_Y - 2) {
			return Blocks.STONE.defaultBlockState();
		}
		if (y < SURFACE_Y) {
			return Blocks.DIRT.defaultBlockState();
		}
		return Blocks.GRASS_BLOCK.defaultBlockState();
	}

	private static BiomeGenerationSettings oreOnlyGenerationSettings(Holder<Biome> biome) {
		BiomeGenerationSettings.PlainBuilder settings = new BiomeGenerationSettings.PlainBuilder();
		var featureSteps = biome.value().getGenerationSettings().features();
		for (int step = 0; step < featureSteps.size(); step++) {
			HolderSet<PlacedFeature> features = featureSteps.get(step);
			int featureStep = step;
			for (Holder<PlacedFeature> feature : features) {
				feature.unwrapKey().ifPresent(key -> {
					if (key.identifier().getPath().startsWith("ore_")) {
						settings.addFeature(featureStep, feature);
					}
				});
			}
		}
		return settings.build();
	}

	public static void freezeTime(ServerLevel level) {
		level.dimensionTypeRegistration().value().defaultClock().ifPresent(clock -> {
			ServerClockManager clockManager = level.clockManager();
			if (clockManager.getInstance(clock).totalTicks() != 6000L) {
				clockManager.setTotalTicks(clock, 6000L);
			}
			if (!clockManager.getInstance(clock).isPaused()) {
				clockManager.setPaused(clock, true);
			}
		});
	}
}
