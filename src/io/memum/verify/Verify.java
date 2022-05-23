package io.memum.verify;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class Verify extends JavaPlugin implements Listener {

    //Instance of main class for reconnections
    public static Verify INSTANCE;
    
    //PostgreSQL connection variable
    public static Connection conn;

    //Variable to access config.yml
    public FileConfiguration config = getConfig();

    //Function to initiate connection to the server
    public static void instantiateConn(boolean close) {
        try {
            if (close) conn.close(); //Close the connection if it already exists
        } catch(Exception e) {}
        try {
            Class.forName("org.postgresql.Driver");

            //Get the host and port from the config.yml
            String host = INSTANCE.config.getString("host");
            String port = INSTANCE.config.getString("port");

            INSTANCE.getLogger().log(Level.INFO, "Attempting connection to PostgreSQL database at " + host + ":" + port + "...");

            conn = DriverManager.getConnection(
                    "jdbc:postgresql://" + host + ":" + port + "/" + INSTANCE.config.getString("database"),
                    INSTANCE.config.getString("username"), INSTANCE.config.getString("password"));

            INSTANCE.getLogger().log(Level.INFO, "Successfully made connection to database!");
        } catch (ClassNotFoundException | SQLException e) {
            INSTANCE.getLogger().log(Level.SEVERE, "An error occurred attempting to connect to the Postgres database.");
            e.printStackTrace();
        }
    }
    
    @Override
    public void onEnable() {

        INSTANCE = this;
        
        //Set defaults for config.yml
        config.addDefault("host", "localhost");
        config.addDefault("port", "5432");
        config.addDefault("database", "memum");
        config.addDefault("username", "user");
        config.addDefault("password", "pass");
        config.addDefault("heartbeat", "NONE");
        config.options().copyDefaults(true);
        saveConfig();

        instantiateConn(false); //Initiate the connection

        //Register join event class
    	PluginManager pl = getServer().getPluginManager();
        pl.registerEvents(new JoinEvent(), this);

        //Register command
        this.getCommand("memumdb").setExecutor(new MemumCommand());
        
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    ResultSet rs = conn.prepareStatement("SELECT 1").executeQuery();
                    if (!rs.next()) {
                        System.out.println("Database was disconnected. Attempting reconnect...");
                        instantiateConn(true);
                    } else if (!INSTANCE.config.getString("heartbeat").equals("NONE")) {
                        try {
                            URL heartbeat = new URL(INSTANCE.config.getString("heartbeat"));
                            InputStream is = heartbeat.openStream();
                            is.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (SQLException e) {
                    System.out.println("Database was disconnected. Attempting reconnect...");
                    instantiateConn(true);
                }
            }
        };
        timer.schedule(task, 0L, 60000L);
    }
    
    // Fired when plugin is disabled
    @Override
    public void onDisable() {

    }
}
