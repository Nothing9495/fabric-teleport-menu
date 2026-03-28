package io.wispforest.owo.mixin.registry;

import com.mojang.serialization.Lifecycle;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.pond.OwoSimpleRegistryExtensions;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.class_2370;
import net.minecraft.class_2385;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9248;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(class_2370.class)
public abstract class SimpleRegistryMixin<T> implements class_2385<T>, OwoSimpleRegistryExtensions<T> {

    @Shadow private Map<T, class_6880.class_6883<T>> intrusiveValueToEntry;
    @Shadow @Final private Map<class_5321<T>, class_6880.class_6883<T>> keyToEntry;
    @Shadow @Final private Map<class_2960, class_6880.class_6883<T>> idToEntry;
    @Shadow @Final private Map<T, class_6880.class_6883<T>> valueToEntry;
    @Shadow @Final private ObjectList<class_6880.class_6883<T>> rawIdToEntry;
    @Shadow @Final private Reference2IntMap<T> entryToRawId;
    @Shadow @Final private Map<class_5321<T>, class_9248> keyToEntryInfo;
    @Shadow private Lifecycle lifecycle;

    //--

    /**
     * Copy of the {@link class_2370#method_10272} function but uses {@link List#set} instead of {@link List#add} for {@link class_2370#rawIdToEntry}
     */
    public class_6880.class_6883<T> owo$set(int id, class_5321<T> arg, T object, class_9248 arg2) {
        this.valueToEntry.remove(object);

        OwoFreezer.checkRegister("Registry Set Calls"); //this.assertNotFrozen(arg);

        Objects.requireNonNull(arg);
        Objects.requireNonNull(object);

        class_6880.class_6883<T> reference;

        if (this.intrusiveValueToEntry != null) {
            reference = this.intrusiveValueToEntry.remove(object);

            if (reference == null) {
                throw new AssertionError("Missing intrusive holder for " + arg + ":" + object);
            }

            ((ReferenceAccessor<T>) reference).owo$setRegistryKey(arg);
        } else {
            reference = this.keyToEntry.computeIfAbsent(arg, k -> class_6880.class_6883.method_40234(this.method_46770(), k));
            ((ReferenceAccessor<T>) reference).owo$setValue((T)object);
        }

        this.keyToEntry.put(arg, reference);
        this.idToEntry.put(arg.method_29177(), reference);
        this.valueToEntry.put(object, reference);
        this.rawIdToEntry.set(id, reference);
        this.entryToRawId.put(object, id);
        this.keyToEntryInfo.put(arg, arg2);
        this.lifecycle = this.lifecycle.add(arg2.comp_2355());

        // TODO: SHOULD WE BE REFIREING THE EVENT?
        RegistryEntryAddedCallback.event(this).invoker().onEntryAdded(id, arg.method_29177(), (T)object);

        return reference;
    }
}
