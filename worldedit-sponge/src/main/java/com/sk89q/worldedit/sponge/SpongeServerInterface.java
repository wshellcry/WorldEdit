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

import com.google.common.base.Optional;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.sponge.data.BukkitBiomeRegistry;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Description;
import com.sk89q.worldedit.util.command.Dispatcher;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SpongeServerInterface extends ServerInterface implements MultiUserPlatform {
    public Game game;
    public WorldEditPlugin plugin;
    private boolean hookingEvents;

    public SpongeServerInterface(WorldEditPlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
        this.biomes = new BukkitBiomeRegistry();
    }

    boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public int resolveItem(String name) {
        /*Material mat = Material.matchMaterial(name);
        return mat == null ? 0 : mat.getId();*/
    }

    @Override
    public boolean isValidMobType(String type) {
        return game.getRegistry().getType(EntityType.class, type).isPresent();
    }

    @Override
    public void reload() {
        plugin.loadConfiguration();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        game.getSyncScheduler().runRepeatingTaskAfter(plugin, task, period, delay);
    }

    @Override
    public List<LocalWorld> getWorlds() {
        Collection<World> worlds = game.getServer().getWorlds();
        List<LocalWorld> ret = new ArrayList<LocalWorld>(worlds.size());

        for (World world : worlds) {
            ret.add(SpongeUtil.getLocalWorld(world));
        }

        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof SpongePlayer) {
            return player;
        } else {
            Optional<org.spongepowered.api.entity.player.Player> spongePlayer = game.getServer().getPlayer(player.getUniqueId());
            return spongePlayer.isPresent() ? new SpongePlayer(plugin, this, spongePlayer.get()) : null;
        }
    }

    @Nullable
    @Override
    public SpongeWorld matchWorld(com.sk89q.worldedit.world.World world) {
        if (world instanceof SpongeWorld) {
            return (SpongeWorld) world;
        } else {
            Optional<World> spongeWorld = game.getServer().getWorld(world.getName());
            return spongeWorld.isPresent() ? new SpongeWorld(spongeWorld.get()) : null;
        }
    }

    @Override
    public void registerCommands(Dispatcher dispatcher) {

        for (CommandMapping command : dispatcher.getCommands()) {
            plugin.getGame().getCommandDispatcher().register(this, new SpongeCommandCallable(plugin, command), command.getAllAliases());
        }
    }

    @Override
    public void registerGameHooks() {
        hookingEvents = true;
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return plugin.getLocalConfiguration();
    }

    @Override
    public String getVersion() {
        return getPlatformVersion();
    }

    @Override
    public String getPlatformName() {
        return "Sponge-Official";
    }

    @Override
    public String getPlatformVersion() {
        Optional<PluginContainer> container = game.getPluginManager().fromInstance(plugin);
        return container.isPresent() ? container.get().getVersion() : "UNKNOWN";
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilities = new EnumMap<Capability, Preference>(Capability.class);
        capabilities.put(Capability.CONFIGURATION, Preference.NORMAL);
        capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL);
        capabilities.put(Capability.GAME_HOOKS, Preference.PREFERRED);
        capabilities.put(Capability.PERMISSIONS, Preference.PREFERRED);
        capabilities.put(Capability.USER_COMMANDS, Preference.PREFERRED);
        capabilities.put(Capability.WORLD_EDITING, Preference.PREFERRED);
        return capabilities;
    }

    public void unregisterCommands() {
        for (org.spongepowered.api.util.command.CommandMapping spongeCmd : game.getCommandDispatcher().getOwnedBy(plugin)) {
            game.getCommandDispatcher().removeMapping(spongeCmd);
        }
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<Actor>();
        for (org.spongepowered.api.entity.player.Player player : game.getServer().getOnlinePlayers()) {
            users.add(new SpongePlayer(plugin, this, player));
        }
        return users;
    }
}
