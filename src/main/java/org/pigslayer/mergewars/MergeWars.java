package org.pigslayer.mergewars;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.pigslayer.mergewars.Commands.StartGame;
import org.pigslayer.mergewars.Commands.Teams;
import org.pigslayer.mergewars.GameFlow.ChunkManager;
import org.pigslayer.mergewars.GameFlow.GameManager;
import org.pigslayer.mergewars.GameFlow.GamePhases.SetupManager;

import java.lang.reflect.InvocationTargetException;

public final class MergeWars extends JavaPlugin {

    private static MergeWars plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new SetupManager(),this);

        setDualCommand("teams", Teams.class);
        setCommand("startgame", StartGame.class);
        GameManager.initialize();
        ChunkManager.cacheChunks();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    private <T extends CommandExecutor & TabCompleter> void setDualCommand(String command, Class<T> clazz){
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            getCommand(command).setTabCompleter(instance);
            getCommand(command).setExecutor(instance);
        }catch(NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | NullPointerException e){
            throw new RuntimeException(e);
        }
    }

    private <T extends CommandExecutor> void setCommand(String command, Class<T> clazz){
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            getCommand(command).setExecutor(instance);
        }catch(NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | NullPointerException e){
            throw new RuntimeException(e);
        }
    }

    public static void runTimer(BukkitRunnable runnable, long delay, long period){
        runnable.runTaskTimer(plugin,delay,period);
    }

    public static void runLater(BukkitRunnable runnable,long delay){
        runnable.runTaskLater(plugin,delay);
    }
}
