package base;

import java.io.*;
import java.nio.channels.NotYetConnectedException;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.opencsv.CSVReader;

import org.codehaus.jackson.map.ObjectMapper;

import base.Main.Windows;
import base.controllers.AdminSignInControl;
import base.controllers.ControlInterface;
import javafx.fxml.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.fxml.*;
import javafx.application.Application;


public class Lib {

    static ObjectMapper mapper = new ObjectMapper();


    public static int max(ArrayList<Integer> ints){
        int temp=0;
        for (int i : ints){
            temp = i>temp ? i : temp;
        }

        return temp;
    }

    //FIXME this is a terrible horible no good very bad way to do this
    public static int mode(ArrayList<Integer> ints){
        HashMap<Integer, Integer> map = new HashMap<>();
        int tempIn = 0;
        int tempVal = 0;

        for(Integer i : ints){
            if(!map.keySet().contains(i)){
                map.put(i, 1);
            }else{
                map.put(i, map.get(i)+1);
            }
        }

        for (Integer key : map.keySet()){
            if(tempVal<=map.get(key)){
                tempIn = key;
                tempVal = map.get(key);
            }
        }

        return tempIn;
    }

    public static String arrayToString(Object[] groups){
        return arrayToString(groups, ",");
    }

    public static String arrayToString(Object[] groups, String delim){
        if(groups!=null){
            String str="|";
            if(groups.length>1){
                for(int i=0; i<=groups.length-1; i++){
                    str+=groups[i].toString();
                    str+=",";
                }
                str+=groups[groups.length-1].toString();
            }
            return str+"|";
        }else{
            return "";
        }
    }

    public static String arrayToLinebreakString(Object[] array, String spacing){
        String str="";
        if(array.length>1){
            for(int i=0; i<=array.length-1; i++){
                str+=spacing;
                str+=array[i].toString();
                str+="\n";
            }
        }
        return str;
    }

    public static String[] stringToArray(String string, boolean hasSides) {
        String commaString = "";
        if(hasSides){
            commaString = string.substring(1, string.length()-2);
        }else{
            commaString = string;
        }
        return commaString.split(",");
    }

    public static String[] stringToArray(String string){
        return stringToArray(string, false);
    }


    public static void report(String re){
        //TODO this should report to the gui, not the console
        System.out.println(re);
    }

    //FIXME why are there so many  h e c k i n  try catches
    public static List<CustomMatch> convertMatches(String filePath){
        report("NOW CONVERTING MATCHES FROM: "+filePath);
        CSVReader reader;
        try{
            reader = new CSVReader(new FileReader(filePath), ',', '|');
        }catch(FileNotFoundException e){
            report("Match Convert file not found:\n"+e);
            return null;
        }

        List<CustomMatch> collectedMatches = new ArrayList<CustomMatch>();
        List<String[]> rows=null;
        try{
            rows = reader.readAll();
        }catch(IOException e){
            report("Match Read IO Exception:\n"+e);
        }
        for(String[] row : rows){
            if(row[0].equals("Qualifier")){
                collectedMatches.add(new CustomMatch(row));
                // report("Match created");
            }else{
                report("Not a qualifier. Passing.");
            }
        }

        try{
            reader.close();
        }catch(IOException e){
            report("Reader close IO Exception:\n"+e);
        }

        return collectedMatches;
        
    }

    public static List<CustomTeam> generateTeams(String teamCSVPath){
        report("NOW GENERATING TEAMS FROM: "+teamCSVPath);
        CSVReader reader;
        try{
            reader = new CSVReader(new FileReader(teamCSVPath), ',');
        }catch(FileNotFoundException e){
            report("Team Generation file not found:\n"+e);
            return null;
        }

        List<CustomTeam> gennedTeams = new ArrayList<CustomTeam>();
        List<String[]> rows=null;
        try{
            rows = reader.readAll();
        }catch(IOException e){
            report("Team Read IO Exception:\n"+e);
        }
        for(String[] row : rows){
            gennedTeams.add(new CustomTeam(Integer.valueOf(row[0])));
        }

        try{
            reader.close();
        }catch(IOException e){
            report("Reader close IO Exception:\n"+e);
        }

        return gennedTeams;
    }

//    @Deprecated //this b the Big Slow
    public static boolean InternettyChecky() throws Exception {
        long now = (new Date()).getTime();

        Process process = java.lang.Runtime.getRuntime().exec("ping -w 1 -n 2 google.com");
        int x = process.waitFor(); 
        if (x == 0) {
            long dt = (new Date()).getTime() - now;
            report("Internet connection checked and active in " + dt / 1000d + "s, output "+x);
            return true;
        } 
        else {
            long dt = (new Date()).getTime() - now;
            report("Internet connection failed in " + dt / 1000d + "s, output "+x);
            return false;
        } 
    } 

