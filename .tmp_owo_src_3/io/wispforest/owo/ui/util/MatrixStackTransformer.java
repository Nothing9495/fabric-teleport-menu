package io.wispforest.owo.ui.util;

import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Helper interface implemented on top of the {@link class_332} to allow for easier matrix stack transformations
 */
public interface MatrixStackTransformer {

    default MatrixStackTransformer translate(double x, double y, double z) {
        this.getMatrixStack().method_22904(x, y, z);
        return this;
    }

    default MatrixStackTransformer translate(float x, float y, float z) {
        this.getMatrixStack().method_46416(x, y, z);
        return this;
    }

    default MatrixStackTransformer scale(float x, float y, float z) {
        this.getMatrixStack().method_22905(x, y, z);
        return this;
    }

    default MatrixStackTransformer multiply(Quaternionf quaternion) {
        this.getMatrixStack().method_22907(quaternion);
        return this;
    }

    default MatrixStackTransformer multiply(Quaternionf quaternion, float originX, float originY, float originZ) {
        this.getMatrixStack().method_49278(quaternion, originX, originY, originZ);
        return this;
    }

    default MatrixStackTransformer push() {
        this.getMatrixStack().method_22903();
        return this;
    }

    default MatrixStackTransformer pop() {
        this.getMatrixStack().method_22909();
        return this;
    }

    default MatrixStackTransformer multiplyPositionMatrix(Matrix4f matrix) {
        this.getMatrixStack().method_34425(matrix);
        return this;
    }

    default class_4587 getMatrixStack(){
        throw new IllegalStateException("getMatrices() method hasn't been override leading to exception!");
    }
}
