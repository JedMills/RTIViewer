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
 * The BookmarkManager is a class that provides static methods for other classes for creating / editing / deleting
 * Bookmarks within the app. It provides methods to load Bookmarks from XML files in the same folder as a loaded
 * {@link ptmCreation.RTIObject}, showing the {@link CreateBookmarkDialog}, adding Notes to Bookmarks, and writing
 * the {@link Bookmark} details to an XML file when they are created by the user.
 *
 * @see CreateBookmarkDialog
 * @see Bookmark
 * @see Bookmark.Note
 *
 * @author Jed Mills
 */
public class BookmarkManager {

    /** Shown when the user clicks 'Add' to a bookmark in bookmarks pane of the viewer */
    private static CreateBookmarkDialog createBookmarkDialog;

    /** Shown when the user clicks 'Add' in the Notes part of the bookmarks pane of the viewer*/
    private static EditNoteDialog editNoteDialog;

    /** Used to crate a DocumentBuilder, for reading the XML bookmark file when loading an RTIObject */
    private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

    /** Used to create a Transformer, for write the Bookmarks for an RTIObject to an XML file  */
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

    /** Added to the notes part of the XML document, a waste of time if you ask me but makes it back compatible*/
    private static String commentHTMLHeader =   "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\">\n" +
                                                "<html><head><meta name=\"qrichtext\" content=\"1\" /><style type=\"text/css\">\n" +
                                                "p, li { white-space: pre-wrap; }\n" +
                                                "</style></head><body style=\" font-family:'MS Shell Dlg 2'; font-size:8.25pt; font-weight:400; font-style:normal;\">";


    /** Added to each paragraph of a Note's comment in the XML document, again, seems pointless */
    private static String commentHTMLParaStart = "<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">";

    /** End paragraph tag added to the end of a Note in the XML document to math the commentHTMLParaStart */
    private static String commentHTMLParaEnd = "</p>";




    /**
     * Creates a new instance of a {@link CreateBookmarkDialog} for the {@link BookmarkManager#createBookmarkDialog}
     * attribute, and a {@link EditNoteDialog} for the {@link BookmarkManager#editNoteDialog} attribute.
     */
    public static void createDialog(){
        createBookmarkDialog = new CreateBookmarkDialog();
        editNoteDialog = new EditNoteDialog();
    }




