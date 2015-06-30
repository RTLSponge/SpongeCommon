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
package org.spongepowered.common.data.manipulator.mutable.entity;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgeableData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgeableData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.AbstractMutableData;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;

public class SpongeAgeableData extends AbstractMutableData<AgeableData, ImmutableAgeableData> implements AgeableData {

    public static final DataQuery AGE = of("Age");

    public SpongeAgeableData() {
        super(AgeableData.class, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public int getAge() {
        return getValue();
    }

    public SpongeAgeableData setAge(int age) {
        return setValue(age);
    }

    public SpongeAgeableData setBaby() {
        return setAge(Integer.MIN_VALUE);
    }

    public SpongeAgeableData setAdult() {
        return setAge(0);
    }

    public boolean isBaby() {
        return getAge() < 0;
    }

    @Override
    public AgeableData copy() {
        return new SpongeAgeableData().setAge(this.getAge());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(AGE, this.getValue());
    }

    @Override
    public MutableBoundedValue<Integer> age() {
        return new SpongeBoundedValue<Integer>(Keys.AGE);
    }

    @Override
    public Value<Boolean> baby() {
        return null;
    }

    @Override
    public Value<Boolean> adult() {
        return null;
    }

    @Override
    public int compareTo(AgeableData o) {
        return 0;
    }
}
