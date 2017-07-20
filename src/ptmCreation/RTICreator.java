package ptmCreation;

import bookmarks.Bookmark;
import bookmarks.BookmarkManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import toolWindow.RTIViewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jed on 03-Jun-17.
 */
public class RTICreator implements Runnable {

    private RTIObject targetObject;
    private File sourceFile;
    private static LoadingDialog loadingDialog  = new LoadingDialog();

    public RTICreator(File sourceFile){
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
            targetObject = RTIParser.createPtmFromFile(sourceFile, RTIViewer.getMipMapping());
            ArrayList<Bookmark> bookmarks = getBookmarksFromXML(sourceFile);

            if(bookmarks.size() > 0){
                targetObject.setBookmarks(bookmarks);
            }

            RTIViewer.createNewPTMWindow(targetObject);
            RTIViewer.addRecentFile(sourceFile.getAbsolutePath());
        }catch(IOException e){
            e.printStackTrace();
            RTIViewer.fileReadingAlert.setContentText("Error accessing file at: " +
                                                        sourceFile.getPath());

            showFileReadingAlert();
        }catch(RTIFileException e){
            e.printStackTrace();
            RTIViewer.fileReadingAlert.setContentText("Error when reading file at: " +
                                                        sourceFile.getPath() + ": " +
                                                        e.getMessage());
            showFileReadingAlert();
        }catch (RuntimeException e){
            e.printStackTrace();
            RTIViewer.fileReadingAlert.setContentText("Unknown error when reading file at: " +
                                                        sourceFile.getPath() + ".");
            showFileReadingAlert();
        }finally{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.hide();
                }
            });
        }
    }


    private static void showFileReadingAlert(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                RTIViewer.fileReadingAlert.showAndWait();
            }
        });
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



    private static ArrayList<Bookmark> getBookmarksFromXML(File sourceFile){
        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        String fileSuffix = "";
        if(sourceFile.getName().endsWith(".rti")){
            fileSuffix = "_rti.xmp";
        }else if(sourceFile.getName().endsWith(".ptm")){
            fileSuffix = "_ptm.xmp";
        }else{
            return bookmarks;
        }
        String filePrefix = sourceFile.getName().substring(0, sourceFile.getName().length() - 4);
        String bookmarksFileName = filePrefix + fileSuffix;

        String bookmarksPath = sourceFile.getParent() + "\\" + bookmarksFileName;
        File bookmarkFile = new File(bookmarksPath);

        if(bookmarkFile.exists() && !bookmarkFile.isDirectory()){
            try{
                bookmarks = BookmarkManager.createBookmarksFromFile(bookmarkFile);
                return bookmarks;
            }catch (Exception e){
                RTIViewer.fileReadingAlert.setContentText("Error when reading bookmarks file at: " +
                                                            bookmarkFile.getAbsolutePath() +
                                                            ". Bookmarks file will be ignored.");
                showFileReadingAlert();
            }
        }

        return bookmarks;
    }
}
