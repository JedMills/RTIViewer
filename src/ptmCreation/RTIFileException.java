package ptmCreation;

/**
 * Created by jed on 16/05/17.
 */
public class RTIFileException extends Exception {

    public RTIFileException() {
    }

    public RTIFileException(String s) {
        super(s);
    }

    public RTIFileException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RTIFileException(Throwable throwable) {
        super(throwable);
    }

    public RTIFileException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
