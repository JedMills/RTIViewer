package ptmCreation;

import bookmarks.Bookmark;
import javafx.scene.image.WritableImage;
import toolWindow.RTIViewer;
import utils.Utils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * <p>
 * This class represents the RTIObject (HSH, PTM) that is loaded from the disk from a .ptm or .rti file. Each RTIObject
 * is loaded by the {@link RTICreator}, and rendered by it's owner {@link openGLWindow.RTIWindow}. The RTIObject
 * also has a set of {@link Bookmark}s that are loaded from the associated XML file when the RTIObject is loaded,
 * and can be edited and added to by the user. The RTIObject does not change during the program, save for it's
 * {@link Bookmark} list, rather it is the shader program of the RTIObject's owner RTIWindow that change the way it
 * is rendered.
 * </p>
 * <p>
 * RTIObjects use flattened IntBuffers /FloatBuffers to store their coefficients as these are required to
 * pass this data to OpenGL as textures. The 1D arrays are remade into 3D textures of width * height * 3, which are
 * then used to calculate the colour for each pixel.
 * </p>
 *
 * @author Jed Mills
 */
public abstract class RTIObject {

    /** Path of the file */
    protected String filePath;

    /** Width of the image */
    protected int width;

    /** Height of the image */
    protected int height;

    /** Contains the surface normals calculated from the PTM file */
    protected FloatBuffer normals;

    /** The image chown in the preview window that is created when the RTIObject si loaded */
    public WritableImage previewImage;

    /** List of bookmarks fo rthis RTIObject, managed by the{@link bookmarks.BookmarkManager} */
    private ArrayList<Bookmark> bookmarks;


    /**
     * Creates a new RTIObject with the specified width and height.
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



    /**
     * Creates a new RTIObject with the specified width, height and bookmarks.
     *
     * @param filePath      path to the .ptm file this object was created using
     * @param width         width of image
     * @param height        height of image
     * @param bookmarks     bookmaks loaded from a corresponding XML bookmark sfile along with this RTIObject
     */
    public RTIObject(String filePath, int width, int height, ArrayList<Bookmark> bookmarks){
        this.filePath = filePath;
        this.width = width;
        this.height = height;
        this.bookmarks = bookmarks;
    }




    /**
     * Calculates a normal vector from the 6 PTM coefficients. The maths for this methods comes from the original
     * viewer.
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

        //the following maths comes from the original viewer: I'm not sure how it works, but it does! The creators
        //of the original viewer used the original PTM paper, a reference for which can be found in the user guide
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




    /**
     * Calculates the normal vector for each texel using the RTI coefficients from each texel, and stores the values
     * of the normals in the {@link RTIObject#normals} FloatBuffer. Calculating the normals for RGB and LRGB PTMs, and
     * HSH is a different procedure, so this is implemented differently in the subclasses.
     */
    protected abstract void calculateNormals();




    /**
     * Creates an image of the RTIObject suing the deafult rendering and a light at position (0, 0), and stores this in
     * the {@link RTIObject#previewImage} attribute.
     */
    protected abstract void createPreviewImage();




    /**
     * @return  {@link RTIObject#filePath}
     */
    public String getFilePath() {
        return filePath;
    }




    /**
     * @return {@link RTIObject#width}
     */
    public int getWidth() {
        return width;
    }




    /**
     * @return {@link RTIObject#height}
     */
    public int getHeight() {
        return height;
    }




    /**
     * @return {@link RTIObject#normals}
     */
    public FloatBuffer getNormals() {
        return normals;
    }




    /**
     * Sets the {@link RTIObject#bookmarks} attribute
     *
     * @param bookmarks bookmarks belogning to this RTIObject
     */
    public void setBookmarks(ArrayList<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }




    /**
     * @return {@link RTIObject#bookmarks}
     */
    public ArrayList<Bookmark> getBookmarks() {
        return bookmarks;
    }




    /**
     * Adds the given {@link Bookmark} to the {@link RTIObject#bookmarks} attribute, used for when a new
     * bookmark is created by the user, for example.
     *
     * @param bookmark  bookmark to add to this RTIObject
     */
    public void addBookmark(Bookmark bookmark){
        bookmarks.add(bookmark);
        RTIViewer.updateBookmarks(filePath, bookmarks);
    }




    /**
     * Removes the {@link Bookmark} with the same name as the one passed from this RTIObject's
     * {@link RTIObject#bookmarks} list, and gets the {@link RTIViewer} to update the bookmarks XML file for
     * this RTIObject on the disk.
     *
     * @param bookmarkName      name of the bookmark to remove
     */
    public void removeBookmark(String bookmarkName){
        //find the bookmark by its name and remve it
        for(int i = 0; i < bookmarks.size(); i++){
            if(bookmarks.get(i).getName().equals(bookmarkName)){
                bookmarks.remove(i);
                break;
            }
        }
        //update all the bookmark id's in thisRTIObject so they are ordered 0..*
        updateBookmarkIDs();
        //rewrite the bookmarks file on the disk with the updated information
        RTIViewer.updateBookmarks(filePath, bookmarks);
    }




    /**
     * Sets the IDs of all the {@link Bookmark}s in this RTIObject's {@link RTIObject#bookmarks} so they
     * are in ascending order so they are saved porperly in the XML file on disk. Also orders the IDs of all the
     * {@link Bookmark.Note}s of all the bookmarks in the same way
     *
     * @see Bookmark
     * @see Bookmark.Note
     */
    public void updateBookmarkIDs(){
        //loop through all the bookmarks and set their id in ascending order
        for(int i = 0; i < bookmarks.size(); i++){
            bookmarks.get(i).setId(i);

            //do the same for each of the bookmark's notes
            for(int j = 0; j < bookmarks.get(i).getNotes().size(); j++){
                bookmarks.get(i).getNotes().get(j).setId(j);
            }
        }
    }




    /**
     * Gets the {@link Bookmark} object with the given name from this RTIObject's lis tof bookmarks. Returns
     * null if a bookmark can't be found with that name.
     *
     * @see RTIObject#bookmarks
     *
     * @param name      name of the bookmark to get
     * @return          the bookmark object with the given name
     */
    public Bookmark getBookmarkByName(String name){
        //find the bookmark by name and return it
        for(Bookmark bookmark : bookmarks){
            if (bookmark.getName().equals(name)){
                return bookmark;
            }
        }
        //otherwise return null
        return null;
    }
}
