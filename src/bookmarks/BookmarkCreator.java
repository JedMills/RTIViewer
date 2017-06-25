package bookmarks;

import javafx.application.Platform;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;


/**
 * Created by Jed on 25-Jun-17.
 */
public class BookmarkCreator {

    private static CreateBookmarkDialog createBookmarkDialog = new CreateBookmarkDialog();

    public static void showCreateBookmarkDialog() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createBookmarkDialog.show();
            }
        });
    }


    public static void createNewBookmark(String name){

    }


    public static ArrayList<Bookmark> readBookmarks(String fileName) throws Exception{
        File file = new File(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

        return new ArrayList<Bookmark>();
    }

}
