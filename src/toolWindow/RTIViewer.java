package toolWindow;

import bookmarks.Bookmark;
import bookmarks.BookmarkManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import openGLWindow.RTIWindowHSH;
import openGLWindow.RTIWindowLRGB;
import openGLWindow.RTIWindow;
import openGLWindow.RTIWindowRGB;
import ptmCreation.*;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by Jed on 29-May-17.
 */
public class RTIViewer extends Application {

    public static Stage primaryStage;
    private static Scene mainScene;

    public static int width = 350 ;
    public static int height = 700;

    public static Utils.Vector2f globalLightPos = new Utils.Vector2f(0, 0);
    public static SimpleDoubleProperty globalDiffGainVal = new SimpleDoubleProperty(FilterParamsPane.INITIAL_DIFF_GAIN_VAL);

    public static SimpleDoubleProperty globalDiffColourVal = new SimpleDoubleProperty(FilterParamsPane.INITIAL_DIFF_COLOUR_VAL);

    public static SimpleDoubleProperty globalSpecularityVal = new SimpleDoubleProperty(FilterParamsPane.INITIAL_SPEC_VAL);

    public static SimpleDoubleProperty globalHighlightSizeVal = new SimpleDoubleProperty(FilterParamsPane.INITIAL_HIGHLIGHT_VAL);

    public static SimpleDoubleProperty globalNormUnMaskGain = new SimpleDoubleProperty(FilterParamsPane.INITIAL_NORM_UN_MASK_GAIN_VAL);

    public static SimpleDoubleProperty globalNormUnMaskEnv = new SimpleDoubleProperty(FilterParamsPane.INITIAL_NORM_UN_MASK_ENV_VAL);

    public static SimpleDoubleProperty globalImgUnMaskGain = new SimpleDoubleProperty(FilterParamsPane.INITIAL_IMG_UN_MASK_GAIN_VAL);


    private static LightControlGroup lightControlGroup;
    private static FilterParamsPane paramsPane;
    private static BottomTabPane bottomTabPane;
    public static VBox flowPane;


    public enum ShaderProgram{DEFAULT, NORMALS, DIFF_GAIN, SPEC_ENHANCE, NORM_UNSHARP_MASK, IMG_UNSHARP_MASK,
                                COEFF_UN_MASK}

    public enum ViewerTheme{DEFAULT, METRO_DARK, METRO_LIGHT;}

    public static ShaderProgram currentProgram = ShaderProgram.DEFAULT;

    public static RTIWindow selectedWindow;

    public static Alert entryAlert;
    public static Alert fileReadingAlert;
    public static Alert bookmarksAlert;

    public static final FileChooser fileChooser = new FileChooser();
    public static final DirectoryChooser directoryChooser = new DirectoryChooser();
    public static File defaultSaveDirectory;
    public static File defaultOpenDirectory;

    private static ArrayList<RTIWindow> RTIWindows = new ArrayList<>();

    public static final Image THUMBNAIL = new Image("images/rtiThumbnail.png");


    @Override
    public void start(Stage primaryStage) throws Exception {
        RTIViewer.primaryStage = primaryStage;
        setupPrimaryStage();

        createAlerts();
        loadPreferences();

        mainScene = createScene(primaryStage);
        mainScene.getStylesheets().add("stylesheets/default.css");

        primaryStage.setScene(mainScene);
        primaryStage.getIcons().add(THUMBNAIL);

        primaryStage.show();
        primaryStage.sizeToScene();
    }