    /**
     * Shows the {@link BookmarkManager#createBookmarkDialog} on the JavaFX thread.
     *
     * @param currentBookmarkNames  names of all current Bookmarks for the RTIObject, used to ensure no duplicate names
     */
    public static void showCreateBookmarkDialog(List<String> currentBookmarkNames) {
        createBookmarkDialog.setCurrentBookmarkNames(currentBookmarkNames);

        //show the dialog on the JavaFX thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createBookmarkDialog.show();
            }
        });
    }




    /**
     * Shows the {@link BookmarkManager#editNoteDialog} on the JavaFX thread.
     *
     * @param note                  the {@link Bookmark.Note} to be edited
     * @param selectedBookmarkName  the parent {@link Bookmark} of the {@link Bookmark.Note} to be edited
     */
    public static void showEditNoteDialog(Bookmark.Note note, String selectedBookmarkName){
        //set the attributes of the EditNoteDialog so that it knows which note to edit
        editNoteDialog.setTargetNote(note);
        editNoteDialog.setTargetBookmark(selectedBookmarkName);
        editNoteDialog.setTitle("Edit note");

        //run the dialog on the JavaFX thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                editNoteDialog.show();
            }
        });
    }




    /**
     * Creates a new {@link Bookmark.Note} with the timestamp as the current time, and no all other parameters
     * as 0 / empty. Adds it to the given selectedBookmarkName param, and shows the
     * {@link BookmarkManager#editNoteDialog} to immediately edit the note.
     *
     * @param selectedBookmarkName  the parent {@link Bookmark} to add the {@link Bookmark.Note} to
     */
    public static void showAddNoteDialog(String selectedBookmarkName){
        //create a new note with the current time
        editNoteDialog.setTitle("New note");
        //the LocalDateTime adds a letter 'T' to the start of the time, so replace that with a space
        Bookmark.Note note = new Bookmark.Note(0, "", "",
                                                LocalDateTime.now().toString().replaceAll("T", " "),
                                                "");

        //find the bookmark that is the note's parent, using its name, and add this note to it
        for(Bookmark bookmark : RTIViewer.selectedWindow.rtiObject.getBookmarks()){
            if(bookmark.getName().equals(selectedBookmarkName)){
                bookmark.addNote(note);
                break;
            }
        }

        //show the dialog to edit this note
        showEditNoteDialog(note, selectedBookmarkName);
    }




    /**
     * Calls {@link BookmarkManager#createBookmarksFromFile(String)} using the {@param file}
     *
     * @param file          XML file to read Bookmarks from
     * @return              All the Bookmarks that were parsed from the XML file
     * @throws Exception    If there is an error constructing the DocumentBuilder or parsing the XML file
     */
    public static ArrayList<Bookmark> createBookmarksFromFile(File file) throws Exception{
        return createBookmarksFromFile(file.getAbsolutePath());
    }




    /**
     * Uses a new DocumentBuilder to parse the XML file specified into memory. Creates an {@link ArrayList} of new
     * Bookmarks from the parsed XML data. This method can read XML files produced by this app, and the original
     * RTIViewer.
     *
     * @see Bookmark
     *
     * @param fileName      Path of the XML file to read Bookmarks from
     * @return              All the Bookmarks that were parsed from the XML file
     * @throws Exception    If there is an error constructing the DocumentBuilder or parsing the XML file
     */
    public static ArrayList<Bookmark> createBookmarksFromFile(String fileName) throws Exception{
        //to store the parsed Bookmarks
        ArrayList<Bookmark> bookmarkObjects = new ArrayList<>();

        //create a new DocumentBuilder, which will turn the XML file into a traversable tree in memory
        File file = new File(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        //parse the XML data
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        //get all the nodes from the tree which are Bookmarks
        NodeList bookmarks = doc.getElementsByTagName("rti:Bookmark");

        //get the details from the XML tree for each bookmark node, and create a new Bookmark from it
        for(int i = 0; i < bookmarks.getLength(); i++){
            Element xmlBookmark = (Element) bookmarks.item(i);

            //all the sub elements of the Bookmark node which will make the new Bookmark object
            HashMap<String, String> renderingInfo = getRenderingInfo(xmlBookmark);
            HashMap<String, Double> renderingParams = getRenderingParams(xmlBookmark);
            ArrayList<Bookmark.Note> notes = getBookmarkNotes(xmlBookmark);

            //finally make the thing
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




    /**
     * Traverses the Element, which should be a Bookmark from the XML bookmark file, and returns a map of the
     * params and their values. The returned map will contain at least a 'id' key with the rendering type, as per
     * the types specified by the {@link Bookmark#renderingMode} attribute JavaDoc. Contains the Bookmark's id,
     * name, creator, xoom, pan, light pos, and rendering mode.
     *
     * @see Bookmark#renderingMode
     *
     * @param bookmark          the Bookmark xml tree node to find the rendering params of
     * @return                  the rendering params of the bookmark node
     * @throws Exception        if there's an error traversing the tree, likely caused by bad XML
     */
    private static HashMap<String, String> getRenderingInfo(Element bookmark) throws Exception{
        HashMap<String, String> info = new HashMap<>();

        Element renderingInfo = (Element) bookmark.getElementsByTagName("rti:RenderingInfo").item(0);

        //id inside the XML document
        String id = getContentByName(bookmark, "rti:ID");
        info.put("id", id);

        //name fo the bookmark
        String name = getContentByName(bookmark, "rti:Name");
        info.put("name", name);

        //which program created the bookmark, this app or the original creator
        String creator = getContentByName(bookmark, "xmp:CreatorTool");
        info.put("creator", creator);

        // you got it, it's the zoom, not compatible between viewers
        String zoom = getContentByName(renderingInfo, "rti:Zoom");
        info.put("zoom", zoom);

        //the pan, which is compatible between viewers
        Element pan = (Element) renderingInfo.getElementsByTagName("rti:Pan").item(0);
        String panX = getContentByName(pan, "rti:x");
        String panY = getContentByName(pan, "rti:y");
        info.put("panX", panX);
        info.put("panY", panY);

        //light position for the bookmark
        Element incidence = (Element) renderingInfo.getElementsByTagName("rti:Incidence").item(0);
        String lightX = getContentByName(incidence, "rti:x");
        String lightY = getContentByName(incidence, "rti:y");
        info.put("lightX", lightX);
        info.put("lightY", lightY);

        //the type of rendering filter for the bookmark, see the bookmark class renderingMode attr
        Element renderingMode = (Element) renderingInfo.getElementsByTagName("rti:RenderingMode").item(0);
        String renderingID = getContentByName(renderingMode, "rti:RenderingModeID");
        info.put("renderingID", renderingID);

        return info;
    }




    /**
     * Gets all the rendering parameters for the kind of filter that the bookmark represents. The map returned
     * will be of different sizes depending on the bookmark's rendering mode.
     *
     * @param bookmark      bookmark xml tree node to find the rendering params of
     * @return              the rendering params for the bookmark
     * @throws Exception    if there's an error parsing the XML tree
     */
    private static HashMap<String, Double> getRenderingParams(Element bookmark) throws Exception{
        HashMap<String, Double> renderingParamsMap = new HashMap<>();

        //get these so we can look into them to find the rendering params
        Element renderingInfo = (Element) bookmark.getElementsByTagName("rti:RenderingInfo").item(0);
        Element renderingMode = (Element) renderingInfo.getElementsByTagName("rti:RenderingMode").item(0);

        //get the rendering params, and go through them if they exist
        Element renderingParams = (Element) renderingMode.getElementsByTagName("rti:Parameters").item(0);
        if(renderingParams != null){
            NodeList params = renderingParams.getElementsByTagName("rti:Parameter");
            for(int i = 0; i < params.getLength(); i++){
                //get each rendering param and its value, and store it int he map
                Node param = params.item(i);
                NamedNodeMap attributes = param.getAttributes();
                Node attrName = attributes.getNamedItem("name");
                Node value = attributes.getNamedItem("value");

                renderingParamsMap.put(attrName.getTextContent(), Double.parseDouble(value.getTextContent()));

            }
        }
        return renderingParamsMap;
    }




    /**
     * Traverses the notes part of the bookmark node passed, getting all the info from them, and creating a new
     * {@link Bookmark.Note} for each one, which is then returned.
     *
     * @see Bookmark.Note
     *
     * @param bookmark      Bookmark xml tree node to get the notes from
     * @return              Created Notes from the xml for the passed bookmark node
     * @throws Exception    If there's an error parsing the xml tree, usually from bad xml
     */
    private static ArrayList<Bookmark.Note> getBookmarkNotes(Element bookmark) throws Exception{
        ArrayList<Bookmark.Note> notesArray = new ArrayList<>();

        //get the node for the notes, and if it exists (not null) traverse the nodes
        Element notesSection = (Element) bookmark.getElementsByTagName("rti:Notes").item(0);
        if(notesSection != null){
            NodeList allNotes = notesSection.getElementsByTagName("rti:Note");

            for(int i = 0; i < allNotes.getLength(); i++){
                //for each note, get the items that a note contains from the xml tree
                Element xmlNote = (Element) allNotes.item(i);

                //all pretty self explanatory
                int id = Integer.parseInt(getContentByName(xmlNote, "rti:ID"));
                String subject = getContentByName(xmlNote, "rti:Subject");
                String author = getContentByName(xmlNote, "rti:Author");
                String timeStamp = getContentByName(xmlNote, "rti:TimeStamp");

                //the comments in the XML are as (seemingly unnecessary) HTMl, so are wrapped up in a load of tags,
                //which we will strip out later
                String htmlComment = getContentByName(xmlNote, "rti:Comment");
                //the angle brackets in the HTML are entity characters, so replace them with the actual chars
                htmlComment = htmlComment.replaceAll("&lt;", "<");
                htmlComment = htmlComment.replaceAll("&gt;", ">");

                //now use jsoup to get the actual comment content by paragraphs
                org.jsoup.nodes.Document commentDoc = Jsoup.parse(htmlComment);
                org.jsoup.nodes.Element body = commentDoc.body();

                Elements paragraphs = body.getElementsByTag("p");

                String comment = "";
                for(int j = 0; j < paragraphs.size(); j++){
                    comment += paragraphs.get(j).text() + System.lineSeparator();
                }

                //finally got the stuff for the note
                Bookmark.Note note = new Bookmark.Note(id, subject, author, timeStamp, comment);
                notesArray.add(note);
            }
        }
        return notesArray;
    }




    /**
     * Convenience method which returns the content of the XML tree's node, with the specified name.
     *
     * @param element       node to find the value of the tag of (if that makes sense)
     * @param name          name of the tag to find the value of
     * @return              the value contained by the tag (String param)
     * @throws Exception    if tag doesn't actually exist, or error traversing the tree
     */
    private static String getContentByName(Element element, String name) throws Exception{
        return element.getElementsByTagName(name).item(0).getTextContent();
    }




    /**
     * Create a new XML file in the path specified (will overwrite an existing file with same path), writing
     * the passed bookmarks as XML in the same convention as the original viewer, so the file created "should"
     * be compatible with bookmark files from the original viewer.
     *
     * @param filePath      path to the file to write
     * @param bookmarks     all the bookmarks to write as xml
     * @throws Exception    if there was an error creating / writing the file
     */
    public static void writeBookmarksToFile(String filePath, ArrayList<Bookmark> bookmarks) throws Exception{
        //create an XML tee in memory of the bookmarks
        Document document = createXMLDocument(bookmarks);

        //make a new transformer to handle actually writing the tree as the XML text
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(filePath));

        //this is it actually writing the stuff to the file
        transformer.transform(source, streamResult);
    }




    /**
     * Creates a traversable XML tree object from the passed bookmarks array. The tree follows the convention of
     * theXML from the original Viewer, so these should be cross compatible, save for the zoom level and the
     * highlight box in the original viewer. This method does the whole shebang, write the bookmark and all its
     * rendering params and all its notes and all their fields.
     *
     * @see Bookmark
     * @see Bookmark.Note
     *
     * @param bookmarks         the bookmarks to turn into a beautiful XML tree
     * @return                  the bookmarks as a beautiful XML tree
     * @throws Exception        if there was some error creating the document builder
     */
    private static Document createXMLDocument(ArrayList<Bookmark> bookmarks) throws Exception{
        //make our new XML document in memory
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        //all this stuff is at the top of the original viewer's XML, so it's in this one's too
        Element rootElement = doc.createElement("rdf:RDF");
        rootElement.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        rootElement.setAttribute("xmlns:rti", "http://culturalheritageimaging.org/resources/ns/rti/1.0#");
        rootElement.setAttribute("xmlns:xmp", "http://ns.adobe.com/xap/1.0/");
        doc.appendChild(rootElement);

        //dunno what the point of this element in the tree is: seems like another unnecessary layer
        Element description = doc.createElement("rdf:Description");
        description.setAttribute("rdf:about", "");
        rootElement.appendChild(description);

        //here are the bookmarks, which are contained inside a Bag
        Element bookmarksElem = doc.createElement("rti:Bookmarks");
        description.appendChild(bookmarksElem);
        Element bagElem = doc.createElement("rdf:Bag");
        bookmarksElem.appendChild(bagElem);

        for(Bookmark bookmark : bookmarks){
            //each bookmark is a list item in the bookmark bag
            Element li = doc.createElement("rdf:li");
            bagElem.appendChild(li);

            //make the bookmark item
            Element bookmarkElem = doc.createElement("rti:Bookmark");
            bookmarkElem.setAttribute("rdf:parseType", "Resource");
            li.appendChild(bookmarkElem);

            //add its name, id, and the tool that created it (this Viewer app)
            createSingleAttrNode(doc, bookmarkElem, "rti:ID", bookmark.getId());
            createSingleAttrNode(doc, bookmarkElem, "rti:Name", bookmark.getName());
            createSingleAttrNode(doc, bookmarkElem, "xmp:CreatorTool", bookmark.getCreator());

            //rendering info node contains the zoom, panX/Y, lightX/Y
            Element renderInfo = createNode(doc, bookmarkElem, "rti:RenderingInfo", true);
            createSingleAttrNode(doc, renderInfo, "rti:Zoom", bookmark.getZoom());

            Element pan = createNode(doc, renderInfo, "rti:Pan", true);

            createSingleAttrNode(doc, pan, "rti:x", bookmark.getPanX());
            createSingleAttrNode(doc, pan, "rti:y", bookmark.getPanY());

            Element incidence = createNode(doc, renderInfo, "rti:Incidence", true);
            createSingleAttrNode(doc, incidence, "rti:x", bookmark.getLightX());
            createSingleAttrNode(doc, incidence, "rti:y", bookmark.getLightY());

            //the rendering mode, and a document comment showing which number maps to which rendering type
            Element renderMode = createNode(doc, renderInfo, "rti:RenderingMode", true);
            Comment comment = doc.createComment(  "0=DEFAULT,\n" +
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

            //the parameters for the rendering type are also list items in a bag
            Element parameters = createNode(doc, renderMode, "rti:Parameters", false);
            Element paramsBag = createNode(doc, parameters, "rdf:Bag", false);

            for(String key : bookmark.getRenderingParams().keySet()){
                //add each rendering parameters as a list item
                Element paramsLi = createNode(doc, paramsBag, "rdf:li", false);

                //the param and its value
                Element param = doc.createElement("rti:Parameter");
                param.setAttribute("value", String.valueOf(bookmark.getRenderingParams().get(key)));
                param.setAttribute("name", key);
                paramsLi.appendChild(param);
            }

            //the last thing in the bookmark node are the Notes, also contained as list items in a bag
            Element notesElem = createNode(doc, bookmarkElem, "rti:Notes", false);
            Element notesBag = createNode(doc, notesElem, "rdf:Bag", false);

            for(Bookmark.Note note : bookmark.getNotes()){
                //create the note as a list item, and add all its fields as nodes inside the li
                Element notesLi = createNode(doc, notesBag, "rdf:li", false);
                Element noteElem = createNode(doc, notesLi, "rti:Note", false);
                createSingleAttrNode(doc, noteElem, "rti:ID", note.getId());
                createSingleAttrNode(doc, noteElem, "rti:Subject", note.getSubject());
                createSingleAttrNode(doc, noteElem, "rti:Author", note.getAuthor());
                createSingleAttrNode(doc, noteElem, "rti:TimeStamp", note.getTimeStamp());

                //the comment is as HTML, so we have to stick that big chuck of taggage on the start of it,
                //and write each paragraph wrapped in <p> tags
                String noteComment = commentHTMLHeader;
                String[] paragraphs = note.getComment().split("\\n");
                for(String line : paragraphs){
                    noteComment += "\\n" + commentHTMLParaStart + line + commentHTMLParaEnd;
                }
                createSingleAttrNode(doc, noteElem, "rti:Comment", noteComment);
            }
        }

        //the document's finally made! :D
        return doc;
    }





    /**
     * Convenience method that creates a new Element and adds it to the given parent Element. If parseTypeRes is true,
     * will add 'rdf:parseType=Resource' inside the xml tag for the new element.
     *
     * @param document          document that the new element is part of
     * @param parent            parent fo the element to be created
     * @param name              name fo the element to be created
     * @param parseTypeRes      whether to have 'rdf:parseType=Resource' in the new element's tag
     * @return                  the newly created element
     */
    private static Element createNode(Document document, Element parent, String name, boolean parseTypeRes){
        Element element = document.createElement(name);
        if(parseTypeRes){element.setAttribute("rdf:parseType", "Resource");}
        parent.appendChild(element);
        return  element;
    }




    /**
     * Convenience method, calls {@link BookmarkManager#createSingleAttrNode(Document, Element, String, String)},
     * by taking the String value of the passes Integer contents.
     *
     * @see BookmarkManager#createSingleAttrNode(Document, Element, String, Double)
     *
     * @param document          document that the new element is part of
     * @param parent            parent fo the element to be created
     * @param name              name fo the element to be created
     * @param contents          singular contents of the node to be created
     */
    private static void createSingleAttrNode(Document document, Element parent, String name, Integer contents){
        createSingleAttrNode(document, parent, name, String.valueOf(contents));
    }




    /**
     * Convenience method, calls {@link BookmarkManager#createSingleAttrNode(Document, Element, String, String)},
     * by taking the String value of the passes Double contents.
     *
     * @see BookmarkManager#createSingleAttrNode(Document, Element, String, Double)
     *
     * @param document          document that the new element is part of
     * @param parent            parent fo the element to be created
     * @param name              name of the element to be created
     * @param contents          singular contents of the node to be created
     */
    private static void createSingleAttrNode(Document document, Element parent, String name, Double contents){
        createSingleAttrNode(document, parent, name, String.valueOf(contents));
    }





    /**
     * Convenience method, creates a new Element for the given Document, adds it to the parent Element, and gives
     * this element a single item as its contents, as specified by the contents parameter.
     *
     * @param document          document that the new element is part of
     * @param parent            parent fo the element to be created
     * @param name              name of the element to be created
     * @param contents          singular contents of the node to be created
     */
    private static void createSingleAttrNode(Document document, Element parent, String name, String contents){
        Element element = document.createElement(name);
        element.appendChild(document.createTextNode(contents));
        parent.appendChild(element);
    }





    /**
     * Removes the passed {@link Bookmark.Note} from the currently selected {@link Bookmark} from the currently selected
     * {@link openGLWindow.RTIWindow}'s {@link RTIObject}, and gets the {@link RTIViewer} to save the XML file again
     * so it's updated.
     *
     * @param note  the {@link Bookmark.Note} to remove
     */
    public static void deleteNote(Bookmark.Note note){
        RTIObject rtiObject = RTIViewer.selectedWindow.rtiObject;

        search:{
            //find the note from the currently selected bookmark and remove it
            for (Bookmark bookmark : rtiObject.getBookmarks()) {
                for (Bookmark.Note note1 : bookmark.getNotes()) {
                    //we've found the fight bookmark, so remove the note and stop looking
                    if (note1 == note) {
                        bookmark.removeNote(note);
                        break search;
                    }
                }
            }
        }
        //save the bookmark XML file so it is updated
        RTIViewer.updateBookmarks(rtiObject.getFilePath(), rtiObject.getBookmarks());
    }





    /**
     * Gets the current rendering parameters from the {@link RTIViewer}, and re-saves the bookmarks XML file so the
     * currently selected {@link Bookmark} has the current rendering parameters.
     *
     * @param bookmarkName  name of the currently selected bookmark to save the current rendering params to
     */
    public static void updateBookmark(String bookmarkName){
        //get the currently selected RTIObject
        RTIObject rtiObject = RTIViewer.selectedWindow.rtiObject;

        for(Bookmark bookmark : rtiObject.getBookmarks()){
            if(bookmark.getName().equals(bookmarkName)){
                //update the correct bookmark object in memory
                RTIViewer.selectedWindow.updateBookmark(bookmark);

                //now save all the bookmarks to the XML file
                RTIViewer.updateBookmarks(rtiObject.getFilePath(), rtiObject.getBookmarks());
                RTIViewer.setSelectedBookmark(bookmarkName);
                break;
            }
        }
    }
}
