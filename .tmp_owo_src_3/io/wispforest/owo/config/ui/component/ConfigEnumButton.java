package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import net.minecraft.class_1074;
import net.minecraft.class_2561;
import net.minecraft.class_437;

@ApiStatus.Internal
public class ConfigEnumButton extends ButtonComponent implements OptionValueProvider {

    @Nullable protected Option<? extends Enum<?>> backingOption = null;
    @Nullable protected Enum<?>[] backingValues = null;
    protected int selectedIndex = 0;

    protected boolean wasRightClicked = false;

    public ConfigEnumButton() {
        super(class_2561.method_43473(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.updateMessage();
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        this.wasRightClicked = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        return super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public void method_25306() {
        if (this.wasRightClicked || class_437.method_25442()) {
            this.selectedIndex--;
            if (this.selectedIndex < 0) this.selectedIndex += this.backingValues.length;
        } else {
            this.selectedIndex++;
            if (this.selectedIndex > this.backingValues.length - 1) this.selectedIndex -= this.backingValues.length;
        }

        this.updateMessage();

        super.method_25306();
    }

    @Override
    protected boolean method_25351(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || super.method_25351(button);
    }

    protected void updateMessage() {
        if (this.backingOption == null) return;

        var enumName = StringUtils.uncapitalize(this.backingValues.getClass().componentType().getSimpleName());
        var valueName = this.backingValues[this.selectedIndex].name().toLowerCase(Locale.ROOT);

        var optionValueKey = this.backingOption.translationKey() + ".value." + valueName;

        this.method_25355(class_1074.method_4663(optionValueKey)
                ? class_2561.method_43471(optionValueKey)
                : class_2561.method_43471("text.config." + this.backingOption.configName() + ".enum." + enumName + "." + valueName)
        );
    }

    public ConfigEnumButton init(Option<? extends Enum<?>> option, int selectedIndex) {
        this.backingOption = option;
        this.backingValues = (Enum<?>[]) option.backingField().field().getType().getEnumConstants();
        this.selectedIndex = selectedIndex;

        this.updateMessage();

        return this;
    }

    public ConfigEnumButton select(int index) {
        this.selectedIndex = index;
        this.updateMessage();

        return this;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Object parsedValue() {
        return this.backingValues[this.selectedIndex];
    }
}
