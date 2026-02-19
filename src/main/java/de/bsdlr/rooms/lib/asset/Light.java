package de.bsdlr.rooms.lib.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.ColorLight;

import javax.annotation.Nonnull;

public class Light {
    public static final BuilderCodec<Light> CODEC = BuilderCodec.builder(Light.class, Light::new)
            .appendInherited(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                    (light, s) -> light.enabled = s,
                    light -> light.enabled,
                    (light, parent) -> light.enabled = parent.enabled)
            .add()
            .appendInherited(new KeyedCodec<>("MinRed", Codec.BYTE),
                    (light, s) -> light.minRed = s,
                    light -> light.minRed,
                    (light, parent) -> light.minRed = parent.minRed)
            .documentation("The default is Byte.MIN_VALUE: " + Byte.MIN_VALUE)
            .add()
            .appendInherited(new KeyedCodec<>("MaxRed", Codec.BYTE),
                    (light, s) -> light.maxRed = s,
                    light -> light.maxRed,
                    (light, parent) -> light.maxRed = parent.maxRed)
            .documentation("The default is Byte.MAX_VALUE: " + Byte.MAX_VALUE)
            .add()
            .appendInherited(new KeyedCodec<>("MinGreen", Codec.BYTE),
                    (light, s) -> light.minGreen = s,
                    light -> light.minGreen,
                    (light, parent) -> light.minGreen = parent.minGreen)
            .documentation("The default is Byte.MIN_VALUE: " + Byte.MIN_VALUE)
            .add()
            .appendInherited(new KeyedCodec<>("MaxGreen", Codec.BYTE),
                    (light, s) -> light.maxGreen = s,
                    light -> light.maxGreen,
                    (light, parent) -> light.maxGreen = parent.maxGreen)
            .documentation("The default is Byte.MAX_VALUE: " + Byte.MAX_VALUE)
            .add()
            .appendInherited(new KeyedCodec<>("MinBlue", Codec.BYTE),
                    (light, s) -> light.minBlue = s,
                    light -> light.minBlue,
                    (light, parent) -> light.minBlue = parent.minBlue)
            .documentation("The default is Byte.MIN_VALUE: " + Byte.MIN_VALUE)
            .add()
            .appendInherited(new KeyedCodec<>("MaxBlue", Codec.BYTE),
                    (light, s) -> light.maxBlue = s,
                    light -> light.maxBlue,
                    (light, parent) -> light.maxBlue = parent.maxBlue)
            .documentation("The default is Byte.MAX_VALUE: " + Byte.MAX_VALUE)
            .add()
            .appendInherited(new KeyedCodec<>("MinRadius", Codec.BYTE),
                    (light, s) -> light.minRadius = s,
                    light -> light.minRadius,
                    (light, parent) -> light.minRadius = parent.minRadius)
//            .addValidator(Validators.min((byte) 1))
            .documentation("The default is Byte.MIN_VALUE: " + Byte.MIN_VALUE)
            .add()
            .appendInherited(new KeyedCodec<>("MaxRadius", Codec.BYTE),
                    (light, s) -> light.minRadius = s,
                    light -> light.minRadius,
                    (light, parent) -> light.minRadius = parent.minRadius)
            .documentation("The default is Byte.MAX_VALUE: " + Byte.MAX_VALUE)
            .add()
            .validator((light, results) -> {
                if (light.minRadius > light.maxRadius) results.fail("MinRadius has to be greater than MaxRadius.");
            })
            .build();
    protected boolean enabled = false;
    protected byte minRed = Byte.MIN_VALUE;
    protected byte maxRed = Byte.MAX_VALUE;
    protected byte minGreen = Byte.MIN_VALUE;
    protected byte maxGreen = Byte.MAX_VALUE;
    protected byte minBlue = Byte.MIN_VALUE;
    protected byte maxBlue = Byte.MAX_VALUE;
    protected byte minRadius = Byte.MIN_VALUE;
    protected byte maxRadius = Byte.MAX_VALUE;

    public Light() {}

    public Light(boolean enabled) {
        this.enabled = enabled;
    }

    public Light(byte minRed, byte maxRed, byte minGreen, byte maxGreen, byte minBlue, byte maxBlue, byte minRadius, byte maxRadius) {
        this.enabled = true;
        this.minRed = minRed;
        this.maxRed = maxRed;
        this.minGreen = minGreen;
        this.maxGreen = maxGreen;
        this.minBlue = minBlue;
        this.maxBlue = maxBlue;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
    }

    public Light(@Nonnull Light other) {
        this.enabled = other.enabled;
        this.minRed = other.minRed;
        this.maxRed = other.maxRed;
        this.minGreen = other.minGreen;
        this.maxGreen = other.maxGreen;
        this.minBlue = other.minBlue;
        this.maxBlue = other.maxBlue;
        this.minRadius = other.minRadius;
        this.maxRadius = other.maxRadius;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public byte getMinRed() {
        return minRed;
    }

    public byte getMaxRed() {
        return maxRed;
    }

    public byte getMinGreen() {
        return minGreen;
    }

    public byte getMaxGreen() {
        return maxGreen;
    }

    public byte getMinBlue() {
        return minBlue;
    }

    public byte getMaxBlue() {
        return maxBlue;
    }

    public byte getMinRadius() {
        return minRadius;
    }

    public byte getMaxRadius() {
        return maxRadius;
    }

    public boolean matches(ColorLight light) {
        if (!enabled) return true;
        if (light == null) return false;
        if (light.radius < minRadius || light.radius > maxRadius) return false;
        if (light.red < minRed || light.red > maxRed) return false;
        if (light.green < minGreen || light.green > maxGreen) return false;
        return !(light.blue < minBlue || light.blue > maxBlue);
    }

    @Override
    public String toString() {
        if (!enabled) return "Light{enabled=false}";
        return "Light{red=" +
                minRed +
                ".." +
                maxRed +
                ",green" +
                minGreen +
                ".." +
                maxGreen +
                ",blue" +
                minBlue +
                ".." +
                maxBlue +
                ",radius" +
                minRadius +
                ".." +
                maxRadius +
                "}";
    }
}
