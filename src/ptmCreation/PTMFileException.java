package ptmCreation;

/**
 * Created by jed on 16/05/17.
 */
public class PTMFileException extends Exception {

    public PTMFileException() {
    }

    public PTMFileException(String s) {
        super(s);
    }

    public PTMFileException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PTMFileException(Throwable throwable) {
        super(throwable);
    }

    public PTMFileException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
