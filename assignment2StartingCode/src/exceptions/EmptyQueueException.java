package exceptions;

/**
 * Checked exception thrown by {@link implementations.MyQueue} operations when
 * an attempt is made to access or remove an element from an empty queue.
 *
 * <p>This exception signals that the queue's length is zero at the time of the
 * call, making the requested operation impossible to complete.  Callers must
 * either catch this exception or declare it in their {@code throws} clause.</p>
 *
 * @author Sagar Girishbhai Kumbhar (Javadoc &amp; QA)
 * @version 1.0
 * @see implementations.MyQueue
 * @see utilities.QueueADT
 */
public class EmptyQueueException extends Exception
{
    /**
     * Serial version UID required for correct serialisation of this
     * {@link Exception} subclass across different JVM versions.
     */
    private static final long serialVersionUID = 682267963980463371L;

    /**
     * Constructs an {@code EmptyQueueException} with no detail message.
     * The cause is not initialised and may subsequently be set using
     * {@link Throwable#initCause(Throwable)}.
     */
    public EmptyQueueException()
    {
        super();
    }

    /**
     * Constructs an {@code EmptyQueueException} with the specified detail
     * message.
     *
     * @param message a human-readable description of the error condition that
     *                caused this exception to be thrown; retrievable later via
     *                {@link Throwable#getMessage()}.
     */
    public EmptyQueueException( String message )
    {
        super( message );
    }
}
