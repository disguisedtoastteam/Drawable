package tn.disguisedtoast.drawable.homeModule.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import preview.PreviewScene;
import preview.StartScene;
import tn.disguisedtoast.drawable.homeModule.models.Page;
import tn.disguisedtoast.drawable.settingsModule.controllers.SettingsViewController;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HomeController implements StartScene.CameraButtonCallback, Initializable {
    @FXML
    public TextField search;
    @FXML
    public Button export;
    @FXML
    private AnchorPane addButtonPane;
    @FXML
    private HBox pagesPreviewHBox;

    public static Stage primaryStage;
    private List<PageCellViewController> pageCellViewControllers;
    private String pagesPath = System.getProperty("user.dir") + "\\src\\main\\RelatedFiles\\generated_views\\pages";

    private PageCellViewController.PageClickCallback pageClickCallback = page -> {
        SettingsViewController.showStage(page.getFolderName());
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ((Button) this.addButtonPane.getChildren().get(0)).setOnAction(event -> {
            //TODO Go to stream page
        });

        //Loading pages
        pageCellViewControllers = new ArrayList<>();
        List<Page> pages = loadPages();
        for (Page page : pages) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/homeLayouts/PageCellView.fxml"));
                VBox pagePane = loader.load();
                PageCellViewController pageCellViewController = loader.getController();
                pageCellViewController.setPage(page, pageClickCallback);
                pageCellViewControllers.add(pageCellViewController);

                pagesPreviewHBox.getChildren().addAll(pagePane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.search.setOnKeyReleased(event -> {
            if (this.search.getText().isEmpty()) {
                Platform.runLater(() -> {
                    this.pagesPreviewHBox.getChildren().clear();
                    this.pagesPreviewHBox.getChildren().add(addButtonPane);
                    this.pagesPreviewHBox.getChildren().addAll(
                            this.pageCellViewControllers.stream().map(
                                    pageCellViewController -> pageCellViewController.getPagePane()
                            ).collect(Collectors.toList())
                    );
                });
            } else {
                Platform.runLater(() -> {
                    this.pagesPreviewHBox.getChildren().clear();
                    this.pagesPreviewHBox.getChildren().addAll(
                            this.pageCellViewControllers.stream().filter(
                                    pageCellViewController -> pageCellViewController.getPage().getName().contains(this.search.getText())
                            ).map(
                                    pageCellViewController -> pageCellViewController.getPagePane()
                            ).collect(Collectors.toList())
                    );
                });
            }
        });

        /*pages = new ArrayList<>();
        imageList.setCellFactory(new PageCellFactory());
        loadPages();
        imageList.getItems().addAll(pages);

        this.search.setOnKeyReleased(event -> {
            List<Page> subPages = pages.stream().filter(page -> page.getName().contains(this.search.getText())).collect(Collectors.toList());
            System.out.println(subPages);
            Platform.runLater(() -> {
                imageList.getItems().clear();
                imageList.getItems().addAll(subPages);
            });
        });*/
    }

    @FXML
    public void New(ActionEvent event) {
        /*primaryStage = new Stage();
        primaryStage.setScene(new StartScene(this).getScene());
        primaryStage.setHeight(200);
        primaryStage.setWidth(400);
        primaryStage.setResizable(false);
        primaryStage.show();*/
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

    @FXML
    public void exportProject(ActionEvent event) {
        // String command ="cmd /c ionic start newProject";
        // Runtime rt = Runtime.getRuntime();
        /*ProcessBuilder processBuilder = new ProcessBuilder();
        // Windows
        processBuilder.command("cmd.exe", "/c", "ping -n 3 google.com");
        try {
           // rt.exec(new String[]{"cmd.exe","/c","ionic "});
            Process process = Runtime.getRuntime().exec(command);
            Scanner kb = new Scanner(process.getInputStream());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

        DirectoryChooser dc = new DirectoryChooser();
        //dc.showDialog(primaryStage);
        File f = dc.showDialog(primaryStage);
        String s = f.getAbsolutePath();
        System.out.println(s);

        ProcessBuilder processBuilder = new ProcessBuilder();
        TextField projectName = new TextField();
        System.setProperty("user.dir", s);

        // Windows
        processBuilder.command("cmd.exe", "/c", "ionic start testProject blank");

        try {

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("wait");
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private List<Page> loadPages() {
        File root = new File(System.getProperty("user.dir") + "\\src\\main\\RelatedFiles\\generated_views\\pages");
        String[] directories = root.list((dir, name) -> (new File(dir, name).isDirectory()));
        List<Page> pages = new ArrayList<>();
        for (String dir : directories) {
            try {
                JsonObject jsonObject = new JsonParser().parse(new FileReader(pagesPath + "/" + dir + "/conf.json")).getAsJsonObject();
                String pageName = jsonObject.get("page").getAsString();
                Page pg = new Page(pageName, pagesPath + "/" + dir);
                pages.add(pg);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pages;
    }
}
