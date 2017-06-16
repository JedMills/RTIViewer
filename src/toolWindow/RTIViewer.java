package toolWindow;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import openGLWindow.PTMWindow;
import openGLWindow.PTMWindowLRGB;
import openGLWindow.PTMWindowRGB;
import org.lwjgl.system.CallbackI;
import ptmCreation.PTMObject;
import ptmCreation.PTMObjectLRGB;
import ptmCreation.PTMObjectRGB;
import utils.Utils;

import java.util.ArrayList;

/**
 * Created by Jed on 29-May-17.
 */
public class RTIViewer extends Application {

    public static Stage primaryStage;
    private static Scene mainScene;

    public static int width = 400 ;
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

    private LightControlGroup lightControlGroup;
    private ComboBox<String> filterTypeBox;
    private FilterParamsPane paramsPane;
    private static BottomTabPane bottomTabPane;
    public FlowPane flowPane;

    public enum GlobalParam{DIFF_GAIN, DIFF_COLOUR, SPECULARITY, HIGHTLIGHT_SIZE, NORM_UN_MASK_GAIN, NORM_UN_MASK_ENV,
                            IMG_UN_MASK_GAIN, COEFF_UN_MASK_GAIN;}


    public enum ShaderProgram{DEFAULT, NORMALS, DIFF_GAIN, SPEC_ENHANCE, NORM_UNSHARP_MASK, IMG_UNSHARP_MASK,
                                COEFF_UN_MASK;}

    public static ShaderProgram currentProgram = ShaderProgram.DEFAULT;

    public static PTMWindow selectedWindow;

    public static Alert entryAlert;
    public static Alert fileReadingAlert;
    public final FileChooser fileChooser = new FileChooser();

    private static ArrayList<PTMWindow> ptmWindows = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        this.primaryStage.setMinWidth(300);
        this.primaryStage.setMinHeight(600);
        this.primaryStage.setMaxWidth(600);
        this.primaryStage.setMaxHeight(1000);


        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                for(PTMWindow ptmWindow : ptmWindows){
                    ptmWindow.setShouldClose(true);
                }
                Platform.exit();
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

        createAlerts();
        MenuBarListener.init(this);

        mainScene = createScene(primaryStage);

        primaryStage.setScene(mainScene);

