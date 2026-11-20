package com.xulai.animalflee.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.logic.FishingMinigameResult;
import com.xulai.animalflee.network.FishingMinigameResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class FishingMinigameScreen extends Screen {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AnimalFleeMod.MODID, "textures/gui/minigame.png");
    private static final int TEXTURE_SIZE = 512;
    private static final float FULL_CIRCLE = 360.0F;
    private static final float HALF_CIRCLE = 180.0F;
    private static final float POINTER_DEGREES_PER_TICK = 8.0F;
    private static final float POINTER_RADIUS = 73.0F;
    private static final float MISS_PENALTY_DEGREES = 36.0F;

    private final float catchWindow;
    private final float critWindow;
    private final float maxPointerSpeed;
    private final float drainPerTick;

    private boolean finished;
    private long tickCounter;
    private float previousFrame;
    private float pointerDegrees;
    private float pointerSpeed;
    private float remainingDegrees = FULL_CIRCLE;

    public FishingMinigameScreen(Component title, float catchWindow, float critShare, float speedMultiplier, int seconds) {
        super(title);
        this.catchWindow = catchWindow;
        this.critWindow = catchWindow * critShare;
        this.maxPointerSpeed = POINTER_DEGREES_PER_TICK * speedMultiplier;
        this.pointerSpeed = this.maxPointerSpeed;
        this.drainPerTick = FULL_CIRCLE / (seconds * 20.0F);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float ticksSinceLastFrame = tickCounter + partialTick - previousFrame;
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        drawRing(guiGraphics, centerX, centerY, 4, 90.0F, remainingDegrees, 0, 0, 167);
        drawRing(guiGraphics, centerX, centerY, 4, 0.0F, FULL_CIRCLE, 172, 0, 151);
        drawRing(guiGraphics, centerX - 1, centerY - 1, 6, normalizeDegrees(90.0F - HALF_CIRCLE * catchWindow), FULL_CIRCLE * catchWindow, 356, 0, 151);
        drawRing(guiGraphics, centerX - 1, centerY - 1, 6, normalizeDegrees(270.0F - HALF_CIRCLE * catchWindow), FULL_CIRCLE * catchWindow, 356, 0, 151);
        drawRing(guiGraphics, centerX - 1, centerY - 1, 6, normalizeDegrees(90.0F - HALF_CIRCLE * critWindow), FULL_CIRCLE * critWindow, 356, 156, 151);
        drawRing(guiGraphics, centerX - 1, centerY - 1, 6, normalizeDegrees(270.0F - HALF_CIRCLE * critWindow), FULL_CIRCLE * critWindow, 356, 156, 151);
        drawPointer(guiGraphics, centerX + 2, centerY + 2, POINTER_RADIUS);
        pointerDegrees += pointerSpeed * ticksSinceLastFrame;
        previousFrame = tickCounter + partialTick;
        remainingDegrees -= ticksSinceLastFrame * drainPerTick;
        if (remainingDegrees < 0.0F) {
            endGame(FishingMinigameResult.FAIL);
            return;
        }
        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public void tick() {
        if (tickCounter++ % 20 == 0) {
            Minecraft minecraft = this.minecraft;
            boolean speedUp = minecraft != null && minecraft.player != null
                    ? minecraft.player.getRandom().nextBoolean()
                    : Math.random() > 0.5;
            if (speedUp) {
                pointerSpeed = maxPointerSpeed * 2.0F;
            }
        }
        if (pointerSpeed > maxPointerSpeed) {
            pointerSpeed -= maxPointerSpeed / 20.0F;
        } else {
            pointerSpeed = maxPointerSpeed;
        }
        super.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (finished) {
            return true;
        }
        float degrees = normalizeDegrees(pointerDegrees);
        if (isInWindow(degrees, critWindow, 270.0F) || isInWindow(degrees, critWindow, 90.0F)) {
            endGame(FishingMinigameResult.CRIT);
            return true;
        }
        if (isInWindow(degrees, catchWindow, 270.0F) || isInWindow(degrees, catchWindow, 90.0F)) {
            endGame(FishingMinigameResult.SUCCESS);
            return true;
        }
        remainingDegrees -= MISS_PENALTY_DEGREES;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        endGame(FishingMinigameResult.FAIL);
    }

    private void endGame(FishingMinigameResult result) {
        if (finished) {
            return;
        }
        finished = true;
        PacketDistributor.sendToServer(new FishingMinigameResultPacket(result));
        super.onClose();
    }

    private void drawPointer(GuiGraphics guiGraphics, int centerX, int centerY, float radius) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        double x = radius * Math.cos(-(pointerDegrees / (double) HALF_CIRCLE) * Math.PI);
        double y = radius * Math.sin(-(pointerDegrees / (double) HALF_CIRCLE) * Math.PI);
        poseStack.translate(centerX + x, centerY + y, 0.0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F - pointerDegrees));
        guiGraphics.blit(TEXTURE, -2, -5, 326, 6, 11, 6, TEXTURE_SIZE, TEXTURE_SIZE);
        poseStack.popPose();
    }

    private void drawRing(GuiGraphics guiGraphics, int centerX, int centerY, int stroke, float startDegrees, float spanDegrees, int uOffset, int vOffset, int diameter) {
        int radius = diameter / 2;
        int textureCenterX = uOffset + radius;
        int textureCenterY = vOffset + radius;
        for (float degree = startDegrees; degree < startDegrees + spanDegrees; degree++) {
            int x = (int) Math.round(radius * Math.cos(-(degree / (double) HALF_CIRCLE) * Math.PI));
            int y = (int) Math.round(radius * Math.sin(-(degree / (double) HALF_CIRCLE) * Math.PI));
            guiGraphics.blit(TEXTURE, x + centerX, y + centerY, x + textureCenterX, y + textureCenterY, stroke, stroke, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    private float normalizeDegrees(float degrees) {
        float normalized = degrees % FULL_CIRCLE;
        return normalized >= 0.0F ? normalized : normalized + FULL_CIRCLE;
    }

    private boolean isInWindow(float degrees, float halfWindow, float offset) {
        return isBetween(degrees,
                normalizeDegrees(offset - HALF_CIRCLE * halfWindow),
                normalizeDegrees(offset + HALF_CIRCLE * halfWindow));
    }

    private boolean isBetween(float value, float lower, float upper) {
        return lower <= upper
                ? value >= lower && value <= upper
                : !(value <= lower && value >= upper);
    }
}
