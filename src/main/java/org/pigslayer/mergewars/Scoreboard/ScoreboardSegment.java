package org.pigslayer.mergewars.Scoreboard;

import org.bukkit.entity.Player;
import org.pigslayer.mergewars.GameFlow.Team.Team;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardSegment {
    public interface ScoreboardSegmentInterface{
        void execute(ScoreboardSegment segment,Team team);
    }

    protected int weight;
    protected List<String> text = new ArrayList<>();
    protected ScoreboardSegmentInterface updateInterface;
    protected Team team;

    protected ScoreboardSegment(ScoreboardManager.SegmentInfo info, Team team) {
        this.weight = info.weight;
        this.updateInterface = info.updateInterface;
        this.team = team;
    }

    protected ScoreboardSegment addText(String text){
        this.text.add(text);

        return this;
    }

    protected void init(){
        text.clear();
        updateInterface.execute(this,team);
    }

    public void update(){
        text.clear();
        updateInterface.execute(this,team);
    }
}
