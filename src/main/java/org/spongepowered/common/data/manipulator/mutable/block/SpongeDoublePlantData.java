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
package org.spongepowered.common.data.manipulator.mutable.block;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDoublePlantData;
import org.spongepowered.api.data.manipulator.mutable.block.DoublePlantData;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.AbstractMutableData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeDoublePlantData extends AbstractMutableData<DoublePlantData, ImmutableDoublePlantData> implements DoublePlantData {

    public static final DataQuery DOUBLE_SIZE_PLANT_TYPE = of("DoubleSizePlantType");

    private DoublePlantType value = DoublePlantTypes.GRASS;

    public SpongeDoublePlantData() {
        super(DoublePlantData.class);
    }

    public DoublePlantType getType() {
        return this.value;
    }

    public SpongeDoublePlantData setType(DoublePlantType doublePlantType) {
        this.value = checkNotNull(doublePlantType);
        return this;
    }

    @Override
    public DoublePlantData copy() {
        return new SpongeDoublePlantData().setType(this.value);
    }

    @Override
    public int compareTo(DoublePlantData o) {
        return o.get(Keys.DOUBLE_PLANT_TYPE).get().getId().compareTo(this.value.getId());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(DOUBLE_SIZE_PLANT_TYPE, this.value.getId());
    }

    @Override
    public Value<DoublePlantType> type() {
        return new SpongeValue<DoublePlantType>(Keys.DOUBLE_PLANT_TYPE, this.value);
    }
}
