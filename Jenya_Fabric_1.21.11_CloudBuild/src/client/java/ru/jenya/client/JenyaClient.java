package ru.jenya.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import ru.jenya.JenyaMod;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class JenyaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
            JenyaMod.JENYA,
            context -> new GeoEntityRenderer<>(context, JenyaMod.JENYA)
        );
    }
}
