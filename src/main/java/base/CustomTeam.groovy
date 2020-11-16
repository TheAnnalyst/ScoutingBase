package base

import groovy.transform.CompileStatic

/**
 * Represents a FRC team.
 */
@CompileStatic
class CustomTeam {
    static enum Groups {
        Level3Climbers, Level2Climbers, Level2Starters, GotRed,
        HasBorked, LowScorers, HighScorers, Completed3Rockets
    }

    String scoutedName
    String tbaName
    String sponsors
    int number
    private boolean isFullySync = true

    List<String> robotNicknames = []

    private List<CustomMatch> matches = []
    final private List<Pit> pits = []

    private BigDecimal avGPSand
    private BigDecimal avHPShip
    private BigDecimal avHPRocket
    private BigDecimal avHPDrop
    private BigDecimal avCShip
    private BigDecimal avCRocket
    private BigDecimal avCDrop

    private List<Integer> scaleLevels = []
    private BigDecimal consistScaleLevel
    private BigDecimal maxScaleLevel

    private boolean isRamp

    private List<Integer> startHabs = []
    private BigDecimal consistStartHab
    private BigDecimal maxStartHab
    private boolean consistOffHab

    private BigDecimal avFoul
    private BigDecimal avTech
    private BigDecimal totalYellow
    private BigDecimal totalRed

    private BigDecimal eStops
    private BigDecimal borks

    private int totalRPs
    private int totalHabRPs
    private int totalRocketRPs
    private int totalRockets

    private List<Groups> groups = []
    private Map<Integer, String> matchNotes = [:]

    CustomTeam(String name, int number) {
        this.scoutedName = name
        this.number = number

        this.save()
    }

    CustomTeam(int number) {
        this.number = number
        this.save()
    }

