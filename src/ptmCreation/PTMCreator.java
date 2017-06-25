package ptmCreation;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import toolWindow.RTIViewer;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jed on 03-Jun-17.
 */
public class PTMCreator implements Runnable {

    private PTMObject targetObject;
    private File sourceFile;
    private static LoadingDialog loadingDialog  = new LoadingDialog();

    public PTMCreator(File sourceFile){
        this.sourceFile = sourceFile;
    }

    @Override
    public void run() {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.show();
                }
            });
            targetObject = PTMParser.createPtmFromFile(sourceFile);
            RTIViewer.createNewPTMWindow(targetObject);
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
        }finally{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.hide();
                }
            });
        }
    }

    public static void setLoadingDialogTheme(RTIViewer.ViewerTheme theme){
        loadingDialog.scene.getStylesheets().clear();

        if(theme.equals(RTIViewer.ViewerTheme.DEFAULT)){
            //loadingDialog.scene.getStylesheets().add("stylesheets/default.css");
        }else if(theme.equals(RTIViewer.ViewerTheme.METRO_DARK)){
            loadingDialog.scene.getStylesheets().add("stylesheets/metroDarkDialog.css");
        }else if(theme.equals(RTIViewer.ViewerTheme.METRO_LIGHT)){
            loadingDialog.scene.getStylesheets().add("stylesheets/metroLightDialog.css");
        }
    }


    private static class LoadingDialog{

        private ProgressIndicator progIndicator;
        private Stage stage;
        private Scene scene;
        private VBox vBox;
        private Label label;

        public LoadingDialog(){
            stage = new Stage(StageStyle.UNDECORATED);
            progIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
            progIndicator.setId("loadingDialogProgressIndicator");

            vBox = new VBox();
            vBox.setSpacing(20);
            vBox.setId("loadingDialogMainBox");

            vBox.setAlignment(Pos.CENTER);
            label = new Label("Loading file...");
            label.setFont(Font.font(20));
            label.setId("loadingDialogLabel");

            vBox.getChildren().addAll(label, progIndicator);
            scene = new Scene(vBox, 200, 150);
            stage.setScene(scene);
        }

        public void show(){
            stage.show();
        }

        public void hide(){
            stage.hide();
        }

    }
}
