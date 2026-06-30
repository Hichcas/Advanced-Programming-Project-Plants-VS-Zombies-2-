package com.PVZ.model.entity.zombies.base;

public class ScaledProperty {
    public enum Formula { STANDARD, CONSTANT }

    private String key;
    private Formula formula;
    private double arg1;
    private double arg2;

    public ScaledProperty(String key, Formula formula, double arg1, double arg2) {
        this.key = key;
        this.formula = formula;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public double computeScale(double level) {
        switch (formula) {
            case STANDARD: return arg1 + arg2 * level;
            case CONSTANT:
            default: return 1.0;
        }
    }

    public String getKey() { return key; }
    public Formula getFormula() { return formula; }
}
