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

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.api.util.command.CommandSource;

import javax.inject.Inject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Plugin for Bukkit.
 */
@Plugin(id = "worldedit", name = "WorldEdit", version = "DEV") // TODO: template values from build.gradle information
public class WorldEditPlugin {

    private static final Logger log = Logger.getLogger(WorldEditPlugin.class.getCanonicalName());
    public static final String CUI_PLUGIN_CHANNEL = "WECUI";
    private static WorldEditPlugin INSTANCE;

    private SpongeServerInterface server;
    private SpongeConfiguration config;

    @Inject
    private Game game;

    @Inject
    private org.slf4j.Logger spongeLogger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    /**
     * Called on plugin enable.
     */
    @SuppressWarnings("AccessStaticViaInstance")
    @Subscribe
    public void onServerStarting(ServerStartingEvent event) {
        this.INSTANCE = this;

        //noinspection ResultOfMethodCallIgnored
        getConfigDirectory().mkdirs();

        WorldEdit worldEdit = WorldEdit.getInstance();

        loadConfig(); // Load configuration

        // Setup platform
        server = new SpongeServerInterface(this, game);
        worldEdit.getPlatformManager().register(server);

        // Register CUI
        game.getServer().registerChannel(this, new CUIChannelListener(this), CUI_PLUGIN_CHANNEL);

        // Now we can register events
        game.getEventManager().register(this, new WorldEditListener(this));

        // If we are on MCPC+/Cauldron, then Forge will have already loaded
        // Forge WorldEdit and there's (probably) not going to be any other
        // platforms to be worried about... at the current time of writing
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());

    }

    public File getConfigDirectory() {
        return configDir;
    }

    private void loadConfig() {
        createDefaultConfiguration("config.yml"); // Create the default configuration file

        config = new SpongeConfiguration(new YAMLProcessor(new File(getConfigDirectory(), "config.yml"), true), this);
        config.load();
    }

    /**
     * Called on plugin disable.
     */
    @Subscribe
    public void onServerStopping(ServerStoppingEvent event) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.clearSessions();
        worldEdit.getPlatformManager().unregister(server);
        if (config != null) {
            config.unload();
        }
        if (server != null) {
            server.unregisterCommands();
        }
        for (Task task : game.getSyncScheduler().getScheduledTasks(this)) {
            task.cancel();
        }
    }

    /**
     * Loads and reloads all configuration.
     */
    protected void loadConfiguration() {
        config.unload();
        config.load();
    }

    /**
     * Create a default configuration file from the .jar.
     *
     * @param name the filename
     */
    protected void createDefaultConfiguration(String name) {
        File actual = new File(getConfigDirectory(), name);
        if (!actual.exists()) {
            InputStream input = null;
            try {
                JarFile file = new JarFile(getFile());
                ZipEntry copy = file.getEntry("defaults/" + name);
                if (copy == null) throw new FileNotFoundException();
                input = file.getInputStream(copy);
            } catch (IOException e) {
                spongeLogger.error("Unable to read default configuration: " + name);
            }
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }

                    spongeLogger.info("Default configuration file written: " + name);
                } catch (IOException e) {
                    spongeLogger.warn("Failed to write default config file", e);
                } finally {
                    try {
                        input.close();
                    } catch (IOException ignored) {}

                    try {
                        if (output != null) {
                            output.close();
                        }
                    } catch (IOException ignored) {}
                }
            }
        }
    }

    /**
     * Gets the session for the player.
     *
     * @param player a player
     * @return a session
     */
    public LocalSession getSession(Player player) {
        return WorldEdit.getInstance().getSessionManager().get(wrapPlayer(player));
    }

    /**
     * Gets the session for the player.
     *
     * @param player a player
     * @return a session
     */
    public EditSession createEditSession(Player player) {
        com.sk89q.worldedit.entity.Player wePlayer = wrapPlayer(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        BlockBag blockBag = session.getBlockBag(wePlayer);

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(wePlayer.getWorld(), session.getBlockChangeLimit(), blockBag, wePlayer);
        editSession.enableQueue();

        return editSession;
    }

    /**
     * Remember an edit session.
     *
     * @param player a player
     * @param editSession an edit session
     */
    public void remember(Player player, EditSession editSession) {
        com.sk89q.worldedit.entity.Player wePlayer = wrapPlayer(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);

        session.remember(editSession);
        editSession.flushQueue();

        WorldEdit.getInstance().flushBlockBag(wePlayer, editSession);
    }

    /**
     * Returns the configuration used by WorldEdit.
     *
     * @return the configuration
     */
    public SpongeConfiguration getLocalConfiguration() {
        return config;
    }

    /**
     * Used to wrap a Bukkit Player as a LocalPlayer.
     *
     * @param player a player
     * @return a wrapped player
     */
    public SpongePlayer wrapPlayer(Player player) {
        return new SpongePlayer(this, this.server, player);
    }

    public Actor wrapCommandSource(CommandSource sender) {
        if (sender instanceof Player) {
            return wrapPlayer((Player) sender);
        }

        return new SpongeCommandSource(this, sender);
    }

    SpongeServerInterface getInternalPlatform() {
        return server;
    }

    /**
     * Get WorldEdit.
     *
     * @return an instance
     */
    public WorldEdit getWorldEdit() {
        return WorldEdit.getInstance();
    }

    /**
     * Gets the instance of this plugin.
     *
     * @return an instance of the plugin
     * @throws NullPointerException if the plugin hasn't been enabled
     */
    static WorldEditPlugin getInstance() {
        return checkNotNull(INSTANCE);
    }

    public Game getGame() {
        return game;
    }

    public Logger getLogger() {
        return log;
    }
}
