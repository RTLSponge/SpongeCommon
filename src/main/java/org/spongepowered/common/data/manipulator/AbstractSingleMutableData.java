package org.spongepowered.common.data.manipulator;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;

@SuppressWarnings("unchecked")
public abstract class AbstractSingleMutableData<E, M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>>
        extends AbstractMutableData<M, I> {

    private E value;

    public AbstractSingleMutableData(Class<M> manipulatorClass, E value) {
        super(manipulatorClass);
        this.value = checkNotNull(value);
    }

    @Override
    public abstract I asImmutable();

    @Override
    public abstract int compareTo(M o);

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = checkNotNull(value);
    }
}
