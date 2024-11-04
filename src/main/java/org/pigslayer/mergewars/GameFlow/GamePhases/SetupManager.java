package org.pigslayer.mergewars.GameFlow.GamePhases;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.pigslayer.mergewars.Constants;
import org.pigslayer.mergewars.GameFlow.ChunkManager;
import org.pigslayer.mergewars.GameFlow.Team.LandMass;
import org.pigslayer.mergewars.GameFlow.Team.MergePlayer;
import org.pigslayer.mergewars.GameFlow.Team.Team;
import org.pigslayer.mergewars.MergeWars;
import org.pigslayer.mergewars.Utils.ChunkNumberProjector;
import org.pigslayer.mergewars.Utils.ChunkUtils;
import org.pigslayer.mergewars.Utils.ItemGenerator;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SetupManager implements Listener {
    public static class TeamSetupState{
        public HashMap<Player, Chunk> selectedChunks = new HashMap<>();
        public List<Player> lockedPlayers = new ArrayList<>();
        public List<Player> confirmedPlayers = new ArrayList<>();
        public Chunk confirmedChunk;
        private int playerCount;
        private Team team;
        private LandMass landMass;
        private boolean isConfirmed;

        public TeamSetupState(Team team){
            this.playerCount = team.getPlayers().size();
            this.team = team;
            this.landMass = team.getLandMass();
        }

        public boolean isLocked(Player chunkOwner) {
            return lockedPlayers.contains(chunkOwner);
        }

        public boolean isFullConfirmed(){
            return confirmedPlayers.size() >= playerCount;
        }

        public boolean isConfirmed(Player chunkOwner) {
            return confirmedPlayers.contains(chunkOwner);
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

    private static final ItemGenerator NOT_LOCKED = new ItemGenerator("§eShift to select a chunk",1,Material.RED_STAINED_GLASS_PANE);
    private static final ItemGenerator LOCKED = new ItemGenerator("§eRight click to confirm",1,Material.YELLOW_STAINED_GLASS_PANE);
    private static final ItemGenerator CONFIRMED = new ItemGenerator("§3Selection Confirmed",1,Material.GREEN_STAINED_GLASS_PANE);

    private List<UUID> claimedChunks = new ArrayList<>();
    private static SetupManager instance;
    private boolean inSetup = false;
    private HashMap<Team,TeamSetupState> teamSetupStates = new HashMap<>();
    private long setupEnd;

    public SetupManager() {
        instance = this;
    }

    public static void runSetup(List<Team> teams){
        instance.setupEnd = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);

        for(Team t:teams){
            t.setupScoreboard();

            instance.teamSetupStates.put(t,new TeamSetupState(t));

            LandMass area = t.getLandMass();
            area.loadMass();
            for(MergePlayer p:t.getPlayers()){
                AttributeInstance attribute = p.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                attribute.setBaseValue(2f);

                area.enterMass(p.getPlayer(),true);
            }
            area.setCeilingState(true);
        }

        MergeWars.runLater(new BukkitRunnable() {
            @Override
            public void run() {
                instance.inSetup = true;

                MergeWars.runTimer(new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(System.currentTimeMillis() >= instance.setupEnd){
                            this.cancel();
                            instance.inSetup=false;
                            SetupManager.instance.endSetup();
                        }

                        if(!instance.inSetup)
                            this.cancel();

                        for(Team t:teams){
                            t.updateScoreboard();
                        }
                    }
                },1,20);
            }
        },20);


    }

    private void endSetup(){
        for(TeamSetupState state:teamSetupStates.values()){
            if(!state.isConfirmed){
                state.confirmedChunk = state.landMass.getRandomChunk();
            }

            Team team = state.team;

            for(UUID chunk:claimedChunks){
                forceChunkReset(team,state.landMass.getChunk(chunk));
            }
            for(Chunk chunk:state.selectedChunks.values()){
                forceChunkReset(team,chunk);
            }

            team.getLandMass().setCeilingState(false);
            for(MergePlayer p:team.getPlayers()){
                AttributeInstance attribute = p.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                attribute.setBaseValue(0.1f);
                p.getPlayer().sendTitle(
                        Constants.MERGE_TITLE,
                        "§aBEGIN!!!"
                );
            }
        }
    }

    private TeamSetupState getState(Player player){
        MergePlayer mp = MergePlayer.get(player);
        return teamSetupStates.get(mp.getTeam());
    }

    public static TeamSetupState getState(Team team){
        return instance.teamSetupStates.get(team);
    }

    private Team getTeam(Player player){
        return MergePlayer.get(player).getTeam();
    }

    public static long getTimeLeft(){
        return instance.setupEnd-System.currentTimeMillis();
    }

    @EventHandler
    private void attemptPlace(BlockPlaceEvent e){
        if(inSetup)
            e.setCancelled(true);
    }

    @EventHandler
    private void sneak(PlayerToggleSneakEvent e){
        if(!inSetup) return;

        if(!e.isSneaking()) return;

        TeamSetupState state = getState(e.getPlayer());

        if(state.isConfirmed(e.getPlayer())) return;

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

        if(!getTeam(e.getPlayer()).getLandMass().isInArea(e.getPlayer())) return;

        String actionBarText;

        if(!state.isLocked(e.getPlayer())){
            actionBarText = "§eCrouch to select a chunk";
        }else if(!state.isConfirmed(e.getPlayer())){
            actionBarText = "§eLeft Click to confirm your selection";
        }else{
            actionBarText = "§eSelection Confirmed";
        }

        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(actionBarText));

        if(state.isLocked(e.getPlayer())) return;

        updateChunk(e.getPlayer(),e.getPlayer().getLocation().getChunk());
    }

    @EventHandler
    private void interact(PlayerInteractEvent e){
        if(!inSetup) return;

        TeamSetupState state = getState(e.getPlayer());

        if(state.isConfirmed(e.getPlayer())) return;

        if(!state.isLocked(e.getPlayer())) return;

        if(state.isConfirmed(e.getPlayer())){
            state.confirmedPlayers.remove(e.getPlayer());
        }else{
            state.confirmedPlayers.add(e.getPlayer());
            if(state.isFullConfirmed())
                confirmChunk(state);
        }

        state.team.updateScoreboard();
    }

    private void confirmChunk(TeamSetupState state){
        state.isConfirmed = true;

        Chunk chunk = getConfirmedChunk(state);

        state.confirmedChunk = chunk;

        UUID chunkId = state.team.getLandMass().getId(chunk);

        claimedChunks.add(chunkId);

        List<Chunk> chunks = state.selectedChunks.values().stream().toList();
        state.selectedChunks.clear();

        for(Chunk c:chunks){
            reloadChunk(state.team,c);
        }

        for(Team t:teamSetupStates.keySet()){
            checkConfirmed(t,chunkId);
        }

        if(claimedChunks.size()==teamSetupStates.size()){
            setupEnd = Math.min(setupEnd,System.currentTimeMillis()+TimeUnit.SECONDS.toMillis(10));
        }
    }

    private Chunk getConfirmedChunk(TeamSetupState state){
        Map<Chunk, Long> chunkFrequency = state.selectedChunks.values().stream()
                .collect(Collectors.groupingBy(chunk -> chunk, Collectors.counting()));

        long highestValue = Collections.max(chunkFrequency.values());

        List<Chunk> topChunks = chunkFrequency.entrySet().stream()
                .filter(e -> e.getValue() == highestValue)
                .map(Map.Entry::getKey)
                .toList();

        Random random = new Random();
        return topChunks.get(random.nextInt(topChunks.size()));
    }

    private void checkConfirmed(Team team,UUID newChunk){
        TeamSetupState state = getState(team);
        reloadChunk(team,state.landMass.getChunk(newChunk));
        if(state.isConfirmed) return;

        for(Map.Entry<Player, Chunk> entry:state.selectedChunks.entrySet()){
            UUID chunkId = state.landMass.getId(entry.getValue());

            if(claimedChunks.contains(chunkId)){
                List<Player> owners = state.selectedChunks.entrySet().stream()
                        .filter(e->e.getValue()==entry.getValue())
                        .map(Map.Entry::getKey)
                        .toList();
                for(Player p:owners){
                    if(state.isLocked(p)){
                        state.confirmedPlayers.remove(p);
                        unlockPlayer(p);
                    }
                }
            }
        }
    }

    private void updateChunk(Player player,Chunk chunk){
        TeamSetupState state = getState(player);
        Chunk oldChunk = state.selectedChunks.get(player);

        state.selectedChunks.put(player,chunk);

        if(oldChunk!=null)
            reloadChunk(getTeam(player),oldChunk);
        reloadChunk(getTeam(player),chunk);
    }

    private void lockPlayer(Player p){
        TeamSetupState state = getState(p);

        if(claimedChunks.contains(state.landMass.getId(p.getLocation().getChunk()))) return;

        state.lockedPlayers.add(p);

        updateChunk(p,p.getLocation().getChunk());
    }

    private void unlockPlayer(Player p){
        TeamSetupState state = getState(p);

        state.lockedPlayers.remove(p);

        updateChunk(p,p.getLocation().getChunk());
    }

    private void reloadChunk(Team team,Chunk chunk){
        TeamSetupState teamState = getState(team);

        List<Player> chunkOwners = getState(team).selectedChunks.entrySet().stream()
                .filter(e-> ChunkUtils.matchChunk(chunk,e.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        boolean isShowing = !chunkOwners.isEmpty();

        int owners = chunkOwners.stream()
                .filter(p->teamState.isLocked(p))
                .toList().size();

        boolean isLocked = isShowing && teamState.isLocked(chunkOwners);

        boolean isConfirmed = claimedChunks.contains(teamState.landMass.getId(chunk));

        Material type = isLocked ? Material.DIAMOND_BLOCK : Material.BLUE_CONCRETE;

        if(isConfirmed){
            type = Material.BEDROCK;
        }

        Block[] changeBlocks = ChunkManager.getSurfaceMap(team.getLandMass().getId(chunk),chunk);
        List<BlockState> fullStates = new ArrayList<>();
        List<BlockState> emptyStates = new ArrayList<>();

        for(Block b:changeBlocks){
            emptyStates.add(b.getState());

            BlockState state = b.getState();
            state.setType(type);
            fullStates.add(state);
        }

        UUID chunkId = team.getLandMass().getId(chunk);

        for(MergePlayer p:team.getPlayers()){
            Player player = p.getPlayer();

            boolean isVisible = isConfirmed || (isShowing && (chunkOwners.contains(player)||isLocked||getState(player).isLocked(player)));

            if(isVisible){
                if(owners>1) {
                    BlockState[] states = ChunkNumberProjector.getWriting(chunk, owners, chunkId);
                    player.sendBlockChanges(List.of(states));
                }else{
                    player.sendBlockChanges(List.of(ChunkNumberProjector.resetWriting(chunk,chunkId)));
                }
                player.sendBlockChanges(fullStates);
            }else{
                player.sendBlockChanges(List.of(ChunkNumberProjector.resetWriting(chunk,chunkId)));
                player.sendBlockChanges(emptyStates);
            }
        }
    }

    private void forceChunkReset(Team team,Chunk chunk){
        TeamSetupState teamState = getState(team);

        Block[] changeBlocks = ChunkManager.getSurfaceMap(team.getLandMass().getId(chunk),chunk);
        List<BlockState> states = new ArrayList<>();

        for(Block b:changeBlocks){
            states.add(b.getState());
        }

        for(MergePlayer p:team.getPlayers()){
            Player player = p.getPlayer();
            player.sendBlockChanges(states);
        }
    }
}
