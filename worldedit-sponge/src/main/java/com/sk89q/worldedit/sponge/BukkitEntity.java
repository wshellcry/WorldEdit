/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.entity.metadata.EntityType;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to adapt a Bukkit entity into a WorldEdit one.
 */
class BukkitEntity implements Entity {

    private final WeakReference<org.spongepowered.api.entity.Entity> entityRef;

    /**
     * Create a new instance.
     *
     * @param entity the entity
     */
    BukkitEntity(org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<org.spongepowered.api.entity.Entity>(entity);
    }

    @Override
    public Extent getExtent() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return SpongeAdapter.adapt(entity.getWorld());
        } else {
            return NullWorld.getInstance();
        }
    }

    @Override
    public Location getLocation() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return SpongeAdapter.adapt(entity.getLocation());
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public BaseEntity getState() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            if (entity instanceof Player) {
                return null;
            }
            return new BukkitEntity(entity);
        } else {
            return null;
        }
    }

    @Override
    public boolean remove() {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            entity.remove();
            return entity.isRemoved();
        } else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        org.spongepowered.api.entity.Entity entity = entityRef.get();
        if (entity != null && EntityType.class.isAssignableFrom(cls)) {
            return (T) new SpongeEntityType(entity);
        } else {
            return null;
        }
    }
}
