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
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataRegistry;
import org.spongepowered.common.data.ValueProcessor;

import javax.annotation.Nullable;

/**
 * So, considering this is the root of the immutable variants of
 * {@link DataManipulator}, otherwise known as {@link                                     ImmutableDataManipulator}s.
 * The advantage of these types of {@link DataManipulator}s is that they can not be
 * mutated once created. In other words, it's safe to pass around these immutable
 * variants across threads without worry of the underlying values being changed.
 *
 * It may be possible that some commonly used {@link ImmutableDataManipulator}s
 * may be cached for better performance when processing obtaining new
 * {@link ImmutableDataManipulator}s with different values.
 *
 * @param <I> The immutable data manipulator type
 * @param <M> The mutable manipulator type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractImmutableData<I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>> implements ImmutableDataManipulator<I, M> {

    private final Class<I> immutableClass;

    protected AbstractImmutableData(Class<I> immutableClass) {
        this.immutableClass = checkNotNull(immutableClass);
    }

    @Override
    public Optional<I> fill(DataHolder dataHolder) {
        // Basic stuff, getting the processor....
        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getImmutableProcessor(this.immutableClass);
        if (!processor.isPresent()) {
            return Optional.absent();
        }
        return processor.get().fillImmutable(dataHolder, (I) (Object) this);
    }

    @Override
    public Optional<I> fill(DataHolder dataHolder, Function<I, I> overlap) {
        // Basic stuff, getting the processor....
        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getImmutableProcessor(this.immutableClass);
        if (!processor.isPresent()) {
            return Optional.absent();
        }
        return processor.get().fillImmutable(dataHolder, (I) (Object) this, overlap);
    }

    @Override
    public Optional<I> from(DataContainer container) {
        // Basic stuff, getting the processor....
        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getImmutableProcessor(this.immutableClass);
        if (!processor.isPresent()) {
            return Optional.absent();
        }
        return processor.get().fillImmutable(container, (I) (Object) this);
    }

    @Override
    public <E> Optional<I> with(Key<? extends BaseValue<E>> key, E value) {
        // Basic stuff, getting the processor....
        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getImmutableProcessor(this.immutableClass);
        // We actually need to check that the processor is available, otherwise
        // we throw an IllegalArgumentException, because people don't check for
        // support!!
        checkArgument(processor.isPresent(), "Invalid Key for " + this.immutableClass.getCanonicalName() + ". Use supprts(Key) to avoid "
                + "exceptions!");
        // Then we pass it to the processor :)
        return processor.get().with(checkNotNull(key), checkNotNull(value), (I) (Object) this);
    }

    @Override
    public Optional<I> with(BaseValue<?> value) {
        // Basic stuff, getting the processor....

        final Optional<DataProcessor<M, I>> processor = SpongeDataRegistry.getInstance().getImmutableProcessor(this.immutableClass);
        // We actually need to check that the processor is available, otherwise
        // we throw an IllegalArgumentException, because people don't check for
        // support!!
        checkArgument(processor.isPresent(), "Invalid Value for " + this.immutableClass.getCanonicalName() + ". Use supprts(BaseValue) to avoid "
                + "exceptions!");
        // Then we pass it to the processor :)
        // We can easily use the key provided, since that is the identifier, not the actual value itself
        return processor.get().with(value.getKey(), checkNotNull(value).get(), (I) (Object) this);

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
        return get(key).orNull();
    }

    @Override
    public <E> E getOrElse(Key<? extends BaseValue<E>> key, E defaultValue) {
        return get(key).or(checkNotNull(defaultValue, "Provided a null default value for 'getOrElse(Key, null)'!"));
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        final Optional<ValueProcessor<E, V>> optional = SpongeDataRegistry.getInstance().getValueProcessor(key);
        return optional.isPresent() ? optional.get().getApiValueFromContainer(this) : Optional.<V>absent();
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public boolean supports(BaseValue<?> baseValue) {
        return false;
    }

    @Override
    public I copy() {
        return null;
    }

    @Override
    public ImmutableSet<Key<?>> getKeys() {
        return null;
    }

    @Override
    public ImmutableSet<BaseValue<?>> getValues() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.immutableClass);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractImmutableData other = (AbstractImmutableData) obj;
        return Objects.equal(this.immutableClass, other.immutableClass);
    }

    @Override
    public int compareTo(I o) {
        return 0;
    }
}
