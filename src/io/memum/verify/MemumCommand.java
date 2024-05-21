package io.memum.verify;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class MemumCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //Make sure no sneaky players try to run this command somehow
        if (sender instanceof ConsoleCommandSender) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                Verify.instantiateConn(true); //Reload the database connection
                sender.sendMessage(ChatColor.GREEN + "Reloaded the database connection!");
            } else {
                //Run a simple query to the database to check if it's connected
                try {
                    ResultSet rs = Verify.conn.prepareStatement("SELECT 1").executeQuery();
                    if (!rs.next()) {
                        sender.sendMessage(ChatColor.RED + "The database is currently disconnected.");
                    } else {
                        sender.sendMessage(ChatColor.GREEN + "The database is currently connected.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "The database is currently disconnected.");
                }
            }
        }
        return true;
    }
}
