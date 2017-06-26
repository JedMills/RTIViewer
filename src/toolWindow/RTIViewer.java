package toolWindow;

import bookmarks.BookmarkCreator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import openGLWindow.RTIWindowHSH;
import openGLWindow.RTIWindowLRGB;
import openGLWindow.RTIWindow;
import openGLWindow.RTIWindowRGB;
import ptmCreation.*;
import utils.Utils;

import java.util.ArrayList;

/**
 * Created by Jed on 29-May-17.
 */
public class RTIViewer extends Application {

    public static Stage primaryStage;
    private static Scene mainScene;

    public static int width = 350 ;
    public static int height = 700;

    public static Utils.Vector2f globalLightPos = new Utils.Vector2f(0, 0);
    public static double globalDiffGainVal = FilterParamsPane.INITIAL_DIFF_GAIN_VAL;
    public static double globalDiffColourVal = FilterParamsPane.INITIAL_DIFF_COLOUR_VAL;
    public static double globalSpecularityVal = FilterParamsPane.INITIAL_SPEC_VAL;
    public static double globalHighlightSizeVal = FilterParamsPane.INITIAL_HIGHLIGHT_VAL;
    public static double globalNormUnMaskGain = FilterParamsPane.INITIAL_NORM_UN_MASK_GAIN_VAL;
    public static double globalNormUnMaskEnv = FilterParamsPane.INITIAL_NORM_UN_MASK_ENV_VAL;
    public static double globalImgUnMaskGain = FilterParamsPane.INITIAL_IMG_UN_MASK_GAIN_VAL;
    public static double globalCoeffUnMaskGain = FilterParamsPane.INITIAL_COEFF_UN_MASK_GAIN_VAL;

    private static LightControlGroup lightControlGroup;
    private static FilterParamsPane paramsPane;
    private static BottomTabPane bottomTabPane;
    public static FlowPane flowPane;

    public enum GlobalParam{DIFF_GAIN, DIFF_COLOUR, SPECULARITY, HIGHTLIGHT_SIZE, NORM_UN_MASK_GAIN, NORM_UN_MASK_ENV,
                            IMG_UN_MASK_GAIN, COEFF_UN_MASK_GAIN;}


    public enum ShaderProgram{DEFAULT, NORMALS, DIFF_GAIN, SPEC_ENHANCE, NORM_UNSHARP_MASK, IMG_UNSHARP_MASK,
                                COEFF_UN_MASK;}

    public enum ViewerTheme{DEFAULT, METRO_DARK, METRO_LIGHT;}

    public static ShaderProgram currentProgram = ShaderProgram.DEFAULT;

    public static RTIWindow selectedWindow;

    public static Alert entryAlert;
    public static Alert fileReadingAlert;
    public static final FileChooser fileChooser = new FileChooser();

    private static ArrayList<RTIWindow> RTIWindows = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        RTIViewer.primaryStage = primaryStage;
        setupPrimaryStage();

        createAlerts();

        mainScene = createScene(primaryStage);

        mainScene.getStylesheets().add("stylesheets/default.css");

        primaryStage.setScene(mainScene);

