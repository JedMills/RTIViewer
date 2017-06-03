package toolWindow;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import ptmCreation.PTMCreator;

import java.io.File;

/**
 * Created by Jed on 03-Jun-17.
 */
public class MenuBarListener implements EventHandler<ActionEvent>{

    private RTIViewer rtiViewer;

    private static MenuBarListener ourInstance = new MenuBarListener();

    public static MenuBarListener getInstance() {
        return ourInstance;
    }

    private MenuBarListener() {
    }

    public static void init(RTIViewer rtiViewer){
        ourInstance.rtiViewer = rtiViewer;
    }



    @Override
    public void handle(ActionEvent event) {
        MenuItem source;
        if(event.getSource() instanceof MenuItem){
            source = (MenuItem) event.getSource();

            if(source.getId().equals("open")){
                rtiViewer.fileChooser.setTitle("Open RTI File");
                File file = rtiViewer.fileChooser.showOpenDialog(rtiViewer.primaryStage);
                if(file != null) {
                    Thread thread = new Thread(new PTMCreator(file));
                    thread.start();
                }
            }
        }
    }
}
