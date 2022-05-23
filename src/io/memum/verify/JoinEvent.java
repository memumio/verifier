package io.memum.verify;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class JoinEvent implements Listener {
	Connection c = null;
	String sep = ChatColor.BLUE + "------------------------------------------" + ChatColor.RESET; //Separator string
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
		Connection c = Verify.conn;
		Player p = event.getPlayer();
		UUID rawuuid = p.getUniqueId();
		String uuid = String.valueOf(rawuuid).replaceAll("-", "");

		//Generate a random 6 digit code
		Random generator = new Random();
		int num = generator.nextInt(899999) + 100000;
		String code = String.valueOf(num);

		//Attempt inserting the code into the database
		try {
			PreparedStatement stmt = c.prepareStatement("INSERT INTO public.verifications(code, player) VALUES (?, (SELECT id FROM public.players WHERE uuid = ?)) ON CONFLICT(players) DO UPDATE SET code=EXCLUDED.code, timestamp=NOW()");
			stmt.setString(1, String.valueOf(num));
			stmt.setString(2, uuid);
			stmt.executeUpdate();
			p.kickPlayer(sep + "\n\n" + ChatColor.GRAY + "Your Memum code is\n\n" + ChatColor.BLUE + "[  " + ChatColor.WHITE + ChatColor.BOLD + code + ChatColor.BLUE + "  ]\n\n"+ChatColor.GRAY+"Thank you for using the Memum verification service!\n\n\n" + ChatColor.DARK_GRAY + ChatColor.ITALIC + "Note: This code expires in 15 minutes!\n\n" + sep);
		} catch (SQLException e) {
			e.printStackTrace();
			p.kickPlayer("Please search yourself on Memum.io before requesting a code.");
		}
    }
}
