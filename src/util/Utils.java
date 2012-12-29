package util;

public class Utils {
  public static boolean isBlank(String s) {
    return (s == null) || (s.length() == 0) || (s.trim().length() == 0);
  }
}
