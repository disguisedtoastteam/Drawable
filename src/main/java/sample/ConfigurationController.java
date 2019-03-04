package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;

public class ConfigurationController implements Initializable {


    static String path ;

    @FXML
    private TextField packageName;
    @FXML
    private TextField projectName;



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
        execCommand(newPath);




    }
    public void initialize(URL location, ResourceBundle resources) {

    }



    public  void cloneRepo(String repositoryUrl, String gitLocalRepositoryPath) throws GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(repositoryUrl)
                .setDirectory(new File(gitLocalRepositoryPath))
                .setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out)));
        cloneCommand.call();
        showAlert();


    }

    public  void showAlert(){


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cloning Done");
        alert.setHeaderText("Finished Clonging");

        ButtonType buttonTypeOne = new ButtonType("OKAY");
        alert.getButtonTypes().setAll(buttonTypeOne);


        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            alert.close();
        }

    }


     public void execCommand( String directoryPath) throws IOException {

         String PATH =  System.getProperty("user.dir");
         System.out.println(PATH);
         String scriptName = PATH+"/Replace.py";
         String command = "python "+ scriptName + " " + directoryPath +" "+ packageName.getText();
         String commands[] = new String[]{command};

         Process p = Runtime.getRuntime().exec(command  );



         /*String command = "find ./ -type d -exec perl -pi -e  's/com.example.myapplication/com.example.Test/g' {} \\;\n";
         try
         {
             Process process = Runtime.getRuntime().exec(command);
             Scanner kb = new Scanner(process.getInputStream());
         } catch (IOException e)
         {
             e.printStackTrace();
         }
        */
     }
}
