package base

import java.io.FileWriter
import java.io.IOException
import java.util.List

class Pit {

    int teamNumber
    String teamName
    String scoutName

    boolean level2Climb, level3Climb

    String intakeType // Hatch Cargo Both

    int rocketLevel // 1 2 3

    int mechIssues

    boolean cam, preset, sense, reach, ramp

    String[] nicknames

    int startHab // 1 2

    boolean driverControl, pathing, noControl // auto

    List<String> autoStrats = []

    String autoNotes

    boolean prefHatch, prefCargo

    int ppm //pieces per match

    List<String> teleStrats = []

    int cycleTime

    String teleNotes

    String hpPref // Integral Ideal Unnecessary

    String stratPref // Strong Preferred Flexible

    String notes

    Pit(List<String> csvRow) {
        //TODO match to vars

        Lib.savePit(this, Main.currentSession.eventDir)
    }

    String getName() {
        return "${this.teamNumber}-${this.scoutName}"
    }

    void s3ndToTxt(String directory) {
        FileWriter writer = null
        try {
            writer = new FileWriter("${directory}/${this.getName()}.txt", false)
        } catch (IOException e) {
            Lib.report("File not found error")
            System.out.println(e)
        }

        try {
            writer.write(String.format(
                "Scout name: %s\n" +
                "Team Number: %-4d  Team Name: %s\n" +
                "Nicknames: %s\n" +
                "-----------------------------------------------------------------------\n\n" +
                "AUTO STRATEGY:\n" +
                "    Starts on: %d\n" +
                "    Controlled by...\n" +
                "        Driver? %b\n" +
                "        Paths? %b\n" +
                "        Nothing? %b\n" +
                "    Strategies:\n" +
                "%s\n" +
                "    Notes:\n" +
                "         %s\n" +
                "\n\nTELEOP STRATEGY:\n" +
                "    Prefers: %s\n" +
                "    Game Pieces Per Match: %d    Average Cycle Time (sec): %d\n" +
                "    Strategies:\n" +
                "%s\n" +
                "    Notes:\n" +
                "         %s\n" +
                "\n\nGENERAL:\n" +
                "    Climbs Level 2: %b  Climbs Level 3: %b\n" +
                "    Reaches Level: %d      Intake: %s\n" +
                "    Has...\n" +
                "        Camera? %b\n" +
                "        LimeLight/Sensor? %b\n" +
                "        Presets? %b\n" +
                "        Over-Cargo Reach? %b\n" +
                "        Ramp? %b\n" +
                "    Mechanical issues (1-10): %d\n" +
                "    Human Player Is: %s\n" +
                "    Strategy Is: %s\n" +
                "    Notes:\n" +
                "         %s\n",
                this.scoutName,
                this.teamNumber, this.teamName,
                Lib.arrayToString(this.nicknames),//.substring(1,Lib.arrayToString(this.nicknames).length()-2),
                this.startHab,
                this.driverControl,
                this.pathing,
                this.noControl,
                Lib.arrayToLinebreakString(this.autoStrats.toArray(), "         "),
                this.autoNotes,

                (
                    (this.prefCargo && this.prefHatch) ? "Both" :
                        (this.prefCargo ? "Cargo" : (this.prefHatch ? "Hatch" : "None"))
                ),
                this.ppm, this.cycleTime,
                Lib.arrayToLinebreakString(this.teleStrats.toArray(), "         "),
                this.teleNotes,

                this.level2Climb, this.level3Climb,
                this.rocketLevel, this.intakeType,
                this.cam,
                this.sense,
                this.preset,
                this.reach,
                this.ramp,
                this.mechIssues,
                this.hpPref,
                this.stratPref,
                this.notes
            ))

            writer.close()

        } catch (IOException ignored){ }
    }

}