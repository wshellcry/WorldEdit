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

import com.sk89q.worldedit.LocalSession;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.spongepowered.api.net.ChannelBuf;
import org.spongepowered.api.net.ChannelListener;
import org.spongepowered.api.net.PlayerConnection;

import java.nio.charset.Charset;

/**
 * Handles incoming WorldEditCui init message.
 */
public class CUIChannelListener implements ChannelListener {

    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private final WorldEditPlugin plugin;

    public CUIChannelListener(WorldEditPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handlePayload(PlayerConnection client, String channel, ChannelBuf data) {
        LocalSession session = plugin.getSession(client.getPlayer());
        String text = data.readString();
        session.handleCUIInitializationMessage(text);
        session.describeCUI(plugin.wrapPlayer(client.getPlayer()));
    }
}
