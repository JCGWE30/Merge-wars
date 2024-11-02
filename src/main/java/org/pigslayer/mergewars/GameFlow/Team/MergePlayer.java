package org.pigslayer.mergewars.GameFlow.Team;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class MergePlayer{
    private static final HashMap<UUID,MergePlayer> playerCache = new HashMap<UUID,MergePlayer>();

    private Player player;
    private Team team;
    private int lives = 3;

    public MergePlayer(Player p, Team t) {
        this.player = p;
        this.team = t;
    }
    
    public static MergePlayer convert(Player p,Team t){
        return Optional.ofNullable(playerCache.get(p.getUniqueId())).orElseGet(
                () ->{
                    MergePlayer mergePlayer = new MergePlayer(p,t);
                    playerCache.put(p.getUniqueId(), mergePlayer);
                    return mergePlayer;
                }
        );
    }

    public static MergePlayer get(Player p){
        return playerCache.get(p.getUniqueId());
    }

    public Team getTeam() {
        return team;
    }

    public int getLives() {
        return lives;
    }

    public Player getPlayer() {
        return player;
    }
}
