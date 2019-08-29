package base;

import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cpjd.main.TBA;

import base.controllers.NewSessionControl;
import base.controllers.StartupControl;
import javafx.application.Application;
import javafx.stage.Stage;
import base.controllers.AdminSignInControl;
import base.controllers.ControlInterface;

public class Main {
    public static TBA tbaApi;
    // public static Session sesh = new Session(2019, "Sac 2019", "2019cada",
    // "sac_2019/", 0xffffff);

    public enum Windows {
        adminSignIn("/layouts/adminSignIn.fxml"), newSession("/layouts/newSession.fxml"),
        pitAssignEdit("/layouts/pitAssignEdit.fxml"), pitAssignView("/layouts/pitAssignView.fxml"),
        pitDataEntry("/layouts/pitDataEntry.fxml"), pitLaunch("/layouts/pitLaunch.fxml"),
        pitScoutSignIn("/layouts/pitScoutSignIn.fxml"), sessionLaunch("/layouts/sessionLaunch.fxml"),
        standEntry("/layouts/standEntry.fxml"), standLaunch("/layouts/standLaunch.fxml"),
        standRotEdit("/layouts/standRotEdit.fxml"), standRotView("/layouts/standRotView.fxml"),
        standRotViewAll("/layouts/standRotViewAll.fxml"), startup("/layouts/startup.fxml"),
        statsOffline("/layouts/statsOffline.fxml"), teamCompare("/layouts/teamCompare.fxml"),
        teamSearch("/layouts/teamSearch.fxml"), teamStatsOnline("/layouts/teamStatsOnline.fxml");

        public String filePath;

        private Windows(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String toString() {
            return filePath;
        }

    }

    //EVERYTHING IN THIS MAP MUST BE AN APPLICATION THAT EXTENDS ControlInterface
    //OTHERWISE STUFF WILL BREAK
    public static  HashMap<Windows, Application> controllersMap = new HashMap<Windows, Application>() {
        {
            put(Windows.startup, StartupControl.getInstance());
            put(Windows.newSession, NewSessionControl.getInstance());
            put(Windows.adminSignIn, AdminSignInControl.getInstance());
        }
    };

    //FIXME i know, i know, i'm just too lazy to encrypt it
    public static String adminPw = "croissant";

    public static ArrayList<Windows> backButtonList = new ArrayList<Windows>();
    public static int backIndex = -1; // the PREVIOUS page
    public static boolean isBack = false;

    public static List<Session> activeSessions = new ArrayList<Session>();

    public static Session currentSession = null;
    // public static Windows currentWindow=Windows.startup;
    // public static boolean switchWindow = false;

    public static void main(String[] args) throws Exception {
        TBA.setAuthToken("OPynqKt8K0vueAXqxZzdigY9OBYK3KMgQQrsM4l8jE5cBmGfByhy6YzVIb2Ts7xD");
        tbaApi = new TBA();


        // controllersMap.get(Windows.startup).start(new Stage()); //FIXME does this need to be uncommented?
        // Application.launch(StartupControl.class, args); //TODO this does need to be uncommented

        List<CustomTeam> teamList = Lib.recoverTeams("sac_2019/");

        System.out.println("namesearch: "+Lib.searchForTeamName("Madtown Robotics", teamList).number);
        System.out.println("numbersearch: "+Lib.searchForTeamNumber(1678, teamList).scoutedName);
        System.out.println("nicknamesearch: "+Lib.searchForRobotNickname("orange boi", teamList).number);

    }


}