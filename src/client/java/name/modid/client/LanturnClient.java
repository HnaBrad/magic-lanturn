package name.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import name.modid.LanturnContent;
import name.modid.LightPointEntity;

public class LanturnClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(LanturnContent.LIGHT_POINT_ENTITY, InvisibleLightPointRenderer::new);
	}

	private static final class InvisibleLightPointRenderer extends EntityRenderer<LightPointEntity, EntityRenderState> {
		private InvisibleLightPointRenderer(EntityRendererProvider.Context context) {
			super(context);
		}

		@Override
		public EntityRenderState createRenderState() {
			return new EntityRenderState();
		}

		@Override
		public boolean shouldRender(LightPointEntity entity, Frustum frustum,
				double x, double y, double z, float partialTick) {
			return false;
		}
	}
}