    public static void saveMatch(CustomMatch match, String eventDir){
        saveMatches(new ArrayList<>(Arrays.asList(match)), eventDir);
    }


    public static void saveMatches(List<CustomMatch> matches, String eventDir){
        for(CustomMatch match : matches){
            try{
                mapper.writeValue(new File(eventDir+"backups/matches/qm"+match.getMatchNum()+match.getAlliancePosition()+".json"), match);
            }catch (Exception e){
                report("write failed for match "+match.getMatchNum());
                report(e.toString());
            }
        }
    }

    public static List<CustomMatch> recoverMatches(String eventDir){
        List<CustomMatch> recovered = new ArrayList<>();
        int recoverFails = 0;

        File[] files = new File(eventDir+"backups/matches/").listFiles();

        for(File file : files){
            try{
                recovered.add(mapper.readValue(file, CustomMatch.class));
                // System.out.println(recovered.get(recovered.size()-1));
            }catch(Exception e){
                report("recover failed");
                report(e.toString());
                recoverFails++;
            }
        }
        report(recoverFails+" RECOVER FAILURES");

        return recovered;
    }

    public static void saveTeam(CustomTeam team, String eventDir){
        saveTeams(new ArrayList<>(Arrays.asList(team)), eventDir);
    }

    public static void saveTeams(List<CustomTeam> teams, String eventDir){
        for(CustomTeam team : teams){
            try{
                mapper.writeValue(new File(eventDir+"backups/teams/"+team.getNumber()+".json"), team);
            }catch (Exception e){
                report("write failed for team "+team.getNumber());
                report(e.toString());
            }
        }
    }

    public static List<CustomTeam> recoverTeams(String eventDir){
        List<CustomTeam> recovered = new ArrayList<>();
        int recoverFails = 0;

        File[] files = new File(eventDir+"backups/teams/").listFiles();

        for(File file : files){
            try{
                recovered.add(mapper.readValue(file, CustomTeam.class));
            }catch(Exception e){
                report("recover failed");
                report(e.toString());
                recoverFails++;
            }
        }
        report(recoverFails+" RECOVER FAILURES");

        return recovered;
    }

    /**
     * Search for a team by it's team number
     * @param teamNumber the team number to search for
     * @param teams the list of teams to search through
     * @return the team found with the match team number, or
     * @throws TeamNotFoundException an exception :)
     */
    public static CustomTeam searchForTeamNumber(int teamNumber, List<CustomTeam> teams) throws TeamNotFoundException {
        for(CustomTeam team : teams) {
            if(team.getNumber() == teamNumber) return team;
        }
        throw new TeamNotFoundException("Team " + teamNumber + " could not be found!");
    }

    /**
     * Search for a team by it's team name
     * @param teamName the team name to search for by scouted name or tbaName
     * @param teams the list of teams to search through
     * @return the team found with the match team number, or
     * @throws TeamNotFoundException an exception :)
     */
    public static CustomTeam searchForTeamName(String teamName, List<CustomTeam> teams) throws TeamNotFoundException {
        //TODO improve this function to inclued close matches in team name (ex. "BREAD" vs "B.R.E.A.D.")
        for(CustomTeam team : teams) {
            if(team.getScoutedName().equalsIgnoreCase(teamName)
                || team.getTbaName().equalsIgnoreCase(teamName))

                return team;
        }
        throw new TeamNotFoundException("Team " + teamName + " could not be found!");
    }

    public static CustomTeam searchForRobotNickname(String nickname, List<CustomTeam> teams) throws TeamNotFoundException{
        for(CustomTeam team : teams) {
            for(String nick : team.getRobotNicknames()){
                if(nick.equalsIgnoreCase(nickname)){
                    return team;
                }
            }
        }
        throw new TeamNotFoundException("A robot called "+nickname+" could not be found!");
    }

