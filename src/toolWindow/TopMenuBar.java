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
                "images/folder-4x.png");

        Menu openRecent = createMenu("Open recent", "openRecent", "images/clock-4x.png");

        MenuItem save = createMenuItem("Save as image", "saveAsImage",
                "images/image-4x.png");

        MenuItem close = createMenuItem("Close", "close",
                "images/circle-x-4x.png");

        MenuItem closeRTIWindow = createMenuItem("Close image", "closePTMWindow",
                "images/x-4x.png");

        menuFile.getItems().addAll(open, openRecent, close, save, closeRTIWindow);

        return menuFile;
    }


    private Menu createEditMenu(){
        Menu preferences = new Menu("Preferences");


        MenuItem defaultOpenFolder = createMenuItem("Set default open folder", "defaultOpenFolder",
                "images/home-4x.png");

        MenuItem defaultSaveFolder = createMenuItem("Set default save folder", "defaultSaveFolder",
                "images/book-4x.png");

        Menu setToolbarSize = createMenu("Set toolbar size", "setToolbarSize",
                "images/resize-both-4x.png");

        MenuItem resizeSmall = createMenuItem("300 x 600", "resizeSmall");
        MenuItem resizeMedium = createMenuItem("450 x 800", "resizeMedium");
        MenuItem resizeLarge = createMenuItem("600 x 1000", "resizeLarge");

        setToolbarSize.getItems().addAll(resizeSmall, resizeMedium, resizeLarge);
        preferences.getItems().addAll(setToolbarSize, defaultOpenFolder, defaultSaveFolder);

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


    private Menu createMenu(String label, String id, String iconLocation){
        Menu menu = new Menu(label);
        menu.setId(id);
        menu.setOnAction(MenuBarListener.getInstance());
        Image image = new Image(iconLocation);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(15);
        imageView.setFitWidth(15);
        menu.setGraphic(imageView);
        return menu;
    }
}
