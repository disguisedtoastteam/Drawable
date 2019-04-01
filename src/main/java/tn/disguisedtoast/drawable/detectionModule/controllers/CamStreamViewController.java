package tn.disguisedtoast.drawable.detectionModule.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.commons.io.FileUtils;
import tn.disguisedtoast.drawable.codeGenerationModule.ionic.generation.CodeGenerator;
import tn.disguisedtoast.drawable.codeGenerationModule.ionic.models.DetectedObject;
import tn.disguisedtoast.drawable.codeGenerationModule.ionic.models.exceptions.FailedToCreateHtmlFromIonApp;
import tn.disguisedtoast.drawable.codeGenerationModule.ionic.models.exceptions.NoDetectedObjects;
import tn.disguisedtoast.drawable.codeGenerationModule.ionic.models.exceptions.NoFramesDetected;
import tn.disguisedtoast.drawable.codeGenerationModule.shapeDetection.ShapeDetectionService;
import tn.disguisedtoast.drawable.detectionModule.testMain.Main;
import tn.disguisedtoast.drawable.previewModule.controllers.PreviewController;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class CamStreamViewController implements Initializable, UploadInterface {

    @FXML
    public BorderPane root;
    @FXML
    private StackPane camHolder;
    @FXML
    private HBox previewHolder;

    private WebCamController webCamController;
    private Date lastRequestDate;
    private ShapeDetectionService.UploadCallback mUploadCallback = new ShapeDetectionService.UploadCallback() {
        @Override
        public void onUploaded(List<DetectedObject> objects) {
            try {
                webCamController.drawObjects(objects);

                CodeGenerator.generateTempHtml(CodeGenerator.parse(objects));
                PreviewController.refreshForCamStream();

                retry();
            } catch (NoDetectedObjects | NoFramesDetected e) {
                e.printStackTrace();
                System.out.println("creating file failed");
                retry();
            } catch (JAXBException e) {
                e.printStackTrace();
                System.out.println("marshalling layout failed");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FailedToCreateHtmlFromIonApp failedToCreateHtmlFromIonApp) {
                failedToCreateHtmlFromIonApp.printStackTrace();
            }
        }
    };

    private void retry() {
        try {
            if (!webCamController.isShouldUpload()) {
                return;
            }
            long timeDiff = new Date().getTime() - lastRequestDate.getTime();
            if (timeDiff < 2000) {
                Thread.sleep(2000 - timeDiff);
            }
            File file = webCamController.getFileFromImage();
            ShapeDetectionService.upload(file, mUploadCallback);
            lastRequestDate = new Date();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private BorderPane drawingPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void init(int camIndex) {
        setWebCamHolder(camIndex);
        setPreviewHolder();
        Main.primaryStage.setScene(new Scene(root));
        Main.primaryStage.sizeToScene();
    }

    //used to call webcamstream into preview holder
    public void setWebCamHolder(int camIndex) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/layouts/detectionViews/WebCamHolder.fxml"));
            Pane pane = loader.load();
            loader.getLocation().openStream();
            webCamController = loader.getController();
            webCamController.init(camIndex, this);
            camHolder.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //used to call preview into preview holder
    public void setPreviewHolder(){
        String htmlString = null;
        try {
            String tempFilePath = System.getProperty("user.dir") + "\\src\\main\\RelatedFiles\\generated_views\\pages\\temp.html";
            File htmlTemplateFile = new File(CodeGenerator.class.getResource("/codeGenerationModule/preview_template.html").toURI());
            htmlString = FileUtils.readFileToString(htmlTemplateFile);

            //writing html file
            File newHtmlFile = new File(tempFilePath);
            FileUtils.writeStringToFile(newHtmlFile, htmlString);
            Node root = PreviewController.getView(tempFilePath, null);
            previewHolder.getChildren().addAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startUpload() {
        try {
            File file = webCamController.getFileFromImage();
            ShapeDetectionService.upload(file, mUploadCallback);
            lastRequestDate = new Date();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}