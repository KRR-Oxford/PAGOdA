package uk.ac.ox.cs.pagoda.util;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PagodaProperties {

	public static final String CONFIG_FILE = "pagoda.properties";
	public static final boolean DEFAULT_DEBUG = false;
	private static final boolean DEFAULT_USE_ALWAYS_SIMPLE_UPPER_BOUND;
	private static final boolean DEFAULT_USE_SKOLEM_UPPER_BOUND;
	private static final Path DEFAULT_STATISTICS_DIR;

	public static boolean shellModeDefault = false;
	private static boolean debug = DEFAULT_DEBUG;

	static {
		boolean defaultUseAlwaysSimpleUpperBound = false;
		boolean defaultUseSkolemUpperBound = true;
		Path defaultStatisticsDir = null;

		try(InputStream in = PagodaProperties.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			Properties config = new Properties();
			config.load(in);
			in.close();
			Logger logger = Logger.getLogger("PagodaProperties");
			if(config.containsKey("debug")) {
				debug = Boolean.parseBoolean(config.getProperty("debug"));
				logger.info("Debugging mode is enabled");

				if(config.containsKey("statisticsDir")) {
					defaultStatisticsDir = Paths.get(config.getProperty("statisticsDir"));
					logger.info("The directory where statistics are saved is: \"" + defaultStatisticsDir + "\"");
				}
			}
			if(config.containsKey("useAlwaysSimpleUpperBound")) {
				defaultUseAlwaysSimpleUpperBound =
						Boolean.parseBoolean(config.getProperty("useAlwaysSimpleUpperBound"));
				if(defaultUseAlwaysSimpleUpperBound)
					logger.info("By default the simple upper bound is always used");
			}
			if(config.containsKey("useSkolemUpperBound")) {
				defaultUseSkolemUpperBound = Boolean.parseBoolean(config.getProperty("useSkolemUpperBound"));
				if(defaultUseSkolemUpperBound)
					logger.info("By default the Skolem upper bound is enabled");
				else
					logger.info("By default the Skolem upper bound is disabled");
			}

		} catch(IOException e) {
			e.printStackTrace();
		}
		DEFAULT_USE_ALWAYS_SIMPLE_UPPER_BOUND = defaultUseAlwaysSimpleUpperBound;
		DEFAULT_USE_SKOLEM_UPPER_BOUND = defaultUseSkolemUpperBound;
		DEFAULT_STATISTICS_DIR = defaultStatisticsDir;
	}

	String dataPath = null;
	String ontologyPath;
	String queryPath = null;
	String answerPath = null;
	boolean toClassify = true;
	boolean toCallHermiT = true;
	boolean shellMode = shellModeDefault;
	private boolean useAlwaysSimpleUpperBound = DEFAULT_USE_ALWAYS_SIMPLE_UPPER_BOUND;
	private boolean useSkolemUpperBound = DEFAULT_USE_SKOLEM_UPPER_BOUND;
	private Path statisticsDir = DEFAULT_STATISTICS_DIR;

	public PagodaProperties(String path) {
		java.util.Properties m_properties = new java.util.Properties();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(path);
			m_properties.load(inputStream);

			setOntologyPath(m_properties.getProperty("ONTOLOGY"));
			setDataPath(m_properties.getProperty("DATA"));
			setQueryPath(m_properties.getProperty("QUERY"));
			setAnswerPath(m_properties.getProperty("ANSWER"));
			setToClassify(Boolean.parseBoolean(m_properties.getProperty("TO_CLASSIFY")));
			setToCallHermiT(Boolean.parseBoolean(m_properties.getProperty("CALL_HERMIT")));

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	public PagodaProperties() {
	}

	public static boolean isDebuggingMode() {
		return debug;
	}

	public static boolean getDefaultUseAlwaysSimpleUpperBound() {
		return DEFAULT_USE_ALWAYS_SIMPLE_UPPER_BOUND;
	}

	public static Path getDefaultStatisticsDir() {
		return DEFAULT_STATISTICS_DIR;
	}

	public static boolean getDefaultUseSkolemUpperBound() {
		return DEFAULT_USE_SKOLEM_UPPER_BOUND;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String path) {
		dataPath = path;
	}

	public String getOntologyPath() {
		return ontologyPath;
	}

	public void setOntologyPath(String path) {
		ontologyPath = path;
	}

	public String getQueryPath() {
		return queryPath;
	}

	public void setQueryPath(String path) {
		queryPath = path;
	}

	public String getAnswerPath() {
		return answerPath;
	}

	public void setAnswerPath(String path) {
		answerPath = path;
	}

	public boolean getToClassify() {
		return toClassify;
	}

	public void setToClassify(boolean flag) {
		toClassify = flag;
	}

	public boolean getToCallHermiT() {
		return toCallHermiT;
	}

	public void setToCallHermiT(boolean flag) {
		toCallHermiT = flag;
	}

	public boolean getShellMode() {
		return shellMode;
	}

	public void setShellMode(boolean flag) {
		shellMode = flag;
	}

	public boolean getUseAlwaysSimpleUpperBound() {
		return useAlwaysSimpleUpperBound;
	}

	public void setUseAlwaysSimpleUpperBound(boolean flag) {
		useAlwaysSimpleUpperBound = flag;
	}

	public boolean getUseSkolemUpperBound() {
		return useSkolemUpperBound;
	}

	public void setUseSkolemUpperBound(boolean flag) {
		useSkolemUpperBound = flag;
	}

	public Path getStatisticsDir() {
		return statisticsDir;
	}

	public void setStatisticsDir(Path statisticsDir) {
		this.statisticsDir = statisticsDir;
	}
}
