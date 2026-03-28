package io.wispforest.owo.ui.component;

import Z;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ui.access.EditBoxAccessor;
import io.wispforest.owo.mixin.ui.access.EditBoxWidgetAccessor;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.Observable;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_5250;
import net.minecraft.class_7529;
import net.minecraft.class_7530;
import net.minecraft.class_7533;

public class TextAreaComponent extends class_7529 {

    protected final Observable<String> textValue = Observable.of("");
    protected final EventStream<OnChanged> changedEvents = OnChanged.newStream();
    protected final class_7530 editBox;

    protected final Observable<Boolean> displayCharCount = Observable.of(false);
    protected final Observable<Integer> maxLines = Observable.of(-1);

    protected TextAreaComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        super(class_310.method_1551().field_1772, 0, 0, 0, 0, class_2561.method_43473(), class_2561.method_43473());
        this.editBox = ((EditBoxWidgetAccessor) this).owo$getEditBox();
        this.sizing(horizontalSizing, verticalSizing);

        this.textValue.observe(this.changedEvents.sink()::onChanged);
        Observable.observeAll(this.widgetWrapper()::notifyParentIfMounted, this.displayCharCount, this.maxLines);

        super.method_44401(s -> {
            this.textValue.set(s);

            if (this.maxLines.get() < 0) return;
            this.widgetWrapper().notifyParentIfMounted();
        });
    }

    @Override
    @Deprecated(forRemoval = true)
    public void method_44401(Consumer<String> changeListener) {
        Owo.debugWarn(Owo.LOGGER, "setChangeListener stub on TextAreaComponent invoked");
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        this.cursorStyle(this.method_44392() && mouseX >= this.method_46426() + this.field_22758 - 9 ? CursorStyle.NONE : CursorStyle.TEXT);
    }

    @Override
    protected void method_44384(class_332 context) {
        this.field_22759 -= 1;

        var matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_46416(-9, 1, 0);

        int previousMaxLength = this.editBox.method_44409();
        this.editBox.method_44411(Integer.MAX_VALUE);

        super.method_44384(context);

        this.editBox.method_44411(previousMaxLength);

        matrices.method_22909();
        this.field_22759 += 1;

        if (this.displayCharCount.get()) {
            var text = this.editBox.method_44418()
                    ? class_2561.method_43469("gui.multiLineEditBox.character_limit", this.editBox.method_44421().length(), this.editBox.method_44409())
                    : class_2561.method_43470(String.valueOf(this.editBox.method_44421().length()));

            var textRenderer = class_310.method_1551().field_1772;
            context.method_27535(textRenderer, text, this.method_46426() + this.field_22758 - textRenderer.method_27525(text), this.method_46427() + this.field_22759 + 3, 0xa0a0a0);
        }
    }

    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        this.field_22758 -= 9;
        var result = super.method_25402(mouseX, mouseY, button);
        this.field_22758 += 9;

        return result;
    }

    @Override
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        boolean result = super.method_25404(keyCode, scanCode, modifiers);

        if (keyCode == GLFW.GLFW_KEY_TAB) {
            this.editBox.method_44420("    ");
            return true;
        } else {
            return result;
        }
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);

        int cursor = this.editBox.method_44424();
        int selection = ((EditBoxAccessor) this.editBox).owo$getSelectionEnd();

        ((EditBoxAccessor) this.editBox).owo$setWidth(this.width() - this.method_44385() - 9);
        this.editBox.method_44414(this.method_44405());

        super.inflate(space);
        this.editBox.method_44414(this.method_44405());

        this.editBox.method_44412(class_7533.field_39535, cursor);
        ((EditBoxAccessor) this.editBox).owo$setSelectionEnd(selection);
    }

    public EventSource<OnChanged> onChanged() {
        return changedEvents.source();
    }

    public TextAreaComponent maxLines(int maxLines) {
        this.maxLines.set(maxLines);
        return this;
    }

    public int maxLines() {
        return this.maxLines.get();
    }

    public TextAreaComponent displayCharCount(boolean displayCharCount) {
        this.displayCharCount.set(displayCharCount);
        return this;
    }

    public boolean displayCharCount() {
        return this.displayCharCount.get();
    }

    public TextAreaComponent text(String text) {
        this.method_44400(text);
        return this;
    }

    @Override
    public int heightOffset() {
        return this.displayCharCount.get() ? -12 : 0;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "display-char-count", UIParsing::parseBool, this::displayCharCount);
        UIParsing.apply(children, "max-length", UIParsing::parseUnsignedInt, this::method_44402);
        UIParsing.apply(children, "max-lines", UIParsing::parseUnsignedInt, this::maxLines);
        UIParsing.apply(children, "text", $ -> $.getTextContent().strip(), this::text);
    }

    public interface OnChanged {
        void onChanged(String value);

        static EventStream<OnChanged> newStream() {
            return new EventStream<>(subscribers -> value -> {
                for (var subscriber : subscribers) {
                    subscriber.onChanged(value);
                }
            });
        }
    }
}
