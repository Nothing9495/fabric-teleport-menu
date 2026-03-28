package io.wispforest.owo.text;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2588;
import net.minecraft.class_5348;
import net.minecraft.class_7417;

public record InsertingTextContent(int index) implements class_7417 {

    public static final class_7417.class_8823<InsertingTextContent> TYPE = new class_8823<>(
            CodecUtils.toMapCodec(StructEndecBuilder.of(Endec.INT.fieldOf("index", InsertingTextContent::index), InsertingTextContent::new)),
            "owo:insert"
    );

    @Override
    public <T> Optional<T> method_27659(class_5348.class_5245<T> visitor) {
        var current = TranslationContext.getCurrent();

        if (current == null || current.method_11023().length <= index) {return visitor.accept("%" + (index + 1) + "$s");}

        Object arg = current.method_11023()[index];

        if (arg instanceof class_2561 text) {
            return text.method_27657(visitor);
        } else {
            return visitor.accept(arg.toString());
        }
    }

    @Override
    public <T> Optional<T> method_27660(class_5348.class_5246<T> visitor, class_2583 style) {
        var current = TranslationContext.getCurrent();

        if (current == null || current.method_11023().length <= index) {
            return visitor.accept(style, "%" + (index + 1) + "$s");
        }

        Object arg = current.method_11023()[index];

        if (arg instanceof class_2561 text) {
            return text.method_27658(visitor, style);
        } else {
            return visitor.accept(style, arg.toString());
        }
    }

    @Override
    public class_8823<?> method_54163() {
        return TYPE;
    }
}
