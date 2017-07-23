package toolWindow;

import bookmarks.Bookmark;
import bookmarks.BookmarkManager;
import imageCreation.ImageCreator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import openGLWindow.RTIWindow;

import java.io.File;


/**
 * This singleton listens to events from the BottomTabPane abd acts on them.
 */
public class BottomTabPaneListener implements EventHandler<ActionEvent> {

    /** The BottomTabPane that this listener listens to */
    private BottomTabPane bottomTabPane;

    /** The singleton instance of this class */
    private static BottomTabPaneListener ourInstance = new BottomTabPaneListener();

    /**
     * @return {@link BottomTabPaneListener#ourInstance}
     */
    public static BottomTabPaneListener getInstance() {
        return ourInstance;
    }

    /**
     * Creates a new BottomTabPaneListener.
     */
    private BottomTabPaneListener() {}

    /**
     * Sets the BottomTabPane that this listener will listen to.
     *
     * @param bottomTabPane sets {@link BottomTabPaneListener#bottomTabPane}
     */
    public static void init(BottomTabPane bottomTabPane){
        ourInstance.bottomTabPane = bottomTabPane;
    }


    /**
     * Handles events from the buttons and combo boxes and notes lists of the BottomTabPane.
     *
     * @param event the event that happened
     */
    @Override
    public void handle(ActionEvent event) {
        RTIWindow selectedWindow = RTIViewer.selectedWindow;
        //the note that is currently selected in the bottom tab pane
        Bookmark.Note selectedNote = bottomTabPane.getNotesList().getSelectionModel().getSelectedItem();

        //the bookmark name that is currently selected in the bottom tab pane
        String selectedBookmarkName = bottomTabPane.getBookmarkComboBox().getSelectionModel().getSelectedItem();

        if(RTIViewer.selectedWindow != null) {
            if (event.getSource() instanceof Button) {
                Button sourceButton = (Button) event.getSource();

                if (sourceButton.getId().equals("addBookmarkButton")) {
                    //show the dialog to add  a new bookmark
                    BookmarkManager.showCreateBookmarkDialog(bottomTabPane.getCurrentBookmarkNames());

                } else if (sourceButton.getId().equals("deleteBookmarkButton")) {
                    //remove the bookmark from the list and update the bookmark xml file
                    String bookmarkName = bottomTabPane.getBookmarkComboBox().getValue();
                    RTIViewer.selectedWindow.deleteBookmark(bookmarkName);

                } else if (sourceButton.getId().equals("addNote")) {
                    //add a new note
                    BookmarkManager.showAddNoteDialog(selectedBookmarkName);

                } else if (sourceButton.getId().equals("delNote") && selectedNote != null){
                    //delete the currently selected note
                    BookmarkManager.deleteNote(selectedNote);
                    RTIViewer.setSelectedBookmark(selectedBookmarkName);

                } else if(sourceButton.getId().equals("editNote") && selectedNote != null){
                    BookmarkManager.showEditNoteDialog(selectedNote, selectedBookmarkName);

                } else if(sourceButton.getId().equals("updateBookmark")){
                    BookmarkManager.updateBookmark(selectedBookmarkName);

                }else if(sourceButton.getId().equals("saveAsButton")){

                    //make sure at least one of the channels is going to be saved
                    if(bottomTabPane.redChannelButton.isSelected() ||
                            bottomTabPane.greenChannelButton.isSelected() ||
                            bottomTabPane.blueChannelButton.isSelected()){

                        RTIViewer.fileChooser.setTitle("Save RTI as image...");

                        //get the file format extension from the dropdown
                        String fileType = bottomTabPane.imageFormatsSelector.getSelectionModel().
                                                                            selectedItemProperty().getValue();
                        //if the user's set the default save directory, open the file chooser there
                        if(RTIViewer.defaultSaveDirectory != null){
                            RTIViewer.fileChooser.setInitialDirectory(RTIViewer.defaultSaveDirectory);
                        }
                        //add the image format extension to the file chooser's list
                        RTIViewer.fileChooser.getExtensionFilters().clear();
                        RTIViewer.fileChooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter("." + fileType, "*." + fileType));

                        //getwhere the user chose to save the file
                        File destination = RTIViewer.fileChooser.showSaveDialog(RTIViewer.primaryStage);

                        //if they didn't choose a file then just return
                        if(destination == null){return;}

                        //otherwise, find hw the current RTIObject is being rendered
                        float[] renderParams = getCurrentRenderParams();

                        boolean isGreyscale = false;
                        String colourFormat = bottomTabPane.colourModelSelector.getValue();
                        if(colourFormat.equals("Greyscale")){isGreyscale = true;}

                        //and get the image creator to create an image file on the disk of that rendering mode
                        ImageCreator.saveImage(RTIViewer.selectedWindow.rtiObject,
                                                RTIViewer.globalLightPos.x, RTIViewer.globalLightPos.y,
                                                RTIViewer.currentProgram,
                                                bottomTabPane.redChannelButton.isSelected(),
                                                bottomTabPane.greenChannelButton.isSelected(),
                                                bottomTabPane.blueChannelButton.isSelected(),
                                                fileType,
                                                destination,
                                                renderParams,
                                                isGreyscale);

                        //reset the file chooser
                        RTIViewer.fileChooser.getExtensionFilters().clear();
                    }else{
                        //otherwise tell the user to select a channel
                        RTIViewer.entryAlert.setContentText("Please select at least one colour channel to save.");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                RTIViewer.entryAlert.showAndWait();
                            }
                        });
                    }
                }

            } else if (event.getSource() instanceof ComboBox) {

                //the selected bookmark has been changed, so show the notes for that
                ComboBox<String> sourceBox = (ComboBox<String>) event.getSource();
                bottomTabPane.showNotes(sourceBox.getSelectionModel().getSelectedItem());
                Bookmark selectedBookmark = selectedWindow.rtiObject.getBookmarkByName(selectedBookmarkName);
                if(selectedBookmark != null) {
                    RTIViewer.setRenderParamsFromBookmark(selectedBookmark);
                }
            }
        }
    }


    /**
     * Gets the parameters for the current rendering mode from the tool window sliders and returns them in the
     * float array. Returns an empty array if there are no rendering parameters for the current mode.
     *
     * @return  the current rendering mode's rendering parameters.
     */
    private float[] getCurrentRenderParams(){
        //pretty self explanatory
        if(RTIViewer.currentProgram.equals(RTIViewer.ShaderProgram.DIFF_GAIN)){
            return new float[]{RTIWindow.normaliseDiffGainVal()};

        }else if(RTIViewer.currentProgram.equals(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK)){
            return new float[]{RTIWindow.normaliseImgUnMaskGainVal()};

        }else if(RTIViewer.currentProgram.equals(RTIViewer.ShaderProgram.SPEC_ENHANCE)){
            return new float[]{ RTIWindow.normaliseDiffColVal(),
                                RTIWindow.normaliseSpecVal(),
                                RTIWindow.normaliseHighlightSizeVal()};

        }

        return new float[]{};
    }
}
