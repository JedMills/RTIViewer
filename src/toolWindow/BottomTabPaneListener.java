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
 * Created by Jed on 25-Jun-17.
 */
public class BottomTabPaneListener implements EventHandler<ActionEvent> {

    private BottomTabPane bottomTabPane;

    private static BottomTabPaneListener ourInstance = new BottomTabPaneListener();

    public static BottomTabPaneListener getInstance() {
        return ourInstance;
    }

    private BottomTabPaneListener() {
    }

    public static void init(BottomTabPane bottomTabPane){
        ourInstance.bottomTabPane = bottomTabPane;
    }


    @Override
    public void handle(ActionEvent event) {
        RTIWindow selectedWindow = RTIViewer.selectedWindow;
        Bookmark.Note selectedNote = bottomTabPane.getNotesList().getSelectionModel().getSelectedItem();
        String selectedBookmarkName = bottomTabPane.getBookmarkComboBox().getSelectionModel().getSelectedItem();

        if(RTIViewer.selectedWindow != null) {
            if (event.getSource() instanceof Button) {
                Button sourceButton = (Button) event.getSource();

                if (sourceButton.getId().equals("addBookmarkButton")) {
                    BookmarkManager.showCreateBookmarkDialog(bottomTabPane.getCurrentBookmarkNames());

                } else if (sourceButton.getId().equals("deleteBookmarkButton")) {
                    String bookmarkName = bottomTabPane.getBookmarkComboBox().getValue();

                    RTIViewer.selectedWindow.deleteBookmark(bookmarkName);

                } else if (sourceButton.getId().equals("addNote")) {
                    BookmarkManager.showAddNoteDialog(selectedBookmarkName);

                } else if (sourceButton.getId().equals("delNote") && selectedNote != null){
                    BookmarkManager.deleteNote(selectedNote);

                    RTIViewer.setSelectedBookmark(selectedBookmarkName);

                } else if(sourceButton.getId().equals("editNote") && selectedNote != null){
                    BookmarkManager.showEditNoteDialog(selectedNote, selectedBookmarkName);

                } else if(sourceButton.getId().equals("updateBookmark")){
                    BookmarkManager.updateBookmark(selectedBookmarkName);

                }else if(sourceButton.getId().equals("saveAs")){

                    if(bottomTabPane.redChannelButton.isSelected() ||
                            bottomTabPane.greenChannelButton.isSelected() ||
                            bottomTabPane.blueChannelButton.isSelected()){

                        RTIViewer.fileChooser.setTitle("Save RTI as image...");

                        String fileType = bottomTabPane.imageFormats.getSelectionModel().selectedItemProperty().getValue();

                        if(RTIViewer.defaultSaveDirectory != null){
                            RTIViewer.fileChooser.setInitialDirectory(RTIViewer.defaultSaveDirectory);
                        }
                        RTIViewer.fileChooser.getExtensionFilters().clear();
                        RTIViewer.fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("."+fileType, "*."+fileType));
                        File destination = RTIViewer.fileChooser.showSaveDialog(RTIViewer.primaryStage);

                        if(destination == null){return;}

                        float[] renderParams = getCurrentRenderParams();


                        ImageCreator.saveImage(RTIViewer.selectedWindow.rtiObject,
                                                RTIViewer.globalLightPos.x, RTIViewer.globalLightPos.y,
                                                RTIViewer.currentProgram,
                                                bottomTabPane.redChannelButton.isSelected(),
                                                bottomTabPane.greenChannelButton.isSelected(),
                                                bottomTabPane.blueChannelButton.isSelected(),
                                                fileType,
                                                destination,
                                                renderParams);

                        RTIViewer.fileChooser.getExtensionFilters().clear();
                    }else{
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

                ComboBox<String> sourceBox = (ComboBox<String>) event.getSource();
                bottomTabPane.showNotes(sourceBox.getSelectionModel().getSelectedItem());

                Bookmark selectedBookmark = selectedWindow.rtiObject.getBookmarkByName(selectedBookmarkName);
                if(selectedBookmark != null) {
                    RTIViewer.setRenderParamsFromBookmark(selectedBookmark);
                }
            }
        }
    }


    private float[] getCurrentRenderParams(){
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
