package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Optional;
import com.google.common.collect.MapMaker;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.DataManipulatorRegistry;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;

import java.util.Map;

@SuppressWarnings("unchecked")
public class SpongeDataRegistry implements DataManipulatorRegistry {

    private static final SpongeDataRegistry instance = new SpongeDataRegistry();
    private final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> builderMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> immutableBuilderMap =
            new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends DataManipulator<?, ?>>, DataProcessor<?, ?>> processorMap = new MapMaker().concurrencyLevel(4).makeMap();
    private Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataProcessor<?, ?>> immutableProcessorMap =
            new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Key<? extends BaseValue<?>>, ValueProcessor<?, ?>> valueProcessorMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends DataManipulator<?, ?>>, BlockDataProcessor<?>> blockDataMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends DataManipulator<?, ?>>, BlockValueProcessor<?, ?>> blockValueMap = new MapMaker().concurrencyLevel(4).makeMap();

    private SpongeDataRegistry() {
    }


    public static SpongeDataRegistry getInstance() {
        return SpongeDataRegistry.instance;
    }


    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void register(Class<T> manipulatorClass,
            Class<I> immutableManipulatorClass, DataManipulatorBuilder<T, I> builder) {
        if (!this.builderMap.containsKey(checkNotNull(manipulatorClass))) {
            this.builderMap.put(manipulatorClass, checkNotNull(builder));
            this.immutableBuilderMap.put(checkNotNull(immutableManipulatorClass), builder);
        } else {
            throw new IllegalStateException("Already registered the DataUtil for " + manipulatorClass.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataManipulatorBuilder<T, I>> getBuilder(Class<T>
            manipulatorClass) {
        return Optional.fromNullable((DataManipulatorBuilder<T, I>) (Object) this.builderMap.get(checkNotNull(manipulatorClass)));
    }

    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataManipulatorBuilder<T, I>> getBuilderForImmutable(
            Class<I> immutableManipulatorClass) {
        return Optional.fromNullable((DataManipulatorBuilder<T, I>) (Object) this.immutableBuilderMap.get(checkNotNull(immutableManipulatorClass)));
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void registerDataProcessor(Class<T> manipulatorClass,
            Class<I> immutableManipulatorClass, DataProcessor<T, I> processor) {
        checkState(!this.processorMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + manipulatorClass.getCanonicalName());
        this.processorMap.put(manipulatorClass, checkNotNull(processor));
        this.immutableProcessorMap.put(immutableManipulatorClass, processor);
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void registerDataProcessorAndImpl(Class<T> manipulatorClass,
            Class<? extends T> implClass, Class<I> immutableDataManipulator, Class<? extends I> implImClass, DataProcessor<T, I> processor) {
        checkState(!this.processorMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + manipulatorClass.getCanonicalName());
        checkState(!this.processorMap.containsKey(checkNotNull(implClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + implClass.getCanonicalName());
        this.builderMap.put(manipulatorClass, processor);
        this.immutableBuilderMap.put(immutableDataManipulator, processor);
        this.processorMap.put(manipulatorClass, checkNotNull(processor));
        this.processorMap.put(implClass, processor);
        this.immutableProcessorMap.put(immutableDataManipulator, processor);
        this.immutableProcessorMap.put(implImClass, processor);
    }

    @SuppressWarnings("unchecked")
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getUtil(Class<T>
            manipulatorClass) {
        return Optional.fromNullable((DataProcessor<T, I>) (Object) this.processorMap.get(checkNotNull(manipulatorClass)));
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void registerBlockProcessor(Class<T> manipulatorclass,
            BlockDataProcessor<T> util) {
        if (!this.blockDataMap.containsKey(checkNotNull(manipulatorclass))) {
            this.blockDataMap.put(manipulatorclass, checkNotNull(util));
        } else {
            throw new IllegalStateException("Already registered a SpongeBlockProcessor for the given DataManipulator: " + manipulatorclass
                    .getCanonicalName());
        }
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void registerBlockProcessorAndImpl(Class<T> manipulatorClass,
            Class<? extends T> implClass,
            Class<I> immutableManipulatorClass, Class<? extends I> implImmManClass, BlockDataProcessor<T> processor) {
        checkState(!this.blockDataMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + manipulatorClass.getCanonicalName());
        checkState(!this.blockDataMap.containsKey(checkNotNull(implClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + implClass.getCanonicalName());
        this.blockDataMap.put(manipulatorClass, checkNotNull(processor));
        this.blockDataMap.put(implClass, processor);
    }


    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getProcessor(
            Class<T> mutableClass) {
        return Optional.of((DataProcessor<T, I>) (Object) this.processorMap.get(checkNotNull(mutableClass)));
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getImmutableProcessor(Class<I>
            immutableClass) {
        return Optional.of((DataProcessor<T, I>) (Object) this.immutableProcessorMap.get(checkNotNull(immutableClass)));
    }

    public <T extends DataManipulator<T, ?>> Optional<BlockDataProcessor<T>> getBlockDataFor(Class<T>
            manipulatorClass) {
        return Optional.of((BlockDataProcessor<T>) (Object) this.blockDataMap.get(checkNotNull(manipulatorClass)));
    }


    public <E, V extends BaseValue<E>> Optional<ValueProcessor<E, V>> getValueProcessor(Key<V> key) {
        return Optional.of((ValueProcessor<E, V>) (Object)  this.valueProcessorMap.get(key));
    }

    public <E> Optional<ValueProcessor<E, ? extends BaseValue<E>>> getBaseValueProcessor(Key<? extends BaseValue<E>> key) {
        return Optional.<ValueProcessor<E, ? extends BaseValue<E>>>of((ValueProcessor<E, ? extends BaseValue<E>>) (Object) this.valueProcessorMap.get
                (key));
    }
}
