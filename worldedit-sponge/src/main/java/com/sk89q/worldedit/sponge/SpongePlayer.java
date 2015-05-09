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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.world.AbstractWorld;
import org.spongepowered.api.data.manipulators.entities.FlyingData;
import org.spongepowered.api.data.manipulators.entities.GameModeData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SpongePlayer extends AbstractPlayerActor {

    private Player player;
    private WorldEditPlugin plugin;

    public SpongePlayer(WorldEditPlugin plugin, MultiUserPlatform server, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public int getItemInHand() {
        Optional<ItemStack> itemStack = player.getItemInHand();
        //return itemStack.isPresent() ? itemStack.get().
        return 0; // no ids
    }

    @Override
    public BaseBlock getBlockInHand() throws WorldEditException {
        Optional<ItemStack> itemStack = player.getItemInHand();
        return SpongeUtil.toBlock(getWorld(), itemStack);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public WorldVector getPosition() {
        Location loc = player.getLocation();
        return new WorldVector(SpongeUtil.getLocalWorld(player.getWorld()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public double getPitch() {
        return player.getRotation().getY();
    }

    @Override
    public double getYaw() {
        return player.getRotation().getX();
    }

    @Override
    public void giveItem(int type, int amt) {
        //player.getI
        //player.getInventory().addItem(new ItemStack(type, amt));
        // TODO: How do we handle not having ids?
    }

    @Override
    public void printRaw(String msg) {
        player.sendMessage(Texts.of(msg));
    }

    @Override
    public void print(String msg) {
        player.sendMessage(Texts.of(TextColors.DARK_PURPLE, msg));
    }

    @Override
    public void printDebug(String msg) {
        player.sendMessage(Texts.of(TextColors.GRAY, msg));
    }

    @Override
    public void printError(String msg) {
        player.sendMessage(Texts.of(TextColors.RED, msg));
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        player.setLocation(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        player.setRotation(new Vector3d(yaw, pitch, player.getRotation().getZ()));
    }

    @Override
    public String[] getGroups() {
        Optional<PermissionService> service = plugin.getGame().getServiceManager().provide(PermissionService.class);
        if (!service.isPresent()) {
            return new String[0];
        } else {
            Set<Context> activeContexts = player.getActiveContexts();
            List<String> ret = new ArrayList<String>();
            for (Subject group : service.get().getGroupSubjects().getAllSubjects()) {
                if (player.isChildOf(activeContexts, group)) {
                    ret.add(group.getIdentifier());
                }
            }
            return ret.toArray(new String[ret.size()]);
        }
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return new BukkitPlayerBlockBag(player);
    }

    @Override
    public boolean hasPermission(String perm) {
        return player.hasPermission(perm);
    }

    @Override
    public AbstractWorld getWorld() {
        return SpongeUtil.getLocalWorld(player.getWorld());
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        player.getConnection().sendCustomPayload(plugin, WorldEditPlugin.CUI_PLUGIN_CHANNEL, send.getBytes(CUIChannelListener.UTF_8_CHARSET));
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean hasCreativeMode() {
        Optional<GameModeData> data = player.getData(GameModeData.class);
        return data.isPresent() && data.get().getGameMode() == GameModes.CREATIVE;
    }

    @Override
    public void floatAt(int x, int y, int z, boolean alwaysGlass) {
        if (alwaysGlass /*|| !player.getAllowFlight()*/) { // TODO: Flight
            super.floatAt(x, y, z, alwaysGlass);
            return;
        }

        setPosition(new Vector(x + 0.5, y, z + 0.5));
        player.getOrCreate(FlyingData.class);
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public com.sk89q.worldedit.util.Location getLocation() {
        Location nativeLocation = player.getLocation();
        Vector rotation = SpongeUtil.toVector(player.getRotation());
        Vector position = SpongeUtil.toVector(nativeLocation);
        return new com.sk89q.worldedit.util.Location(
                getWorld(),
                position,
                rotation);
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return null;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(this.player.getUniqueId(), player.getName());
    }

    private static class SessionKeyImpl implements SessionKey {
        // If not static, this will leak a reference

        private final UUID uuid;
        private final String name;

        private SessionKeyImpl(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        @Override
        public UUID getUniqueId() {
            return uuid;
        }

        @Nullable
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isActive() {
            return WorldEditPlugin.getInstance().getGame().getServer().getPlayer(uuid).isPresent();
        }

        @Override
        public boolean isPersistent() {
            return true;
        }

    }

}
