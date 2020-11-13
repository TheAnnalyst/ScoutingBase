package base

import java.util.HashMap

import com.cpjd.models.matches.Match
import com.cpjd.utils.exceptions.DataNotFoundException

@CompileStatic
class CustomMatch {

    private int teamNum
    private String matchType
    private int matchNum
    private String alliancePosition
    private boolean isBlueAlliance

    private int HPShipGame
    private int HPShipSand
    private int HPRocketGame
    private int HPRocketSand
    private int HPDropGame
    private int HPDropSand

    private int CShipGame
    private int CShipSand
    private int CRocketGame
    private int CRocketSand
    private int CDropGame
    private int CDropSand

    private int scaleLevel
    private boolean isHelp = false

    private int startHab
    private boolean crossedLine = false

    private Long fouls
    private Long techs
    private boolean yellow
    private boolean red
    private boolean eStopped
    private boolean borked

    private int points
    private int nonFoulPoints = 0
    private int rankingPoints = 0
    private boolean rRocket = false, lRocket = false, habRP = false, crRP = false

    HashMap<String, Object> scoreBreakdown
    private boolean tbaSynced = false

    private String matchNotes


    CustomMatch(String[] csvRow) {
        this.matchType = csvRow[0]
        this.matchNum = Integer.valueOf(csvRow[1])
        this.teamNum = Integer.valueOf(csvRow[2])

        // I do not know what this debug print is for
        System.out.println(csvRow[3].charAt(0))

        this.alliancePosition = csvRow[3]
        this.isBlueAlliance = this.alliancePosition.startsWith('B')

        this.startHab = Integer.valueOf(csvRow[4])

        this.CShipSand = Integer.valueOf(csvRow[5])
        this.CRocketSand = Integer.valueOf(csvRow[6])
        this.CDropSand = Integer.valueOf(csvRow[7])
        this.HPShipSand = Integer.valueOf(csvRow[8])
        this.HPRocketSand = Integer.valueOf(csvRow[9])
        this.HPDropSand = Integer.valueOf(csvRow[10])

        this.CShipGame = Integer.valueOf(csvRow[11])
        this.CRocketGame = Integer.valueOf(csvRow[12])
        this.CDropGame = Integer.valueOf(csvRow[13])
        this.HPShipGame = Integer.valueOf(csvRow[14])
        this.HPRocketGame = Integer.valueOf(csvRow[15])
        this.HPDropGame = Integer.valueOf(csvRow[16])

        try {
            this.scaleLevel = Integer.valueOf(csvRow[17])
        } catch(NumberFormatException e) {
            Lib.report("Scale Level is not a number. Setting to 0.")
            this.scaleLevel = 0
        }

        this.techs = Long.valueOf(csvRow[18])
        this.fouls = Long.valueOf(csvRow[19])
        this.yellow = csvRow[20].equalsIgnoreCase("true")
        this.red = csvRow[21].equalsIgnoreCase("true")
        this.borked = csvRow[22].equalsIgnoreCase("true")
        this.points = Integer.valueOf(csvRow[23])

        this.matchNotes = csvRow[24]

        this.matchNotes = this.matchNotes.replace("\n", ".  ")

        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    public int sandPlaces() {
        HPShipSand + HPRocketSand + CShipSand + CRocketSand
    }

    public void syncTBA() {
        Match foundMatch = new Match()
        try {
            foundMatch = Main.tbaApi.getMatch("${Main.currentSession.tbaEventKey}_qm${this.matchNum}")
        } catch(DataNotFoundException e) {
            System.out.println("Match not found. \n${e}")
            return
        }

        //get the correct points TODO find a better way to do blue vs red
        int blueScore = foundMatch.blue.getScore()getBlueScoreBreakdow
        if (this.points != blueScore && this.isBlueAlliance) {
            Lib.report(
                "Scouted points for match ${this.matchNum}, Blue Alliance are incorrect. Scouted points: ${this.points} TBA points: ${blueScore}"
            )
            this.points = blueScore
        }

        int redScore = foundMatch.red.getScore();
        if (this.points != redScore && this.isRedAlliance) {
            Lib.report(
                "Scouted points for match ${this.matchNum}, Red Alliance are incorrect. Scouted points: ${this.points} TBA points: ${redScore}"
            )
            this.points = redScore
        }

        //get the big fancy hashmap
        if (this.isBlueAlliance) {
            this.scoreBreakdown = foundMatch.getBlueScoreBreakdown()
        } else {
            this.scoreBreakdown = foundMatch.getRedScoreBreakdown()
        }

        System.out.println(this.scoreBreakdown)

        //FIXME get hekd i guess

//        //sync fouls
//        System.out.println(this.fouls)
//        System.out.println(scoreBreakdown.get("foulCount"))
//        if(this.fouls > (Long)scoreBreakdown.get("foulCount")) {
//            Lib.report(String.format("Fouls for match %d, alliance position %s are more than the total fouls. %nReported fouls: %d%nTotal fouls: %d",
//                         this.matchNum, this.alliancePosition, this.fouls, this.scoreBreakdown.get("foulCount")))
//            this.fouls = (Long)scoreBreakdown.get("foulCount")
//        }
//
//        //sync tech fouls
//        if(this.techs > (Long)scoreBreakdown.get("techFoulCount")) {
//            Lib.report(String.format("Tech fouls for match %d, alliance position %s are more than the total tech fouls. %nReported tech fouls: %d%nTotal fouls: %d",
//                        this.matchNum, this.alliancePosition, this.techs, this.scoreBreakdown.get("techFoulCount")))
//            this.techs = (Long)scoreBreakdown.get("techFoulCount")
//        }

        //get the points excluding points from fouls
        this.nonFoulPoints = this.points - this.scoreBreakdown.get("foulPoints").intValue()

        int alPos = Character.getNumericValue(
            this.alliancePosition.charAt(this.alliancePosition.length() - 1)
        )
        //the starting hab
        int habChar = Character.getNumericValue(this.scoreBreakdown.get("preMatchLevelRobot" + alPos).toString().charAt(8))
        if (this.startHab != habChar) {
            Lib.report("Start level for match ${this.matchNum}, alliance position ${this.alliancePosition} is incorrect. \nReported level: ${this.startHab}\nTBA level: ${habChar}")
            this.startHab = habChar
        }

        //get the number of ranking points
        this.rankingPoints = this.scoreBreakdown.get("rp").intValue()

//        this.rRocket = (boolean)this.scoreBreakdown.get("completedRocketNear")
//        this.lRocket = (boolean)this.scoreBreakdown.get("completedRocketFar")

        this.habRP = this.scoreBreakdown.get("habDockingRankingPoint")
        this.crRP = this.scoreBreakdown.get("completeRocketRankingPoint")

        //TODO add more?

        this.tbaSynced = true

        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    String toReadableString() {
        return String.format("Team Number: %d, Match Type: %s, Match Number: %d, Position: %s, isBlue: %b, "
                                +"HPShipGame: %d, HPShipSand: %d, HPRocketGame: %d, HPRocketSand: %d, HPDropGame: %d, "
                                +"HPDropSand: %d, CShipGame: %d, CShipSand: %d, CRocketGame: %d, CRocketSand: %d, CDropGame: %d, "
                                +"CDropSand: %d, Scale Level: %d, Is a Helper: %b, Starting Level: %d, Crossed the Line: %b, Fouls: %d, Tech Fouls: %d, Yellow Card: %b, "
                                +"Red Card: %b, Emergency Stop: %b, Broken: %b, Total Points: %d, Points w/o Penalties: %d, "
                                +"Ranking Points: %d, Filled Right Rocket: %b, Filled Left Rocket: %b, Hab Docking RP: %b, "
                                +"Rocket RP: %b, Synced? %b, Notes: %s%n",
                                this.teamNum, this.matchType, this.matchNum, this.alliancePosition, this.isBlueAlliance,
                                this.HPShipGame, this.HPShipSand, this.HPRocketGame, this.HPRocketSand, this.HPDropGame,
                                this.HPDropSand, this.CShipGame, this.CShipSand, this.CRocketGame, this.CRocketSand, this.CDropGame,
                                this.CDropSand, this.scaleLevel, this.isHelp, this.startHab, this.crossedLine, this.fouls, this.techs, this.yellow,
                                this.red, this.eStopped, this.borked, this.points, this.nonFoulPoints,
                                this.rankingPoints, this.rRocket, this.lRocket, this.habRP,
                                this.crRP, this.tbaSynced, this.matchNotes)
    }

    @Override
    String toString() {
        def commaJoinParts = [
            this.teamNum, this.matchType, this.matchNum, this.alliancePosition,
            this.isBlueAlliance, this.HPShipGame, this.HPShipSand, this.HPRocketGame,
            this.HPRocketSand, this.HPDropGame, this.HPDropSand, this.CShipGame,
            this.CShipSand, this.CRocketGame, this.CRocketSand, this.CRocketGame,
            this.CRocketSand, this.CDropGame, this.CDropSand, this.scaleLevel, this.isHelp,
            this.startHab, this.crossedLine, this.fouls, this.techs, this.yellow,
            this.red, this.eStopped, this.borked, this.points, this.nonFoulPoints,
            this.rankingPoints, this.rRocket, this.lRocket, this.habRP, this.crRP, this.tbaSynced
        ]
        return commaJoinParts.join(',') + ",|" + this.matchNotes + "|"
    }

    void setTeamNum(int teamNum) {
        this.teamNum = teamNum
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setMatchType(String matchType) {
        this.matchType = matchType
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setMatchNum(int matchNum) {
        this.matchNum = matchNum
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setAlliancePosition(String alliancePosition) {
        this.alliancePosition = alliancePosition
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setIsBlueAlliance(boolean isBlueAlliance) {
        this.isBlueAlliance = isBlueAlliance
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setHPShipGame(int HPShipGame) {
        this.HPShipGame = HPShipGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setHPShipSand(int HPShipSand) {
        this.HPShipSand = HPShipSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setHPRocketGame(int HPRocketGame) {
        this.HPRocketGame = HPRocketGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setHPRocketSand(int HPRocketSand) {
        this.HPRocketSand = HPRocketSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

   void setHPDropGame(int HPDropGame) {
        this.HPDropGame = HPDropGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setHPDropSand(int HPDropSand) {
        this.HPDropSand = HPDropSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setCShipGame(int CShipGame) {
        this.CShipGame = CShipGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setCShipSand(int CShipSand) {
        this.CShipSand = CShipSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setCRocketGame(int CRocketGame) {
        this.CRocketGame = CRocketGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setCRocketSand(int CRocketSand) {
        this.CRocketSand = CRocketSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setCDropGame(int CDropGame) {
        this.CDropGame = CDropGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setCDropSand(int CDropSand) {
        this.CDropSand = CDropSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setScaleLevel(int scaleLevel) {
        this.scaleLevel = scaleLevel
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setIsHelp(boolean isHelp) {
        this.isHelp = isHelp
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setStartHab(int startHab) {
        this.startHab = startHab
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

   void setCrossedLine(boolean crossedLine) {
        this.crossedLine = crossedLine
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setFouls(Long fouls) {
        this.fouls = fouls
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setTechs(Long techs) {
        this.techs = techs
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setYellow(boolean yellow) {
        this.yellow = yellow
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setRed(boolean red) {
        this.red = red
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setEStopped(boolean eStopped) {
        this.eStopped = eStopped
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setBorked(boolean borked) {
        this.borked = borked
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setPoints(int points) {
        this.points = points
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setNonFoulPoints(int nonFoulPoints) {
        this.nonFoulPoints = nonFoulPoints
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setRankingPoints(int rankingPoints) {
        this.rankingPoints = rankingPoints
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setRRocket(boolean rRocket) {
        this.rRocket = rRocket
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setLRocket(boolean lRocket) {
        this.lRocket = lRocket
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setHabRP(boolean habRP) {
        this.habRP = habRP
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setCrRP(boolean crRP) {
        this.crRP = crRP
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setScoreBreakdown(HashMap<String,Object> scoreBreakdown) {
        this.scoreBreakdown = scoreBreakdown
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setTbaSynced(boolean tbaSynced) {
        this.tbaSynced = tbaSynced
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setMatchNotes(String matchNotes) {
        this.matchNotes = matchNotes
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }
}
