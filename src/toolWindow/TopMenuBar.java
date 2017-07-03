package toolWindow;

import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.Stage;

import javafx.scene.control.MenuBar;

import java.util.ArrayList;

/**
 * Created by Jed on 23-Jun-17.
 */
public class TopMenuBar extends MenuBar {

    private Stage primaryStage;
    private ArrayList<String> recentFilesList = new ArrayList<>();

    public TopMenuBar(Stage primaryStage){
        super();
        this.primaryStage = primaryStage;

        createMenuBar(primaryStage);
    }

    private void createMenuBar(Stage primaryStage){
        Menu menuFile = createFileMenu();
        Menu menuEdit = createEditMenu();

        Menu menuView = new Menu("View");
        getMenus().addAll(menuFile, menuEdit, menuView);
        prefWidthProperty().bind(primaryStage.widthProperty());
    }

    private Menu createFileMenu(){
        Menu menuFile = new Menu("File");

        MenuItem open = createMenuItem("Open", "open",
                                                "file:rsc/images/icons/folder-4x.png");

        Menu openRecent = new Menu("Open recent");
        openRecent.setId("openRecent");
        openRecent.setOnAction(MenuBarListener.getInstance());
        Image image = new Image("file:rsc/images/icons/clock-4x.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(15);
        imageView.setFitHeight(15);
        openRecent.setGraphic(imageView);

        MenuItem save = createMenuItem("Save as image", "saveAsImage",
                                                "file:rsc/images/icons/image-4x.png");

        MenuItem close = createMenuItem("Close", "close",
                                                "file:rsc/images/icons/circle-x-4x.png");

        MenuItem closeRTIWindow = createMenuItem("Close image", "closePTMWindow",
                                                "file:rsc/images/icons/x-4x.png");

        menuFile.getItems().addAll(open, openRecent, close, save, closeRTIWindow);

        return menuFile;
    }


    private Menu createEditMenu(){
        Menu preferences = new Menu("Preferences");

        Menu themes = new Menu("Themes");

        MenuItem defaultTheme = new MenuItem("Default");
        defaultTheme.setId("defaultTheme");
        defaultTheme.setOnAction(MenuBarListener.getInstance());

        MenuItem metroDarkTheme = new MenuItem("Metro Dark");
        metroDarkTheme.setId("metroDarkTheme");
        metroDarkTheme.setOnAction(MenuBarListener.getInstance());

        MenuItem metroLightTheme = new MenuItem("Metro Light");
        metroLightTheme.setId("metroLightTheme");
        metroLightTheme.setOnAction(MenuBarListener.getInstance());

        themes.getItems().addAll(defaultTheme, metroDarkTheme, metroLightTheme);

        MenuItem defaultOpenFolder = createMenuItem("Set default open folder", "defaultOpenFolder",
                                                "file:rsc/images/icons/home-4x.png");

        MenuItem defaultSaveFolder = createMenuItem("Set default save folder", "defaultSaveFolder",
                                                "file:rsc/images/icons/book-4x.png");

        preferences.getItems().addAll(themes, defaultOpenFolder, defaultSaveFolder);

        return preferences;
    }




    private MenuItem createMenuItem(String label, String id){
        MenuItem menuItem = new MenuItem(label);
        menuItem.setId(id);
        menuItem.setOnAction(MenuBarListener.getInstance());

        return menuItem;
    }

    private MenuItem createMenuItem(String label, String id, String iconLocation){
        MenuItem menuItem = createMenuItem(label, id);
        Image image = new Image(iconLocation);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(15);
        imageView.setFitWidth(15);
        menuItem.setGraphic(imageView);

        return menuItem;
    }
}
