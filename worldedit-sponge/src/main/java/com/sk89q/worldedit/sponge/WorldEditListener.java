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

// $Id$

package com.sk89q.worldedit.sponge;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.world.World;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.entity.EntityInteractionTypes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChangeGameModeEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractEvent;
import org.spongepowered.api.world.Location;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldEditListener {

    private WorldEditPlugin plugin;
    private boolean ignoreLeftClickAir = false;

    /**
     * Called when a player plays an animation, such as an arm swing
     *
     * @param event Relevant event details
     */

    /**
     * Construct the object;
     *
     * @param plugin the plugin
     */
    public WorldEditListener(WorldEditPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onGamemode(PlayerChangeGameModeEvent event) {
        if (!plugin.getInternalPlatform().isHookingEvents()) {
            return;
        }

        // this will automatically refresh their session, we don't have to do anything
        WorldEdit.getInstance().getSession(plugin.wrapPlayer(event.getPlayer()));
    }

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    @Subscribe
    public void onPlayerCommandPreprocess(CommandEvent event) {
        String[] split = event.getMessage().split(" ");

        if (split.length > 0) {
            split[0] = split[0].substring(1);
            split = plugin.getWorldEdit().getPlatformManager().getCommandManager().commandDetection(split);
        }

        final String newMessage = "/" + StringUtil.joinString(split, " ");

        if (!newMessage.equals(event.getMessage())) {
            event.setMessage(newMessage);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                if (!event.getMessage().isEmpty()) {
                    plugin.getServer().dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
                }

                event.setCancelled(true);
            }
        }
    }

    /**
     * Called when a player interacts
     *
     * @param event Relevant event details
     */
    @Subscribe
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getInternalPlatform().isHookingEvents()) {
            return;
        }

        if (event.)
        if (event.useItemInHand() == Result.DENY) {
            return;
        }

        final AbstractPlayerActor player = plugin.wrapPlayer(event.getPlayer());
        final World world = player.getWorld();
        final WorldEdit we = plugin.getWorldEdit();


        EntityInteractionType type = event.getInteractionType();
        if (type == EntityInteractionTypes.PICK_BLOCK) {
            final Location pos = event.
            final com.sk89q.worldedit.util.Location wePos = SpongeAdapter.adapt()

            if (we.handleBlockLeftClick(player, wePos)) {
                event.setCancelled(true);
            }

            if (we.handleArmSwing(player)) {
                event.setCancelled(true);
            }

            if (!ignoreLeftClickAir) {
                final int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        ignoreLeftClickAir = false;
                    }
                }, 2);

                if (taskId != -1) {
                    ignoreLeftClickAir = true;
                }
            }
        } else if (action == Action.LEFT_CLICK_AIR) {
            if (ignoreLeftClickAir) {
                return;
            }

            if (we.handleArmSwing(player)) {
                event.setCancelled(true);
            }


        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            final Block clickedBlock = event.getClickedBlock();
            final WorldVector pos = new WorldVector(LocalWorldAdapter.adapt(world), clickedBlock.getX(),
                    clickedBlock.getY(), clickedBlock.getZ());

            if (we.handleBlockRightClick(player, pos)) {
                event.setCancelled(true);
            }

            if (we.handleRightClick(player)) {
                event.setCancelled(true);
            }
        } else if (action == Action.RIGHT_CLICK_AIR) {
            if (we.handleRightClick(player)) {
                event.setCancelled(true);
            }
        }
    }
}
