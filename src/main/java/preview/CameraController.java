package preview;

import code_generation.entities.DetectedObject;
import code_generation.entities.views.ConstraintLayout;
import code_generation.service.CodeGenerator;
import code_generation.service.ShapeDetectionService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CameraController {
    private FlowPane bottomCameraControlPane;
    private BorderPane root;
    private ImageView imgWebCamCapturedImage;
    private boolean stopCamera = false;
    ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
    private BorderPane webCamPane;
    private Button btnCamreaStop;
    private Button btnCamreaStart;
    private Frame frame;
    private int webcamIndex = -1;
    private Thread thread;
    private FrameGrabber grabber;
    private BufferedImage bufferedFrame;
    private Pane webcampPane;
    static ArrayList<DetectedObject> detectedObjects;

    private Date lastRequestDate;
    private ShapeDetectionService.UploadCallback mUploadCallback = new ShapeDetectionService.UploadCallback() {
        @Override
        public void onUploaded(List<DetectedObject> objects) {
            try {
                ConstraintLayout layout = CodeGenerator.parse(objects).getLayout();
                CodeGenerator.generateLayoutFile(layout);
                File file = getFileFromImage();
                long timeDiff = new Date().getTime() - lastRequestDate.getTime();
                if (timeDiff < 2000) {
                    Thread.sleep(2000 - timeDiff);
                }
                ShapeDetectionService.upload(file, mUploadCallback);
                lastRequestDate = new Date();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("creating file failed");
            } catch (JAXBException e) {
                e.printStackTrace();
                System.out.println("marshalling layout failed");
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Sleeping interrupted");
            }
        }
    };

    private File getFileFromImage() throws IOException {
        File file = new File("frame.jpg");
        ImageIO.write(bufferedFrame, "jpg", file);
        return file;
    }

    private boolean mDidUpload = false;

    public CameraController(int cameraIndex) throws IOException {
        root = new BorderPane();
        webCamPane = new BorderPane();


        imgWebCamCapturedImage = new ImageView();
           initilizingDetected().forEach(
                   d -> {


                       webcampPane = new Pane(imgWebCamCapturedImage, drawRectanglesUponDetecting(d));

                   }
           );

        webCamPane.setStyle("-fx-background-color: #ccc;");
        webCamPane.setCenter(webcampPane);
        root.setCenter(webCamPane);
        bottomCameraControlPane = new FlowPane();
        bottomCameraControlPane.setOrientation(Orientation.HORIZONTAL);
        bottomCameraControlPane.setAlignment(Pos.CENTER);
        bottomCameraControlPane.setHgap(20);
        bottomCameraControlPane.setVgap(10);
        bottomCameraControlPane.setPrefHeight(40);
        createCameraControls();
        root.setBottom(bottomCameraControlPane);
        root.setPrefHeight(660);
        root.setPrefWidth(700);

        this.webcamIndex = cameraIndex;

        grabber = new VideoInputFrameGrabber(webcamIndex);
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        startWebCamStream();
    }


    protected void startWebCamStream() {
        stopCamera = false;
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                //final OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
                //final OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
                final Java2DFrameConverter paintConverter = new Java2DFrameConverter();
                try {
                    while (!stopCamera) {
                        if ((frame = grabber.grab()) != null) {
                            //opencv_core.IplImage img = converter.convert(frame);
                            //opencv_core.cvFlip(img, img, 1);
                            //frame = grabberConverter.convert(img);
                            bufferedFrame = paintConverter.getBufferedImage(frame, 1);


                            if (!mDidUpload) {
                                ShapeDetectionService.upload(getFileFromImage(), mUploadCallback);
                                lastRequestDate = new Date();
                                mDidUpload = true;
                            }

                           final Image mainImage = SwingFXUtils.toFXImage(bufferedFrame, null);
                            imageProperty.set(mainImage);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        imgWebCamCapturedImage.imageProperty().bind(imageProperty);
    }

    private void createCameraControls() {
        btnCamreaStop = new Button();
        btnCamreaStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                stopWebCamCamera();
            }
        });
        btnCamreaStop.setText("Stop Camera");
        btnCamreaStart = new Button();
        btnCamreaStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                startWebCamCamera();
            }
        });
        btnCamreaStart.setDisable(true);
        btnCamreaStart.setText("Start Camera");
        bottomCameraControlPane.getChildren().add(btnCamreaStart);
        bottomCameraControlPane.getChildren().add(btnCamreaStop);
    }

    protected void startWebCamCamera() {
        stopCamera = false;
        startWebCamStream();
        btnCamreaStop.setDisable(false);
        btnCamreaStart.setDisable(true);
    }

    protected void stopWebCamCamera() {
        stopCamera = true;
        btnCamreaStart.setDisable(false);
        btnCamreaStop.setDisable(true);
    }

    public BufferedImage getBufferedFrame() {
        return bufferedFrame;
    }

    public BorderPane getRoot() {
        return root;
    }


    public Rectangle drawRectanglesUponDetecting(DetectedObject detectedObject){
        System.out.println(detectedObject.getScore());
        Rectangle r = new Rectangle();
        if(detectedObject == null){
            System.out.println("cant draw shit");
        }
        else{

            r.setX(detectedObject.getBox().getxMin());
            r.setY(detectedObject.getBox().getyMin());
            r.setWidth(detectedObject.getBox().getWidth());
            r.setHeight(detectedObject.getBox().getHeight());
            r.setFill(Color.TRANSPARENT);
            switch ((int)detectedObject.getClasse()){
                case 1 :
                    r.setStroke(Color.RED);
                case 2 :
                    r.setStroke(Color.YELLOW);
                case 3 :
                    r.setStroke(Color.GREEN);
                case 4 :
                    r.setStroke(Color.BLUE);
                    default:
                    r.setStroke(Color.BLACK);

            }

            r.setStrokeWidth(10);
            return  r;
        }

    return r;
    }


    //temporary just to test rectangles
    private ArrayList<DetectedObject> initilizingDetected() {
        List<DetectedObject> objects = new ArrayList<DetectedObject>();
        List<String> jsonList = new ArrayList<>();
        jsonList.add("[{\"box\": {\"ymin\": 0.025714846327900887, \"xmax\": 0.6734870672225952, \"ymax\": 0.1516139805316925, \"xmin\": 0.11724734306335449}, \"score\": 0.999972939491272, \"class\": 2.0}, {\"box\": {\"ymin\": 0.1873399019241333, \"xmax\": 0.6572803854942322, \"ymax\": 0.2848241627216339, \"xmin\": 0.09451477229595184}, \"score\": 0.8894684314727783, \"class\": 1.0}, {\"box\": {\"ymin\": 0.32904940843582153, \"xmax\": 0.6667465567588806, \"ymax\": 0.5112229585647583, \"xmin\": 0.45405611395835876}, \"score\": 0.9979177117347717, \"class\": 2.0}, {\"box\": {\"ymin\": 0.3343687951564789, \"xmax\": 0.4240373373031616, \"ymax\": 0.43329837918281555, \"xmin\": 0.14275193214416504}, \"score\": 0.8916385769844055, \"class\": 1.0}, {\"box\": {\"ymin\": 0.468723326921463, \"xmax\": 0.4361937642097473, \"ymax\": 0.5646662712097168, \"xmin\": 0.14424824714660645}, \"score\": 0.9951849579811096, \"class\": 1.0}, {\"box\": {\"ymin\": 0.7543667554855347, \"xmax\": 0.6917593479156494, \"ymax\": 0.9149238467216492, \"xmin\": 0.14663562178611755}, \"score\": 0.9999821186065674, \"class\": 2.0}]");
        jsonList.add("[{\"box\": {\"ymin\": 0.016395270824432373, \"xmax\": 0.7258040308952332, \"ymax\": 0.11139510571956635, \"xmin\": 0.062356315553188324}, \"score\": 0.9999881982803345, \"class\": 2.0}, {\"box\": {\"ymin\": 0.025009112432599068, \"xmax\": 0.7368699908256531, \"ymax\": 0.10822702944278717, \"xmin\": 0.32776495814323425}, \"score\": 0.9618628621101379, \"class\": 2.0}, {\"box\": {\"ymin\": 0.13623343408107758, \"xmax\": 0.7395766973495483, \"ymax\": 0.38101688027381897, \"xmin\": 0.5247385501861572}, \"score\": 0.9975408315658569, \"class\": 2.0}, {\"box\": {\"ymin\": 0.16348318755626678, \"xmax\": 0.4583187997341156, \"ymax\": 0.24609439074993134, \"xmin\": 0.07989010959863663}, \"score\": 0.8506920337677002, \"class\": 1.0}, {\"box\": {\"ymin\": 0.3143550455570221, \"xmax\": 0.510962963104248, \"ymax\": 0.430195689201355, \"xmin\": 0.06380912661552429}, \"score\": 0.8270204067230225, \"class\": 1.0}, {\"box\": {\"ymin\": 0.4802985191345215, \"xmax\": 0.6668537855148315, \"ymax\": 0.7226132154464722, \"xmin\": 0.1673123687505722}, \"score\": 0.9946135878562927, \"class\": 2.0}, {\"box\": {\"ymin\": 0.7992014288902283, \"xmax\": 0.6791974306106567, \"ymax\": 0.9526691436767578, \"xmin\": 0.08723928034305573}, \"score\": 0.9072731137275696, \"class\": 1.0}]");
        jsonList.add("[{\"box\": {\"ymin\": 0.012067390605807304, \"xmax\": 0.6467838287353516, \"ymax\": 0.09356971085071564, \"xmin\": 0.09848043322563171}, \"score\": 0.9999938011169434, \"class\": 2.0}, {\"box\": {\"ymin\": 0.10611597448587418, \"xmax\": 0.6688644289970398, \"ymax\": 0.18377980589866638, \"xmin\": 0.41415777802467346}, \"score\": 0.9705276489257812, \"class\": 1.0}, {\"box\": {\"ymin\": 0.10954340547323227, \"xmax\": 0.3605649769306183, \"ymax\": 0.29378557205200195, \"xmin\": 0.0997256264090538}, \"score\": 0.9981801509857178, \"class\": 2.0}, {\"box\": {\"ymin\": 0.21654166281223297, \"xmax\": 0.6746408343315125, \"ymax\": 0.2840975224971771, \"xmin\": 0.4199020564556122}, \"score\": 0.9778462052345276, \"class\": 1.0}, {\"box\": {\"ymin\": 0.33130982518196106, \"xmax\": 0.5925101041793823, \"ymax\": 0.40880081057548523, \"xmin\": 0.23642855882644653}, \"score\": 0.9942663311958313, \"class\": 1.0}, {\"box\": {\"ymin\": 0.43194296956062317, \"xmax\": 0.6656274795532227, \"ymax\": 0.5113154053688049, \"xmin\": 0.44246482849121094}, \"score\": 0.9909635782241821, \"class\": 1.0}, {\"box\": {\"ymin\": 0.5298413038253784, \"xmax\": 0.6755447387695312, \"ymax\": 0.7721793055534363, \"xmin\": 0.3784107565879822}, \"score\": 0.9782417416572571, \"class\": 2.0}, {\"box\": {\"ymin\": 0.8122527003288269, \"xmax\": 0.6851688027381897, \"ymax\": 0.9762448668479919, \"xmin\": 0.06944391876459122}, \"score\": 0.999618649482727, \"class\": 2.0}]");
        jsonList.add("[{\"box\": {\"ymin\": 0.01581699028611183, \"xmax\": 0.6475163698196411, \"ymax\": 0.12089867889881134, \"xmin\": 0.08967076987028122}, \"score\": 0.9999881982803345, \"class\": 2.0}, {\"box\": {\"ymin\": 0.12883709371089935, \"xmax\": 0.6634530425071716, \"ymax\": 0.23044073581695557, \"xmin\": 0.40666040778160095}, \"score\": 0.9940405488014221, \"class\": 1.0}, {\"box\": {\"ymin\": 0.1533840447664261, \"xmax\": 0.3469344675540924, \"ymax\": 0.3606189489364624, \"xmin\": 0.0940157100558281}, \"score\": 0.999599277973175, \"class\": 2.0}, {\"box\": {\"ymin\": 0.26969030499458313, \"xmax\": 0.676900327205658, \"ymax\": 0.35914477705955505, \"xmin\": 0.4122067093849182}, \"score\": 0.9947493672370911, \"class\": 1.0}, {\"box\": {\"ymin\": 0.41563326120376587, \"xmax\": 0.5954150557518005, \"ymax\": 0.5158135890960693, \"xmin\": 0.2366953343153}, \"score\": 0.9921473860740662, \"class\": 1.0}, {\"box\": {\"ymin\": 0.5463553667068481, \"xmax\": 0.6632205247879028, \"ymax\": 0.649465799331665, \"xmin\": 0.44401276111602783}, \"score\": 0.9888402819633484, \"class\": 1.0}, {\"box\": {\"ymin\": 0.6770913600921631, \"xmax\": 0.6707838177680969, \"ymax\": 0.9581685066223145, \"xmin\": 0.3807696998119354}, \"score\": 0.9914342164993286, \"class\": 2.0}]");
        jsonList.add("[{\"box\": {\"ymin\": 0.03821076825261116, \"xmax\": 0.7488583922386169, \"ymax\": 0.13521051406860352, \"xmin\": 0.1799294650554657}, \"score\": 0.9999219179153442, \"class\": 2.0}, {\"box\": {\"ymin\": 0.16840465366840363, \"xmax\": 0.7196097373962402, \"ymax\": 0.24481137096881866, \"xmin\": 0.5433585047721863}, \"score\": 0.817418098449707, \"class\": 2.0}, {\"box\": {\"ymin\": 0.16856978833675385, \"xmax\": 0.4790855050086975, \"ymax\": 0.243069589138031, \"xmin\": 0.20463848114013672}, \"score\": 0.9988706707954407, \"class\": 1.0}, {\"box\": {\"ymin\": 0.2849094271659851, \"xmax\": 0.5940459966659546, \"ymax\": 0.5119214653968811, \"xmin\": 0.3065434396266937}, \"score\": 0.9984373450279236, \"class\": 2.0}, {\"box\": {\"ymin\": 0.5362015962600708, \"xmax\": 0.8081621527671814, \"ymax\": 0.6060941815376282, \"xmin\": 0.4857512414455414}, \"score\": 0.9022560119628906, \"class\": 1.0}, {\"box\": {\"ymin\": 0.6555392146110535, \"xmax\": 0.6144459247589111, \"ymax\": 0.7385701537132263, \"xmin\": 0.40026524662971497}, \"score\": 0.9646076560020447, \"class\": 1.0}, {\"box\": {\"ymin\": 0.8161802887916565, \"xmax\": 0.7786926627159119, \"ymax\": 0.9809336066246033, \"xmin\": 0.17182320356369019}, \"score\": 0.9999579191207886, \"class\": 2.0}]");
        jsonList.add("[{\"box\": {\"ymin\": 0.040865182876586914, \"xmax\": 0.7273321747779846, \"ymax\": 0.171548992395401, \"xmin\": 0.16464856266975403}, \"score\": 0.9998561143875122, \"class\": 2.0}, {\"box\": {\"ymin\": 0.2122548669576645, \"xmax\": 0.4740588068962097, \"ymax\": 0.3058491349220276, \"xmin\": 0.20741981267929077}, \"score\": 0.9979639053344727, \"class\": 1.0}, {\"box\": {\"ymin\": 0.21429763734340668, \"xmax\": 0.7280877232551575, \"ymax\": 0.3078218698501587, \"xmin\": 0.54476398229599}, \"score\": 0.8325586915016174, \"class\": 1.0}, {\"box\": {\"ymin\": 0.3722515106201172, \"xmax\": 0.5906668901443481, \"ymax\": 0.6535466909408569, \"xmin\": 0.30715855956077576}, \"score\": 0.9962480664253235, \"class\": 2.0}, {\"box\": {\"ymin\": 0.692771315574646, \"xmax\": 0.812876284122467, \"ymax\": 0.7865605354309082, \"xmin\": 0.48643046617507935}, \"score\": 0.9434722661972046, \"class\": 1.0}, {\"box\": {\"ymin\": 0.8430420756340027, \"xmax\": 0.6217881441116333, \"ymax\": 0.9570510387420654, \"xmin\": 0.399746298789978}, \"score\": 0.9260500073432922, \"class\": 1.0}]");


        for (String json : jsonList) {
            System.out.println("inside for loop");
            objects = new Gson().fromJson(json, new TypeToken<List<DetectedObject>>() {
            }.getType());
        }
        return (ArrayList<DetectedObject>) objects;
    }
}