    static class TeamNotFoundException extends Exception { TeamNotFoundException(String message) { super(message); } }
    public static void savePit(Pit pit, String eventDir){
        savePits(new ArrayList<Pit>(Arrays.asList(pit)), eventDir);
    }
    public static void savePitData(Pit pit, String eventDir){
        FileWriter writer = null;
        try{
            writer = new FileWriter(eventDir+"data/pit/"+pit.teamNumber+"-"+pit.scoutName+".csv", false);
        }catch(IOException e){
            Lib.report("File not found error");
            System.out.println(e);
        }

        try{
            writer.write(
                    pit.teamNumber+","+
                    pit.teamName+","+
                    pit.scoutName+","+
                    pit.level2Climb+","+
                    pit.level3Climb+","+
                    pit.intakeType+","+
                    pit.rocketLevel+","+
                    pit.mechIssues+","+
                    pit.cam+","+
                    pit.preset+","+
                    pit.sense+","+
                    pit.reach+","+
                    pit.ramp+","+
                    arrayToString(pit.nicknames, ",")+","+
                    pit.startHab+","+
                    pit.driverControl+","+
                    pit.pathing+","+
                    pit.noControl+","+
                    arrayToString(pit.autoStrats.toArray())+","+
                    pit.autoNotes+","+
                    pit.prefHatch+","+
                    pit.prefCargo+","+
                    pit.ppm+","+
                    arrayToString(pit.teleStrats.toArray())+","+
                    pit.cycleTime+","+
                    pit.teleNotes+","+
                    pit.hpPref+","+
                    pit.stratPref+","+
                    pit.notes
            );

            writer.close();
        }catch(Exception e){
            report(e.toString());
        }
    }

    public static void savePits(List<Pit> pits, String eventDir){
        for(Pit pit : pits){
            try{
                mapper.writeValue(new File(eventDir+"backups/pits/"+pit.getName()+".json"), pit);
            }catch (Exception e){
                report("write failed for pit "+pit.getName());
                report(e.toString());
            }
        }
    }

    public static List<Pit> recoverPits(String eventDir){
        List<Pit> recovered = new ArrayList<>();

        File[] files = new File(eventDir+"backups/pits/").listFiles();

        for(File file : files){
            try{
                recovered.add(mapper.readValue(file, Pit.class));
            }catch(Exception e){
                report("recover failed");
                report(e.toString());
            }
        }

        return recovered;
    }

    public static void saveSession(Session session){
        try{
            mapper.writeValue(new File("main_storage/"+session.tbaEventKey+".json"), session);
            report("done");
        }catch (Exception e){
            report("write failed for session "+session.tbaEventKey);
            report(e.toString());
        }
    }

    public static List<Session> recoverAllSessions(){
        List<Session> recovered = new ArrayList<>();

        File[] files = new File("main_storage/").listFiles();

        for(File file : files){
            try{
                recovered.add(mapper.readValue(file, Session.class));
            }catch(Exception e){
                report("recover failed");
                report(e.toString());
            }
        }

        return recovered;
    }


    public static void memeStart(Stage primaryStage, Parent root, String title) throws Exception{

        Scene scene = new Scene(root, 600, 400);
    
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show(); 
    }

    public static void memeStart(Stage primaryStage, Parent root) throws Exception{
        memeStart(primaryStage, root, "BREAD 5940 Scouting Base");
    }


    public static <T extends Application & ControlInterface> void pageChangeRequest(Main.Windows reqPage, boolean isBack, T app){

        // try{ 
            System.out.println("APP HASH: "+app);
            T newApp = (T)(Main.controllersMap.get(reqPage));
            report(newApp.toString());
            try{
                report(newApp.getName().toString());
                newApp.start(new Stage());
            }catch(Exception e){
                report(e.toString());
            }
            report(app.getName().toString());
            newApp.setPreviousPage(app.getName());
            report("Page changed to "+reqPage.toString()+". This "+((isBack) ? "was" : "was not")+" a back button change.");
        // }catch(Exception e){
        //     report("Page launch failed. Exception: "+e.toString()+" at "+e.getCause().toString());
        //     return;
        // }
        try{
            app.getStage().close();
        }catch(Exception e){
            report("App stop failed. Exception: "+e.toString());
        }

    }


    public static <T extends Application & ControlInterface> void pwProteccPageChangeRequest(Main.Windows finalPage, boolean isBack, T app){
        try{
            AdminSignInControl newApp = (AdminSignInControl)(Main.controllersMap.get(Windows.adminSignIn));
            newApp.start(new Stage());
            newApp.setPreviousPage(app.getName());
            newApp.setFollowThroughPage(finalPage);
            System.out.println("APP HASH: "+newApp);
            report("Follow through page: "+newApp.getFollowThroughPage().toString());
            report("Page changed to "+Windows.adminSignIn.toString()+". This "+((isBack) ? "was" : "was not")+" a back button change.");
        }catch(Exception e){
            report("Page launch failed. Exception: "+e.getMessage()+", "+e.toString());
            return;
        }

        try{
            app.getStage().close();
        }catch(Exception e){
            report("App stop failed. Exception: "+e.toString());
        }
    }

}