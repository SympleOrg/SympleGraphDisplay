package top.symple.symplegraphdisplay.util;

import androidx.annotation.NonNull;

import top.symple.symplegraphdisplay.managers.data.Color;

public class ColorRGBA {
    private final int red;
    private final int green;
    private final int blue;
    private final float alpha;

    public ColorRGBA(int red, int green, int blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public ColorRGBA(int red, int blue, int green) {
        this(red, blue, green, 1f);
    }

    public static ColorRGBA fromColor(Color color) {
        if(color == null) return null;
        return new ColorRGBA(color.red(), color.green(), color.blue(), color.alpha());
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("rgba(%s, %s, %s, %s)", this.red, this.green, this.blue, this.alpha);
    }
}
