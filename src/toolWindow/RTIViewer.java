package toolWindow;

import bookmarks.Bookmark;
import bookmarks.BookmarkManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

/**<p>
 *  This is the Main class. This class represents the actual Viewer application, and is the toolbar pane containing the
 *  specular ball, the sliders and the bottom tab pane. This class has the global rendering mode and global rendering
 *  parameters that all the {@link RTIWindow}s use to know how to render their {@link RTIObject}s.
 * </p>
 * <p>
 *  This class also owns the various alert boxes that the app uses, and the file choosers. Most of the components of the
 *  app use this class as a means of sending information to tbe acted upon by other classes, such as wwen updating the
 *  pan in a {@link RTIWindow}, the change in pan is sent to the RTIViewer class so that it can get the preview window
 *  at the bottom to change the viewport rectangle.
 * </p>
 *
 * @see BottomTabPane
 * @see FilterParamsPane
 * @see LightControlGroup
 * @see RTIWindow
 *
 * @author Jed Mills
 */
public class RTIViewer extends Application {

    /** The window that this toolbar exists in */
    public static Stage primaryStage;

    /** The scene that is displayed in the primaryStage, contains the vertical alignment box with all the widgets */
    private static Scene mainScene;

    /** Width of the toolbar when the app first opens */
    public static final int INITIAL_WIDTH = 350 ;

    /** Height of the toolbar when the app first opens */
    public static final int INITIAL_HEIGHT = 700;

    /** The global light position that all RTIWindows use to render with */
    public static Utils.Vector2f globalLightPos = new Utils.Vector2f(0, 0);

    /** The global value of gain for the diffuse gain rendering mode that all RTIWindows use*/
    public static SimpleDoubleProperty globalDiffGainVal = new SimpleDoubleProperty(FilterParamsPane.INITIAL_DIFF_GAIN_VAL);

    /** The global value of diffuse colour  for the specular enhancement rendering mode that all RTIWindows use*/
    public static SimpleDoubleProperty globalDiffColourVal = new SimpleDoubleProperty(FilterParamsPane.INITIAL_DIFF_COLOUR_VAL);

    /** The global value of specularity for the specular enhancement rendering mode that all RTIWindows use*/
    public static SimpleDoubleProperty globalSpecularityVal = new SimpleDoubleProperty(FilterParamsPane.INITIAL_SPEC_VAL);

    /** The global value of highlight size for the specular enhancement rendering mode that all RTIWindows use*/
    public static SimpleDoubleProperty globalHighlightSizeVal = new SimpleDoubleProperty(FilterParamsPane.INITIAL_HIGHLIGHT_VAL);

    /** The global value of gain for the normals enhancement rendering mode that all RTIWindows use*/
    public static SimpleDoubleProperty globalNormUnMaskGain = new SimpleDoubleProperty(FilterParamsPane.INITIAL_NORM_UN_MASK_GAIN_VAL);

    /** The global value of environment for the normals enhancement rendering mode that all RTIWindows use*/
    public static SimpleDoubleProperty globalNormUnMaskEnv = new SimpleDoubleProperty(FilterParamsPane.INITIAL_NORM_UN_MASK_ENV_VAL);

    /** The global value of gain for the image unsharp masking rendering mode that all RTIWindows use*/
    public static SimpleDoubleProperty globalImgUnMaskGain = new SimpleDoubleProperty(FilterParamsPane.INITIAL_IMG_UN_MASK_GAIN_VAL);

    /** The light control group underneath the menu bar */
    private static LightControlGroup lightControlGroup;

    /** The filter params pane underneath the light control group*/
    private static FilterParamsPane paramsPane;

    /** The tab pane at the bottom of the toolbar */
    private static BottomTabPane bottomTabPane;

    /** The vertical box that contains all the widget groups for the toolbar */
    public static VBox vBox;

    /** The menu bar at the top of the toolwindow*/
    private static TopMenuBar menuBar;

    /**
     * Represents the different rendering modes that {@link RTIWindow}s can have.
     */
    public enum ShaderProgram{DEFAULT, NORMALS, DIFF_GAIN, SPEC_ENHANCE, NORM_UNSHARP_MASK, IMG_UNSHARP_MASK}

