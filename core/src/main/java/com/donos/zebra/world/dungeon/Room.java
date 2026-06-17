package com.donos.zebra.world.dungeon;

public final class Room {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCenterX() {
        return x + width / 2;
    }

    public int getCenterY() {
        return y + height / 2;
    }

    public boolean overlaps(Room other, int padding) {
        return x - padding < other.x + other.width + padding
            && x + width + padding > other.x - padding
            && y - padding < other.y + other.height + padding
            && y + height + padding > other.y - padding;
    }
}
