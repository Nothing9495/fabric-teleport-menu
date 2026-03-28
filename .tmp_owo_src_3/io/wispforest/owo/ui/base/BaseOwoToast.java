package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_9779;

@ApiStatus.Experimental
public abstract class BaseOwoToast<R extends ParentComponent> implements class_368 {

    protected final R rootComponent;
    protected final VisibilityPredicate<R> visibilityPredicate;

    protected int virtualWidth = 1000, virtualHeight = 1000;

    protected BaseOwoToast(Supplier<R> components, VisibilityPredicate<R> predicate) {
        this.rootComponent = components.get();
        this.visibilityPredicate = predicate;

        this.rootComponent.inflate(Size.of(this.virtualWidth, this.virtualHeight));
        this.rootComponent.mount(null, 0, 0);
    }

    protected BaseOwoToast(Supplier<R> rootComponent, Duration timeout) {
        this(rootComponent, VisibilityPredicate.timeout(timeout));
    }

    @Override
    public class_369 method_1986(class_332 context, class_374 manager, long startTime) {
        var client = class_310.method_1551();

        var tickCounter = class_310.method_1551().method_60646();

        this.rootComponent.draw(OwoUIDrawContext.of(context), -1000, -1000, tickCounter.method_60637(false), tickCounter.method_60636());

        return this.visibilityPredicate.test(this, startTime);
    }

    @Override
    public int method_29050() {
        return this.rootComponent.fullSize().height();
    }

    @Override
    public int method_29049() {
        return this.rootComponent.fullSize().width();
    }

    @FunctionalInterface
    public interface VisibilityPredicate<R extends ParentComponent> {
        class_369 test(BaseOwoToast<R> toast, long startTime);

        static <R extends ParentComponent> VisibilityPredicate<R> timeout(Duration timeout) {
            return (toast, startTime) -> System.currentTimeMillis() - startTime <= timeout.get(ChronoUnit.MILLIS) ? class_369.field_2209 : class_369.field_2210;
        }
    }
}
