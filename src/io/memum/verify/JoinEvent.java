package io.memum.verify;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class JoinEvent implements Listener {
	Connection c = null;
	String sep = ChatColor.BLUE + "------------------------------------------" + ChatColor.RESET; //Separator string
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
		Connection c = Verify.conn;
		Player p = event.getPlayer();

		//Attempt inserting the code into the database
		try {
			PreparedStatement stmt = c.prepareStatement("SELECT generate_server_verification_code(?) AS verification_code;");
			stmt.setObject(1, p.getUniqueId(), java.sql.Types.OTHER);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String code = rs.getString("verification_code");
				p.kickPlayer(sep + "\n\n" + ChatColor.GRAY + "Your Memum code is\n\n" + ChatColor.BLUE + "[  "
						+ ChatColor.WHITE + ChatColor.BOLD + code + ChatColor.BLUE + "  ]\n\n" + ChatColor.GRAY
						+ "Thank you for using the Memum verification service!\n\n\n" + ChatColor.DARK_GRAY
						+ ChatColor.ITALIC + "Note: This code expires in 15 minutes!\n\n" + sep);
			} else {
				p.kickPlayer(
						"An unknown error occurred while trying to generate your verification code. Please try again later or contact support.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			p.kickPlayer("An unknown error occurred while trying to generate your verification code. Please try again later or contact support.");
		}
    }
}
