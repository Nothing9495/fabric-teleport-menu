package io.wispforest.owo.shader;

import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.minecraft.class_1041;
import net.minecraft.class_276;
import net.minecraft.class_284;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_6367;
import org.lwjgl.opengl.GL30;

/**
 * A simple GlProgram implementation that applies an approximate gaussian
 * blur to the quads it draws. Look at {@link Surface#blur(float, float)} for a
 * reference implementation
 */
public class BlurProgram extends GlProgram {

    private class_284 inputResolution;
    private class_284 directions;
    private class_284 quality;
    private class_284 size;
    private class_276 input;

    public BlurProgram() {
        super(class_2960.method_60655("owo", "blur"), class_290.field_1592);

        WindowResizeCallback.EVENT.register((client, window) -> {
            if (this.input == null) return;
            this.input.method_1234(window.method_4489(), window.method_4506(), class_310.field_1703);
        });
    }

    public void setParameters(int directions, float quality, float size) {
        this.directions.method_1251((float) directions);
        this.size.method_1251(size);
        this.quality.method_1251(quality);
    }

    @Override
    public void use() {
        var buffer = class_310.method_1551().method_1522();

        this.input.method_1235(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.field_1476);
        GL30.glBlitFramebuffer(0, 0, buffer.field_1482, buffer.field_1481, 0, 0, buffer.field_1482, buffer.field_1481, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.method_1235(false);

        this.inputResolution.method_1255((float) buffer.field_1482, (float) buffer.field_1481);
        this.backingProgram.method_34583("InputSampler", this.input.method_30277());

        super.use();
    }

    @Override
    protected void setup() {
        this.inputResolution = this.findUniform("InputResolution");
        this.directions = this.findUniform("Directions");
        this.quality = this.findUniform("Quality");
        this.size = this.findUniform("Size");

        var window = class_310.method_1551().method_22683();
        this.input = new class_6367(window.method_4489(), window.method_4506(), false, class_310.field_1703);
    }
}