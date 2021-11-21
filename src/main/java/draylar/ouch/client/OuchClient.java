package draylar.ouch.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Environment(EnvType.CLIENT)
public class OuchClient implements ClientModInitializer {

    public static final List<String> HIT_MESSAGES = Arrays.asList("Ouch!", "Dramatic!", "Bang!", "Crash!", "Slam!", "Whack!");
    private static final Random RANDOM = new Random();
    public static final List<Popup> POPUPS = new ArrayList<>();

    public static void spawnPopup(String message, int age, Vec3d origin, double xVariance, double yVariance, double zVariance, int color) {
        POPUPS.add(new Popup(message, age, origin.add((RANDOM.nextDouble() - 0.5) * 2 * xVariance, RANDOM.nextDouble() * yVariance, (RANDOM.nextDouble() - 0.5) * 2 * zVariance), color));
    }

    @Override
    public void onInitializeClient() {
        // Handle popup request packet
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("popup_update"), (client, handler, buf, responseSender) -> {
            int id = buf.readInt();
            double damage = buf.readDouble();

            client.execute(() -> {
                @Nullable Entity found = client.world.getEntityById(id);
                if(found != null) {
                    String stringMessage = String.format("%,.1f", damage);
                    stringMessage = stringMessage.endsWith(".0") ? stringMessage.replace(".0", "") : stringMessage;
                    OuchClient.spawnPopup(stringMessage, 25, found.getPos().add(0, found.getHeight() * 1.25, 0), found.getWidth(), 0.5, found.getWidth(), 0xffffff);

                    // 10% chance for a bonus message.
                    Random random = found.world.random;
                    if(random.nextDouble() <= 0.10) {
                        String bonusMessage;
                        bonusMessage = OuchClient.HIT_MESSAGES.get(random.nextInt(OuchClient.HIT_MESSAGES.size()));
                        OuchClient.spawnPopup(bonusMessage, 25, found.getPos().add(0, found.getHeight() * 1.25, 0), found.getWidth(), 0.5, found.getWidth(), 0xe33119);
                    };
                }
            });
        });

        // Tick Popups
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            POPUPS.forEach(Popup::tick);
            POPUPS.removeIf(Popup::shouldRemove);
        });

        // Render Popups
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MatrixStack matrices = context.matrixStack();
            ClientPlayerEntity entity = MinecraftClient.getInstance().player;
            Vec3d vec3d = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity).getPositionOffset(entity, context.tickDelta());

            for (Popup popup : POPUPS) {
                // Determine lerped position of popup
                double popupX = MathHelper.lerp(context.tickDelta(), popup.getPrevPosition().getX(), popup.getPosition().getX());
                double popupY = MathHelper.lerp(context.tickDelta(), popup.getPrevPosition().getY(), popup.getPosition().getY());
                double popupZ = MathHelper.lerp(context.tickDelta(), popup.getPrevPosition().getZ(), popup.getPosition().getZ());
                popupX = popupX - context.camera().getPos().getX() + vec3d.getX();
                popupY = popupY - context.camera().getPos().getY() + vec3d.getY();
                popupZ = popupZ - context.camera().getPos().getZ() + vec3d.getZ();

                // Initial positioning
                matrices.push();
                matrices.translate(popupX, popupY, popupZ);
                matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
                matrices.scale(-0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = matrices.peek().getModel();

                // If the particle is about to dye, scale out.
                matrices.scale(popup.getScale(), popup.getScale(), popup.getScale());

                MinecraftClient.getInstance().textRenderer.draw(
                        new LiteralText(popup.getMessage()).setStyle(Style.EMPTY.withBold(true)),
                        0,
                        0,
                        popup.getColor(),
                        false,
                        matrix4f,
                        context.consumers(),
                        false,
                        0,
                        LightmapTextureManager.pack(15, 15));

                matrices.pop();
            }
        });
    }
}
