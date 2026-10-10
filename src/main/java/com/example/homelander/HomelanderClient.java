package com.example.homelander;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class HomelanderClient implements ClientModInitializer {
    private static int tickCounter;
    private static boolean laserWasHeld;
    private static boolean speedWasHeld;

    @Override
    public void onInitializeClient() {
        final KeyBinding laser = bind("laser", GLFW.GLFW_KEY_H);
        final KeyBinding flight = bind("flight", GLFW.GLFW_KEY_V);
        final KeyBinding wave = bind("shockwave", GLFW.GLFW_KEY_G);
        final KeyBinding speed = bind("speed", GLFW.GLFW_KEY_R);
        final KeyBinding punch = bind("punch", GLFW.GLFW_KEY_B);
        final KeyBinding land = bind("landing", GLFW.GLFW_KEY_N);
        final KeyBinding sonic = bind("sonic", GLFW.GLFW_KEY_F);
        final KeyBinding pull = bind("pull", GLFW.GLFW_KEY_C);
        final KeyBinding vision = bind("vision", GLFW.GLFW_KEY_X);
        final KeyBinding shield = bind("shield", GLFW.GLFW_KEY_Z);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer playerRenderer) {
                helper.register(new HomelanderCapeFeature(playerRenderer));
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getNetworkHandler() == null) {
                laserWasHeld = false;
                speedWasHeld = false;
                return;
            }
            tickCounter++;
            // isPressed() means the key is physically held; wasPressed() fires only once.
            boolean laserHeld = client.currentScreen == null && laser.isPressed();
            boolean speedHeld = client.currentScreen == null && speed.isPressed();
            if (laserHeld && (!laserWasHeld || tickCounter % 3 == 0)) send((byte) 0);
            if (!laserHeld && laserWasHeld) send((byte) 6);
            if (speedHeld && (!speedWasHeld || tickCounter % 3 == 0)) send((byte) 3);
            if (!speedHeld && speedWasHeld) send((byte) 7);
            laserWasHeld = laserHeld;
            speedWasHeld = speedHeld;
            while (flight.wasPressed()) send((byte) 1);
            while (wave.wasPressed()) send((byte) 2);
            while (punch.wasPressed()) send((byte) 4);
            while (land.wasPressed()) send((byte) 5);
            while (sonic.wasPressed()) send((byte) 8);
            while (pull.wasPressed()) send((byte) 9);
            while (vision.wasPressed()) send((byte) 10);
            while (shield.wasPressed()) send((byte) 11);
        });
    }

    private static KeyBinding bind(String action, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.homelander." + action, InputUtil.Type.KEYSYM, key,
                "category.homelander.powers"));
    }

    private static void send(byte action) {
        ClientPlayNetworking.send(new AbilityPayload(action));
    }
}
