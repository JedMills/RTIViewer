package bookmarks;

import javafx.application.Platform;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.*;
import ptmCreation.RTIObject;
import toolWindow.RTIViewer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Jed on 25-Jun-17.
 */
public class BookmarkManager {

    private static CreateBookmarkDialog createBookmarkDialog;
    private static EditNoteDialog editNoteDialog;
    private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();


    private static String commentHTMLHeader =  "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\">\n" +
                                        "<html><head><meta name=\"qrichtext\" content=\"1\" /><style type=\"text/css\">\n" +
                                        "p, li { white-space: pre-wrap; }\n" +
                                        "</style></head><body style=\" font-family:'MS Shell Dlg 2'; font-size:8.25pt; font-weight:400; font-style:normal;\">";


    private static String commentHTMLParaStart = "<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">";
    private static String commentHTMLParaEnd = "</p>";


    public static void createDialog(){
        createBookmarkDialog = new CreateBookmarkDialog();
        editNoteDialog = new EditNoteDialog();
    }


    public static void showCreateBookmarkDialog(List<String> currentBookmarkNames) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createBookmarkDialog.setCurrentBookmarkNames(currentBookmarkNames);
                createBookmarkDialog.show();
            }
        });
    }

    public static void showEditNoteDialog(Bookmark.Note note, String selectedBookmarkName){
        editNoteDialog.setTargetNote(note);
        editNoteDialog.setTargetBookmark(selectedBookmarkName);
        editNoteDialog.setTitle("Edit note");

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                editNoteDialog.show();
            }
        });
    }


    public static void showAddNoteDialog(String selectedBookmarkName){
        editNoteDialog.setTitle("New note");
        Bookmark.Note note = new Bookmark.Note(0, "", "",
                                                LocalDateTime.now().toString().replaceAll("T", " "),
                                                "");

        for(Bookmark bookmark : RTIViewer.selectedWindow.rtiObject.getBookmarks()){
            if(bookmark.getName().equals(selectedBookmarkName)){
                bookmark.addNote(note);
                break;
            }
        }


        showEditNoteDialog(note, selectedBookmarkName);
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



    public static void writeBookmarksToFile(String filePath, ArrayList<Bookmark> bookmarks) throws Exception{
        Document document = createXMLDocument(bookmarks);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(filePath));
        transformer.transform(source, streamResult);
    }



    private static Document createXMLDocument(ArrayList<Bookmark> bookmarks) throws Exception{
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("rdf:RDF");
        rootElement.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        rootElement.setAttribute("xmlns:rti", "http://culturalheritageimaging.org/resources/ns/rti/1.0#");
        rootElement.setAttribute("xmlns:xmp", "http://ns.adobe.com/xap/1.0/");
        doc.appendChild(rootElement);

        Element description = doc.createElement("rdf:Description");
        description.setAttribute("rdf:about", "");
        rootElement.appendChild(description);

        Element bookmarksElem = doc.createElement("rti:Bookmarks");
        description.appendChild(bookmarksElem);

        Element bagElem = doc.createElement("rdf:Bag");
        bookmarksElem.appendChild(bagElem);

        for(Bookmark bookmark : bookmarks){
            Element li = doc.createElement("rdf:li");
            bagElem.appendChild(li);

            Element bookmarkElem = doc.createElement("rti:Bookmark");
            bookmarkElem.setAttribute("rdf:parseType", "Resource");
            li.appendChild(bookmarkElem);


            createSingleAttrNode(doc, bookmarkElem, "rti:ID", bookmark.getId());
            createSingleAttrNode(doc, bookmarkElem, "rti:Name", bookmark.getName());
            createSingleAttrNode(doc, bookmarkElem, "xmp:CreatorTool", bookmark.getCreator());

            Element renderInfo = createNode(doc, bookmarkElem, "rti:RenderingInfo", true);
            createSingleAttrNode(doc, renderInfo, "rti:Zoom", bookmark.getZoom());

            Element pan = createNode(doc, renderInfo, "rti:Pan", true);


            createSingleAttrNode(doc, pan, "rti:x", bookmark.getPanX());
            createSingleAttrNode(doc, pan, "rti:y", bookmark.getPanY());

            Element incidence = createNode(doc, renderInfo, "rti:Incidence", true);
            createSingleAttrNode(doc, incidence, "rti:x", bookmark.getLightX());
            createSingleAttrNode(doc, incidence, "rti:y", bookmark.getLightY());

            Element renderMode = createNode(doc, renderInfo, "rti:RenderingMode", true);
            Comment comment = doc.createComment( "0=DEFAULT,\n" +
                    "1=DIFFUSE_GAIN,\n" +
                    "2=SPECULAR_ENHANCEMENT,\n" +
                    "3=NORMAL_ENHANCEMENT,\n" +
                    "4=UNSHARP_MASKING_IMG,\n" +
                    "5=UNSHARP_MASKING_LUM,\n" +
                    "6=COEFF_ENHANCEMENT,\n" +
                    "7=DETAIL_ENHANCEMENT,\n" +
                    "8=DYN_DETAIL_ENHANCEMENT\n" +
                    "9=NORMALS_VISUALIZATION");
            renderMode.appendChild(comment);
            createSingleAttrNode(doc, renderMode, "rti:RenderingModeID", bookmark.getRenderingMode());

            Element parameters = createNode(doc, renderMode, "rti:Parameters", false);
            Element paramsBag = createNode(doc, parameters, "rdf:Bag", false);

            for(String key : bookmark.getRenderingParams().keySet()){
                Element paramsLi = createNode(doc, paramsBag, "rdf:li", false);

                Element param = doc.createElement("rti:Parameter");
                param.setAttribute("value", String.valueOf(bookmark.getRenderingParams().get(key)));
                param.setAttribute("name", key);
                paramsLi.appendChild(param);
            }

            Element notesElem = createNode(doc, bookmarkElem, "rti:Notes", false);

            Element notesBag = createNode(doc, notesElem, "rdf:Bag", false);

            for(Bookmark.Note note : bookmark.getNotes()){
                Element notesLi = createNode(doc, notesBag, "rdf:li", false);
                Element noteElem = createNode(doc, notesLi, "rti:Note", false);
                createSingleAttrNode(doc, noteElem, "rti:ID", note.getId());
                createSingleAttrNode(doc, noteElem, "rti:Subject", note.getSubject());
                createSingleAttrNode(doc, noteElem, "rti:Author", note.getAuthor());
                createSingleAttrNode(doc, noteElem, "rti:TimeStamp", note.getTimeStamp());

                String noteComment = commentHTMLHeader;
                String[] paragraphs = note.getComment().split("\\n");
                for(String line : paragraphs){
                    noteComment += "\\n" + commentHTMLParaStart + line + commentHTMLParaEnd;
                }
                createSingleAttrNode(doc, noteElem, "rti:Comment", noteComment);
            }
        }


        return doc;
    }

    private static Element createNode(Document document, Element parent, String name, boolean parseTypeRes){
        Element element = document.createElement(name);
        if(parseTypeRes){element.setAttribute("rdf:parseType", "Resource");}
        parent.appendChild(element);
        return  element;
    }

    private static void createSingleAttrNode(Document document, Element parent, String name, Integer contents){
        createSingleAttrNode(document, parent, name, String.valueOf(contents));
    }

    private static void createSingleAttrNode(Document document, Element parent, String name, Double contents){
        createSingleAttrNode(document, parent, name, String.valueOf(contents));
    }

    private static void createSingleAttrNode(Document document, Element parent, String name, String contents){
        Element element = document.createElement(name);
        element.appendChild(document.createTextNode(contents));
        parent.appendChild(element);
    }


    public static void deleteNote(Bookmark.Note note){
        RTIObject rtiObject = RTIViewer.selectedWindow.rtiObject;

        search:{
            for (Bookmark bookmark : rtiObject.getBookmarks()) {
                for (Bookmark.Note note1 : bookmark.getNotes()) {
                    if (note1 == note) {
                        bookmark.removeNote(note);
                        break search;
                    }
                }
            }
        }
        RTIViewer.updateBookmarks(rtiObject.getFilePath(), rtiObject.getBookmarks());

    }


    public static void updateBookmark(String bookmarkName){
        RTIObject rtiObject = RTIViewer.selectedWindow.rtiObject;

        for(Bookmark bookmark : rtiObject.getBookmarks()){
            if(bookmark.getName().equals(bookmarkName)){
                RTIViewer.selectedWindow.updateBookmark(bookmark);
                RTIViewer.updateBookmarks(rtiObject.getFilePath(), rtiObject.getBookmarks());
                RTIViewer.setSelectedBookmark(bookmarkName);
                break;
            }
        }
    }
}
