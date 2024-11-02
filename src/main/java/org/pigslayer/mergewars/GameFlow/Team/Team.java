package org.pigslayer.mergewars.GameFlow.Team;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.pigslayer.mergewars.GameFlow.ChunkManager;

import java.util.*;

public class Team {
    public enum Color{
        RED(Material.REDSTONE_BLOCK,"§c"),
        CYAN(Material.DIAMOND_BLOCK,"§3"),
        GREEN(Material.EMERALD_BLOCK,"§a"),
        BLUE(Material.LAPIS_BLOCK,"§9"),
        PURPLE(Material.AMETHYST_BLOCK,"§5");

        public Material material;
        public String colorSymbol;

        Color(Material material,String colorSymbol){
            this.material = material;
            this.colorSymbol = colorSymbol;
        }
    }

    protected LandMass teamArea;
    protected String name;
    protected Color color = Color.RED;
    protected List<MergePlayer> players;

    public Team(String name, List<Player> players,int areaOffset,World world){
        this.name = name;
        this.players = players.stream().map((p)-> MergePlayer.convert(p,this)).toList();

        teamArea = new LandMass(ChunkManager.highPoints.keySet().toArray(new Chunk[0]), world, areaOffset);
    }

    public List<MergePlayer> getPlayers(){
        return players;
    }

    public LandMass getTeamArea() {
        return teamArea;
    }
}
