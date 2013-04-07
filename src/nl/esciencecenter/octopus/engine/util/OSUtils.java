package nl.esciencecenter.octopus.engine.util;

public class OSUtils {

    public static boolean isWindows() {
        String os = System.getProperty("os.name");

        if (os.contains("Windows")) {
            return true;
        } else {
            return false;
        }
    }

}
