package org.spongepowered.common.data.manipulator.immutable;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableCommandData;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.AbstractImmutableData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeCommandData extends AbstractImmutableData<ImmutableCommandData, CommandData> implements ImmutableCommandData {

    private final String storedCommand;
    private final int success;
    private final boolean tracks;
    private final Text lastOutput;

    public ImmutableSpongeCommandData(String storedCommand, int success, boolean tracks, Text lastOutput) {
        super(ImmutableCommandData.class);
        this.storedCommand = storedCommand;
        this.success = success;
        this.tracks = tracks;
        this.lastOutput = lastOutput;
    }

    @Override
    public ImmutableValue<String> storedCommand() {
        return new ImmutableSpongeValue<String>(Keys.COMMAND, this.storedCommand);
    }

    @Override
    public ImmutableValue<Integer> successCount() {
        return null;
    }

    @Override
    public ImmutableValue<Boolean> doesTrackOutput() {
        return null;
    }

    @Override
    public ImmutableOptionalValue<Text> lastOutput() {
        return null;
    }

    @Override
    public CommandData asMutable() {
        return null;
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }
}
