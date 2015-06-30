package org.spongepowered.common.data.manipulator;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;

public abstract class AbstractSingleImmutableData<E, I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>>
        extends AbstractImmutableData<I, M> {

    protected final E value;

    public AbstractSingleImmutableData(Class<I> immutableClass, E value) {
        super(immutableClass);
        this.value = checkNotNull(value);
    }

    public E getValue() {
        return value;
    }

    @Override
    public abstract M asMutable();
}
