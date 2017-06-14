package ptmCreation;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import toolWindow.RTIViewer;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jed on 03-Jun-17.
 */
public class PTMCreator implements Runnable {

    private PTMObject targetObject;
    private File sourceFile;

    public PTMCreator(File sourceFile){
        this.sourceFile = sourceFile;
    }

    @Override
    public void run() {
        try {
            RTIViewer.setCursor(Cursor.WAIT);
            targetObject = PTMParser.createPtmFromFile(sourceFile);
            RTIViewer.createNewPTMWindow(targetObject);
            RTIViewer.setCursor(Cursor.DEFAULT);
        }catch(IOException e){
            RTIViewer.fileReadingAlert.setContentText("Error accessing file at: " +
                                                        sourceFile.getPath());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    RTIViewer.fileReadingAlert.showAndWait();
                }
            });
        }catch(PTMFileException e){
            RTIViewer.fileReadingAlert.setContentText("Error when parsing file at: " +
                                                        sourceFile.getPath() + ": " +
                                                        e.getMessage());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    RTIViewer.fileReadingAlert.showAndWait();
                }
            });
        }
    }
}
