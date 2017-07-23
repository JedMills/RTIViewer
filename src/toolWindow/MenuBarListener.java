package toolWindow;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import ptmCreation.RTICreator;

import java.io.File;

/**
 * Listens to inputs that the user makes in the {@link TopMenuBar} and calls the relevant action. All menu items
 * in the {@link TopMenuBar} should register this class as a listener.
 *
 * @see TopMenuBar
 */
public class MenuBarListener implements EventHandler<ActionEvent>{

    /** The singleton instance of the listener */
    private static MenuBarListener ourInstance = new MenuBarListener();

    /**
     * @return {@link MenuBarListener#ourInstance}
     */
    public static MenuBarListener getInstance() {
        return ourInstance;
    }

    /**
     * Create a new MenuBarListener.
     */
    private MenuBarListener() {}


    /**
     * Carries out the relevant action for when items in the {@link TopMenuBar} are clicked.
     *
     * @param event the event from the menu item in the {@link TopMenuBar} that was clicked
     */
    @Override
    public void handle(ActionEvent event) {
        MenuItem source;
        if(event.getSource() instanceof MenuItem){
            source = (MenuItem) event.getSource();

            if(source.getId().equals("open")){
                //the user is trying to open a new RTI file
                RTIViewer.fileChooser.setTitle("Open RTI File");

                //if the use has set a default open directory, make the file chooser start there
                if(RTIViewer.defaultOpenDirectory != null &&
                        RTIViewer.defaultOpenDirectory.exists() &&
                        RTIViewer.defaultOpenDirectory.isDirectory()) {
                    RTIViewer.fileChooser.setInitialDirectory(RTIViewer.defaultOpenDirectory);
                }
                //open the file chooser and get the file that the user selected to open
                File file = RTIViewer.fileChooser.showOpenDialog(RTIViewer.primaryStage);
                //if they actually chose a file, try and read it
                if(file != null) {
                    Thread thread = new Thread(new RTICreator(file));
                    thread.start();
                }

            }else if(source.getId().equals("close")){
                //close the whole app
                RTIViewer.closeEverything();

            }else if(source.getId().equals("closePTMWindow")){
                //close the currently selected RTIWindow
                RTIViewer.closeCurrentWindow();

            }else if(source.getId().equals("saveAsImage")){
                //open the save tab in the bottom tab pane to get the user to save the snapshot
                RTIViewer.setFocusSave();

            }else if(source.getId().equals("defaultOpenFolder")){
                //get the use rto choose a default open folder
                RTIViewer.directoryChooser.setTitle("Set default open folder");
                File folder = RTIViewer.directoryChooser.showDialog(RTIViewer.primaryStage);
                //if the user actually chose a folder, set the default folder as that one
                if(folder != null){
                    RTIViewer.defaultOpenDirectory = folder;
                    RTIViewer.saveDefaultOpenDirectory();
                }

            }else if(source.getId().equals("defaultSaveFolder")){
                //get the use rto choose a default save folder
                RTIViewer.directoryChooser.setTitle("Set default save folder");
                File folder = RTIViewer.directoryChooser.showDialog(RTIViewer.primaryStage);
                //if the user actually chose a folder, set the default folder as that one
                if(folder != null){
                    RTIViewer.defaultSaveDirectory = folder;
                    RTIViewer.saveDefaultSaveDirectory();
                }

            }else if(source.getId().equals("resizeSmall")){
                RTIViewer.resize(300, 600);

            }else if(source.getId().equals("resizeMedium")){
                RTIViewer.resize(450, 800);

            }else if(source.getId().equals("resizeLarge")){
                RTIViewer.resize(600, 1000);

            }else if(source.getId().equals("recentFileItem")){
                //open the file from the recent files list
                File file = new File(source.getText());

                //check if the file can still be found in that location
                if(!file.exists()){
                    RTIViewer.fileReadingAlert.setContentText("Unable to find file. Check that is still exists.");
                    RTIViewer.fileReadingAlert.show();
                    return;
                }

                new Thread(new RTICreator(file)).start();
            }
        }
    }

}
