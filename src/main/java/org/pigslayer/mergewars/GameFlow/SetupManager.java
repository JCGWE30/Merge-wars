package org.pigslayer.mergewars.GameFlow;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.pigslayer.mergewars.GameFlow.Team.LandMass;
import org.pigslayer.mergewars.GameFlow.Team.MergePlayer;
import org.pigslayer.mergewars.GameFlow.Team.ParrelelChunk;
import org.pigslayer.mergewars.GameFlow.Team.Team;
import org.pigslayer.mergewars.Utils.ChunkUtils;

import java.util.*;

/*
Setup Order
1 - Move around and see only your chunk
2 - Lock in your chunk, you chunk will stop moving, see others chunks
3 - Confirm your chunk, it will turn solid
 */

public class SetupManager implements Listener {
    private static class TeamSetupState{
        public HashMap<Player,ParrelelChunk> selectedChunks = new HashMap<>();
        public List<Player> lockedPlayers = new ArrayList<>();

        public boolean isLocked(Player chunkOwner) {
            return lockedPlayers.contains(chunkOwner);
        }

        public boolean isLocked(List<Player> players){
            for(Player p : players){
                if(isLocked(p)){
                    return true;
                }
            }
            return false;
        }
    }

    private static SetupManager instance;
    private boolean inSetup = false;
    private HashMap<Team,TeamSetupState> teamSetupStates = new HashMap<>();

    public SetupManager() {
        instance = this;
    }

    protected static void runSetup(List<Team> teams){
        instance.inSetup = true;

        for(Team t:teams){
            instance.teamSetupStates.put(t,new TeamSetupState());

            LandMass area = t.getTeamArea();
            area.loadMass();
            for(MergePlayer p:t.getPlayers()){
                AttributeInstance attribute = p.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                attribute.setBaseValue(2f);

                area.enterMass(p.getPlayer(),true);
            }
            area.setCeilingState(true);
        }
    }

    private TeamSetupState getState(Player player){
        MergePlayer mp = MergePlayer.get(player);
        return teamSetupStates.get(mp.getTeam());
    }

    private TeamSetupState getState(Team team){
        return teamSetupStates.get(team);
    }

    private Team getTeam(Player player){
        return MergePlayer.get(player).getTeam();
    }

    @EventHandler
    private void sneak(PlayerToggleSneakEvent e){
        if(!e.isSneaking()) return;

        TeamSetupState state = getState(e.getPlayer());

        if(state.lockedPlayers.contains(e.getPlayer())){
            unlockPlayer(e.getPlayer());
        }else{
            lockPlayer(e.getPlayer());
        }
    }

    @EventHandler
    private void move(PlayerMoveEvent e){
        if(!inSetup) return;

        TeamSetupState state = getState(e.getPlayer());

        if(state.isLocked(e.getPlayer())) return;

        Block b = e.getPlayer().getLocation().getBlock();

        ParrelelChunk selectedChunk = ParrelelChunk.convert(b.getChunk());

        updateChunk(e.getPlayer(),selectedChunk);
    }

    private void updateChunk(Player player,ParrelelChunk chunk){
        TeamSetupState state = getState(player);
        ParrelelChunk oldChunk = state.selectedChunks.get(player);

        state.selectedChunks.put(player,chunk);

        reloadChunk(getTeam(player),oldChunk);
        reloadChunk(getTeam(player),chunk);
    }

    private void lockPlayer(Player p){
        TeamSetupState state = getState(p);

        state.lockedPlayers.add(p);

        ParrelelChunk pChunk = ParrelelChunk.convert(p.getLocation().getChunk());

        reloadChunk(getTeam(p),pChunk);
        //Update Hotbar here
    }

    private void unlockPlayer(Player p){
        TeamSetupState state = getState(p);

        state.lockedPlayers.remove(p);

        ParrelelChunk pChunk = ParrelelChunk.convert(p.getLocation().getChunk());

        reloadChunk(getTeam(p),pChunk);
        //Update Hotbar here
    }

    private void reloadChunk(Team team,ParrelelChunk chunk){

        List<Player> chunkOwners = getState(team).selectedChunks.entrySet().stream()
                .filter(e->e.getValue()==chunk)
                .map(Map.Entry::getKey)
                .toList();

        boolean isShowing = !chunkOwners.isEmpty();

        boolean isLocked = isShowing && getState(team).isLocked(chunkOwners);

        Material type = isLocked ? Material.DIAMOND_BLOCK : Material.BLUE_CONCRETE;

        Block[] changeBlocks = ChunkManager.getSurfaceMap(chunk);
        List<BlockState> fullStates = new ArrayList<>();
        List<BlockState> emptyStates = new ArrayList<>();

        for(Block b:changeBlocks){
            emptyStates.add(b.getState());

            BlockState state = b.getState();
            state.setType(type);
            fullStates.add(state);
        }

        for(MergePlayer p:team.getPlayers()){
            Player player = p.getPlayer();

            boolean isVisible = isShowing && (chunkOwners.contains(player)||isLocked||getState(player).isLocked(player));

            if(isVisible){
                player.sendBlockChanges(fullStates);
            }else{
                player.sendBlockChanges(emptyStates);
            }
        }
    }
}
