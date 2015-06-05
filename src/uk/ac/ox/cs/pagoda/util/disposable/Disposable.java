package uk.ac.ox.cs.pagoda.util.disposable;


/**
 * Every public method of a subclass of this class,
 * as first instruction, should check if the object has already been disposed
 * and, if so, should throw a <tt>DisposedException</tt>.
 */
public abstract class Disposable {

    private boolean disposed = false;

    /**
     * This method must be called after the use of the object.
     * <p>
     * Every overriding method must call <tt>super.dispose()</tt> as first instruction.
     */
    public void dispose() {
        if(isDisposed()) throw new AlreadyDisposedException();
        disposed = true;
    }

    public final boolean isDisposed() {
        return disposed;
    }

    private class AlreadyDisposedException extends RuntimeException {
    }

}
