package base

import java.awt.Window
import java.io.File
import java.util.List
import java.util.HashMap
import java.util.List

import base.controllers.*
import com.cpjd.main.TBA

import org.apache.commons.lang3.ObjectUtils.Null

import javafx.application.Application
import javafx.stage.Stage

/**
 * The main class for the scouting base.
 */
@CompileStatic
class Main {

    static TBA tbaApi
    private final static String TBA_KEY = 'OPynqKt8K0vueAXqxZzdigY9OBYK3KMgQQrsM4l8jE5cBmGfByhy6YzVIb2Ts7xD'
    // public static Session sesh = new Session(2019, "Sac 2019", "2019cada",
    // "sac_2019/", 0xffffff)

    static DataRecoveryThread matchRecoveryThread
    static DataRecoveryThread teamRecoveryThread

//    public static List<CustomTeam> teamSearchResults = new List<>()
    static CustomTeam teamResult

    private final static List<String> STAND_SCOUTERS = ["geran", "matt", "nick", "thomas", "claire", "max"]

    enum Windows {

        adminSignIn("/layouts/adminSignIn.fxml"), newSession("/layouts/newSession.fxml"),
        pitAssignEdit("/layouts/pitAssignEdit.fxml"), pitAssignView("/layouts/pitAssignView.fxml"),
        pitDataEntry("/layouts/pitDataEntry.fxml"), pitLaunch("/layouts/pitLaunch.fxml"),
        pitScoutSignIn("/layouts/pitScoutSignIn.fxml"), sessionLaunch("/layouts/sessionLaunch.fxml"),
        standEntry("/layouts/standEntry.fxml"), standLaunch("/layouts/standLaunch.fxml"),
        standRotEdit("/layouts/standRotEdit.fxml"), standRotView("/layouts/standRotView.fxml"),
        standRotViewAll("/layouts/standRotViewAll.fxml"), startup("/layouts/startup.fxml"),
        statsOffline("/layouts/statsOffline.fxml"), teamCompare("/layouts/teamCompare.fxml"),
        teamSearch("/layouts/teamSearch.fxml"), statsOnline("/layouts/statsOnline.fxml"),
        teamSearchResults("/layouts/teamSearchResults.fxml")

        String filePath

        private Windows(String filePath) {
            this.filePath = filePath
        }

        @Override
        String toString() {
            return filePath
        }

    }

    //EVERYTHING IN THIS MAP MUST BE AN APPLICATION THAT EXTENDS ControlInterface
    //OTHERWISE STUFF WILL BREAK
    static Map<Windows, Application> controllersMap = [:] {
        {
            put(Windows.startup, StartupControl.getInstance())
            put(Windows.newSession, NewSessionControl.getInstance())
            put(Windows.adminSignIn, AdminSignInControl.getInstance())
            put(Windows.sessionLaunch, SessionLaunchControl.getInstance())
            put(Windows.pitLaunch, new PitLaunchControl())
            put(Windows.pitDataEntry, new PitDataEntryControl())
            put(Windows.teamSearchResults, new TeamSearchResultsControl())
            put(Windows.statsOffline, new StatsOfflineControl())
            put(Windows.statsOnline, new StatsOnlineControl())
            put(Windows.standEntry, new StandEntryControl())
            put(Windows.standLaunch, new StandLaunchControl())
            put(Windows.teamSearch, new TeamSearchControl())
        }
    }

    //FIXME i know, i know, i'm just too lazy to encrypt it
    static String adminPw = "croissant"

    static Map<String, Session> activeSessions = [:]
    static List<CustomMatch> openMatches = []
    static List<CustomTeam> openTeams = []
    static List<Pit> openPits = []
    static List<CustomTeam> searchResults = []

    static synchronized void addToOpenMatches(List<CustomMatch> matches){
        openMatches.addAll(matches)
    }

    static synchronized void addToOpenTeams(List<CustomTeam> teams){
        openTeams.addAll(teams)
    }

    static Session currentSession = null
    // public static Windows currentWindow=Windows.startup
    // public static boolean switchWindow = false

    static void main(List<String> args) throws Exception {

        TBA.setAuthToken(TBA_KEY)
        tbaApi = new TBA()
        List<Session> tempActiveSessions= Lib.recoverAllSessions()

        for (Session sesh : tempActiveSessions){
            activeSessions.put(sesh.toString(), sesh)
//            Lib.report
        }

        currentSession = activeSessions.get("CCC")

        if (currentSession == null) {
            // Help
            currentSession = new Session(2019, "CCC", "ccc_2019", "ccc_2019/", new CustColor("#FFFFFF"))
        }


        for (String scouter : STAND_SCOUTERS) {
            openMatches.addAll(Lib.convertMatches("${currentSession.dataDir}stand/${scouter}.csv"))
        }
        openTeams.addAll(Lib.generateTeams("${currentSession.eventDir}teams.csv"))

        for (CustomMatch match : openMatches){
            match.syncTBA()
        }

        for (CustomTeam team : openTeams){
            for (CustomMatch match : openMatches){
                if (match.teamNum == team.teamNum){
                    team.addMatch(match)
                }
            }
            team.syncTBA()
        }


        Lib.saveTeams(openTeams, currentSession.eventDir)

        for (CustomTeam team : openTeams){
            team.sendToTxt("${currentSession.eventDir}teams/")
        }

        openTeams.clear()

        for (Session sesh : tempActiveSessions){
            activeSessions.put(sesh.toString(), sesh)
//            Lib.report
        }

//        currentSession = activeSessions.get("Sac 2019")
//        openTeams.addAll(Lib.generateTeams(eventDir+"teams.csv"))

        Application.launch(StartupControl.class, args)

    }

}
