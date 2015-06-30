/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.manipulator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataRegistry;
import org.spongepowered.common.data.ValueProcessor;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Basically, this is the default implementation that automatically delegates
 * <b>ALL</b> actual data processing to either their associated
 * {@link DataProcessor}s or {@link ValueProcessor}s to avoid relying on
 * implementation residing in the actua {@link DataManipulator}s themselves.
 * As all vanilla related manipulators are based on data existing from
 * Minecraft's current implementation (i.e. not an ECS), it is required
 * that all processing exists in the associated processors and not within these
 * {@link DataManipulator}s. Unfortunately, due to JDK 6's poor generic
 * inference calculator, most of the delegating methods to their processors
 * require extra casting to {@link Object} and recasting to the practical
 * {@code M} generics.
 *
 * @param <M> The generic of the DataManipulator from the API
 * @param <I> The type of the ImmutableDatAManipulator from the API
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMutableData<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements DataManipulator<M, I> {

    // We need this field for referencing to retrieve the processors as needed. This can never be null
    private final Class<M> manipulatorClass;

    protected AbstractMutableData(Class<M> manipulatorClass) {
        this.manipulatorClass = checkNotNull(manipulatorClass);
    }

    @Override
    public Optional<M> fill(DataHolder dataHolder) {
        // Basic stuff, getting the processor....
        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getProcessor(this.manipulatorClass);
        if (!processor.isPresent()) {
            return Optional.absent();
        }
        // .... and delegate to the processor!
        return processor.get().fill(dataHolder, (M) (Object) this);
    }

    @Override
    public Optional<M> fill(DataHolder dataHolder, Function<I, M> overlap) {
        // Basic stuff, getting the processor....
        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getProcessor(this.manipulatorClass);
        if (!processor.isPresent()) {
            return Optional.absent();
        }
        // .... and delegate to the processor!
        return processor.get().fill(dataHolder, (M) (Object) this, overlap);
    }

    @Override
    public Optional<M> from(DataContainer container) {
        // Basic stuff, getting the processor....
        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getProcessor(this.manipulatorClass);
        if (!processor.isPresent()) {
            return Optional.absent();
        }
        return processor.get().fill(container, (M) (Object) this);
    }

    @Override
    public <E> M set(Key<? extends BaseValue<E>> key, E value) {
        // Basic stuff, getting the processor....
        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getProcessor(this.manipulatorClass);
        // We actually need to check that the processor is available, otherwise
        // we throw an IllegalArgumentException, because people don't check for
        // support!!
        checkArgument(processor.isPresent(), "Invalid Key for " + this.manipulatorClass.getCanonicalName() + ". Use supprts(Key) to avoid "
                + "exceptions!");
        // Then we pass it to the processor :)
        return processor.get().set(checkNotNull(key), checkNotNull(value), (M) (Object) this);
    }

    @Override
    public M set(BaseValue<?> value) {
        // Basic stuff, getting the processor....

        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getProcessor(this.manipulatorClass);
        // We actually need to check that the processor is available, otherwise
        // we throw an IllegalArgumentException, because people don't check for
        // support!!
        checkArgument(processor.isPresent(), "Invalid Value for " + this.manipulatorClass.getCanonicalName() + ". Use supprts(BaseValue) to avoid "
                + "exceptions!");
        // Then we pass it to the processor :)
        return processor.get().set(value.getKey(), checkNotNull(value).get(), (M) (Object) this);
    }

    @Override
    public M set(BaseValue<?>... values) {
        // We can do this with a for loop quite easily...
        for (BaseValue<?> value : checkNotNull(values)) {
            try { // Though we should be "argument" aware,
                // so we use the try catch.
                set(value);
            } catch (IllegalArgumentException e) {
                e.printStackTrace(); // print the stacktrace, but we continue
            }
        }
        // finally, return this object, casted for JDK 6's really bad generic inference calculations.
        return (M) (Object) this;
    }

    @Override
    public M set(Collection<? extends BaseValue<?>> values) { // Basically, the exact same as above...
        // We can do this with a for loop quite easily...
        for (BaseValue<?> value : checkNotNull(values)) {
            try { // Though we should be "argument" aware,
                  // so we use the try catch.
                set(value);
            } catch (IllegalArgumentException e) {
                e.printStackTrace(); // print the stacktrace, but we continue
            }
        }
        // finally, return this object, casted for JDK 6's really bad generic inference calculations.
        return (M) (Object) this;
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        // Basic retrieval of the processor
        Optional<ValueProcessor<E, ?>> processor = SpongeDataRegistry.getInstance().getBaseValueProcessor(key);
        // If we have a processor, well, we can retrieve the value, if not, c'est la vie.
        return processor.isPresent() ? processor.get().getValueFromContainer(this) : Optional.<E>absent();
    }

    @Nullable
    @Override
    public <E> E getOrNull(Key<? extends BaseValue<E>> key) {
        return get(key).orNull(); // Just use the provided optional
    }

    @Override
    public <E> E getOrElse(Key<? extends BaseValue<E>> key, E defaultValue) {
        return get(key).or(checkNotNull(defaultValue)); // Or use the optional with a default value
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        final Optional<ValueProcessor<E, V>> optional = SpongeDataRegistry.getInstance().getValueProcessor(key);
        return optional.isPresent() ? optional.get().getApiValueFromContainer(this) : Optional.<V>absent();
    }

    @Override
    public boolean supports(Key<?> key) {
        // Again, more basic getting of the processor
        Optional<ValueProcessor<?, ?>> processor = SpongeDataRegistry.getInstance().getValueProcessor(key);
        // If the processor is present, and if the processor says that this container supports the key!
        return processor.isPresent() && processor.get().supports(this);
    }

    @Override
    public boolean supports(BaseValue<?> baseValue) {
        // Again, more basic getting of the processor
        Optional<ValueProcessor<?, ?>> processor = SpongeDataRegistry.getInstance().getValueProcessor(baseValue.getKey());
        // If the processor is present, and if the processor says that this container supports the key!
        return processor.isPresent() && processor.get().supports(this);
    }

    @Override
    public ImmutableSet<Key<?>> getKeys() {
        // Dunno whether I should be lazy or not. I totally could be though.
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<BaseValue<?>> getValues() {
        // Again, laziness. Should I actually override this?
        return ImmutableSet.of();
    }

    @Override
    public int hashCode() {
        // The only thing we can really hashcode, overrides should be supported.
        return Objects.hashCode(this.manipulatorClass);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractMutableData other = (AbstractMutableData) obj;
        return Objects.equal(this.manipulatorClass, other.manipulatorClass);
    }

    @Override
    public DataContainer toContainer() {
        // Yep, empty container
        return new MemoryDataContainer();
    }
}
