package org.pigslayer.mergewars.Scoreboard;

import org.bukkit.entity.Player;
import org.pigslayer.mergewars.GameFlow.GameManager;
import org.pigslayer.mergewars.GameFlow.SetupManager;
import org.pigslayer.mergewars.GameFlow.Team.Team;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
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
        segment.addText("§eConfirmed Players");
        segment.addText("§72/4");
    }
}
