package indexer;

import java.io.PrintStream;
import java.io.PrintWriter;

public class DocumentHandlerException extends Exception {
  private Throwable cause;

  /**
   * Default constructor.
   */
  public DocumentHandlerException() {
    super();
  }

  /**
   * Constructs with message.
   */
  public DocumentHandlerException(String message) {
    super(message);
  }

  /**
   * Constructs with chained exception.
   */
  public DocumentHandlerException(Throwable cause) {
    super(cause.toString());
    this.cause = cause;
  }

  /**
   * Constructs with message and exception.
   */
  public DocumentHandlerException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Retrieves nested exception.
   *
   * @return   Exception
   */
  public Throwable getException() {
    return cause;
  }

  /**
   * Retrieves nested exception.
   *
   * @Overrides printStackTrace()
   * 
   */
  public void printStackTrace() {
    printStackTrace(System.err);
  }
  
  /**
   * Retrieves nested exception.
   *
   * @Overrides printStackTrace(PrintStream ps)
   * 
   */
  public void printStackTrace(PrintStream ps) {
    synchronized (ps) {
      super.printStackTrace(ps);
      if (cause != null) {
        ps.println("--- Nested Exception ---");
        cause.printStackTrace(ps);
      }
    }
  }
  /**
   * Retrieves nested exception.
   *
   * @Overrides printStackTrace(PrintWriter pw)
   * 
   */
  public void printStackTrace(PrintWriter pw) {
    synchronized (pw) {
      super.printStackTrace(pw);
      if (cause != null) {
        pw.println("--- Nested Exception ---");
        cause.printStackTrace(pw);
      }
    }
  }
}
