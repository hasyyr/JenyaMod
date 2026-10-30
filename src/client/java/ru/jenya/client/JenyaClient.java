package ru.jenya.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import ru.jenya.JenyaEntity;
import ru.jenya.JenyaMod;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class JenyaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
            JenyaMod.JENYA,
            context -> new GeoEntityRenderer<>(context, new JenyaModel())
        );

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && hand == net.minecraft.util.Hand.MAIN_HAND && entity instanceof JenyaEntity) {
                MinecraftClient.getInstance().setScreen(new JenyaCommandScreen());
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
}
