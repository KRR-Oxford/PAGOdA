package uk.ac.ox.cs.pagoda.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Properties {
	
	String dataPath = null;
	public String getDataPath() { return dataPath; }
	public void setDataPath(String path) { dataPath = path; }
	
	String ontologyPath;
	public String getOntologyPath() { return ontologyPath; }
	public void setOntologyPath(String path) { ontologyPath = path; }
	
	String queryPath = null; 
	public String getQueryPath() { return queryPath; }
	public void setQueryPath(String path) { queryPath = path; }
	
	String answerPath = null; 
	public String getAnswerPath() { return answerPath; }
	public void setAnswerPath(String path) { answerPath = path; }
	
	boolean toClassify = true;
	public boolean getToClassify() { return toClassify; }
	public void setToClassify(boolean flag) { toClassify = flag; }
	
	boolean toCallHermiT = true; 
	public boolean getToCallHermiT() { return toCallHermiT; }
	public void setToCallHermiT(boolean flag) { toCallHermiT = flag; }
	
	public static boolean shellModeDefault = false;
	
	boolean shellMode = shellModeDefault;
	public boolean getShellMode() { return shellMode; }
	public void setShellMode(boolean flag) { shellMode = flag; }
	
	public Properties(String path) {
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
	public Properties() { }

}
