package toolWindow;

import bookmarks.BookmarkCreator;
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
        if(event.getSource() instanceof Button){
            Button sourceButton = (Button) event.getSource();
            if(sourceButton.getId().equals("addBookmarkButton")){
                if(RTIViewer.selectedWindow != null) {
                    BookmarkCreator.showCreateBookmarkDialog();
                }
            }else if(sourceButton.getId().equals("deleteBookmarkButton")){
                System.out.println("Delete");
            }
        }else if(event.getSource() instanceof ComboBox){
            ComboBox<String> sourceBox = (ComboBox<String>) event.getSource();
            bottomTabPane.showNotes(sourceBox.getSelectionModel().getSelectedItem());
        }
    }
}
