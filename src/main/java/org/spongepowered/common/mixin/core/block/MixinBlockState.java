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
package org.spongepowered.common.mixin.core.block;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@NonnullByDefault
@Mixin(net.minecraft.block.state.BlockState.StateImplementation.class)
public abstract class MixinBlockState extends BlockStateBase implements BlockState {

    @Shadow
    @SuppressWarnings("rawtypes")
    private final ImmutableMap properties = null;
    @Shadow private final Block block = null;

    @Override
    public BlockType getType() {
        return (BlockType) getBlock();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getPropertyNames() {
        ImmutableMap<IProperty, Comparable<?>> properties = this.getProperties();
        List<String> names = Lists.newArrayListWithCapacity(properties.size());
        for (IProperty property : properties.keySet()) {
            names.add(property.getName());
        }
        return Collections.unmodifiableCollection(names);
    }

    @Override
    public ImmutableCollection<DataManipulator<?>> getManipulators() {

        return null;
    }

    @Override
    public <M extends DataManipulator<M>> Optional<M> getManipulator(Class<M> manipulatorClass) {
        return null;
    }

    @Override
    public <M extends DataManipulator<M>> Optional<BlockState> withData(M manipulator) {
        return null;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("BlockType"), this.getType().getId());
        container.set(of("Data"), this.getManipulators());
        return container;
    }
}