        primaryStage.show();
        primaryStage.sizeToScene();
    }


    private void setupPrimaryStage(){
        RTIViewer.primaryStage.setResizable(true);

        RTIViewer.primaryStage.setMinWidth(300);
        RTIViewer.primaryStage.setMinHeight(600);
        RTIViewer.primaryStage.setMaxWidth(600);
        RTIViewer.primaryStage.setMaxHeight(1000);


        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                closeEverything();
            }
        });

        this.primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resizeGUI();
            }
        });

        this.primaryStage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resizeGUI();
            }
        });

        primaryStage.setTitle("RTI Viewer");
    }


    private void createAlerts(){
        entryAlert = new Alert(Alert.AlertType.WARNING);
        entryAlert.setTitle("Invalid Entry");

        fileReadingAlert = new Alert(Alert.AlertType.ERROR);
        entryAlert.setTitle("PTM File Reading Error");
        BookmarkCreator.createDialog();
    }

    private Scene createScene(Stage primaryStage){
        flowPane = new FlowPane();
        flowPane.setId("mainSceneFlowPane");
        Scene scene = new Scene(flowPane, width, height);

        MenuBar menuBar = new TopMenuBar(primaryStage);
        flowPane.getChildren().add(menuBar);

        lightControlGroup = new LightControlGroup(this, primaryStage, flowPane);
        flowPane.getChildren().add(lightControlGroup);

        paramsPane = new FilterParamsPane(this, scene);
        flowPane.getChildren().add(paramsPane);

        bottomTabPane = new BottomTabPane(this, scene);
        flowPane.getChildren().add(bottomTabPane);

        flowPane.setMargin(lightControlGroup, new Insets(10, 0, 5, 0));
        flowPane.setMargin(paramsPane, new Insets(5, 0, 5, 0));
        flowPane.setMargin(bottomTabPane, new Insets(5, 0, 0, 0));
        flowPane.setAlignment(Pos.TOP_CENTER);
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }


    public static void setGlobalVal(GlobalParam param, double value){
        if(param.equals(GlobalParam.DIFF_GAIN)){globalDiffGainVal = value;}
        else if(param.equals(GlobalParam.DIFF_COLOUR)){globalDiffColourVal = value;}
        else if(param.equals(GlobalParam.SPECULARITY)){globalSpecularityVal = value;}
        else if(param.equals(GlobalParam.HIGHTLIGHT_SIZE)){globalHighlightSizeVal = value;}
        else if(param.equals(GlobalParam.NORM_UN_MASK_GAIN)){globalNormUnMaskGain = value;}
        else if(param.equals(GlobalParam.NORM_UN_MASK_ENV)){globalNormUnMaskEnv = value;}
        else if(param.equals(GlobalParam.IMG_UN_MASK_GAIN)){globalImgUnMaskGain = value;}
        else if(param.equals(GlobalParam.COEFF_UN_MASK_GAIN)){globalCoeffUnMaskGain = value;}
    }

    public static void createNewPTMWindow(RTIObject RTIObject){
        try {
            if(RTIObject instanceof PTMObjectRGB) {
                RTIWindow rtiWindow = new RTIWindowRGB((PTMObjectRGB) RTIObject);
                Thread thread = new Thread(rtiWindow);
                thread.start();
                RTIWindows.add(rtiWindow);
            }else if(RTIObject instanceof PTMObjectLRGB){
                RTIWindow rtiWindow = new RTIWindowLRGB((PTMObjectLRGB) RTIObject);
                Thread thread = new Thread(rtiWindow);
                thread.start();
                RTIWindows.add(rtiWindow);
            }else if(RTIObject instanceof RTIObjectHSH){
                RTIWindow rtiWindow = new RTIWindowHSH((RTIObjectHSH) RTIObject);
                Thread thread = new Thread(rtiWindow);
                thread.start();
                RTIWindows.add(rtiWindow);
            }
        }catch(Exception e){
            fileReadingAlert.setContentText("Couldn't compile OpenGL shader: " + e.getMessage());
            fileReadingAlert.showAndWait();
        }
    }

    public static void updateWindowFilter(String filterType){
        ShaderProgram programToSet = ShaderProgram.DEFAULT;

        if(filterType.equals("Default view")){programToSet = ShaderProgram.DEFAULT;}
        else if(filterType.equals("Normals visualisation")){programToSet = ShaderProgram.NORMALS;}
        else if(filterType.equals("Diffuse gain")){programToSet = ShaderProgram.DIFF_GAIN;}
        else if(filterType.equals("Specular enhancement")){programToSet = ShaderProgram.SPEC_ENHANCE;}
        else if(filterType.equals("Normal unsharp masking")){programToSet = ShaderProgram.NORM_UNSHARP_MASK;}
        else if(filterType.equals("Image unsharp masking")){programToSet = ShaderProgram.IMG_UNSHARP_MASK;}
        else if(filterType.equals("Coefficient unsharp masking")){programToSet = ShaderProgram.COEFF_UN_MASK;}

        currentProgram = programToSet;

        for(RTIWindow RTIWindow : RTIWindows){
            RTIWindow.setCurrentProgram(programToSet);
        }
    }


    public static void removeWindow(RTIWindow window){
        for(RTIWindow RTIWindow : RTIWindows){
            if (RTIWindow.equals(window)){
                RTIWindows.remove(window);
                setNoFocusedWindow();
                break;
            }
        }
    }

    public static void setCursor(Cursor cursor){
        mainScene.setCursor(cursor);
    }


    public static void setFocusedWindow(RTIWindow rtiWindow){
        selectedWindow = rtiWindow;
        bottomTabPane.updateSelectedWindow(rtiWindow);
        bottomTabPane.updateViewportRect(rtiWindow.getViewportX(),
                            rtiWindow.getViewportY(), rtiWindow.getImageScale());
    }

    private static void setNoFocusedWindow(){
        bottomTabPane.setNoFocusedWindow();

        selectedWindow = null;
    }


    private void resizeGUI(){
        lightControlGroup.updateSize(primaryStage.getWidth(), primaryStage.getHeight());
        paramsPane.updateSize(primaryStage.getWidth(), primaryStage.getHeight());
        bottomTabPane.updateSize(primaryStage.getWidth(), primaryStage.getHeight());
        if(selectedWindow != null){
            bottomTabPane.updateViewportRect(   selectedWindow.getViewportX(),
                                                selectedWindow.getViewportY(),
                                                selectedWindow.getImageScale());
        }
    }


    public static void updateViewportPos(RTIWindow RTIWindow, Float x, Float y, float imageScale){
        if(RTIWindow.equals(selectedWindow)) {
            bottomTabPane.updateViewportRect(x, y, imageScale);
        }
    }


    public static int getHeight() {
        return height;
    }

    public static void closeEverything(){
        for(RTIWindow RTIWindow : RTIWindows){
            RTIWindow.setShouldClose(true);
        }
        Platform.exit();
    }

    public static void closeCurrentWindow(){
        if(selectedWindow != null){
            selectedWindow.setShouldClose(true);
            removeWindow(selectedWindow);
        }
    }

    public static void setFocusSave(){
        bottomTabPane.getSelectionModel().select(2);
        bottomTabPane.requestFocus();
    }


    public static void setTheme(ViewerTheme theme){

        mainScene.getStylesheets().clear();
        RTICreator.setLoadingDialogTheme(theme);

        if(theme.equals(ViewerTheme.DEFAULT)){
            mainScene.getStylesheets().add("stylesheets/default.css");
        }else if(theme.equals(ViewerTheme.METRO_DARK)){
            mainScene.getStylesheets().add("stylesheets/metroDark.css");
        }else if(theme.equals(ViewerTheme.METRO_LIGHT)){
            mainScene.getStylesheets().add("stylesheets/metroLight.css");
        }


    }
}
