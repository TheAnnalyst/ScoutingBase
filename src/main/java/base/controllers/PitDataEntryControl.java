package base.controllers;

import java.awt.Checkbox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle.Control;

import base.*;
import base.Main.Windows;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.stage.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class PitDataEntryControl extends Application implements ControlInterface{

    Windows previousPage = Windows.pitLaunch;
    @FXML AnchorPane basePane;
    @FXML Label sessionTitle;
    static PitLaunchControl inst;


    @FXML TextField teamNumber;
    @FXML TextField teamName;
    @FXML TextField scoutName;
    @FXML Button submit;

    @FXML CheckBox climbs;
    @FXML RadioButton level2;
    @FXML RadioButton level3;

    @FXML CheckBox cargoIntake;
    @FXML CheckBox hatchIntake;

    @FXML RadioButton rocketLevel3;
    @FXML RadioButton rocketLevel2;
    @FXML RadioButton rocketLevel1;

    @FXML Slider mechanicalIssues;

    @FXML CheckBox hasCamera;
    @FXML CheckBox usesPresets;
    @FXML CheckBox hasSensor;
    @FXML CheckBox reachCargo;
    @FXML CheckBox rampbot;

    @FXML TextArea nicknames;

    @FXML RadioButton startHab1;
    @FXML RadioButton startHab2;

    @FXML CheckBox driverControl;
    @FXML CheckBox pathing;
    @FXML CheckBox noControl;

    @FXML CheckBox autoCloseRocketHatch;
    @FXML CheckBox autoFarRocketHatch;
    @FXML CheckBox autoFrontShipHatch;
    @FXML CheckBox autoOtherShipHatch;
    @FXML CheckBox autoShipCargo;
    @FXML CheckBox autoline;
    @FXML CheckBox autoMultiPiece;
    @FXML CheckBox autoNoStrat;
    @FXML CheckBox autoOtherStrat;

    @FXML TextArea autoStratNotes;

    @FXML CheckBox prefHatch;
    @FXML CheckBox prefCargo;

    @FXML Slider piecesPerMatch;

    @FXML CheckBox teleopShipCargo;
    @FXML CheckBox teleopShipHatch;
    @FXML CheckBox teleopRocketCargo;
    @FXML CheckBox teleopRocketHatch;
    @FXML CheckBox teleopDefense;
    @FXML CheckBox teleopMixed;
    @FXML CheckBox teleopFlex;
    @FXML CheckBox teleopStratOther;

    @FXML Slider cycleTime;

    @FXML TextArea teleopStratNotes;

    @FXML RadioButton hpIntegral;
    @FXML RadioButton hpIdeal;
    @FXML RadioButton hpNoPref;

    @FXML RadioButton sticksStrat;
    @FXML RadioButton prefStrat;
    @FXML RadioButton flexStrat;

    @FXML TextArea notes;

    @Override
    public Windows getPreviousPage() {
        return this.previousPage;
    }

    @Override
    public void setPreviousPage(Windows prev) {
        this.previousPage = prev;
    }

    @Override
    public AnchorPane getBasePane() {
        return basePane;
    }

    @Override
    public Windows getName() {
        return Windows.pitDataEntry;
    }

    @Override
    public <T extends Application & ControlInterface> T getThis() {
        return (T) this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Lib.memeStart(primaryStage, FXMLLoader.load(getClass().getResource(this.getName().toString())));
    }

    @Override
    public void handleBack(ActionEvent event){
        ControlInterface.super.handleBack(event);
    }

    @FXML
    public void handleSubmit(ActionEvent event){
        // link to Pit.java

        Pit tempPit = new Pit();

        tempPit.teamNumber = Integer.valueOf(this.teamNumber.getText());
        tempPit.teamName = this.teamName.getText();
        tempPit.scoutName = this.scoutName.getText();
        tempPit.level2Climb = this.level2.isSelected();
        tempPit.level3Climb = this.level3.isSelected();
        tempPit.intakeType = (this.cargoIntake.isSelected() && this.hatchIntake.isSelected())
                                ? "Both" : (this.cargoIntake.isSelected() ? "Cargo"
                                    : (this.hatchIntake.isSelected() ? "Hatch" : "None"));
        tempPit.rocketLevel = (this.rocketLevel3.isSelected() ? 3
                                : (this.rocketLevel2.isSelected() ? 2
                                : (this.rocketLevel1.isSelected() ? 1 : 0)));
        tempPit.mechIssues = (int)Math.floor(this.mechanicalIssues.getValue());
        tempPit.cam = this.hasCamera.isSelected();
        tempPit.preset = this.usesPresets.isSelected();
        tempPit.sense = this.hasSensor.isSelected();
        tempPit.reach = this.reachCargo.isSelected();
        tempPit.ramp = this.rampbot.isSelected();
        tempPit.nicknames = Lib.stringToArray(this.nicknames.getText(), false);

        tempPit.startHab = (this.startHab2.isSelected() ? 2 : (this.startHab1.isSelected() ? 1: 0));
        tempPit.driverControl = this.driverControl.isSelected();
        tempPit.pathing = this.pathing.isSelected();
        tempPit.noControl = this.noControl.isSelected();
        tempPit.autoStrats = new ArrayList<String>(Arrays.asList(
                this.autoCloseRocketHatch.getText(),
                this.autoFarRocketHatch.getText(),
                this.autoFrontShipHatch.getText(),
                this.autoOtherShipHatch.getText(),
                this.autoShipCargo.getText(),
                this.autoline.getText(),
                this.autoMultiPiece.getText(),
                this.autoNoStrat.getText(),
                this.autoOtherStrat.getText()
        ));
        tempPit.autoNotes = this.autoStratNotes.getText();

        tempPit.prefHatch = this.prefHatch.isSelected();
        tempPit.prefCargo = this.prefCargo.isSelected();
        tempPit.ppm = (int)this.piecesPerMatch.getValue();
        tempPit.teleStrats = new ArrayList<>(Arrays.asList(
                this.teleopShipCargo.getText(),
                this.teleopShipHatch.getText(),
                this.teleopRocketCargo.getText(),
                this.teleopRocketHatch.getText(),
                this.teleopDefense.getText(),
                this.teleopMixed.getText(),
                this.teleopFlex.getText(),
                this.teleopStratOther.getText()
        ));
        tempPit.cycleTime = (int)this.cycleTime.getValue();
        tempPit.teleNotes = this.teleopStratNotes.getText();

        tempPit.hpPref = (this.hpIntegral.isSelected() ? "Integral"
                            : (this.hpIdeal.isSelected() ? "Ideal"
                            : "Unnecessary"));
        tempPit.stratPref = (this.sticksStrat.isSelected() ? "Strong"
                            : (this.prefStrat.isSelected() ? "Preferred"
                            : "Flexible"));
        tempPit.notes = this.notes.getText();




        Lib.savePit(tempPit, Main.currentSession.eventDir);
        Lib.savePitData(tempPit, Main.currentSession.eventDir);
        Main.openPits.add(tempPit);
        tempPit.s3ndToTxt(Main.currentSession.eventDir+"pit/");
        System.out.println("Yes");

        Lib.pageChangeRequest(Windows.pitLaunch, false, this.getThis());
    }

}