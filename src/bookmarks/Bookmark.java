package bookmarks;

import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jed on 25-Jun-17.
 */
public class Bookmark {

    private int id;

    private String name;

    private String creator;

    private double zoom;

    private double panX;

    private double panY;

    private double lightX;

    private double lightY;

    private int renderingMode;

    private HashMap<String, Double> renderingParams;

    private ArrayList<Note> notes;



    public static class Note{

        private int id;
        private String subject;
        private String author;
        private String timeStamp;
        private String comment;

        public Note(int id, String subject, String author, String timeStamp, String comment) {
            this.id = id;
            this.subject = subject;
            this.author = author;
            this.timeStamp = timeStamp;
            this.comment = comment;
        }

        public int getId() {
            return id;
        }

        public String getSubject() {
            return subject;
        }

        public String getAuthor() {
            return author;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public String getComment() {
            return comment;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }




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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public double getZoom() {
        return zoom;
    }

    public double getPanX() {
        return panX;
    }

    public double getPanY() {
        return panY;
    }

    public double getLightX() {
        return lightX;
    }

    public double getLightY() {
        return lightY;
    }

    public int getRenderingMode() {
        return renderingMode;
    }

    public HashMap<String, Double> getRenderingParams() {
        return renderingParams;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addNote(Note note){
        notes.add(note);
    }

    public void removeNote(Note note){
        notes.remove(note);
    }


    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void setPanX(double panX) {
        this.panX = panX;
    }

    public void setPanY(double panY) {
        this.panY = panY;
    }

    public void setLightX(double lightX) {
        this.lightX = lightX;
    }

    public void setLightY(double lightY) {
        this.lightY = lightY;
    }

    public void setRenderingMode(int renderingMode) {
        this.renderingMode = renderingMode;
    }

    public void setRenderingParams(HashMap<String, Double> renderingParams) {
        this.renderingParams = renderingParams;
    }
}
