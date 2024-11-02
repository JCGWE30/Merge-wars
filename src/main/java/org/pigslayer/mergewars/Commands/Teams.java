package org.pigslayer.mergewars.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.security.KeyPair;
import java.util.*;

public class Teams implements CommandExecutor, TabCompleter {
    protected static HashMap<String,List<UUID>> teams = new HashMap<>();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length==0){
            sender.sendMessage("§cInvalid number of arguments");
            return true;
        }
        try {
            switch (args[0]) {
                case "add":
                    if (!teamAdd(args[1])){
                        sender.sendMessage("§cThat team already exists");
                        break;
                    }
                    sender.sendMessage("§aTeam Created");
                    break;
                case "remove":
                    if (!teamRemove(args[1])){
                        sender.sendMessage("§cThat team does not exist");
                        break;
                    }
                    sender.sendMessage("§aTeam Removed");
                    break;
                case "list":
                    sender.sendMessage(teamList());
                    break;
                case "set":
                    if (!teamSet(args[2],args[1])){
                        sender.sendMessage("§cCannot add player to team");
                        break;
                    }
                    sender.sendMessage("§aPlayer added to team");
                    break;
                case "debug":
                    try {
                        for (int i = 0; i < Integer.parseInt(args[1]); i++) {
                            String team = UUID.randomUUID().toString();
                            teamAdd(team);
                            for(Player p:Bukkit.getOnlinePlayers()){
                                teamSet(team,p.getName());
                            }
                        }
                    }catch(NumberFormatException ignored){
                        sender.sendMessage("§cArgument error");
                    }
                    break;
            }
        }catch(ArrayIndexOutOfBoundsException ignored){
            sender.sendMessage("§cInvalid number of arguments");
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length==1){
            return List.of("add","remove","set","list","debug");
        }
        if(args.length==2){
            switch (args[0]){
                case "remove":
                    return new ArrayList<>(teams.keySet());
                case "set":
                    return null;
            }
        }
        if(args.length==3&& Objects.equals(args[0], "set")){
            return new ArrayList<>(teams.keySet());
        }
        return List.of();
    }

    private boolean teamAdd(String team){
        if(teams.containsKey(team))
            return false;
        teams.put(team,new ArrayList<>());
        return true;
    }

    private boolean teamRemove(String team){
        if(!teams.containsKey(team))
            return false;
        teams.remove(team);
        return true;
    }

    private boolean teamSet(String team, String player){
        Player p = Bukkit.getPlayer(player);

        if(p==null)
            return false;
        if(!teams.containsKey(team))
            return false;
        if(teams.get(team).contains(p.getUniqueId()))
            return false;

        for(List<UUID> list:teams.values()){
            list.remove(p.getUniqueId());
        }

        teams.get(team).add(p.getUniqueId());

        return true;
    }

    private String teamList(){
        StringBuilder string = new StringBuilder();
        for(Map.Entry<String,List<UUID>> entry:teams.entrySet()){
            string.append(String.format("§e%s members:\n",entry.getKey()));
            for(UUID uid: entry.getValue()){
                Player p = Bukkit.getPlayer(uid);
                if(p==null)
                    throw new RuntimeException("A player that should not be null is null");
                string.append(String.format("§a%s\n",p.getName()));
            }
            string.append("\n");
        }
        return string.toString();
    }
}