    void addMatch(CustomMatch match) {
        this.avGPSand = (this.avGPSand + match.sandPlaces()) / 2

        this.avHPShip = (this.avHPShip + (match.hpShipGame + match.hpShipSand) / 2)
        this.avHPRocket = (this.avHPRocket + (match.hpRocketGame + match.hpRocketSand) / 2)
        this.avHPDrop = (this.avHPDrop + (match.hpDropGame + match.hpDropGame) / 2)
        this.avCShip = (this.avCShip + (match.cShipGame + match.cShipSand) / 2)
        this.avCRocket = (this.avCRocket + (match.cRocketGame + match.cRocketSand) / 2)
        this.avCDrop = (this.avCDrop + (match.cDropGame + match.cDropSand) / 2)

        this.scaleLevels.add(match.scaleLevel)
        this.consistScaleLevel = Lib.mode(this.scaleLevels)
        this.maxScaleLevel = this.scaleLevels.max()
        this.isRamp = this.isRamp || match.isHelp

        this.startHabs.add(match.startHab)

        this.consistStartHab = Lib.mode(this.startHabs)
        this.maxStartHab = this.startHabs.max()
        this.consistOffHab = consistStartHab != 0

        this.avFoul = (this.avFoul + match.fouls) / 2
        this.avTech = (this.avTech + match.techs) / 2

        if (match.yellow) this.totalYellow++
        if (match.red) this.totalRed++

        if (match.eStopped) this.eStops++
        if (match.borked) this.borks++
        if (match.habRP) this.totalHabRPs++
        if (match.crRP) this.totalRocketRPs++
        if (match.rRocket || match.lRocket) this.totalRockets++

        this.totalRPs += match.rankingPoints

        this.matchNotes.put(match.matchNum, match.matchNotes)
        this.matches.add(match)
        this.isFullySync = this.isFullySync && match.tbaSynced

        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void addPit(Pit pitData) {
        this.pits.add(pitData)
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void addGroups() {
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void syncTBA() {
        this.tbaName = Main.tbaApi.getTeam(this.number).nickname
        this.sponsors = Main.tbaApi.getTeam(this.number).name //i stg this is the sponsors

        // I have no idea why we `&& true` here but it feels important
        // Maybe some sort of weird situation where isFullySync isn't a boolean?
        this.isFullySync = this.isFullySync && true

        Lib.report("Team ${this.number} synced.")
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    // does NOT include matches, scale levels, start habs, or match notes
    String toReadableString() {
        return String.format(
            'Number: %d, Name: %s/%s, Robot Nicknames: %s, Sponsors: %s, Is Synced? %b, Average GPs in Sand: %f, '
            + 'Average hps on Cargo Ship: %f, Average hps on Rocket: %f, Average Dropped hps: %f, Average Cargo on Cargo Ship: %f, '
            + 'Average Cargo on Rocket: %f, Average Dropped Cargo: %f, Consistant Scale Level: %f, Maximum Scale Level: %f, '
            + 'Is ramp? %b, Consistant Starting Hab Level: %f, Maximum Starting Hab Level: %f, Consistantly Off Hab? %b, '
            + 'Average Fouls: %f, Average Tech Fouls: %f, Total Yellow Cards: %f, Total Red Cards: %f, Total Emergency Stops: %f, '
            + 'Total Breaks: %f, Total Ranking Points: %d, Total Hab RPs: %d, Total Rocket RPs: %d, Total Full Rockets: %d, '
            + 'Groups: %s',

            this.number,
            this.scoutedName,
            this.tbaName,
            this.robotNicknames.join(', '),
            this.sponsors,
            this.isFullySync,
            this.avGPSand,

            this.avHPShip, this.avHPRocket, this.avHPDrop, this.avCShip,
            this.avCRocket, this.avCDrop, this.consistScaleLevel, this.maxScaleLevel,
            this.isRamp, this.consistStartHab, this.maxStartHab, this.consistOffHab,
            this.avFoul, this.avTech, this.totalYellow, this.totalRed, this.eStops,
            this.borks, this.totalRPs, this.totalHabRPs, this.totalRocketRPs, this.totalRockets,
            this.groups.join(', ')
        )
    }

    void sendToTxt(String directory) {
        FileWriter writer = null
        String path = "${directory}${this.number}.txt"
        try {
            writer = new FileWriter(path, false)
        } catch (IOException e) {
            Lib.report("File not found error while opening ${path}")
            System.out.println(e)
        }

        try {
            writer.write(String.format(
                'IS FULLY SYNCED WITH TBA: %b\n' +
                '%-4d  (%s/%s) \nSponsors: %s\n' +
                'Nicknames: %s\n' +
                '-----------------------------------------------------------------------\n\n' +
                'Groups: %s\n' +
                'Consistently Off the Hab? %b    Most Common Starting Hab: %.0f    Maximum Starting Hab: %.0f\n' +
                'Starting Hab Levels (By Match): %s\n\n' +
                'Is Ramp Bot? %b                 Most Common Scale Level: %.0f     Maximum Scale Level: %.0f\n' +
                'Scale Levels (By Match): %s\n\n' +
                'Total Ranking Points: %d    Hab-Related RPs: %d    Rocket-Related RPs: %d    Filled Rockets: %d\n\n'+
                'Average Fouls Per Match: %.2f    Average Tech Fouls Per Match: %.2f\n' +
                'Total Yellow Cards: %.0f           Total Red Cards: %.0f\n' +
                'Total Emergency Stops: %.0f        Total Robot Breakages: %.0f\n\n' +
                'Average Game Pieces Placed (Sandstorm): %.2f\n' +
                'Average Hatch Panel Placement:    Average Cargo Placement:\n' +
                '    Cargo Ship: %.2f                  Cargo Ship: %.2f\n' +
                '    Rocket: %.2f                      Rocket: %.2f\n' +
                '    Dropped: %.2f                     Dropped: %.2f\n\n' +
                'Match Number | Note\n',
                this.isFullySync,
                this.number, this.scoutedName, this.tbaName, this.sponsors,
                this.robotNicknames.join(', '),

                this.groups.join(', '),
                this.consistOffHab, this.consistStartHab, this.maxStartHab,
                this.startHabs.join(', '),
                this.isRamp, this.consistScaleLevel, this.maxScaleLevel,
                this.scaleLevels.join(', '),
                this.totalRPs, this.totalHabRPs, this.totalRocketRPs, this.totalRockets,
                this.avFoul, this.avTech,
                this.totalYellow, this.totalRed,
                this.eStops, this.borks,
                this.avGPSand,

                this.avHPShip, this.avCShip,
                this.avHPRocket, this.avCRocket,
                this.avHPDrop, this.avHPDrop
            ))

            for (key in this.matchNotes.keySet()) {
                writer.write(String.format('%12d | %s\n', key, this.matchNotes.get(key)))
            }
            writer.write('\n-----------------------------------------------------------------------\n\nMATCHES: \n')
            for (CustomMatch match in this.matches) {
                Lib.report("${this.number}, match ${match.matchNum}")
                writer.write(match.toString() + '\n')
            }
            writer.close()
        } catch (Exception ignored) { }
    }

    void setScoutedName(String scoutedName) {
        this.scoutedName = scoutedName
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setTbaName(String tbaName) {
        this.tbaName = tbaName
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setSponsors(String sponsors) {
        this.sponsors = sponsors
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setNumber(int number) {
        this.number = number
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setIsFullySync(boolean isFullySync) {
        this.isFullySync = isFullySync
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setRobotNicknames(List<String> robotNicknames) {
        this.robotNicknames = robotNicknames
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setMatches(List<CustomMatch> matches) {
        this.matches = matches
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setAvGPSand(BigDecimal avGPSand) {
        this.avGPSand = avGPSand
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setAvhpShip(BigDecimal avHPShip) {
        this.avHPShip = avHPShip
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setAvhpRocket(BigDecimal avHPRocket) {
        this.avHPRocket = avHPRocket
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setAvhpDrop(BigDecimal avHPDrop) {
        this.avHPDrop = avHPDrop
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setAvcShip(BigDecimal avCShip) {
        this.avCShip = avCShip
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setAvcRocket(BigDecimal avCRocket) {
        this.avCRocket = avCRocket
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setAvcDrop(BigDecimal avCDrop) {
        this.avCDrop = avCDrop
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setScaleLevels(List<Integer> scaleLevels) {
        this.scaleLevels = scaleLevels
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setConsistScaleLevel(BigDecimal consistScaleLevel) {
        this.consistScaleLevel = consistScaleLevel
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setMaxScaleLevel(BigDecimal maxScaleLevel) {
        this.maxScaleLevel = maxScaleLevel
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setIsRamp(boolean isRamp) {
        this.isRamp = isRamp
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setStartHabs(List<Integer> startHabs) {
        this.startHabs = startHabs
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setConsistStartHab(BigDecimal consistStartHab) {
        this.consistStartHab = consistStartHab
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setMaxStartHab(BigDecimal maxStartHab) {
        this.maxStartHab = maxStartHab
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setConsistOffHab(boolean consistOffHab) {
        this.consistOffHab = consistOffHab
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setAvFoul(BigDecimal avFoul) {
        this.avFoul = avFoul
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setAvTech(BigDecimal avTech) {
        this.avTech = avTech
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setTotalYellow(BigDecimal totalYellow) {
        this.totalYellow = totalYellow
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setTotalRed(BigDecimal totalRed) {
        this.totalRed = totalRed
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setEStops(BigDecimal eStops) {
        this.eStops = eStops
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setBorks(BigDecimal borks) {
        this.borks = borks
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setTotalRPs(int totalRPs) {
        this.totalRPs = totalRPs
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setTotalHabRPs(int totalHabRPs) {
        this.totalHabRPs = totalHabRPs
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setTotalRocketRPs(int totalRocketRPs) {
        this.totalRocketRPs = totalRocketRPs
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setTotalRockets(int totalRockets) {
        this.totalRockets = totalRockets
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setGroups(List<Groups> groups) {
        this.groups = groups
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    void setMatchNotes(Map<Integer, String> matchNotes) {
        this.matchNotes = matchNotes
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }

    private void save() {
        Lib.saveTeam(this, Main.currentSession.eventDir)
    }
}
