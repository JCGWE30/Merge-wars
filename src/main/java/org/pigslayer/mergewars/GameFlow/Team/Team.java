package org.pigslayer.mergewars.GameFlow.Team;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.pigslayer.mergewars.GameFlow.ChunkManager;
import org.pigslayer.mergewars.Scoreboard.ScoreboardManager;

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
    public String name;
    public Color color = Color.RED;

    protected LandMass teamArea;
    protected List<MergePlayer> players;
    protected ScoreboardManager scoreboard;

    public Team(String name, List<Player> players,int areaOffset,World world){
        this.name = name;
        this.players = players.stream().map((p)-> MergePlayer.convert(p,this)).toList();

        scoreboard = new ScoreboardManager(this);
        teamArea = new LandMass(ChunkManager.chunkMap.keySet().toArray(new Chunk[0]), world, areaOffset);
    }

    public List<MergePlayer> getPlayers(){
        return players;
    }

    public LandMass getLandMass() {
        return teamArea;
    }

    public void setupScoreboard() {
        scoreboard.initScoreboard();
    }

    public void updateScoreboard(){
        scoreboard.reloadScoreboard();
    }
}
