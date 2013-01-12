package misc;

import jade.core.Agent;

public class Logger {

  public static org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger("root");

  public static void info(String message, Object... args) {
    log4j.info(format(null, message, args));
  }
  public static void info(Throwable e, String message, Object... args) {
    log4j.info(format(null, message, args), e);
  }
  public static void info(Agent agent, String message, Object... args) {
    log4j.info(format(agent, message, args));
  }
  public static void info(Agent agent, Throwable e, String message, Object... args) {
    log4j.info(format(agent, message, args), e);
  }

  public static void warn(String message, Object... args) {
    log4j.warn(format(null, message, args));
  }
  public static void warn(Throwable e, String message, Object... args) {
    log4j.warn(format(null, message, args), e);
  }
  public static void warn(Agent agent, Throwable e, String message, Object... args) {
    log4j.warn(format(agent, message, args), e);
  }
  public static void warn(Agent agent, String message, Object... args) {
    log4j.warn(format(agent, message, args));
  }

  public static void error(String message, Object... args) {
    log4j.error(format(null, message, args));
  }
  public static void error(Throwable e, String message, Object... args) {
    log4j.error(format(null, message, args), e);
  }
  public static void error(Agent agent, Throwable e, String message, Object... args) {
    log4j.error(format(agent, message, args), e);
  }
  public static void error(Agent agent, String message, Object... args) {
    log4j.error(format(agent, message, args));
  }
  
  static String format(Agent agent, String msg, Object... args) {
    try {
      if(agent != null) {
        msg = "Agent: " + agent.getLocalName() + " - " + msg;
      }
      if (args != null && args.length > 0) {
        return String.format(msg, args);
      }
      return msg;
    } catch (Exception e) {
      return msg;
    }
  }
}
