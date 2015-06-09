package uk.ac.ox.cs.pagoda.util;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * A collection of utility methods for testing.
 */
public class TestUtil {

    public static final String CONFIG_FILE = "test.properties";
    private static final Logger LOGGER = Logger.getLogger("Tester");
    private static boolean isConfigLoaded = false;
    private static Properties config;

    public static Properties getConfig() {
        if(!isConfigLoaded) {
            try(InputStream in = TestUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
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

    /**
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

    public static Path getAnswersFilePath(String name) {
        URL givenAnswersURL = TestUtil.class.getClassLoader()
                                            .getResource(name);
        if(givenAnswersURL == null) throw new RuntimeException("Missing answers file:" + name);
        return Paths.get(givenAnswersURL.getPath());
    }

    public static void logInfo(Object msg) {
        LOGGER.info(msg);
    }

    public static void logDebug(Object msg) {
        LOGGER.debug(msg);
    }

    public static void logError(Object msg) {
        LOGGER.error(msg);
    }

    public static void logError(Object msg, Throwable t) {
        LOGGER.error(msg, t);
    }

}
