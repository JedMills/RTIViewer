package toolWindow;

import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.Stage;

import javafx.scene.control.MenuBar;

/**
 * Created by Jed on 23-Jun-17.
 */
public class TopMenuBar extends MenuBar {

    private Stage primaryStage;

    public TopMenuBar(Stage primaryStage){
        super();
        this.primaryStage = primaryStage;

        createMenuBar(primaryStage);
    }

    private void createMenuBar(Stage primaryStage){
        Menu menuFile = new Menu("File");
        MenuItem open = new MenuItem("Open");

        Image openIcon = new Image("file:rsc/images/icons/folder-4x.png");
        ImageView openView = new ImageView(openIcon);
        openView.setFitHeight(15);
        openView.setFitWidth(15);
        open.setGraphic(openView);
        open.setOnAction(MenuBarListener.getInstance());
        open.setId("open");

        MenuItem save = new MenuItem("Save as image");
        save.setOnAction(MenuBarListener.getInstance());
        save.setId("saveAsImage");
        Image saveIcon = new Image("file:rsc/images/icons/image-4x.png");
        ImageView saveView = new ImageView(saveIcon);
        saveView.setFitWidth(15);
        saveView.setFitHeight(15);
        save.setGraphic(saveView);


        MenuItem close = new MenuItem("Close");
        close.setOnAction(MenuBarListener.getInstance());
        close.setId("close");
        Image closeIcon = new Image("file:rsc/images/icons/circle-x-4x.png");
        ImageView closeView = new ImageView(closeIcon);
        closeView.setFitHeight(15);
        closeView.setFitWidth(15);
        close.setGraphic(closeView);

        MenuItem closePTMWindow = new MenuItem("Close image");
        closePTMWindow.setOnAction(MenuBarListener.getInstance());
        closePTMWindow.setId("closePTMWindow");
        Image closeWinIcon = new Image("file:rsc/images/icons/x-4x.png");
        ImageView closeWinView = new ImageView(closeWinIcon);
        closeWinView.setFitWidth(15);
        closeWinView.setFitHeight(15);
        closePTMWindow.setGraphic(closeWinView);

        menuFile.getItems().addAll(open, close, save, closePTMWindow);

        Menu menuEdit = new Menu("Edit");
        MenuItem preferences = new MenuItem("Preferences");
        preferences.setOnAction(MenuBarListener.getInstance());
        preferences.setId("preferences");
        Image prefsIcon = new Image("file:rsc/images/icons/cog-4x.png");
        ImageView prefsView = new ImageView(prefsIcon);
        prefsView.setFitHeight(15);
        prefsView.setFitWidth(15);
        preferences.setGraphic(prefsView);

        menuEdit.getItems().addAll(preferences);

        Menu menuView = new Menu("View");
        getMenus().addAll(menuFile, menuEdit, menuView);
        prefWidthProperty().bind(primaryStage.widthProperty());

    }
}
