package fr.ludos.command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Bukkit.broadcastMessage( "Hello World" );
        Bukkit.broadcastMessage( args.toString() );
        return true;
    }
}