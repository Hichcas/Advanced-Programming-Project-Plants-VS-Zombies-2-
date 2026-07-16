package com.PVZ.model.minigame.wallnutbowling;


public class BowlingNut {

    private final NutType type;
    private double x;
    private double y;
    private double vx;
    private double vy;
    private int hits;
    private boolean alive = true;

    public BowlingNut(NutType type, double x, double y, double vx, double vy) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public void move(float delta) {
        x += vx * delta;
        y += vy * delta;
    }

    public void rotate(double degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double nvx = vx * cos - vy * sin;
        double nvy = vx * sin + vy * cos;
        this.vx = nvx;
        this.vy = nvy;
    }

    public void flipVertical() {
        vy = -vy;
    }

    public NutType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setY(double y) { this.y = y; }

    public int getHits() { return hits; }
    public void registerHit() { hits++; }

    public boolean isAlive() { return alive; }
    public void kill() { alive = false; }
}
