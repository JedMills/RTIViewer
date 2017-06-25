package toolWindow;

import bookmarks.BookmarkCreator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;


/**
 * Created by Jed on 25-Jun-17.
 */
public class BookmarkPaneListener implements EventHandler<ActionEvent> {
    private static BookmarkPaneListener ourInstance = new BookmarkPaneListener();

    public static BookmarkPaneListener getInstance() {
        return ourInstance;
    }

    private BookmarkPaneListener() {
    }


    @Override
    public void handle(ActionEvent event) {
        if(event.getSource() instanceof Button){
            Button sourceButton = (Button) event.getSource();
            if(sourceButton.getId().equals("addBookmarkButton")){
                BookmarkCreator.showCreateBookmarkDialog();
            }else if(sourceButton.getId().equals("deleteBookmarkButton")){
                System.out.println("Delete");
            }
        }
    }
}
