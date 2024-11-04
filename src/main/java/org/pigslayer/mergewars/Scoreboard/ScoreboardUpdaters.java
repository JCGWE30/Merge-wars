package org.pigslayer.mergewars.Scoreboard;

import org.pigslayer.mergewars.GameFlow.GameManager;
import org.pigslayer.mergewars.GameFlow.GamePhases.SetupManager;
import org.pigslayer.mergewars.GameFlow.Team.Team;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreboardUpdaters {
    protected static void updateTeams(ScoreboardSegment segment, Team team){
        for (Team t : GameManager.getActiveTeams()) {
            segment.addText(t.color.colorSymbol+t.name);
        }
    }

    protected static void updateEventTimer(ScoreboardSegment segment, Team team){
        Date date = new Date(SetupManager.getTimeLeft());
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        String formattedTime = formatter.format(date);

        segment.addText("§eSetup End in:");
        segment.addText("§7"+formattedTime);
    }

    protected static void updateSetupDisplay(ScoreboardSegment segment, Team team){
        SetupManager.TeamSetupState state = SetupManager.getState(team);
        if(state==null) return;

        int confirmed = state.confirmedPlayers.size();

        if(confirmed==team.getPlayers().size()){
            segment.addText("§eWaiting for setup")
                    .addText("§eto end");
        }else{
            segment.addText("§eConfirmed Players");
            segment.addText("§7"+confirmed+"/"+team.getPlayers().size());
        }
    }
}
