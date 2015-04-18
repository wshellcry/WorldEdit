package com.sk89q.worldedit.sponge;


import com.google.common.base.Optional;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.command.CommandMapping;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import java.util.List;

/**
 * Wrapper for Sponge commands
 */
public class SpongeCommandCallable implements CommandCallable {
    private final WorldEditPlugin plugin;
    private final CommandMapping weCommand;

    public SpongeCommandCallable(WorldEditPlugin plugin, CommandMapping weCommand) {
        this.plugin = plugin;
        this.weCommand = weCommand;
    }

    @Override
    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException {
        CommandEvent event = new CommandEvent(plugin.wrapCommandSource(source), weCommand.getPrimaryAlias() + " " + arguments);
        plugin.getWorldEdit().getEventBus().post(event);

        return Optional.of(CommandResult.success()); // TODO: Give block counts?

    }

    @Override
    public boolean testPermission(CommandSource source) {
        CommandLocals locals = new CommandLocals();
        locals.put(Actor.class, plugin.wrapCommandSource(source));
        return weCommand.getCallable().testPermission(locals);
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.<Text>of(Texts.of(weCommand.getDescription().getShortDescription()));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.<Text>of(Texts.of(weCommand.getDescription().getHelp()));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Texts.of(weCommand.getDescription().getUsage());
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        CommandSuggestionEvent event = new CommandSuggestionEvent(plugin.wrapCommandSource(source), weCommand.getPrimaryAlias() + arguments);
        plugin.getWorldEdit().getEventBus().post(event);
        return event.getSuggestions();
    }
}
