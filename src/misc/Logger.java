package misc;


public class Logger {

  public static org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger("root");

  /**
   * Log with DEBUG level
   * 
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void debug(String message, Object... args) {
    log4j.debug(format(message, args));
  }

  /**
   * Log with DEBUG level
   * 
   * @param e
   *          the exception to log
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void debug(Throwable e, String message, Object... args) {
    log4j.debug(format(message, args), e);
  }

  /**
   * Log with INFO level
   * 
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void info(String message, Object... args) {
    log4j.info(format(message, args));
  }

  /**
   * Log with INFO level
   * 
   * @param e
   *          the exception to log
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void info(Throwable e, String message, Object... args) {
    log4j.info(format(message, args), e);
  }

  /**
   * Log with WARN level
   * 
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void warn(String message, Object... args) {
    log4j.warn(format(message, args));
  }

  /**
   * Log with WARN level
   * 
   * @param e
   *          the exception to log
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void warn(Throwable e, String message, Object... args) {
    log4j.warn(format(message, args), e);
  }

  /**
   * Log with ERROR level
   * 
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void error(String message, Object... args) {
    log4j.error(format(message, args));
  }

  /**
   * Log with ERROR level
   * 
   * @param e
   *          the exception to log
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void error(Throwable e, String message, Object... args) {
    log4j.error(format(message, args), e);
  }

  /**
   * Log with FATAL level
   * 
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void fatal(String message, Object... args) {
    log4j.fatal(format(message, args));
  }

  /**
   * Log with FATAL level
   * 
   * @param e
   *          the exception to log
   * @param message
   *          The message pattern
   * @param args
   *          Pattern arguments
   */
  public static void fatal(Throwable e, String message, Object... args) {
    log4j.fatal(format(message, args), e);
  }

  /**
   * Try to format messages using java Formatter. Fall back to the plain message
   * if error.
   */
  static String format(String msg, Object... args) {
    try {
      if (args != null && args.length > 0) {
        return String.format(msg, args);
      }
      return msg;
    } catch (Exception e) {
      return msg;
    }
  }
}