    /** The current program that all {@link RTIWindow}s use to render their {@link RTIObject}*/
    public static ShaderProgram currentProgram = ShaderProgram.DEFAULT;

    /** The currently selected window tht appears in the preview*/
    public static RTIWindow selectedWindow;

    /** Alert shown when the user types something bad into the widgets */
    public static Alert entryAlert;

    /** Alert shown when there is an error reading or parsing a file */
    public static Alert fileReadingAlert;

    /** Alert shown when there is an error with the bookmarks */
    public static Alert bookmarksAlert;

    /** Th file chooser that can be opened when opening or saving files */
    public static final FileChooser fileChooser = new FileChooser();

    /** The directory chooser for saving the default directories */
    public static final DirectoryChooser directoryChooser = new DirectoryChooser();

    /** Location of the default save directory that the user has set*/
    public static File defaultSaveDirectory;

    /** Location of the default open directory that the user has set*/
    public static File defaultOpenDirectory;

    /** All the currently opend {@link RTIWindow}s*/
    private static ArrayList<RTIWindow> RTIWindows = new ArrayList<>();

    /** The icon for the program*/
    public static final Image THUMBNAIL = new Image("images/rtiThumbnail-128.png");

    /** Recent files list that is loaded from the preferences when the user opens the program*/
    public static ArrayList<String> recentFiles = new ArrayList<>();




