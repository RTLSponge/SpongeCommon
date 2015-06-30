package org.spongepowered.common.data.manipulator.immutable.block;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableAttachedData;
import org.spongepowered.api.data.manipulator.mutable.block.AttachedData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeAttachedData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmmutableSpongeAttachedData extends AbstractImmutableData<ImmutableAttachedData, AttachedData> implements ImmutableAttachedData {

    private final boolean attached;

    public ImmmutableSpongeAttachedData(boolean attached) {
        super(ImmutableAttachedData.class);
        this.attached = attached;
    }

    @Override
    public ImmutableValue<Boolean> attached() {
        return new ImmutableSpongeValue<Boolean>(Keys.ATTACHED, false, this.attached);
    }

    @Override
    public AttachedData asMutable() {
        return new SpongeAttachedData().setAttached(this.attached);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.ATTACHED.getQuery(), this.attached);
    }
}
