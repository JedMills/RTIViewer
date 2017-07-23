package toolWindow;

import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.Stage;

import javafx.scene.control.MenuBar;

import java.util.ArrayList;

/**
 * The menu bar at the top of the tool window, with the menus to click.
 *
 * @see MenuBarListener
 *
 * @author Jed Mills
 */
public class TopMenuBar extends MenuBar {

    /** The menu containing the recent files list */
    private Menu openRecent;

    /** The selector for no mip mapping */
    private RadioMenuItem mipMap0;

    /** The selector for the first mip mapping level*/
    private RadioMenuItem mipMap1;

    /** The selector for the second mip mapping level */
    private RadioMenuItem mipMap2;

    /**
     * Creates a new TopMenuBar.
     *
     * @param primaryStage  window that this TopMenuBar belongs to
     */
    public TopMenuBar(Stage primaryStage){
        super();

        createMenuBar(primaryStage);
    }

    /**
     * Cretaes all of the menu is in the TopMenuBar.
     *
     * @param primaryStage the window that this topMenuBar belongs to
     */
    private void createMenuBar(Stage primaryStage){
        //create the two menus and add them to this bar, simple
        Menu menuFile = createFileMenu();
        Menu menuEdit = createPrefMenu();

        getMenus().addAll(menuFile, menuEdit);
        prefWidthProperty().bind(primaryStage.widthProperty());
    }

    /**
     * Creates a new 'File' menu, containing the 'Open', 'Open Recent', 'Save as image', 'Close' and 'Close image'
     * options. Registers them all with the MenuBarListener.
     *
     * @see MenuBarListener
     *
     * @return the file menu
     */
    private Menu createFileMenu(){
        Menu menuFile = new Menu("File");

        //create all the items in the menu
        MenuItem open = createMenuItem("Open", "open",
                "images/folder-4x.png");

        openRecent = createMenu("Open recent", "openRecent", "images/clock-4x.png");

        updateOpenRecentList();

        MenuItem save = createMenuItem("Save as image", "saveAsImage",
                "images/image-4x.png");

        MenuItem close = createMenuItem("Close", "close",
                "images/circle-x-4x.png");

        MenuItem closeRTIWindow = createMenuItem("Close image", "closePTMWindow",
                "images/x-4x.png");

        menuFile.getItems().addAll(open, openRecent, close, save, closeRTIWindow);

        //and add them to the menu
        return menuFile;
    }


    /**
     * Adda ll the file paths in the {@link RTIViewer#recentFiles} list to the recent files list on the file menu of
     * this TopMenuBar.
     */
    public void updateOpenRecentList(){
        openRecent.getItems().clear();

        //add all the recent files to the recent file menu list
        for(String fileName : RTIViewer.recentFiles){
            MenuItem recentFile = new MenuItem(fileName);
            recentFile.setOnAction(MenuBarListener.getInstance());
            recentFile.setId("recentFileItem");
            openRecent.getItems().add(recentFile);
        }
    }


    /**
     * Create the 'Preferences' menu, which contains the toolbar size,default open and close directories, and the
     * mip mapping level items.
     *
     * @return  the 'Preferences' menu
     */
    private Menu createPrefMenu(){
        Menu preferences = new Menu("Preferences");

        //create all the menu items
        MenuItem defaultOpenFolder = createMenuItem("Set default open folder",
                "defaultOpenFolder", "images/home-4x.png");

        MenuItem defaultSaveFolder = createMenuItem("Set default save folder",
                "defaultSaveFolder", "images/book-4x.png");

        Menu setToolbarSize = createMenu("Set toolbar size",
                "setToolbarSize", "images/resize-both-4x.png");

        MenuItem resizeSmall = createMenuItem("300 x 600", "resizeSmall");
        MenuItem resizeMedium = createMenuItem("450 x 800", "resizeMedium");
        MenuItem resizeLarge = createMenuItem("600 x 1000", "resizeLarge");

        Menu mipMappingMenu = createMipMappingMenu();

        //and add them to the preferences menu
        setToolbarSize.getItems().addAll(resizeSmall, resizeMedium, resizeLarge);
        preferences.getItems().addAll(setToolbarSize, defaultOpenFolder,
                defaultSaveFolder, mipMappingMenu);

        return preferences;
    }


    /**
     * Creates the mi pmapping level menu
     *
     * @return  the mip mapping level menu
     */
    private Menu createMipMappingMenu(){
        Menu menu = createMenu("Set mip mapping", "setMipMapping",
                "images/layers-4x.png");

        //create the menu items
        ToggleGroup toggleGroup = new ToggleGroup();
        mipMap0 = createRadioMenuItem("No mip mapping", "mipMapping0",
                toggleGroup);
        mipMap1 = createRadioMenuItem("Mip map 1 ", "mipMapping1",
                toggleGroup);
        mipMap2 = createRadioMenuItem("Mip map 2", "mipMapping2",
                toggleGroup);

        //and add them to the mip mapping menu
        mipMap0.setSelected(true);
        menu.getItems().addAll(mipMap0, mipMap1, mipMap2);

        return menu;
    }


    /**
     * Convenience method to create a new radio menu item, with the given label, id, and toggle group,
     * and add the MenuBarListener as a listener.
     *
     * @param label             label for the menu item
     * @param id                javafx id for the menu item
     * @param toggleGroup       toggle group the the menu item will be part of
     * @return                  the newly created radio button menu item
     */
    private RadioMenuItem createRadioMenuItem(String label, String id, ToggleGroup toggleGroup){
        RadioMenuItem radioMenuItem = new RadioMenuItem(label);
        radioMenuItem.setId(id);
        radioMenuItem.setOnAction(MenuBarListener.getInstance());
        radioMenuItem.setToggleGroup(toggleGroup);

        return radioMenuItem;
    }



    /**
     * Convenience method to create a new menu item, with the given label, and id
     * and add the MenuBarListener as a listener.
     *
     * @param label             label for the menu item
     * @param id                javafx id for the menu item
     * @return                  the newly created menu item
     */
    private MenuItem createMenuItem(String label, String id){
        MenuItem menuItem = new MenuItem(label);
        menuItem.setId(id);
        menuItem.setOnAction(MenuBarListener.getInstance());

        return menuItem;
    }


    /**
     * Convenience method to create a new menu item, with the given label, id, and icon at the given path,
     * and add the MenuBarListener as a listener.
     *
     * @param label             label for the menu item
     * @param id                javafx id for the menu item
     * @param iconLocation      path to the icon for this menu item
     * @return                  the newly created menu item
     */
    private MenuItem createMenuItem(String label, String id, String iconLocation){
        MenuItem menuItem = createMenuItem(label, id);
        Image image = new Image(iconLocation);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(15);
        imageView.setFitWidth(15);
        menuItem.setGraphic(imageView);

        return menuItem;
    }


    /**
     * Convenience method to create a new menu , with the given label, id, and icon at the given path,
     * and add the MenuBarListener as a listener.
     *
     * @param label             label for the menu
     * @param id                javafx id for the menu
     * @param iconLocation      path to the icon for this menu
     * @return                  the newly created menu
     */
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

    /**
     * @return  whether {@link TopMenuBar#mipMap0} is selected
     */
    public boolean mipMapping0(){
        return mipMap0.isSelected();
    }


    /**
     * @return  whether {@link TopMenuBar#mipMap1} is selected
     */
    public boolean mipMapping1(){
        return mipMap1.isSelected();
    }


    /**
     * @return  whether {@link TopMenuBar#mipMap2} is selected
     */
    public boolean mipMapping2(){
        return mipMap2.isSelected();
    }
}