    /**
     * Main method that launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }




    /**
     * Represents the start of the JavaFX application. Creates all the widgets, alerts and layouts of the tool window,
     * and lods java preferences, if they exist.
     *
     * @param primaryStage  window that the toolbar exists in
     * @throws Exception    if there's an error on startup
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        RTIViewer.primaryStage = primaryStage;
        //set the width, height, thumnail etc.
        setupPrimaryStage();

        //create the alert boxes
        createAlerts();

        //load preferences such as the recent files list
        loadPreferences();

        //create all the widgetsand layotu
        mainScene = createScene(primaryStage);
        mainScene.getStylesheets().add("stylesheets/default.css");

        primaryStage.setScene(mainScene);
        primaryStage.getIcons().add(THUMBNAIL);

        //show the window
        primaryStage.show();
        primaryStage.sizeToScene();
    }




    /**
     * Sets the min and max size of the toolbar window, and adds listeners to he width and height so the toolbar can
     * resize nicely when the window resizes.
     */
    private void setupPrimaryStage(){
        RTIViewer.primaryStage.setResizable(true);

        RTIViewer.primaryStage.setMinWidth(300);
        RTIViewer.primaryStage.setMinHeight(600);
        RTIViewer.primaryStage.setMaxWidth(600);
        RTIViewer.primaryStage.setMaxHeight(1000);

        //close everything when the user clicks the close button
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                closeEverything();
            }
        });

        //resize the GUI nicely when the width and height of the window are changed
        ChangeListener<Number> changeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resizeGUI();
            }
        };
        primaryStage.widthProperty().addListener(changeListener);
        primaryStage.heightProperty().addListener(changeListener);

        primaryStage.setTitle("RTI Viewer");
    }




    /**
     * Creates the alerts that are shown when something bad happens.
     */
    private void createAlerts(){
        //pretty self explanatory
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




    /**
     * Sets thumbnails of alerts that are given to it to the RTI logo
     *
     * @param alerts    alerts boxes to give the beautiful RTI logo
     */
    private void setThumbnails(Alert... alerts){
        for(Alert alert : alerts) {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(THUMBNAIL);
        }
    }




    /**
     * Creates the widget panes in the main toolbar, and adds them to the toolbar.
     *
     * @param primaryStage
     * @return
     */
    private Scene createScene(Stage primaryStage){
        vBox = new VBox();
        vBox.setId("mainSceneBox");
        Scene scene = new Scene(vBox, INITIAL_WIDTH, INITIAL_HEIGHT);

        //menu bar at the top of the toolbar
        menuBar = new TopMenuBar(primaryStage);
        vBox.getChildren().add(menuBar);

        //light control group with the specular ball and spinners
        lightControlGroup = new LightControlGroup();
        vBox.getChildren().add(lightControlGroup);

        //params pane with the dropdown and sliders
        paramsPane = new FilterParamsPane(this, scene);
        vBox.getChildren().add(paramsPane);

        //tab pane at the bottom
        bottomTabPane = new BottomTabPane();
        vBox.getChildren().add(bottomTabPane);

        vBox.setMargin(lightControlGroup, new Insets(5, 3, 5, 3));
        vBox.setMargin(paramsPane, new Insets(5, 3, 5, 3));
        vBox.setMargin(bottomTabPane, new Insets(5, 3, 0, 3));
        vBox.setAlignment(Pos.TOP_CENTER);

        return scene;
    }




    /**
     * Creates a new RTIWindow of the correct subclass for the type of RTIObject that was passed. Runs the
     * window runnable so it initialises and opens.
     *
     * @see RTIWindow
     * @see RTIObject
     *
     * @param RTIObject     the RTIObject to create an RTIwindow for
     */
    public static void createNewPTMWindow(RTIObject RTIObject){
        try {
            //create the right type of window for the RTIObject given
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
            //most likely a problem setting up opengl
            fileReadingAlert.setContentText("Couldn't compile OpenGL shader: " + e.getMessage());
            fileReadingAlert.showAndWait();
        }
    }




    /**
     * Causes the {@link RTIWindow#currentProgram} to be set to the corresponding program for the given name.
     * Updates all the RTIWindows so that they now render using this program.
     *
     * @param filterType    name of the filter to change all of the windows to
     */
    public static void updateWindowFilter(String filterType){
        ShaderProgram programToSet = ShaderProgram.DEFAULT;

        //select the right program from the given name
        if(filterType.equals("Default view")){programToSet = ShaderProgram.DEFAULT;}
        else if(filterType.equals("Normals visualisation")){programToSet = ShaderProgram.NORMALS;}
        else if(filterType.equals("Diffuse gain (PTM) | Normals enhancement (HSH)")){programToSet = ShaderProgram.DIFF_GAIN;}
        else if(filterType.equals("Specular enhancement")){programToSet = ShaderProgram.SPEC_ENHANCE;}
        else if(filterType.equals("Normal unsharp masking")){programToSet = ShaderProgram.NORM_UNSHARP_MASK;}
        else if(filterType.equals("Image unsharp masking")){programToSet = ShaderProgram.IMG_UNSHARP_MASK;}

        //update all the RTIWindows with the programto rnder with
        updateWindowFilter(programToSet);
    }




    /**
     * Updates all the {@link RTIWindow}s with the program to render with, this method is called when the user changes
     * the current rendering mode.
     *
     * @param shaderProgram     program to update all the windows with
     */
    public static void updateWindowFilter(ShaderProgram shaderProgram){
        currentProgram = shaderProgram;

        for(RTIWindow RTIWindow : RTIWindows){
            RTIWindow.setCurrentProgram(shaderProgram);
        }
    }




    /**
     * Removes the given {@link RTIWindow} from the RTIViewer's list of window,s and sets no window as being focused
     * so that the preview window and information boxes are blank. This method is called when a RTIWindow is closed.
     *
     * @param window    RTIWindow to remove
     */
    public static void removeWindow(RTIWindow window){
        for(RTIWindow RTIWindow : RTIWindows){
            if (RTIWindow.equals(window)){
                RTIWindows.remove(window);
                setNoFocusedWindow();
                break;
            }
        }
    }




    /**
     * Sets the currently selected window to the window passed.
     *
     * @param rtiWindow     currently selected window
     */
    public static void setFocusedWindow(RTIWindow rtiWindow){
        selectedWindow = rtiWindow;
        //update all of the info and the preview picture in the bottom tab pane
        bottomTabPane.updateSelectedWindow(rtiWindow);
        bottomTabPane.updateViewportRect(rtiWindow.getViewportX(),
                            rtiWindow.getViewportY(), rtiWindow.getImageScale());
    }




    /**
     * Sets the window information and preview picture in the {@link RTIViewer#bottomTabPane} to be blank, called when
     * an RTIWindow is closed.
     */
    private static void setNoFocusedWindow(){
        //make all the info fields about the selected window blank
        bottomTabPane.setNoFocusedWindow();
        selectedWindow = null;
    }




    /**
     * Resizes all of the widgets in the toll window, so everything looks nice.
     */
    public static void resizeGUI(){
        //gets everything toresize itself
        vBox.setPrefHeight(primaryStage.getHeight());
        vBox.setPrefWidth(primaryStage.getWidth());

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




    /**
     * Called when the pan of an {@link RTIWindow} is changed, so that the viewport rectangle on the preview image in
     * the bottom tab pane can be changed.
     *
     * @param RTIWindow     window where the viewport changed
     * @param x             x position f the RTIWindow's pan
     * @param y             y position f the RTIWindow's pan
     * @param imageScale    scale of the image in the RTIWindow
     */
    public static void updateViewportPos(RTIWindow RTIWindow, Float x, Float y, float imageScale){
        if(RTIWindow.equals(selectedWindow)) {
            bottomTabPane.updateViewportRect(x, y, imageScale);
        }
    }




    /**
     * Closes all the RTIWindows and the main toolbar, and closes java.
     */
    public static void closeEverything(){
        //close all the windows
        for(RTIWindow RTIWindow : RTIWindows){
            RTIWindow.setShouldClose(true);
        }
        //quit java
        Platform.exit();
    }




    /**
     * Closes the currently selected RTIWindow.
     */
    public static void closeCurrentWindow(){
        //close the currently selected window
        if(selectedWindow != null){
            selectedWindow.setShouldClose(true);
            removeWindow(selectedWindow);
        }
    }



    /**
     * Sets the save tab on the BottomTabPane to open, and makes it highlighted, called when the user clicks
     * save in the menu bar.
     */
    public static void setFocusSave(){
        bottomTabPane.getSelectionModel().select(2);
        bottomTabPane.requestFocus();
    }




    /**
     * Creates a new bookmark with the given namefor the currently selected {@link RTIWindow}.
     *
     * @param name  name of the new bookmark
     */
    public static void createBookmark(String name){
        if(selectedWindow != null){
            selectedWindow.addBookmark(name);
            setSelectedBookmark(name);
        }
    }




    /**
     * Sets the list of bookmarks in the {@link RTIViewer#bottomTabPane} to the given list. Writes the bookmarks
     * to the XML file with the appropriate name for the currently selected {@link RTIObject}. This method is called
     * when a new bookmark is created or one is deleted.
     *
     * @see BookmarkManager
     * @see Bookmark
     * @see RTIObject
     *
     * @param rtiObjPath        path of the RTIObject that the bookmarks belong to
     * @param bookmarks         bookmarks of the RTIObject that are to be saved
     */
    public static void updateBookmarks(String rtiObjPath, ArrayList<Bookmark> bookmarks){
        //set the bookmarks in the bookmarks list
        bottomTabPane.setBookmarks(bookmarks);

        //find the appropriate name for the bookmarks XML file
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

        //write the bookmarks to the file
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




    /**
     * Sets the selected bookmark in the {@link RTIViewer#bottomTabPane} to the with with the given name.
     *
     * @param bookmarkName  name of the bookmark to select
     */
    public static void setSelectedBookmark(String bookmarkName){
        bottomTabPane.setSelectedBookmark(bookmarkName);
    }




    /**
     * Called when the user selects a bookmark from the bookmarks list for an RTIObject. Updates the global rendering
     * mode and rendering params to match those in the bookmark.
     *
     * @param bookmark  bookmark that has been selected by the user
     */
    public static void setRenderParamsFromBookmark(Bookmark bookmark){
        //light position to set
        globalLightPos.x = (float) bookmark.getLightX();
        globalLightPos.y = (float) bookmark.getLightY();

        //set the rendering mode and the relevant rendering params from the bookmark
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


        //set the pan and zoom for the currently selected window fromthe bookmark
        if(selectedWindow != null){
            selectedWindow.setImageScale((float) bookmark.getZoom());
            selectedWindow.setViewportX((float) bookmark.getPanX());
            selectedWindow.setViewportY((float) bookmark.getPanY());
        }
        //update all the control widgets so they match the bookamark
        updateViewerControls();
    }




    /**
     * Called when a bookmark is selected that changes the global rendering params such as light position and rendering
     * mode, so that the control widgets match the newly changed values.
     */
    private static void updateViewerControls(){
        lightControlGroup.updateLightControls(LightControlGroup.LightEditor.RESIZE);
        paramsPane.updateFromBookmark();
    }




    /**
     * Called when the user sets the default save directory, saves this directory as a Java preference so that is can be
     * remembered when the user next runs the app.
     */
    public static void saveDefaultSaveDirectory(){
        //save the directory in the java preferences so it can be remembered when the app is next opened
        try {
            String location = defaultSaveDirectory.getCanonicalPath();
            Preferences preferences = Preferences.userNodeForPackage(RTIViewer.class);
            preferences.put("defaultSaveDir", location);
            preferences.flush();
        }catch(IOException|BackingStoreException e){
            e.printStackTrace();
        }
    }




    /**
     * Called when the user sets the default open directory, saves this directory as a Java preference so that is can be
     * remembered when the user next runs the app.
     */
    public static void saveDefaultOpenDirectory(){
        //save the directory in the java preferences so it can be remembered when the app is next opened
        try{
            String location = defaultOpenDirectory.getCanonicalPath();
            Preferences preferences = Preferences.userNodeForPackage(RTIViewer.class);
            preferences.put("defaultOpenDir", location);
            preferences.flush();
        }catch(IOException|BackingStoreException e){
            e.printStackTrace();
        }
    }




    /**
     * Add a file name to the list of recent files, called when the user opens a new RTIObject.
     *
     * @param filePath      pathof the file to go one the recent files list
     */
    public static void addRecentFile(String filePath){
        //if the file's already in the list, move the file name to the top of the list
        //instead of just adding it to the list
        if(Utils.checkIn(filePath, recentFiles)) {
            int index = 0;
            for (String s : recentFiles) {
                if (s.equals(filePath)) {
                    break;
                }
                index++;
            }
            recentFiles.remove(index);
        }
        //the list can have a maximum of 8 items
        if(recentFiles.size() == 8){
            recentFiles.remove(recentFiles.size() - 1);
        }
        //save the recent files list in the java preferences so it can be remembered when the user reopens the app
        recentFiles.add(0, filePath);
        saveRecentFiles();
        menuBar.updateOpenRecentList();
    }



    /**
     * Saves the recent files list to the java preferences so that the list remains when the user reopens the app.
     */
    private static void saveRecentFiles(){
        Preferences preferences = Preferences.userNodeForPackage(RTIViewer.class);

        //add all the file paths to the saved preferences
        for(int i = 0; i < 8; i++) {
            if (i < recentFiles.size()) {
                preferences.put("recentFile" + String.valueOf(i), recentFiles.get(i));
            } else {
                //save an empty string if there aren't 8 files, which is dealt with when loading them back
                preferences.put("recentFile" + String.valueOf(i), "");
            }
        }

    }


    /**
     * Load the default open directory, default save directory, and recent files list from the java preferences,
     * called when the app firsts starts up.
     */
    private static void loadPreferences(){
        Preferences preferences = Preferences.userNodeForPackage(RTIViewer.class);

        //get the default open and save dir locations
        String defaultOpenPath = preferences.get("defaultOpenDir", "");
        String defaultSavePath = preferences.get("defaultSaveDir", "");

        //get all the recent files list
        ArrayList<String> recentFilesList = new ArrayList<>();
        for(int i = 0; i < 8 ; i++){
            String recentFile = preferences.get("recentFile" + String.valueOf(i), "");
            //if there aren't 8 recent files, we saved an empty string in the file's place, so only add
            //the non-empty strings
            if(!recentFile.equals("")){
                recentFilesList.add(recentFile);
            }
        }

        recentFiles = recentFilesList;

        if(!defaultOpenPath.equals("")){defaultOpenDirectory = new File(defaultOpenPath);}
        if(!defaultSavePath.equals("")){defaultSaveDirectory = new File(defaultSavePath);}

    }


    /**
     * Called when the width or height property of the main window is changed. Resizes all of the widgets in the gui.
     *
     * @param width     new width of the main window
     * @param height    new height of the main window
     */
    public static void resize(int width, int height){
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        resizeGUI();
    }

    /**
     * Called when a new RTIObject is opened to find the currently selected mip mapping level, so that the
     * RTICreator knows whether to mip map or not.
     *
     * @see RTICreator
     * @see RTIObject
     *
     * @return      the currently selected mip mapping level
     */
    public static int getMipMapping(){
        if(menuBar.mipMapping0()){return 0;}
        else if(menuBar.mipMapping1()){return 1;}
        else if(menuBar.mipMapping2()){return 2;}

        return -1;
    }
}
