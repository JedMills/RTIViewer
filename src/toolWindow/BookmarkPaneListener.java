package toolWindow;

import bookmarks.BookmarkManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;


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
        if(RTIViewer.selectedWindow != null) {
            if (event.getSource() instanceof Button) {
                Button sourceButton = (Button) event.getSource();
                if (sourceButton.getId().equals("addBookmarkButton")) {
                    BookmarkManager.showCreateBookmarkDialog(bottomTabPane.getCurrentBookmarkNames());
                } else if (sourceButton.getId().equals("deleteBookmarkButton")) {
                    String bookmarkName = bottomTabPane.getBookmarkComboBox().getValue();
                    RTIViewer.selectedWindow.deleteBookmark(bookmarkName);
                } else if (sourceButton.getId().equals("addNote")) {
                    if(bottomTabPane.getBookmarkComboBox().getSelectionModel().getSelectedItem() != null) {
                        BookmarkManager.showAddNoteDialog(bottomTabPane.getBookmarkComboBox().getSelectionModel().getSelectedItem());
                    }
                } else if (sourceButton.getId().equals("delNote")){
                    if(bottomTabPane.getNotesList().getSelectionModel().getSelectedItem() != null){
                        BookmarkManager.deleteNote(bottomTabPane.getBookmarkComboBox().getSelectionModel().getSelectedItem(),
                                                    bottomTabPane.getNotesList().getSelectionModel().getSelectedItem());

                    }
                }
            } else if (event.getSource() instanceof ComboBox) {
                ComboBox<String> sourceBox = (ComboBox<String>) event.getSource();
                bottomTabPane.showNotes(sourceBox.getSelectionModel().getSelectedItem());
            }
        }
    }
}
