package io.wispforest.owo.ui.base;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.parsing.ConfigureHotReloadScreen;
import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class BaseUIModelHandledScreen<R extends ParentComponent, S extends class_1703> extends BaseOwoHandledScreen<R, S> {

    /**
     * The UI model this screen is built upon, parsed from XML.
     * This is usually not relevant to subclasses, the UI adapter
     * inherited from {@link BaseOwoScreen} is more interesting
     */
    protected final UIModel model;
    protected final Class<R> rootComponentClass;

    protected final @Nullable class_2960 modelId;

    protected BaseUIModelHandledScreen(S handler, class_1661 inventory, class_2561 title, Class<R> rootComponentClass, BaseUIModelScreen.DataSource source) {
        super(handler, inventory, title);
        var providedModel = source.get();
        if (providedModel == null) {
            source.reportError();
            this.invalid = true;
        }

        this.rootComponentClass = rootComponentClass;
        this.model = providedModel;

        this.modelId = source instanceof BaseUIModelScreen.DataSource.AssetDataSource assetSource
                ? assetSource.assetPath()
                : null;
    }

    protected BaseUIModelHandledScreen(S handler, class_1661 inventory, class_2561 title, Class<R> rootComponentClass, class_2960 modelId) {
        this(handler, inventory, title, rootComponentClass, BaseUIModelScreen.DataSource.asset(modelId));
    }

    @Override
    protected @NotNull OwoUIAdapter<R> createAdapter() {
        return this.model.createAdapter(rootComponentClass, this);
    }

    @Override
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (Owo.DEBUG && this.modelId != null && keyCode == GLFW.GLFW_KEY_F5 && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            this.field_22787.method_1507(new ConfigureHotReloadScreen(this.modelId, this));
            return true;
        }

        return super.method_25404(keyCode, scanCode, modifiers);
    }
}
