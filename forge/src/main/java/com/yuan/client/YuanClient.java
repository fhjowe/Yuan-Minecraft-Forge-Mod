package com.yuan.client;

import com.yuan.Yuan;
import com.yuan.client.particle.YuanParticleRender;
import com.yuan.client.cosmic.YuanCosmicModelLoader;
import com.yuan.client.particle.YuanParticleTypes;
import com.yuan.client.render.YuanSwordBEWLR;
import com.yuan.client.render.YuanSwordPlayerLayer;
import com.yuan.space_slash.YuanSpaceSlashEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class YuanClient {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("YuanCosmic");

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new YuanSwordPlayerLayer(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        LOGGER.info("[YuanCosmic] registering space slash entity renderer");
        event.registerEntityRenderer(Yuan.SPACE_SLASH_ENTITY.get(), YuanSpaceSlashEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(YuanParticleTypes.GILDED_SPARK.get(), YuanParticleRender.SparkProvider::new);
        event.registerSpriteSet(YuanParticleTypes.GILDED_RING.get(), YuanParticleRender.RingProvider::new);
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        event.register(YuanSwordBEWLR.BASE_MODEL);
    }

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        LOGGER.info("[YuanCosmic] registering cosmic_neo loader");
        event.register("cosmic_neo", YuanCosmicModelLoader.INSTANCE);
    }
}
