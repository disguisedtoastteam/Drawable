package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {




    public void initialize(URL location, ResourceBundle resources) {

    }

    public void handleAction(ActionEvent actionEvent) throws GitAPIException, IOException {
        System.out.println("we cloning nigga");
        //cloneRepo("https://github.com/selimmouelhi/projetdbll.git", "/Users/selimmouelhi/Desktop/holymoly6");
       //cloneReposecond();


        Parent root = FXMLLoader.load(getClass().getResource("/layouts/ConfigurationFxml.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Configuration");
        stage.setScene(new Scene(root));
        stage.show();


    }





}
