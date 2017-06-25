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
                File file = RTIViewer.fileChooser.showOpenDialog(RTIViewer.primaryStage);
                if(file != null) {
                    Thread thread = new Thread(new PTMCreator(file));
                    thread.start();
                }
            }else if(source.getId().equals("close")){
                RTIViewer.closeEverything();
            }else if(source.getId().equals("closePTMWindow")){
                RTIViewer.closeCurrentWindow();
            }else if(source.getId().equals("saveAsImage")){
                RTIViewer.setFocusSave();
            }else if(source.getId().equals("defaultTheme")){
                RTIViewer.setTheme(RTIViewer.ViewerTheme.DEFAULT);
            }else if(source.getId().equals("metroDarkTheme")){
                RTIViewer.setTheme(RTIViewer.ViewerTheme.METRO_DARK);
            }else if(source.getId().equals("metroLightTheme")){
                RTIViewer.setTheme(RTIViewer.ViewerTheme.METRO_LIGHT);
            }
        }
    }
}
