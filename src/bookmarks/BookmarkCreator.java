package bookmarks;

import javafx.application.Platform;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Jed on 25-Jun-17.
 */
public class BookmarkCreator {

    private static CreateBookmarkDialog createBookmarkDialog;


    public static void createDialog(){
        createBookmarkDialog = new CreateBookmarkDialog();
    }


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


    public static ArrayList<Bookmark> createBookmarksFromFile(File file) throws Exception{
        return createBookmarksFromFile(file.getAbsolutePath());
    }


    public static ArrayList<Bookmark> createBookmarksFromFile(String fileName) throws Exception{
        ArrayList<Bookmark> bookmarkObjects = new ArrayList<>();

        File file = new File(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList bookmarks = doc.getElementsByTagName("rti:Bookmark");

        for(int i = 0; i < bookmarks.getLength(); i++){
            Element xmlBookmark = (Element) bookmarks.item(i);
            HashMap<String, String> renderingInfo = getRenderingInfo(xmlBookmark);
            HashMap<String, Double> renderingParams = getRenderingParams(xmlBookmark);
            ArrayList<Bookmark.Note> notes = getBookmarkNotes(xmlBookmark);

            Bookmark bookmark = new Bookmark(   Integer.parseInt(renderingInfo.get("id")),
                    renderingInfo.get("name"),
                    renderingInfo.get("creator"),
                    Double.parseDouble(renderingInfo.get("zoom")),
                    Double.parseDouble(renderingInfo.get("panX")),
                    Double.parseDouble(renderingInfo.get("panY")),
                    Double.parseDouble(renderingInfo.get("lightX")),
                    Double.parseDouble(renderingInfo.get("lightY")),
                    Integer.parseInt(renderingInfo.get("renderingID")),
                    renderingParams,
                    notes);

            bookmarkObjects.add(bookmark);
        }

        return bookmarkObjects;
    }




    private static HashMap<String, String> getRenderingInfo(Element bookmark) throws Exception{
        HashMap<String, String> info = new HashMap<>();

        Element renderingInfo = (Element) bookmark.getElementsByTagName("rti:RenderingInfo").item(0);

        String id = getContentByName(bookmark, "rti:ID");
        info.put("id", id);

        String name = getContentByName(bookmark, "rti:Name");
        info.put("name", name);

        String creator = getContentByName(bookmark, "xmp:CreatorTool");
        info.put("creator", creator);

        String zoom = getContentByName(renderingInfo, "rti:Zoom");
        info.put("zoom", zoom);

        Element pan = (Element) renderingInfo.getElementsByTagName("rti:Pan").item(0);
        String panX = getContentByName(pan, "rti:x");
        String panY = getContentByName(pan, "rti:y");
        info.put("panX", panX);
        info.put("panY", panY);

        Element incidence = (Element) renderingInfo.getElementsByTagName("rti:Incidence").item(0);
        String lightX = getContentByName(incidence, "rti:x");
        String lightY = getContentByName(incidence, "rti:y");
        info.put("lightX", lightX);
        info.put("lightY", lightY);

        Element renderingMode = (Element) renderingInfo.getElementsByTagName("rti:RenderingMode").item(0);
        String renderingID = getContentByName(renderingMode, "rti:RenderingModeID");
        info.put("renderingID", renderingID);



        return info;
    }

    private static HashMap<String, Double> getRenderingParams(Element bookmark) throws Exception{
        HashMap<String, Double> renderingParamsMap = new HashMap<>();

        Element renderingInfo = (Element) bookmark.getElementsByTagName("rti:RenderingInfo").item(0);
        Element renderingMode = (Element) renderingInfo.getElementsByTagName("rti:RenderingMode").item(0);

        Element renderingParams = (Element) renderingMode.getElementsByTagName("rti:Parameters").item(0);
        if(renderingParams != null){
            NodeList params = renderingParams.getElementsByTagName("rti:Parameter");
            for(int i = 0; i < params.getLength(); i++){
                Node param = params.item(i);
                NamedNodeMap attributes = param.getAttributes();
                Node attrName = attributes.getNamedItem("name");
                Node value = attributes.getNamedItem("value");

                renderingParamsMap.put(attrName.getTextContent(), Double.parseDouble(value.getTextContent()));

            }
        }
        return renderingParamsMap;
    }



    private static ArrayList<Bookmark.Note> getBookmarkNotes(Element bookmark) throws Exception{
        ArrayList<Bookmark.Note> notesArray = new ArrayList<>();

        Element notesSection = (Element) bookmark.getElementsByTagName("rti:Notes").item(0);
        if(notesSection != null){
            NodeList allNotes = notesSection.getElementsByTagName("rti:Note");
            for(int i = 0; i < allNotes.getLength(); i++){
                Element xmlNote = (Element) allNotes.item(i);
                int id = Integer.parseInt(getContentByName(xmlNote, "rti:ID"));
                String subject = getContentByName(xmlNote, "rti:Subject");
                String author = getContentByName(xmlNote, "rti:Author");
                String timeStamp = getContentByName(xmlNote, "rti:TimeStamp");

                String htmlComment = getContentByName(xmlNote, "rti:Comment");
                htmlComment = htmlComment.replaceAll("&lt;", "<");
                htmlComment = htmlComment.replaceAll("&gt;", ">");
                org.jsoup.nodes.Document commentDoc = Jsoup.parse(htmlComment);
                org.jsoup.nodes.Element body = commentDoc.body();

                Elements paragraphs = body.getElementsByTag("p");

                String comment = "";
                for(int j = 0; j < paragraphs.size(); j++){
                    comment += paragraphs.get(j).text() + System.lineSeparator();
                }

                Bookmark.Note note = new Bookmark.Note(id, subject, author, timeStamp, comment);
                notesArray.add(note);
            }
        }


        return notesArray;
    }

    private static String getContentByName(Element element, String name) throws Exception{
        return element.getElementsByTagName(name).item(0).getTextContent();
    }


}
