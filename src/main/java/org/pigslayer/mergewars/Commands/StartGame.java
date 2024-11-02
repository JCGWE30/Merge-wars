package org.pigslayer.mergewars.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.pigslayer.mergewars.GameFlow.GameManager;

public class StartGame implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameManager.startGame(Teams.teams);
        return true;
    }
}
