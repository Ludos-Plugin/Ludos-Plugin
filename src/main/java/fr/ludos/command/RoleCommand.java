package fr.ludos.command;


import fr.ludos.role.Role;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class RoleCommand implements TabExecutor {

    private static final String randomRole = "random";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
        if ( ! EnumUtils.isValidEnum(RoleCommandOptions.class, args[0]) ) {
            return false;
        }
        RoleCommandOptions config = RoleCommandOptions.valueOf( args[0] );

        return handleConfigsCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1) {
            return Arrays.stream(RoleCommandOptions.values()).map(RoleCommandOptions::toString)
                .sorted()
                .collect(Collectors.toList());
        }
        if ( ! EnumUtils.isValidEnum(RoleCommandOptions.class, args[0]) ) {
            return null;
        }
        RoleCommandOptions config = RoleCommandOptions.valueOf( args[0] );

        return handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
    }

    private boolean handleConfigsCommand(CommandSender sender, Command command, String label, String[] args, RoleCommandOptions config) {
        switch (config) {
            case get:
                Player getTarget = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
                if (getTarget == null) {
                    sender.sendMessage(randomRole); // TODO: Translate
                    return true;
                }

                Role.Builder role = Role.getRole(getTarget);
                sender.sendMessage(role == null ? randomRole : role.getId());

                return true;
            case reset:
                Player removeTarget = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
                if (removeTarget == null) {
                    return false;
                }

                Role.removeRole(removeTarget);
                if ( removeTarget != sender ) {
                    sender.sendMessage("The role of Player " + removeTarget.getName() + " is now randomly chosen");
                }

                return true;
            case set:
                if (args.length == 0) {
                    return false;
                }
                if ( ! Role.registered.containsKey(args[0]) ) {
                    return false;
                }
                Player setTarget = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
                if (setTarget == null) {
                    return false;
                }

                Role.setRole(setTarget, args[0]);
                if ( setTarget != sender ) {
                    sender.sendMessage("The role of Player " + setTarget.getName() + " is now " + args[0]);
                }

                return true;
            default:
                return false;
        }
    }

    private List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args, RoleCommandOptions config) {
        switch (config) {
            case get:
            case reset:
                return null;
            case set:
                switch (args.length) {
                    case 0:
                    case 1:
                        return Role.registered.keySet().stream()
                            .sorted()
                            .collect(Collectors.toList());
                    default:
                        return null;
                }
            default:
                return null;
        }
    }

    public String getUsage() {

        StringBuilder usage = new StringBuilder("/<command> ");

        // usage.append('<');
        // usage.append( Stream.concat(Role.registered.keySet().stream(), Stream.of(randomRole))
        //                 .sorted()
        //                 .collect(Collectors.joining(" | ")) );
        // usage.append('>');

        // usage.append(' ');

        usage.append('[');
        usage.append( Arrays.stream(RoleCommandOptions.values()).map(RoleCommandOptions::toString)
                        .sorted()
                        .collect(Collectors.joining( " | ")) );
        usage.append(']');

        usage.append(' ');

        usage.append("[player]");

        return usage.toString();
    }
}