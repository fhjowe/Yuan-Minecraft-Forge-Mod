package com.yuan.client.cosmic;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class YuanCosmicModelLoader implements IGeometryLoader<YuanCosmicModelLoader.CosmicGeometry> {
    public static final YuanCosmicModelLoader INSTANCE = new YuanCosmicModelLoader();

    @Override
    public CosmicGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject cosmicObj = modelContents.getAsJsonObject("cosmic_neo");
        if (cosmicObj == null) {
            throw new JsonParseException("Missing cosmic_neo block");
        }
        List<String> maskTextures = new ArrayList<>();
        if (cosmicObj.has("mask") && cosmicObj.get("mask").isJsonArray()) {
            JsonArray masks = cosmicObj.getAsJsonArray("mask");
            for (int i = 0; i < masks.size(); i++) {
                maskTextures.add(masks.get(i).getAsString());
            }
        } else {
            maskTextures.add(GsonHelper.getAsString(cosmicObj, "mask"));
        }
        JsonObject clean = modelContents.deepCopy();
        clean.remove("cosmic_neo");
        clean.remove("loader");
        BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
        return new CosmicGeometry(baseModel, maskTextures);
    }

    public static final class CosmicGeometry implements IUnbakedGeometry<CosmicGeometry> {
        private final BlockModel baseModel;
        private final List<String> maskTextures;

        public CosmicGeometry(BlockModel baseModel, List<String> maskTextures) {
            this.baseModel = baseModel;
            this.maskTextures = maskTextures;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel baseBaked = baseModel.bake(baker, baseModel, spriteGetter, modelState, modelLocation, true);
            List<ResourceLocation> masks = new ArrayList<>();
            for (String mask : maskTextures) {
                masks.add(ResourceLocation.parse(mask));
            }
            return new YuanCosmicBakedModel(baseBaked, masks);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            baseModel.resolveParents(modelGetter);
        }
    }
}
