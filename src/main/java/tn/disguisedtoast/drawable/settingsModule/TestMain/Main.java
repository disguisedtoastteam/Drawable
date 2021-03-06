package tn.disguisedtoast.drawable.settingsModule.TestMain;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.disguisedtoast.drawable.settingsModule.controllers.SettingsViewController;

public class Main extends Application {
    public static void main(String args[]){
        launch(args);
    }

    public static Stage globalStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        globalStage = primaryStage;

        /*FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/settingsViews/SettingsView.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        //primaryStage.setResizable(false);
        primaryStage.show();*/
        SettingsViewController.showStage(System.getProperty("user.dir") + "/src/main/RelatedFiles/generated_views/pages/page1");
    }
}
