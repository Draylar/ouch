package draylar.ouch.client;

import net.minecraft.util.math.Vec3d;

public class Popup {

    private final String message;
    private int remainingTime;
    private Vec3d prevPosition;
    private final int color;
    private Vec3d position;

    public Popup(String message, int remainingTime, Vec3d position, int color) {
        this.message = message;
        this.remainingTime = remainingTime;
        this.position = position;
        this.prevPosition = position;
        this.color = color;
    }

    public String getMessage() {
        return message;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public Vec3d getPrevPosition() {
        return prevPosition;
    }

    public Vec3d getPosition() {
        return position;
    }

    public void tick() {
        remainingTime--;
        prevPosition = position;
        position = position.add(0, 0.05 * Math.min(1.0f, remainingTime / 10f), 0);
    }

    public boolean shouldRemove() {
        return remainingTime <= 0;
    }

    public float getScale() {
        // 5 -> 1.0
        // 0 -> 0.0
        if(remainingTime <= 5) {
            return remainingTime / 5f;
        }

        return 1.0f;
    }

    public int getColor() {
        return color;
    }
}
