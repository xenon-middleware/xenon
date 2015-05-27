package nl.esciencecenter.xenon.adaptors.ftp;

/**
 * Wrapper class for executing a single operation on an FTPClient that returns a result. The wrapper takes care of checking the
 * status after execution and throwing an exception if necessary.
 *
 * @author Christiaan Meijer
 *
 */
public abstract class FtpQuery<T> extends FtpCommand {
    protected T result;

    /**
     * Gets the result of the query execution when called after calling execute.
     *
     * @return
     */
    public T getResult() {
        return result;
    }
}