    private void setupPrimaryStage(){
        RTIViewer.primaryStage.setResizable(true);

        RTIViewer.primaryStage.setMinWidth(300);
        RTIViewer.primaryStage.setMinHeight(600);
        RTIViewer.primaryStage.setMaxWidth(600);
        RTIViewer.primaryStage.setMaxHeight(1000);


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                closeEverything();
            }
        });

        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resizeGUI();
            }
        });


        primaryStage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resizeGUI();
            }
        });

        primaryStage.setTitle("RTI Viewer");
    }


    private void createAlerts(){
        entryAlert = new Alert(Alert.AlertType.WARNING);
        entryAlert.setHeaderText("");
        entryAlert.setTitle("Invalid Entry");

        fileReadingAlert = new Alert(Alert.AlertType.ERROR);
        fileReadingAlert.setHeaderText("");
        fileReadingAlert.setTitle("Error when reading file");

        bookmarksAlert = new Alert(Alert.AlertType.ERROR);
        bookmarksAlert.setHeaderText("");
        bookmarksAlert.setTitle("Bookmarks error");

        setThumbnails(entryAlert, fileReadingAlert, bookmarksAlert);
        BookmarkManager.createDialog();
    }

    private void setThumbnails(Alert... alerts){
        for(Alert alert : alerts) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(THUMBNAIL);
        }
    }


    private Scene createScene(Stage primaryStage){
        flowPane = new VBox();
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

        flowPane.setMargin(lightControlGroup, new Insets(5, 3, 5, 3));
        flowPane.setMargin(paramsPane, new Insets(5, 3, 5, 3));
        flowPane.setMargin(bottomTabPane, new Insets(5, 3, 0, 3));
        flowPane.setAlignment(Pos.TOP_CENTER);
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
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
        else if(filterType.equals("Diffuse gain (PTM) | Normals enhancement (HSH)")){programToSet = ShaderProgram.DIFF_GAIN;}
        else if(filterType.equals("Specular enhancement")){programToSet = ShaderProgram.SPEC_ENHANCE;}
        else if(filterType.equals("Normal unsharp masking")){programToSet = ShaderProgram.NORM_UNSHARP_MASK;}
        else if(filterType.equals("Image unsharp masking")){programToSet = ShaderProgram.IMG_UNSHARP_MASK;}
        else if(filterType.equals("Coefficient unsharp masking")){programToSet = ShaderProgram.COEFF_UN_MASK;}

        updateWindowFilter(programToSet);
    }

    public static void updateWindowFilter(ShaderProgram shaderProgram){
        currentProgram = shaderProgram;

        for(RTIWindow RTIWindow : RTIWindows){
            RTIWindow.setCurrentProgram(shaderProgram);
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


    public static void resizeGUI(){
        flowPane.setPrefHeight(primaryStage.getHeight());
        flowPane.setPrefWidth(primaryStage.getWidth());
        lightControlGroup.updateSize(primaryStage.getWidth(), primaryStage.getHeight());
        lightControlGroup.updateLightControls(LightControlGroup.LightEditor.RESIZE);
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



    public static void createBookmark(String name){
        if(selectedWindow != null){
            selectedWindow.addBookmark(name);
            setSelectedBookmark(name);
        }
    }

    public static void updateBookmarks(String rtiObjPath, ArrayList<Bookmark> bookmarks){
        bottomTabPane.setBookmarks(bookmarks);

        String fileSuffix;
        if(rtiObjPath.endsWith(".rti")){
            fileSuffix = "_rti.xmp";
        }else if(rtiObjPath.endsWith(".ptm")){
            fileSuffix = "_ptm.xmp";
        }else{
            return;
        }

        String filePrefix = rtiObjPath.substring(0, rtiObjPath.length() - 4);
        String bookmarksFilePath = filePrefix + fileSuffix;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BookmarkManager.writeBookmarksToFile(bookmarksFilePath, bookmarks);
                }catch(Exception e){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            bookmarksAlert.setContentText("Error when writing bookmarks to file." +
                                                        " Changes have not been saved.");
                            bookmarksAlert.showAndWait();
                        }
                    });
                }
            }
        });

        thread.start();
    }

    public static void setSelectedBookmark(String bookmarkName){
        bottomTabPane.setSelectedBookmark(bookmarkName);
    }


    public static void setRenderParamsFromBookmark(Bookmark bookmark){
        globalLightPos.x = (float) bookmark.getLightX();
        globalLightPos.y = (float) bookmark.getLightY();

        if(bookmark.getRenderingMode() == 0){
            updateWindowFilter(ShaderProgram.DEFAULT);
        }else if(bookmark.getRenderingMode() == 1){
            updateWindowFilter(ShaderProgram.DIFF_GAIN);
            globalDiffGainVal.set(bookmark.getRenderingParams().get("gain"));

        }else if(bookmark.getRenderingMode() == 2){
            updateWindowFilter(ShaderProgram.SPEC_ENHANCE);
            globalSpecularityVal.set(bookmark.getRenderingParams().get("specularity"));
            globalDiffColourVal.set(bookmark.getRenderingParams().get("diffuseColor"));
            globalHighlightSizeVal.set(bookmark.getRenderingParams().get("highlightSize"));

        }else if(bookmark.getRenderingMode() == 4){
            updateWindowFilter(ShaderProgram.NORM_UNSHARP_MASK);
            globalImgUnMaskGain.set(bookmark.getRenderingParams().get("gain"));
        }else if(bookmark.getRenderingMode() == 9){
            updateWindowFilter(ShaderProgram.NORMALS);
        }


        if(selectedWindow != null){
            selectedWindow.setImageScale((float) bookmark.getZoom());
            selectedWindow.setViewportX((float) bookmark.getPanX());
            selectedWindow.setViewportY((float) bookmark.getPanY());
        }

        updateViewerControls();
    }



    private static void updateViewerControls(){
        lightControlGroup.updateLightControls(LightControlGroup.LightEditor.RESIZE);
        paramsPane.updateFromBookmark();
    }


    public static void saveDefaultSaveDirectory(){
        try {
            String location = defaultSaveDirectory.getCanonicalPath();
            Preferences preferences = Preferences.userNodeForPackage(RTIViewer.class);
            preferences.put("defaultSaveDir", location);
            preferences.flush();
        }catch(IOException|BackingStoreException e){
            e.printStackTrace();
        }
    }


    public static void saveDefaultOpenDirectory(){
        try{
            String location = defaultOpenDirectory.getCanonicalPath();
            Preferences preferences = Preferences.userNodeForPackage(RTIViewer.class);
            preferences.put("defaultOpenDir", location);
            preferences.flush();
        }catch(IOException|BackingStoreException e){
            e.printStackTrace();
        }
    }



    private static void loadPreferences(){
        Preferences preferences = Preferences.userNodeForPackage(RTIViewer.class);
        String defaultOpenPath = preferences.get("defaultOpenDir", "");
        String defaultSavePath = preferences.get("defaultSaveDir", "");

        if(!defaultOpenPath.equals("")){defaultOpenDirectory = new File(defaultOpenPath);}
        if(!defaultSavePath.equals("")){defaultSaveDirectory = new File(defaultSavePath);}

    }


    public static void resize(int width, int height){
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        resizeGUI();
    }
}