        primaryStage.setResizable(true);
        primaryStage.show();
        resizeGUI();
    }


    private void createAlerts(){
        entryAlert = new Alert(Alert.AlertType.WARNING);
        entryAlert.setTitle("Invalid Entry");

        fileReadingAlert = new Alert(Alert.AlertType.ERROR);
        entryAlert.setTitle("PTM File Reading Error");
    }

    private Scene createScene(Stage primaryStage){
        flowPane = new FlowPane();
        Scene scene = new Scene(flowPane);

        MenuBar menuBar = createMenuBar(primaryStage);
        flowPane.getChildren().add(menuBar);

        lightControlGroup = new LightControlGroup(this, primaryStage, flowPane);
        flowPane.getChildren().add(lightControlGroup);

        filterTypeBox = createFilterChoiceBox();
        flowPane.getChildren().add(filterTypeBox);

        paramsPane = new FilterParamsPane(this, scene, filterTypeBox);
        flowPane.getChildren().add(paramsPane);

        bottomTabPane = new BottomTabPane(this, scene);
        flowPane.getChildren().add(bottomTabPane);

        flowPane.setMargin(lightControlGroup, new Insets(10, 0, 0, 0));
        flowPane.setMargin(filterTypeBox, new Insets(10, 0, 0, 0));
        flowPane.setMargin(paramsPane, new Insets(20, 0, 0, 0));
        flowPane.setMargin(bottomTabPane, new Insets(20, 0, 0, 0));
        flowPane.setAlignment(Pos.TOP_CENTER);
        return scene;
    }


    private MenuBar createMenuBar(Stage primaryStage){
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        MenuItem open = new MenuItem("Open");
        open.setOnAction(MenuBarListener.getInstance());
        open.setId("open");
        MenuItem save = new MenuItem("Save");
        save.setOnAction(MenuBarListener.getInstance());
        save.setId("save");
        MenuItem close = new MenuItem("Close");
        close.setOnAction(MenuBarListener.getInstance());
        close.setId("close");
        MenuItem closePTMWindow = new MenuItem("Close image");
        closePTMWindow.setOnAction(MenuBarListener.getInstance());
        closePTMWindow.setId("closePTMWindow");

        menuFile.getItems().addAll(open, close, save, closePTMWindow);

        Menu menuEdit = new Menu("Edit");
        MenuItem preferences = new MenuItem("Preferences");
        preferences.setOnAction(MenuBarListener.getInstance());
        preferences.setId("preferences");

        menuEdit.getItems().addAll(preferences);

        Menu menuView = new Menu("View");
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

        return menuBar;
    }



    private ComboBox<String> createFilterChoiceBox(){
        ObservableList<String> options = FXCollections.observableArrayList(
                "Default view",
                "Normals visualisation",
                "Diffuse gain",
                "Specular enhancement",
                "Normal unsharp masking",
                "Image unsharp masking",
                "Coefficient unsharp masking"
        );
        ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().select(0);

        comboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                paramsPane.setCurrentFilter(comboBox.getSelectionModel().getSelectedItem());
                updateWindowFilter(comboBox.getSelectionModel().getSelectedItem());
            }
        });

        return comboBox;
    }


    public static void main(String[] args) {
        launch(args);
    }


    public void setGlobalVal(GlobalParam param, double value){
        if(param.equals(GlobalParam.DIFF_GAIN)){globalDiffGainVal = value;}
        else if(param.equals(GlobalParam.DIFF_COLOUR)){globalDiffColourVal = value;}
        else if(param.equals(GlobalParam.SPECULARITY)){globalSpecularityVal = value;}
        else if(param.equals(GlobalParam.HIGHTLIGHT_SIZE)){globalHighlightSizeVal = value;}
        else if(param.equals(GlobalParam.NORM_UN_MASK_GAIN)){globalNormUnMaskGain = value;}
        else if(param.equals(GlobalParam.NORM_UN_MASK_ENV)){globalNormUnMaskEnv = value;}
        else if(param.equals(GlobalParam.IMG_UN_MASK_GAIN)){globalImgUnMaskGain = value;}
        else if(param.equals(GlobalParam.COEFF_UN_MASK_GAIN)){globalCoeffUnMaskGain = value;}
    }

    public static void createNewPTMWindow(PTMObject ptmObject){
        try {
            if(ptmObject instanceof PTMObjectRGB) {
                PTMWindow ptmWindow = new PTMWindowRGB((PTMObjectRGB) ptmObject);
                Thread thread = new Thread(ptmWindow);
                thread.start();
                ptmWindows.add(ptmWindow);
            }else if(ptmObject instanceof PTMObjectLRGB){
                PTMWindow ptmWindow = new PTMWindowLRGB((PTMObjectLRGB) ptmObject);
                Thread thread = new Thread(ptmWindow);
                thread.start();
                ptmWindows.add(ptmWindow);
            }
        }catch(Exception e){
            fileReadingAlert.setContentText("Couldn't compile OpenGL shader: " + e.getMessage());
            fileReadingAlert.showAndWait();
        }
    }

    private void updateWindowFilter(String filterType){
        ShaderProgram programToSet = ShaderProgram.DEFAULT;

        if(filterType.equals("Default view")){programToSet = ShaderProgram.DEFAULT;}
        else if(filterType.equals("Normals visualisation")){programToSet = ShaderProgram.NORMALS;}
        else if(filterType.equals("Diffuse gain")){programToSet = ShaderProgram.DIFF_GAIN;}
        else if(filterType.equals("Specular enhancement")){programToSet = ShaderProgram.SPEC_ENHANCE;}
        else if(filterType.equals("Normal unsharp masking")){programToSet = ShaderProgram.NORM_UNSHARP_MASK;}
        else if(filterType.equals("Image unsharp masking")){programToSet = ShaderProgram.IMG_UNSHARP_MASK;}
        else if(filterType.equals("Coefficient unsharp masking")){programToSet = ShaderProgram.COEFF_UN_MASK;}

        currentProgram = programToSet;

        for(PTMWindow ptmWindow : ptmWindows){
            ptmWindow.setCurrentProgram(programToSet);
        }
    }


    public static void removeWindow(PTMWindow window){
        for(PTMWindow ptmWindow : ptmWindows){
            if (ptmWindow.equals(window)){
                ptmWindows.remove(window);
                setNoFocusedWindow();
                break;
            }
        }
    }

    public static void setCursor(Cursor cursor){
        mainScene.setCursor(cursor);
    }


    public static void setFocusedWindow(PTMWindow ptmWindow){
        selectedWindow = ptmWindow;
        bottomTabPane.setFileText(ptmWindow.ptmObject.getFileName());
        bottomTabPane.setWidthText(String.valueOf(ptmWindow.ptmObject.getWidth()));
        bottomTabPane.setHeightText(String.valueOf(ptmWindow.ptmObject.getHeight()));

        if(ptmWindow instanceof PTMWindowRGB){
            bottomTabPane.setFormatText("PTM RGB");
        }else if(ptmWindow instanceof PTMWindowLRGB){
            bottomTabPane.setFormatText("PTM LRGB");
        }

        bottomTabPane.setPreviewImage(ptmWindow.ptmObject.previewImage);
    }

    private static void setNoFocusedWindow(){
        selectedWindow = null;
        bottomTabPane.setFileText("");
        bottomTabPane.setWidthText("");
        bottomTabPane.setHeightText("");
        bottomTabPane.setFormatText("");
        bottomTabPane.setDefaultImage();
    }


    private void resizeGUI(){
        lightControlGroup.updateSize(primaryStage.getWidth(), primaryStage.getHeight());
        paramsPane.updateSize(primaryStage.getWidth(), primaryStage.getHeight());
        bottomTabPane.updateSize(primaryStage.getWidth(), primaryStage.getHeight());
        resizeFilterChoiceBox();
    }

    private void resizeFilterChoiceBox(){
        filterTypeBox.setPrefWidth(primaryStage.getWidth() / 1.5);

        if(height < 700){
            filterTypeBox.setPrefHeight(10);
        }else if(height < 800){
            filterTypeBox.setPrefHeight(20);
        }else{
            filterTypeBox.setPrefHeight(30);
        }

    }
}
