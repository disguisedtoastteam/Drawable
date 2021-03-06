package preview;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.disguisedtoast.drawable.detectionModule.controllers.CamChooserController;

import java.io.IOException;

public class App extends Application implements CamChooserController.CameraButtonCallback {

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setScene(new Scene(new CamChooserController(this).getRoot()));
        primaryStage.setHeight(200);
        primaryStage.setWidth(400);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void onButtonClicked(int webcamIndex) {
        if (webcamIndex != -1) {
            try {
                PreviewScene previewScene = new PreviewScene(webcamIndex);
                primaryStage.setScene(previewScene.getScene());
                primaryStage.setHeight(700);
                primaryStage.setWidth(1300);
                primaryStage.centerOnScreen();
                previewScene.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
