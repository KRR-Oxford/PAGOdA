package uk.ac.ox.cs.pagoda.util.disposable;

public class DisposedException extends RuntimeException {

    public DisposedException() {
        super();
    }

    public DisposedException(String msg) {
        super(msg);
    }
}
