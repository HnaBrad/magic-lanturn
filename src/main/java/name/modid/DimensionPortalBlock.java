package name.modid;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;

public final class DimensionPortalBlock extends Block {
	public static final AttachmentType<PortalReturn> PORTAL_RETURN = AttachmentRegistry.createPersistent(
			Lanturn.id("portal_return"),
			RecordCodecBuilder.create(instance -> instance.group(
					GlobalPos.CODEC.fieldOf("position").forGetter(PortalReturn::position),
					Codec.FLOAT.fieldOf("yaw").forGetter(PortalReturn::yaw),
					Codec.FLOAT.fieldOf("pitch").forGetter(PortalReturn::pitch)
			).apply(instance, PortalReturn::new)));

	private final ResourceKey<Level> destinationDimension;
	private final Component unavailableMessage;

	public DimensionPortalBlock(BlockBehaviour.Properties properties, ResourceKey<Level> destinationDimension,
			String unavailableMessageKey) {
		super(properties);
		this.destinationDimension = destinationDimension;
		this.unavailableMessage = Component.translatable(unavailableMessageKey);
	}

	@Override
	protected InteractionResult useWithoutItem(net.minecraft.world.level.block.state.BlockState state, Level level,
			BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)
				|| !(level instanceof ServerLevel sourceLevel)) {
			return InteractionResult.SUCCESS;
		}

		if (sourceLevel.dimension().equals(destinationDimension)) {
			return returnPlayer(serverPlayer);
		}
		return enterDimension(serverPlayer, sourceLevel, pos);
	}

	private InteractionResult enterDimension(ServerPlayer player, ServerLevel sourceLevel, BlockPos sourcePos) {
		ServerLevel destination = sourceLevel.getServer().getLevel(destinationDimension);
		if (destination == null) {
			player.sendSystemMessage(unavailableMessage);
			return InteractionResult.FAIL;
		}

		BlockPos arrivalPortal = arrivalPortalPosition(destination, sourcePos);
		if (destination.dimension().equals(Lanturn.VOID_DIMENSION)) {
			createVoidArrivalPlatform(destination, arrivalPortal);
		}
		destination.setBlock(arrivalPortal, defaultBlockState(), 3);
		if (destination.dimension().equals(Lanturn.MINING_DIMENSION)) {
			MiningChunkGenerator.freezeTime(destination);
		}
		BlockPos arrivalPosition = destination.dimension().equals(Lanturn.VOID_DIMENSION)
				? arrivalPortal.offset(1, 1, 0)
				: arrivalPortal.above();
		boolean teleported = player.teleportTo(destination, arrivalPosition.getX() + 0.5,
				arrivalPosition.getY(), arrivalPosition.getZ() + 0.5, Set.of(),
				player.getYRot(), player.getXRot(), false);
		if (teleported) {
			player.setAttached(PORTAL_RETURN, new PortalReturn(
					GlobalPos.of(sourceLevel.dimension(), sourcePos), player.getYRot(), player.getXRot()));
			return InteractionResult.SUCCESS;
		}
		player.sendSystemMessage(Component.translatable("message.lanturn.portal.teleport_failed"));
		return InteractionResult.FAIL;
	}

	private InteractionResult returnPlayer(ServerPlayer player) {
		PortalReturn returnPoint = player.getAttached(PORTAL_RETURN);
		if (returnPoint == null) {
			player.sendSystemMessage(Component.translatable("message.lanturn.portal.no_return"));
			return InteractionResult.FAIL;
		}

		ServerLevel destination = ((ServerLevel) player.level()).getServer()
				.getLevel(returnPoint.position().dimension());
		if (destination == null) {
			player.sendSystemMessage(unavailableMessage);
			return InteractionResult.FAIL;
		}

		BlockPos pos = returnPoint.position().pos();
		boolean teleported = player.teleportTo(destination, pos.getX() + 0.5, pos.getY() + 1.0,
				pos.getZ() + 0.5, Set.of(), returnPoint.yaw(), returnPoint.pitch(), false);
		if (teleported) {
			player.setAttached(PORTAL_RETURN, null);
			return InteractionResult.SUCCESS;
		}
		player.sendSystemMessage(Component.translatable("message.lanturn.portal.teleport_failed"));
		return InteractionResult.FAIL;
	}

	private static BlockPos arrivalPortalPosition(ServerLevel destination, BlockPos sourcePos) {
		if (destination.dimension().equals(Lanturn.MINING_DIMENSION)) {
			return new BlockPos(sourcePos.getX(), 128, sourcePos.getZ());
		}
		if (destination.dimension().equals(Lanturn.VOID_DIMENSION)) {
			return new BlockPos(sourcePos.getX(), 60, sourcePos.getZ());
		}
		return sourcePos;
	}

	private static void createVoidArrivalPlatform(ServerLevel level, BlockPos portalPos) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				level.setBlock(portalPos.offset(x, 0, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
			}
		}
	}

	public record PortalReturn(GlobalPos position, float yaw, float pitch) {
	}
}
