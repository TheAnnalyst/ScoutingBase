package base;


import java.util.ArrayList;

import com.cpjd.main.TBA;
import com.cpjd.models.events.Award;

public class Main{
    public static TBA tbaApi;
    static ArrayList<CustomTeam> teams = new ArrayList<CustomTeam>();
    static ArrayList<CustomMatch> matches = new ArrayList<CustomMatch>();

    //TODO make some sort of session file to store these
    static final int season = 2019;
    static final String event = "Sacramento";
    static final String tbaEventKey = "2019cada";
    static final String eventDir = "sac_2019/";
    static final String dataDir = eventDir+"data/";
    static final String sortsDir = eventDir+"sorts/";
    static final String teamsDir = eventDir+"teams/";

    public static void main(String[] args){
        TBA.setAuthToken("OPynqKt8K0vueAXqxZzdigY9OBYK3KMgQQrsM4l8jE5cBmGfByhy6YzVIb2Ts7xD");
        tbaApi = new TBA();
        for (Award aard : tbaApi.getEventAwards(tbaEventKey)){
            System.out.println(aard.getName());
        }
    }


}