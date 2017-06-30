package ptmCreation;

import bookmarks.Bookmark;
import javafx.scene.image.WritableImage;
import toolWindow.RTIViewer;
import utils.Utils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by jed on 16/05/17.
 */
public abstract class RTIObject {

    /**Path of the file*/
    protected String filePath;

    /**Width of the image*/
    protected int width;

    /**Height of the image*/
    protected int height;

    /**Contains the surface normals calculated from the PTM file*/
    protected FloatBuffer normals;

    public WritableImage previewImage;


    private ArrayList<Bookmark> bookmarks;


    /**
     * Creates a new RTIObject. Sets the passed parameters as relevant attributes.
     * TexelData needs to be a 3D int array:
     *      - first dimension  : length = 3, for red, green then blue colour channels
     *      - second dimension : length = width * height, array for image texels
     *      - third dimension  : length = 6, for the 5 PTM coefficients for each texel
     *
     * @param filePath      path to the .ptm file this object was created using
     * @param width         width of image
     * @param height        height of image
     */
    public RTIObject(String filePath, int width, int height) {
        this.filePath = filePath;
        this.width = width;
        this.height = height;
        bookmarks = new ArrayList<>();
    }


    public RTIObject(String filePath, int width, int height, ArrayList<Bookmark> bookmarks){
        this.filePath = filePath;
        this.width = width;
        this.height = height;
        this.bookmarks = bookmarks;
    }


    /**
     * Calculates a normal vector from the 6 PTM coefficients.
     *
     * @param coeffs    the six PTM coefficients, a0-a5
     * @return          the normalised vector3f
     */
    protected Utils.Vector3f calculateNormal(int[] coeffs){
        float zeroTol = Utils.ZEROTOL;
        float[] a = new float[6];
        //convert the coeffs (basically a byte array) to floats
        for(int i = 0; i < 6; i++){
            a[i] = (float) (coeffs[i] /256.0);
        }

        Utils.Vector3f lengths = new Utils.Vector3f(0f, 0f, 0f);
        if(Math.abs(4 * a[1] * a[0] - a[2] * a[2]) < zeroTol){
            lengths.x = 0.0f;
            lengths.y = 0.0f;
        }else{
            if(Math.abs(a[2]) < zeroTol){
                lengths.x = (float) (-a[3] / (2.0 * a[0]));
                lengths.y = (float) (-a[4] / (2.0 * a[1]));
            }else{
                lengths.x = (float) ((a[2]*a[4] - 2.0*a[1]*a[3])/(4.0*a[0]*a[1] - a[2]*a[2]));
                lengths.y = (float) ((a[2]*a[3] - 2.0*a[0]*a[4])/(4.0*a[0]*a[1] - a[2]*a[2]));
            }
        }

        if(Math.abs(a[0]) < zeroTol && Math.abs(a[1]) < zeroTol && Math.abs(a[2]) < zeroTol &&
                Math.abs(a[3]) < zeroTol && Math.abs(a[4]) < zeroTol){
            lengths.x = 0.0f;
            lengths.y = 0.0f;
            lengths.z = 1.0f;
        }else{
            float length2d = (lengths.x * lengths.x) + (lengths.y * lengths.y);

            int maxFound;
            if((4 * a[0] * a[1] - a[2] * a[2]) > zeroTol && a[0] < -zeroTol){
                maxFound = 1;
            }else{
                maxFound = 0;
            }

            if(length2d > 1 - zeroTol || maxFound == 0){
                int stat = Utils.computeMaximumOnCircle(a, lengths);
                if(stat == -1){
                    length2d = (float) Math.sqrt(length2d);
                    if(length2d > zeroTol){
                        lengths.x /= length2d;
                        lengths.y /= length2d;
                    }
                }
            }
            float disc = (float) (1.0 - (lengths.x * lengths.x) - (lengths.y * lengths.y));
            if(disc < 0.0){
                lengths.z = 0.0f;
            }else{
                lengths.z = (float) Math.sqrt(disc);
            }
        }
        return lengths.normalise();
    }

    protected abstract void calculateNormals();

    protected abstract void createPreviewImage();

    public String getFilePath() {
        return filePath;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public FloatBuffer getNormals() {
        return normals;
    }


    public void setBookmarks(ArrayList<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public ArrayList<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void addBookmark(Bookmark bookmark){
        bookmarks.add(bookmark);
        RTIViewer.updateBookmarks(filePath, bookmarks);
    }

    public void removeBookmark(String bookmarkName){
        for(int i = 0; i < bookmarks.size(); i++){
            if(bookmarks.get(i).getName().equals(bookmarkName)){
                bookmarks.remove(i);
                break;
            }
        }
        updateBookmarkIDs();

        RTIViewer.updateBookmarks(filePath, bookmarks);
    }


    public void updateBookmarkIDs(){
        for(int i = 0; i < bookmarks.size(); i++){
            bookmarks.get(i).setId(i);

            for(int j = 0; j < bookmarks.get(i).getNotes().size(); j++){
                bookmarks.get(i).getNotes().get(j).setId(j);
            }
        }
    }


    public Bookmark getBookmarkByName(String name){
        for(Bookmark bookmark : bookmarks){
            if (bookmark.getName().equals(name)){
                return bookmark;
            }
        }
        return null;
    }
}
