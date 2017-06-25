package bookmarks;

import utils.Utils;

import java.util.ArrayList;

/**
 * Created by Jed on 25-Jun-17.
 */
public class Bookmark {

    private int id;

    private String name;

    private boolean hasHighlightBox;

    private double zoom;

    private Utils.Vector2f pan;

    private Utils.Vector2f lightPos;

    private int renderingMode;

    private ArrayList<Double> renderingParams;

    private ArrayList<Note> notes;


    public Bookmark(int id, String name, boolean hasHighlightBox, double zoom, Utils.Vector2f pan,
                    Utils.Vector2f lightPos, int renderingMode, ArrayList<Double> renderingParams,
                    ArrayList<Note> notes) {
        this.id = id;
        this.name = name;
        this.hasHighlightBox = hasHighlightBox;
        this.zoom = zoom;
        this.pan = pan;
        this.lightPos = lightPos;
        this.renderingMode = renderingMode;
        this.renderingParams = renderingParams;
        this.notes = notes;
    }
}
