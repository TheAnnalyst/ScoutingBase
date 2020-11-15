package base

import javafx.scene.paint.Color

class Session {
    int season
    String event
    String tbaEventKey
    String eventDir
    String dataDir
    String sortsDir
    String teamsDir
    String backupsDir
    CustColor backgroundColor

    Session(int season, String event, String eventKey, String eventDir, CustColor backColor) {
        this.season = season
        this.event = event
        this.tbaEventKey = eventKey
        this.eventDir = eventDir
        this.dataDir = "${eventDir}data/"
        this.sortsDir = "${eventDir}sorts/"
        this.teamsDir = "${eventDir}teams/"
        this.backupsDir = "${eventDir}backups/"
        this.backgroundColor = backColor
    }

    Session(int season, String event, String eventKey, String eventDir, Color backColor) {
        this(season, event, eventKey, eventDir, new CustColor(backColor))
    }

    Session(int season, String event, String eventKey, String eventDir, String backColor) {
        this(season, event, eventKey, eventDir, new CustColor(backColor))
    }

    @Override
    String toString(){
        return event
    }

    String fullToString() {
        return "Season: ${season}, Event Name: ${event}, Event Key: ${tbaEventKey}, Directory: ${eventDir}, Color: ${backgroundColor}"
    }

}
