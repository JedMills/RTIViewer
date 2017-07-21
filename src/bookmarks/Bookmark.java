package bookmarks;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 *   This class represents the bookmark objects, which can be viewed in the Bookmarks tab of the
 *   {@link toolWindow.BottomTabPane}. Bookmarks are loaded from XML bookmark files by the {@link BookmarkManager}
 *   when an {@link ptmCreation.RTIObject} is loaded into memory. Bookmarks can be created by the user when running
 *   the app. the {@link openGLWindow.RTIWindow} responsible for the {@link ptmCreation.RTIObject} it shows creates
 *   the Bookmarks. Bookmarks are saved onto the disk in an XML file by the {@link BookmarkManager} whenever they are
 *   added, deleted, or a {@link Note} is added or deleted.
 * </p>
 * <p>
 *  Bookmarks contain the information about the light angle, pan, zoom, rendering mode and rendering params required
 *  to set an RTIWindow back to the way it was set when the bookmark was created. Notes provide extra detail about the
 *  bookmark.
 * </p>
 * <p>
 *  Bookmarks are largely compatible with the original viewer's bookmarks, using the same XML formatting and layout.
 *  However, the zoom level between the two viewers is incompatible, and if bookmark was created in the old viewer with
 *  a highlight rectangle, this will not be shown in this viewer. Pan, rendering mode, light pos, and notes are cross -
 *  compatible.
 * </p>
 *
 * @see Note
 * @see toolWindow.BottomTabPane
 * @see BookmarkManager
 * @see ptmCreation.RTIObject
 * @see openGLWindow.RTIWindow
 *
 * @author Jed Mills
 *
 */
public class Bookmark {

    /** The id of the Bookmark, used to order them in the BottomTabPane and in the XML file */
    private int id;

    /** Name of the Bookmark as appears to the user in the BottomTabPane and in the XML file*/
    private String name;

    /** The name of the application that created this bookmark, saved in the XML file */
    private String creator;

    /** Zoom level for this Bookmark */
    private double zoom;

    /** X position for this Bookmark */
    private double panX;

    /** Y position for this Bookmark */
    private double panY;

    /** Normalised light X position for this bookmark */
    private double lightX;

    /** Normalised light Y position for this Bookmark */
    private double lightY;

    /** Rendering mode for this Bookmark:
      *      0 = Default
      *      1 = Diffuse gain,
      *      2 = specular enhancement,
      *      3 = normals enhancement,
      *      4 = image unsharp masking,
      *      9 = normals view               */
    private int renderingMode;

    /** Rendering params relevant ot the renderingMode attribute. */
    private HashMap<String, Double> renderingParams;

    /** List of Notes attached to this bookmark*/
    private ArrayList<Note> notes;


