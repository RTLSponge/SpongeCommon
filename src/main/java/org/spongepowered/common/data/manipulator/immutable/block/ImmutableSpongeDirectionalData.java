package org.spongepowered.common.data.manipulator.immutable.block;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirectionalData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeDirectionalData extends AbstractImmutableData<ImmutableDirectionalData, DirectionalData> implements ImmutableDirectionalData {

    private final Direction direction;

    public ImmutableSpongeDirectionalData(Direction direction) {
        super(ImmutableDirectionalData.class);
        this.direction = checkNotNull(direction);
    }

    @Override
    public ImmutableValue<Direction> direction() {
        return new ImmutableSpongeValue<Direction>(Keys.DIRECTION, Direction.NONE, this.direction);
    }

    @Override
    public DirectionalData asMutable() {
        return new SpongeDirectionalData().setDirection(this.direction);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.DIRECTION.getQuery(), this.direction.name());
    }
}
