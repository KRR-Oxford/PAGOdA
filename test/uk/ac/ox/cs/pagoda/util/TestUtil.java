package uk.ac.ox.cs.pagoda.util;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * A collection of utility methods for testing.
 */
public class TestUtil {

    public static final String CONFIG_FILE = "config/test.properties";

    private static boolean isConfigLoaded = false;
    private static Properties config;

    public static Properties getConfig() {
        if(!isConfigLoaded) {
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                config = new java.util.Properties();
                config.load(in);
                in.close();
                isConfigLoaded = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    public static String combinePaths(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    public static void copyFile(String src, String dst) throws IOException {
        Files.copy(Paths.get(src), Paths.get(dst), REPLACE_EXISTING);
    }

    /*
     * Get the log file, which is assumed unique.
     * */
    public static String getLogFileName() {
        Enumeration e = Logger.getRootLogger().getAllAppenders();
        while (e.hasMoreElements()){
            Appender app = (Appender)e.nextElement();
            if (app instanceof FileAppender){
                return ((FileAppender)app).getFile();
            }
        }
        return null;
    }
}
