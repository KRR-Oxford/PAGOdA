package uk.ac.ox.cs.pagoda.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PagodaProperties {

	public static final String CONFIG_FILE = "pagoda.properties";

	public static final boolean DEFAULT_DEBUG = false;
	public static boolean shellModeDefault = false;
	private static boolean debug = DEFAULT_DEBUG;

	static {
		try(InputStream in = PagodaProperties.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			Properties config = new Properties();
			config.load(in);
			in.close();
			if(config.containsKey("debug")) {
				debug = Boolean.parseBoolean(config.getProperty("debug"));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	String dataPath = null;
	String ontologyPath;
	String queryPath = null;
	String answerPath = null;
	boolean toClassify = true;
	boolean toCallHermiT = true;
	boolean shellMode = shellModeDefault;

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

}
