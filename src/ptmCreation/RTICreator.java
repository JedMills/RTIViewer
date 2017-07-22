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
 * This class is basically the runnable that gets started when the user wants to open an {@link RTIObject} from a file
 * on the disk. This class uses the RTIParse to parse the file and determine what type of RTIObject to create, and
 * is responsible for displaying error message sif there is an error reading the .ptm / .rti file.  This class is
 * also responsible for creating the {@link openGLWindow.RTIWindow} that shows the RTIObject.
 *
 * @see RTIObject
 * @see openGLWindow.RTIWindow
 * @see RTIParser
 */
public class RTICreator implements Runnable {

    /** The object that is created from the parsing of the RTI file */
    private RTIObject targetObject;

    /** The target file to read the RTIObject from */
    private File sourceFile;

    /** Dialog that shows when the RTICreator is reading the file and creating the RTIObject */
    private static LoadingDialog loadingDialog  = new LoadingDialog();



    /**
     * Creates a new RTICreator with it's targetFile set as the passed file.
     *
     * @param sourceFile    the file to read the RTIObject from
     */
    public RTICreator(File sourceFile){
        this.sourceFile = sourceFile;
    }




    /**
     * Shows the loading dialog to show that the creator is running. Uses the RTIParser to read the
     * {@link RTICreator#sourceFile} and create an {@link RTIObject} in memory, which gets stored in the
     * {@link RTICreator#targetObject} attribute. Will show relevant error dialogs if there is a problem reading the
     * file.
     *
     * @see RTIParser
     * @see RTIObject
     */
    @Override
    public void run() {
        try {
            //show the loading dialog
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.show();
                }
            });
            //parse the file and create the RTIObject from it
            targetObject = RTIParser.createPtmFromFile(sourceFile, RTIViewer.getMipMapping());
            //see if there is a bookmarks XML file in the same directory, and get them as objects if there is
            ArrayList<Bookmark> bookmarks = getBookmarksFromXML(sourceFile);

            //if there was a XML bookmarks file and it was parsed succesfully, set the objects bookmarks as them
            if(bookmarks.size() > 0){
                targetObject.setBookmarks(bookmarks);
            }

            //create a new window to shoe the RTIObject
            RTIViewer.createNewPTMWindow(targetObject);

            //add to the recent files menu
            RTIViewer.addRecentFile(sourceFile.getAbsolutePath());

        }catch(IOException e){
            //there was an error even getting to the file, it probably does't exist
            e.printStackTrace();
            RTIViewer.fileReadingAlert.setContentText("Error accessing file at: " +
                                                        sourceFile.getPath());

            showFileReadingAlert();

        }catch(RTIFileException e){
            //there was an error parsing the file, could be a bad header or bad texel data
            e.printStackTrace();
            RTIViewer.fileReadingAlert.setContentText("Error when reading file at: " +
                                                        sourceFile.getPath() + ": " +
                                                        e.getMessage());
            showFileReadingAlert();

        }catch (RuntimeException e){
            //loads of things could cause an error reading this file, so there's a catchall here
            e.printStackTrace();
            RTIViewer.fileReadingAlert.setContentText("Unknown error when reading file at: " +
                                                        sourceFile.getPath() + ".");
            showFileReadingAlert();

        }finally{
            //hide the dialog whether we were successful reading the file or not
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.hide();
                }
            });
        }
    }


    /**
     * Shows the {@link RTIViewer#fileReadingAlert} on the JavaFX thread
     */
    private static void showFileReadingAlert(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                RTIViewer.fileReadingAlert.showAndWait();
            }
        });
    }




    /**
     * Looks for a bookmarks XML file in the same directory as the RTIObject file (the sourceFile param).
     * The XMl file should have the same name as the .ptm/.rti file, but with .ptm/.rti replaced with '_ptm.xmp' /
     * '_rti.xmp', respectively. If this file exists, used the {@link BookmarkManager} to create an array of
     * {@link Bookmark}s from the file. If the file doesn't exist, returns an empty array. Both the prm and rti
     * bookamrks file have the same format so are read the same.
     *
     * @see Bookmark
     * @see BookmarkManager
     *
     * @param sourceFile    the .ptm/.rti file that we are looking for bookmarks for
     * @return              an array of Bookmarks created from the file
     */
    private static ArrayList<Bookmark> getBookmarksFromXML(File sourceFile){
        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        //the bookmarks file should have the same first part of the name as the .ptm/.rti file, but with
        // _ptm.xmp / _rti.xmp.
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

        //the path for the bookmarks file is the parent directory of the .ptm/.rti file, and the suppsoed
        //name fo the bookmarks file
        String bookmarksPath = sourceFile.getParent() + "\\" + bookmarksFileName;
        File bookmarkFile = new File(bookmarksPath);

        if(bookmarkFile.exists() && !bookmarkFile.isDirectory()){
            //if the file actually exists, let's read it
            try{
                bookmarks = BookmarkManager.createBookmarksFromFile(bookmarkFile);
                return bookmarks;
            }catch (Exception e){
                //there was  problem reading the bookmarks
                RTIViewer.fileReadingAlert.setContentText("Error when reading bookmarks file at: " +
                                                            bookmarkFile.getAbsolutePath() +
                                                            ". Bookmarks file will be ignored.");
                showFileReadingAlert();
            }
        }

        return bookmarks;
    }


    /**
     * This is the dialog box that appears when the {@link RTICreator} is reading a .ptm/.rti file, and creating
     * an RTIObject in memory. It has a blue circle of dots that light up in a wheel.
     */
    private static class LoadingDialog{

        /** The circle of blue dots that indicate something is actually happening */
        private ProgressIndicator progIndicator;

        /** The window that this dialog exists oin*/
        private Stage stage;

        /** The stuff that is shown in the stage*/
        private Scene scene;

        /** The VBox tha actually contains the loading label and progress indicator */
        private VBox vBox;

        /** The label that si updated with a message for what is happening */
        private Label label;

        /**
         * Creates a new LoadingDialog with a label saying  'Loading File...' and a blue ProgressIndicator.
         * The dialog is 200 x 150 pixels and has no minimise/maximise/close buttons.
         */
        public LoadingDialog(){
            //this makes the window have no min/max/close buttons
            stage = new Stage(StageStyle.UNDECORATED);

            //spinning progress indicator
            progIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
            progIndicator.setId("loadingDialogProgressIndicator");

            //box containing the label and the progress indicator
            vBox = new VBox();
            vBox.setSpacing(20);
            vBox.setId("loadingDialogMainBox");

            //label saying loading file
            vBox.setAlignment(Pos.CENTER);
            label = new Label("Loading file...");
            label.setFont(Font.font(20));
            label.setId("loadingDialogLabel");

            vBox.getChildren().addAll(label, progIndicator);
            scene = new Scene(vBox, 200, 150);
            stage.setScene(scene);
        }

        /**
         * Shows the dialog box.
         */
        public void show(){
            stage.show();
        }


        /**
         * Hides the dialog box.
         */
        public void hide(){
            stage.hide();
        }

    }



    /**
     * This exception is thrown when there is an error parsing the RTI data from a file.
     */
    public static class RTIFileException extends Exception {

    /**
     * Creates a new RTIFileException
     *
     * @param s message for why this error was created
     */
    public RTIFileException(String s) {
        super(s);
    }


    }
}
