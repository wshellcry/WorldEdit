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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts between Bukkit and WorldEdit equivalent objects.
 */
final class SpongeAdapter {

    private SpongeAdapter() {
    }

    /**
     * Convert any WorldEdit world into an equivalent wrapped Bukkit world.
     *
     * <p>If a matching world cannot be found, a {@link RuntimeException}
     * will be thrown.</p>
     *
     * @param world the world
     * @return a wrapped Bukkit world
     */
    public static SpongeWorld asSpongeWorld(World world) {
        if (world instanceof SpongeWorld) {
            return (SpongeWorld) world;
        } else {
            SpongeWorld spongeWorld = WorldEditPlugin.getInstance().getInternalPlatform().matchWorld(world);
            if (spongeWorld == null) {
                throw new RuntimeException("World '" + world.getName() + "' has no matching version in Bukkit");
            }
            return spongeWorld;
        }
    }

    /**
     * Create a WorldEdit world from a Bukkit world.
     *
     * @param world the Bukkit world
     * @return a WorldEdit world
     */
    public static World adapt(org.spongepowered.api.world.World world) {
        checkNotNull(world);
        return new SpongeWorld(world);
    }

    /**
     * Create a Bukkit world from a WorldEdit world.
     *
     * @param world the WorldEdit world
     * @return a Bukkit world
     */
    public static org.spongepowered.api.world.World adapt(World world) {
        checkNotNull(world);
        if (world instanceof SpongeWorld) {
            return ((SpongeWorld) world).getWorld();
        } else {
            org.spongepowered.api.world.World match = Bukkit.getServer().getWorld(world.getName());
            if (match != null) {
                return match;
            } else {
                throw new IllegalArgumentException("Can't find a Sponge world for " + world);
            }
        }
    }

    /**
     * Create a WorldEdit location from a Bukkit location.
     *
     * @param location the Bukkit location
     * @return a WorldEdit location
     */
    public static Location adapt(org.spongepowered.api.world.Location location) {
        checkNotNull(location);
        Vector position = SpongeUtil.toVector(location);
        return new com.sk89q.worldedit.util.Location(
                adapt(location.getWorld()),
                position,
                location.getYaw(),
                location.getPitch());
    }

    /**
     * Create a Bukkit location from a WorldEdit location.
     *
     * @param location the WorldEdit location
     * @return a Bukkit location
     */
    public static org.spongepowered.api.world.Location adapt(Location location) {
        checkNotNull(location);
        Vector position = location.toVector();
        return new org.spongepowered.api.world.Location(
                adapt((World) location.getExtent()),
                position.getX(), position.getY(), position.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    /**
     * Create a Bukkit location from a WorldEdit position with a Bukkit world.
     *
     * @param world the Bukkit world
     * @param position the WorldEdit position
     * @return a Bukkit location
     */
    public static org.spongepowered.api.world.Location adapt(org.spongepowered.api.world.World world, Vector position) {
        checkNotNull(world);
        checkNotNull(position);
        return new org.spongepowered.api.world.Location(
                world,
                position.getX(), position.getY(), position.getZ());
    }

    /**
     * Create a Bukkit location from a WorldEdit location with a Bukkit world.
     *
     * @param world the Bukkit world
     * @param location the WorldEdit location
     * @return a Bukkit location
     */
    public static org.spongepowered.api.world.Location adapt(org.spongepowered.api.world.World world, Location location) {
        checkNotNull(world);
        checkNotNull(location);
        return new org.spongepowered.api.world.Location(
                world,
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    /**
     * Create a WorldEdit entity from a Bukkit entity.
     *
     * @param entity the Bukkit entity
     * @return a WorldEdit entity
     */
    public static Entity adapt(org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        return new BukkitEntity(entity);
    }

}
