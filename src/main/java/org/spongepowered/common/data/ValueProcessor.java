package org.spongepowered.common.data;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.CollectionValue;
import org.spongepowered.api.data.value.mutable.CompositeValueStore;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;

/**
 * An implementation processor for handling a particular {@link BaseValue}.
 * Usually every {@linkplain ValueProcessor} will deal only with
 * {@link Value}s
 * @param <E>
 * @param <V>
 */
public interface ValueProcessor<E, V extends BaseValue<E>> {

    /**
     * Gets the associated {@link Key} that this {@link ValueProcessor}
     * will handle. While the processor will almost a
     * @return
     */
    Key<? extends BaseValue<E>> getKey();

    /**
     * Gets the underlying value as an {@link Optional}. This is the direct
     * implementation of {@link ValueContainer#get(Key)}. As well, since the
     * return type is {@link Optional}, this method too is applicable for
     * {@link ValueContainer#getOrNull(Key)} and
     * {@link ValueContainer#getOrElse(Key, Object)} as they are simply
     * additional methods called from {@link Optional#orNull()} and
     * {@link Optional#or(Object)}.
     *
     * <p>To truly understand the use of this method, the thought process
     * is that a {@link ValueContainer} being mixed in to existing minecraft
     * classes will require a pseudo lookup of the {@link Key} to associate to
     * a field belonging to provided {@link ValueContainer}. Being that the
     * {@link Value} is very specific, the only assumption of mixed in
     * implementations will have no room for extra possibilities. Therefor,
     * this method serves as a "guarantee" that we've found the type of
     * {@link Value} we want to retrieve, and all we need to do is validate
     * that the {@link Value} is infact compatible with the provided
     * {@link ValueContainer}.</p>
     *
     * <p>An example of this type of interaction is getting the health from an
     * {@link Entity}. Normally, when you have an {@link Entity}, you can
     * grab health two ways: <ol><li><pre>{@code entity.get(Keys.HEALTH)}</pre>
     * </li><li><pre>{@code entity.get(HealthData.class).health()}</pre></li>
     * </ol>The advantage of the first is that the use of
     * {@link Entity#get(Key)} will result in a call to this method for a
     * {@link ValueProcessor} handling specifically the health value. The
     * second case will use the same {@link ValueProcessor} but be retrieving
     * the values directly from the {@link HealthData} {@link DataManipulator}.
     * This differs with object creation as the second method will result in an
     * unecessary {@link DataManipulator} to be created, and then finally a
     * {@link ValueProcessor} method call for the {@link DataManipulator}
     * instead of the {@link Entity} having a direct call.</p>
     *
     * <p>The cases where this type of value usage is not preferable is with
     * more complex {@link Value}s, such as {@link CollectionValue},
     * values found for {@link MobSpawnerData} and {@link CommandData}. Using
     * the {@link ValueContainer#get(Key)} and
     * {@link DataManipulator#set(Key, Object)} remains to be the optimal methods
     * to use as they do not rely on the implementation attempting to guess which
     * {@link Key}, {@link Value} and {@link ValueProcessor} is needed to
     * manipulate the data.</p>
     *
     * @param valueContainer The value container to retrieve the value froms
     * @return The value, if available and compatible
     */
    Optional<E> getValueFromContainer(ValueContainer<?> valueContainer);

    /**
     * Gets the actual {@link Value} object wrapping around the underlying value
     * desired from the provided {@link ValueContainer}. This is very similar to
     * {@link #getValueFromContainer(ValueContainer)} except that instead of an
     * actual value, a {@link Value} or extension there of is returned.
     *
     * @param container
     * @return
     */
    Optional<V> getApiValueFromContainer(ValueContainer<?> container);

    <C extends ValueContainer<?>> C transform(C container, Function<E, E> function);

    boolean supports(ValueContainer<?> valueContainer);

    DataTransactionResult offerToStore(BaseValue<E> value);

    DataTransactionResult offerToStore(CompositeValueStore<?, ?> store, E value);



}
