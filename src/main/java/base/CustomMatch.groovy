package base

import com.cpjd.models.matches.Match
import com.cpjd.utils.exceptions.DataNotFoundException
import groovy.transform.CompileStatic

@CompileStatic
class CustomMatch {
    int teamNum
    private String matchType
    int matchNum
    String alliancePosition
    private boolean isBlueAlliance

    int hpShipGame
    int hpShipSand
    int hpRocketGame
    int hpRocketSand
    int hpDropGame
    int hpDropSand

    int cShipGame
    int cShipSand
    int cRocketGame
    int cRocketSand
    int cDropGame
    int cDropSand

    int scaleLevel
    boolean isHelp = false

    int startHab
    private boolean crossedLine = false

    Long fouls
    Long techs
    boolean yellow
    boolean red
    boolean eStopped
    boolean borked

    private long points
    private int nonFoulPoints = 0
    int rankingPoints = 0
    boolean rRocket = false, lRocket = false, habRP = false, crRP = false

    HashMap<String, Object> scoreBreakdown
    boolean tbaSynced = false

    String matchNotes

    CustomMatch(String[] csvRow) {
        this.matchType = csvRow[0]
        this.matchNum = Integer.valueOf(csvRow[1])
        this.teamNum = Integer.valueOf(csvRow[2])

        // I do not know what this debug print is for
        System.out.println(csvRow[3].charAt(0))

        this.alliancePosition = csvRow[3]
        this.isBlueAlliance = this.alliancePosition.startsWith('B')

        this.startHab = Integer.valueOf(csvRow[4])

        this.cShipSand = Integer.valueOf(csvRow[5])
        this.cRocketSand = Integer.valueOf(csvRow[6])
        this.cDropSand = Integer.valueOf(csvRow[7])
        this.hpShipSand = Integer.valueOf(csvRow[8])
        this.hpRocketSand = Integer.valueOf(csvRow[9])
        this.hpDropSand = Integer.valueOf(csvRow[10])

        this.cShipGame = Integer.valueOf(csvRow[11])
        this.cRocketGame = Integer.valueOf(csvRow[12])
        this.cDropGame = Integer.valueOf(csvRow[13])
        this.hpShipGame = Integer.valueOf(csvRow[14])
        this.hpRocketGame = Integer.valueOf(csvRow[15])
        this.hpDropGame = Integer.valueOf(csvRow[16])

        try {
            this.scaleLevel = Integer.valueOf(csvRow[17])
        } catch (NumberFormatException ignored) {
            Lib.report('Scale Level is not a number. Setting to 0.')
            this.scaleLevel = 0
        }

        this.techs = Long.valueOf(csvRow[18])
        this.fouls = Long.valueOf(csvRow[19])
        this.yellow = csvRow[20].equalsIgnoreCase('true')
        this.red = csvRow[21].equalsIgnoreCase('true')
        this.borked = csvRow[22].equalsIgnoreCase('true')
        this.points = Integer.valueOf(csvRow[23])

        this.matchNotes = csvRow[24]

        this.matchNotes = this.matchNotes.replace('\n', '.  ')

        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    int sandPlaces() {
        hpShipSand + hpRocketSand + cShipSand + cRocketSand
    }

    void syncTBA() {
        Match foundMatch
        try {
            foundMatch = Main.tbaApi.getMatch("${Main.currentSession.tbaEventKey}_qm${this.matchNum}")
        } catch (DataNotFoundException e) {
            System.out.println("Match not found. \n${e}")
            return
        }

        //get the correct points TODO find a better way to do blue vs red
        long blueScore = foundMatch.blue.score
        if (this.points != blueScore && this.isBlueAlliance) {
            Lib.report(
                    "Scouted points for match ${this.matchNum}, Blue Alliance are incorrect. Scouted points: ${this.points} TBA points: ${blueScore}"
            )
            this.points = blueScore
        }

        long redScore = foundMatch.red.score
        if (this.points != redScore && !this.isBlueAlliance) {
            Lib.report(
                    "Scouted points for match ${this.matchNum}, Red Alliance are incorrect. Scouted points: ${this.points} TBA points: ${redScore}"
            )
            this.points = redScore
        }

        //get the big fancy hashmap
        if (this.isBlueAlliance) {
            this.scoreBreakdown = foundMatch.blueScoreBreakdown
        } else {
            this.scoreBreakdown = foundMatch.redScoreBreakdown
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
        this.nonFoulPoints = this.points - (this.scoreBreakdown.get('foulPoints') as Number)

        int alPos = Character.getNumericValue(
                this.alliancePosition.charAt(this.alliancePosition.length() - 1)
        )
        //the starting hab
        int habChar = Character.getNumericValue(this.scoreBreakdown.get('preMatchLevelRobot' + alPos).toString().charAt(8))
        if (this.startHab != habChar) {
            Lib.report("Start level for match ${this.matchNum}, alliance position ${this.alliancePosition} is incorrect. \nReported level: ${this.startHab}\nTBA level: ${habChar}")
            this.startHab = habChar
        }

        //get the number of ranking points
        this.rankingPoints = (int) this.scoreBreakdown.get('rp')

//        this.rRocket = (boolean)this.scoreBreakdown.get("completedRocketNear")
//        this.lRocket = (boolean)this.scoreBreakdown.get("completedRocketFar")

        this.habRP = this.scoreBreakdown.get('habDockingRankingPoint')
        this.crRP = this.scoreBreakdown.get('completeRocketRankingPoint')

        //TODO add more?

        this.tbaSynced = true

        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    String toReadableString() {
        return String.format('Team Number: %d, Match Type: %s, Match Number: %d, Position: %s, isBlue: %b, '
                + 'hpShipGame: %d, hpShipSand: %d, hpRocketGame: %d, hpRocketSand: %d, hpDropGame: %d, '
                + 'hpDropSand: %d, cShipGame: %d, cShipSand: %d, cRocketGame: %d, cRocketSand: %d, cDropGame: %d, '
                + 'cDropSand: %d, Scale Level: %d, Is a Helper: %b, Starting Level: %d, Crossed the Line: %b, Fouls: %d, Tech Fouls: %d, Yellow Card: %b, '
                + 'Red Card: %b, Emergency Stop: %b, Broken: %b, Total Points: %d, Points w/o Penalties: %d, '
                + 'Ranking Points: %d, Filled Right Rocket: %b, Filled Left Rocket: %b, Hab Docking RP: %b, '
                + 'Rocket RP: %b, Synced? %b, Notes: %s%n',
                this.teamNum, this.matchType, this.matchNum, this.alliancePosition, this.isBlueAlliance,
                this.hpShipGame, this.hpShipSand, this.hpRocketGame, this.hpRocketSand, this.hpDropGame,
                this.hpDropSand, this.cShipGame, this.cShipSand, this.cRocketGame, this.cRocketSand, this.cDropGame,
                this.cDropSand, this.scaleLevel, this.isHelp, this.startHab, this.crossedLine, this.fouls, this.techs, this.yellow,
                this.red, this.eStopped, this.borked, this.points, this.nonFoulPoints,
                this.rankingPoints, this.rRocket, this.lRocket, this.habRP,
                this.crRP, this.tbaSynced, this.matchNotes)
    }

    @Override
    String toString() {
        def commaJoinParts = [
                this.teamNum, this.matchType, this.matchNum, this.alliancePosition,
                this.isBlueAlliance, this.hpShipGame, this.hpShipSand, this.hpRocketGame,
                this.hpRocketSand, this.hpDropGame, this.hpDropSand, this.cShipGame,
                this.cShipSand, this.cRocketGame, this.cRocketSand, this.cRocketGame,
                this.cRocketSand, this.cDropGame, this.cDropSand, this.scaleLevel, this.isHelp,
                this.startHab, this.crossedLine, this.fouls, this.techs, this.yellow,
                this.red, this.eStopped, this.borked, this.points, this.nonFoulPoints,
                this.rankingPoints, this.rRocket, this.lRocket, this.habRP, this.crRP, this.tbaSynced
        ]
        return commaJoinParts.join(',') + ',|' + this.matchNotes + '|'
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

    void sethpShipGame(int hpShipGame) {
        this.hpShipGame = hpShipGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void sethpShipSand(int hpShipSand) {
        this.hpShipSand = hpShipSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void sethpRocketGame(int hpRocketGame) {
        this.hpRocketGame = hpRocketGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void sethpRocketSand(int hpRocketSand) {
        this.hpRocketSand = hpRocketSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void sethpDropGame(int hpDropGame) {
        this.hpDropGame = hpDropGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void sethpDropSand(int hpDropSand) {
        this.hpDropSand = hpDropSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setcShipGame(int cShipGame) {
        this.cShipGame = cShipGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setcShipSand(int cShipSand) {
        this.cShipSand = cShipSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setcRocketGame(int cRocketGame) {
        this.cRocketGame = cRocketGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setcRocketSand(int cRocketSand) {
        this.cRocketSand = cRocketSand
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setcDropGame(int cDropGame) {
        this.cDropGame = cDropGame
        Lib.saveMatch(this, Main.currentSession.eventDir)
    }

    void setcDropSand(int cDropSand) {
        this.cDropSand = cDropSand
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

    void setScoreBreakdown(HashMap<String, Object> scoreBreakdown) {
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
