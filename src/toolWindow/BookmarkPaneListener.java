package toolWindow;

import bookmarks.Bookmark;
import bookmarks.BookmarkManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import openGLWindow.RTIWindow;


/**
 * Created by Jed on 25-Jun-17.
 */
public class BookmarkPaneListener implements EventHandler<ActionEvent> {

    private BottomTabPane bottomTabPane;

    private static BookmarkPaneListener ourInstance = new BookmarkPaneListener();

    public static BookmarkPaneListener getInstance() {
        return ourInstance;
    }

    private BookmarkPaneListener() {
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
                System.out.println(sourceButton.getId());

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
}
