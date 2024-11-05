package org.pigslayer.mergewars.Scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.pigslayer.mergewars.Constants;
import org.pigslayer.mergewars.GameFlow.Team.MergePlayer;
import org.pigslayer.mergewars.GameFlow.Team.Team;

import java.util.*;

public class ScoreboardManager {
    public static class SegmentInfo{
        int weight;
        ScoreboardSegment.ScoreboardSegmentInterface updateInterface;

        public SegmentInfo(int weight,ScoreboardSegment.ScoreboardSegmentInterface updateInterface){
            this.weight = weight;
            this.updateInterface = updateInterface;
        }
    }
    public static final SegmentInfo SCOREBOARD_TEAMS = new SegmentInfo(0,ScoreboardUpdaters::updateTeams);
    public static final SegmentInfo EVENT_TIMER = new SegmentInfo(1,ScoreboardUpdaters::updateEventTimer);
    public static final SegmentInfo SETUP_STATUS = new SegmentInfo(2,ScoreboardUpdaters::updateSetupDisplay);

    private static final SegmentInfo[] TOTAL_SEGMENTS = {SCOREBOARD_TEAMS, EVENT_TIMER, SETUP_STATUS};
    private static final String[] POSITION_MARKERS = {"§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f", "§k", "§l", "§m", "§n", "§o", "§r",};

    private int length;
    private List<String> currentState = new ArrayList<>();
    private List<Player> observers = new ArrayList<>();
    private Team team;

    public ScoreboardManager(Team team) {
        this.team = team;
        observers.addAll(team.getPlayers().stream().map(MergePlayer::getPlayer).toList());
    }

    public void initScoreboard() {

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(Constants.MERGE_TITLE, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        currentState = getSegments();
        length = currentState.size();

        for(int i = 0; i< currentState.size(); i++) {
            objective.getScore(currentState.get(i)).setScore(i);
        }

        for (Player player : observers) {
            player.setScoreboard(scoreboard);
        }
    }

    public void reloadScoreboard() {
        List<String> text = getSegments();

        length = Math.max(length,text.size());

        for(Player p : observers) {
            Objective obj = p.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
            for(int i = 0; i < length; i++) {

                if (i < currentState.size() && !Objects.equals(currentState.get(i), text.get(i)))
                    p.getScoreboard().resetScores(currentState.get(i));

                obj.getScore(text.get(i)).setScore(i);
            }
        }

        currentState = text;
        length = text.size();
    }

    private List<String> getSegments(){
        List<SegmentInfo> segmentInfo = new ArrayList<>(List.of(TOTAL_SEGMENTS));
        segmentInfo.sort(Comparator.comparingInt((s) -> s.weight));

        List<String> values = new ArrayList<>();

        for(int i = 0; i< segmentInfo.size(); i++) {
            ScoreboardSegment segment = new ScoreboardSegment(segmentInfo.get(i),team);
            segment.init();

            for(String text:segment.text) {
                values.add(POSITION_MARKERS[i]+"§r"+text);
            }

            if(i<segmentInfo.size()-1) {
                values.add(POSITION_MARKERS[i]);
            }
        }

        return values.reversed();
    }
}
