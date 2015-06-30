package org.spongepowered.common.data.manipulator.immutable;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableColoredData;
import org.spongepowered.api.data.manipulator.mutable.ColoredData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.AbstractSingleImmutableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeColoredData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.awt.Color;

public class ImmutableSpongeColorData extends AbstractSingleImmutableData<Color, ImmutableColoredData, ColoredData> implements ImmutableColoredData {

    public ImmutableSpongeColorData(Color value) {
        super(ImmutableColoredData.class, value);
    }

    @Override
    public ColoredData asMutable() {
        return new SpongeColoredData(this.getValue());
    }

    @Override
    public ImmutableValue<Color> color() {
        return new ImmutableSpongeValue<Color>(Keys.COLOR, this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }
}
