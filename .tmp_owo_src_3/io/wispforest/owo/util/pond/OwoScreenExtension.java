package io.wispforest.owo.util.pond;

import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.layers.Layer;
import java.util.List;
import net.minecraft.class_437;

public interface OwoScreenExtension {
    List<Layer<?, ?>.Instance> owo$getInstancesView();
    <S extends class_437, R extends ParentComponent> Layer<S, R>.Instance owo$getInstance(Layer<S, R> layer);

    void owo$updateLayers();
}
