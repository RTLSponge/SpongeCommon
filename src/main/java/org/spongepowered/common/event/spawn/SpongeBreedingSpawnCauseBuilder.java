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
package org.spongepowered.common.event.spawn;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.cause.entity.spawn.BreedingSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.common.AbstractEntitySpawnCauseBuilder;

public class SpongeBreedingSpawnCauseBuilder extends AbstractEntitySpawnCauseBuilder<BreedingSpawnCause, BreedingSpawnCause.Builder>
        implements BreedingSpawnCause.Builder {

    protected EntitySnapshot mate;

    @Override
    public BreedingSpawnCause build() {
        checkState(this.spawnType != null, "SpawnType cannot be null!");
        checkState(this.entitySnapshot != null, "EntitySnapshot cannot be null!");
        checkState(this.mate != null, "The mate cannot be null!");
        return new SpongeBreedingSpawnCause(this);
    }

    @Override
    public BreedingSpawnCause.Builder mate(Entity entity) {
        this.mate = checkNotNull(entity, "Entity cannot be null!").createSnapshot();
        return this;
    }

    @Override
    public BreedingSpawnCause.Builder mate(EntitySnapshot snapshot) {
        this.mate = checkNotNull(snapshot, "Entity snapshot cannot be null!");
        return this;
    }

    @Override
    public BreedingSpawnCause.Builder reset() {
        this.mate = null;
        return super.reset();
    }
}
