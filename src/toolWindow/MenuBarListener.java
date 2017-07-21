package toolWindow;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import ptmCreation.RTICreator;

import java.io.File;

/**
 * Created by Jed on 03-Jun-17.
 */
public class MenuBarListener implements EventHandler<ActionEvent>{

    private static MenuBarListener ourInstance = new MenuBarListener();

    public static MenuBarListener getInstance() {
        return ourInstance;
    }

    private MenuBarListener() {
    }


    @Override
    public void handle(ActionEvent event) {
        MenuItem source;
        if(event.getSource() instanceof MenuItem){
            source = (MenuItem) event.getSource();

            if(source.getId().equals("open")){
                RTIViewer.fileChooser.setTitle("Open RTI File");

                if(RTIViewer.defaultOpenDirectory != null &&
                        RTIViewer.defaultOpenDirectory.exists() &&
                        RTIViewer.defaultOpenDirectory.isDirectory()) {
                    RTIViewer.fileChooser.setInitialDirectory(RTIViewer.defaultOpenDirectory);
                }
                File file = RTIViewer.fileChooser.showOpenDialog(RTIViewer.primaryStage);
                if(file != null) {
                    Thread thread = new Thread(new RTICreator(file));
                    thread.start();
                }

            }else if(source.getId().equals("close")){
                RTIViewer.closeEverything();

            }else if(source.getId().equals("closePTMWindow")){
                RTIViewer.closeCurrentWindow();

            }else if(source.getId().equals("saveAsImage")){
                RTIViewer.setFocusSave();

            }else if(source.getId().equals("defaultOpenFolder")){
                RTIViewer.directoryChooser.setTitle("Set default open folder");
                File folder = RTIViewer.directoryChooser.showDialog(RTIViewer.primaryStage);
                if(folder != null){
                    RTIViewer.defaultOpenDirectory = folder;
                    RTIViewer.saveDefaultOpenDirectory();
                }

            }else if(source.getId().equals("defaultSaveFolder")){
                RTIViewer.directoryChooser.setTitle("Set default save folder");
                File folder = RTIViewer.directoryChooser.showDialog(RTIViewer.primaryStage);
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
                File file = new File(source.getText());

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
