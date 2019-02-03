package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfigurationController implements Initializable {


    static String path ;
    @FXML
    private PasswordField password;

    @FXML
    private TextField projectName;

    @FXML
    private TextField username;

    @FXML
    void ChooseFile(ActionEvent event) {
        DirectoryChooser fileChooser = new DirectoryChooser() ;
        File file = fileChooser.showDialog(null);

        if(file != null)
        {
            path = file.getAbsolutePath();
            System.out.println(path);


        }
        else{
            System.out.println("file is not valid");
        }

    }

    @FXML
    void Validate(ActionEvent event) throws GitAPIException, IOException {
        String newPath = path + "/" + projectName.getText() ;
        System.out.println(newPath);
        new File(newPath).mkdirs();
        cloneRepo("https://github.com/disguisedtoastteam/TestProject.git",newPath);




    }
    public void initialize(URL location, ResourceBundle resources) {

    }



    public static void cloneRepo(String repositoryUrl, String gitLocalRepositoryPath) throws GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(repositoryUrl)
                .setDirectory(new File(gitLocalRepositoryPath))
                .setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out)));
        cloneCommand.call();
        
    }
}
