package ru.jenya.client;

import net.minecraft.util.Identifier;
import ru.jenya.JenyaMod;
import ru.jenya.JenyaEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class JenyaModel extends GeoModel<JenyaEntity> {
    private static final Identifier MODEL = Identifier.of(JenyaMod.MOD_ID, "entity/jenya");
    private static final Identifier TEXTURE = Identifier.of(JenyaMod.MOD_ID, "textures/entity/jenya.png");
    private static final Identifier ANIMATION = Identifier.of(JenyaMod.MOD_ID, "entity/jenya");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(JenyaEntity animatable) {
        return ANIMATION;
    }
}