    /**
     * Creates a new Bookmark, with the given attributes.
     *
     * @param id
     * @param name
     * @param creator
     * @param zoom
     * @param panX
     * @param panY
     * @param lightX
     * @param lightY
     * @param renderingMode
     * @param renderingParams
     * @param notes
     */
    public Bookmark(int id, String name, String creator, double zoom, double panX, double panY,
                    double lightX, double lightY, int renderingMode, HashMap<String, Double> renderingParams,
                    ArrayList<Note> notes) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.zoom = zoom;
        this.panX = panX;
        this.panY = panY;
        this.lightX = lightX;
        this.lightY = lightY;
        this.renderingMode = renderingMode;
        this.renderingParams = renderingParams;
        this.notes = notes;
    }


    /**
     * @return  {@link Bookmark#id}
     */
    public int getId() {
        return id;
    }


    /**
     * @return {@link Bookmark#name}
     */
    public String getName() {
        return name;
    }


    /**
     * @return {@link Bookmark#creator}
     */
    public String getCreator() {
        return creator;
    }


    /**
     * @return {@link Bookmark#zoom}
     */
    public double getZoom() {
        return zoom;
    }


    /**
     * @return {@link Bookmark#panX}
     */
    public double getPanX() {
        return panX;
    }


    /**
     * @return {@link Bookmark#panY}
     */
    public double getPanY() {
        return panY;
    }


    /**
     * @return {@link Bookmark#lightX}
     */
    public double getLightX() {
        return lightX;
    }


    /**
     * @return {@link Bookmark#lightY}
     */
    public double getLightY() {
        return lightY;
    }


    /**
     * @return {@link Bookmark#renderingMode}
     */
    public int getRenderingMode() {
        return renderingMode;
    }


    /**
     * @return {@link Bookmark#renderingParams}
     */
    public HashMap<String, Double> getRenderingParams() {
        return renderingParams;
    }


    /**
     * @return {@link Bookmark#notes}
     */
    public ArrayList<Note> getNotes() {
        return notes;
    }


    /**
     * @param id    set {@link Bookmark#id}
     */
    public void setId(int id) {
        this.id = id;
    }


    /**
     * @param note adds the Note to the {@link Bookmark#notes} ArrayList attribute
     */
    public void addNote(Note note){
        notes.add(note);
    }


    /**
     * @param note removes the Note from the {@link Bookmark#notes} ArrayList attribute
     */
    public void removeNote(Note note){
        notes.remove(note);
    }


    /**
     * @param zoom  set {@link Bookmark#zoom}
     */
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }


    /**
     * @param panX  set {@link Bookmark#panX}
     */
    public void setPanX(double panX) {
        this.panX = panX;
    }


    /**
     * @param panY  set {@link Bookmark#panY}
     */
    public void setPanY(double panY) {
        this.panY = panY;
    }


    /**
     * @param lightX    set {@link Bookmark#lightX}
     */
    public void setLightX(double lightX) {
        this.lightX = lightX;
    }


    /**
     * @param lightY    set {@link Bookmark#lightY}
     */
    public void setLightY(double lightY) {
        this.lightY = lightY;
    }


    /**
     * @param renderingMode set {@link Bookmark#renderingMode}
     */
    public void setRenderingMode(int renderingMode) {
        this.renderingMode = renderingMode;
    }


    /**
     * @param renderingParams   set {@link Bookmark#renderingParams}, should be set alongside {@link Bookmark#renderingMode}
     */
    public void setRenderingParams(HashMap<String, Double> renderingParams) {
        this.renderingParams = renderingParams;
    }






    /** <p>
     *   Notes represent the the notes that can be viewed in the Bookmarks pane of the {@link toolWindow.BottomTabPane}.
     *   A note belongs to one {@link Bookmark}, and provide a way of writing comments on the particular rendering
     *   shown by a  {@link Bookmark}. Notes are created from the XML file along with the {@link Bookmark} they belong to
     *   when an {@link ptmCreation.RTIObject} is loaded by the user, and are saved along with the Bookmarks when any
     *   changes to Bookmarks or Notes are made.
     *  </p>
     * <p>
     *  Notes contain an id, which is used for ordering them in the application and XML files, and a subject, author,
     *  timestamp and comment, which can be seen by the user when the Note is edited or the mouse is hovered over the
     *  Note in the list in the {@link toolWindow.BottomTabPane}.
     * </p>
     *
     * @see Bookmark
     * @see toolWindow.BottomTabPane
     *
     * @author Jed Mills
     */
    public static class Note{

        /** Used to order the Notes inside the app and in the XML file */
        private int id;

        /** Subject of the Note, can be seen and edited by the user */
        private String subject;

        /** Author of the Note, can be seen and edited by the user */
        private String author;

        /** A Date object converted to a String, can be seen by the user */
        private String timeStamp;

        /** Comment, which is written and edited by the user */
        private String comment;


        /**
         * Creates a new Note, and sets the params passed as attributes
         *
         * @param id            sets {@link Note#id}
         * @param subject       sets {@link Note#subject}
         * @param author        sets {@link Note#author}
         * @param timeStamp     sets {@link Note#timeStamp}
         * @param comment       sets {@link Note#comment}
         */
        public Note(int id, String subject, String author, String timeStamp, String comment) {
            this.id = id;
            this.subject = subject;
            this.author = author;
            this.timeStamp = timeStamp;
            this.comment = comment;
        }


        /**
         * @return  {@link Note#id}
         */
        public int getId() {
            return id;
        }


        /**
         * @return {@link Note#subject}
         */
        public String getSubject() {
            return subject;
        }


        /**
         * @return  {@link Note#author}
         */
        public String getAuthor() {
            return author;
        }


        /**
         * @return  {@link Note#timeStamp}
         */
        public String getTimeStamp() {
            return timeStamp;
        }


        /**
         * @return  {@link Note#comment}
         */
        public String getComment() {
            return comment;
        }


        /**
         * @param id    set {@link Note#id}
         */
        public void setId(int id) {
            this.id = id;
        }


        /**
         * @param subject   set {@link Note#subject}
         */
        public void setSubject(String subject) {
            this.subject = subject;
        }


        /**
         * @param author    set {@link Note#author}
         */
        public void setAuthor(String author) {
            this.author = author;
        }


        /**
         * @param comment   set {@link Note#comment}
         */
        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
