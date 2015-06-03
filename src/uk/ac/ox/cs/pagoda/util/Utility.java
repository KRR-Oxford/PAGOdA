package uk.ac.ox.cs.pagoda.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.HermiT.model.Atom;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utility {
	
	public static final String JAVA_FILE_SEPARATOR = "/";
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final int TEST = -1;
	public static final int FLY = 0;
	public static final int UOBM = 1;
	public static final int LUBM = 2;
	public static final int AEO = 3;
	public static final int WINE = 4;
	private static final String TEMP_DIR_PATH = "pagoda_tmp";
	static Stack<PrintStream> outs = new Stack<PrintStream>();
	private static Logger LOGS;
	private static String tempDir;
	private static int asciiX = (int) 'X';
	private static StringBuilder logMessage = new StringBuilder();

	static {
		LOGS = Logger.getLogger("Pagoda");
		LOGS.setLevel(Level.DEBUG);
	}

	static {
		outs.push(System.out);
	}

	static {

	}

	public static String getGlobalTempDirAbsolutePath() {
		if(tempDir == null) {
			try {
				Path path = Files.createTempDirectory(TEMP_DIR_PATH);
				tempDir = path.toString();
				new File(tempDir).deleteOnExit();
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return tempDir;
	}

	public static Set<Atom> toSet(Atom[] data) {
		HashSet<Atom> ret = new HashSet<Atom>();
		for(Atom element : data)
			ret.add(element);
		return ret;
	}
	
	public static boolean redirectSystemOut()
	{
		String stamp = new SimpleDateFormat( "HH:mm:ss").format(new Date());
		return redirectCurrentOut("./console" + stamp + ".txt");
	}
	
	public static boolean redirectCurrentOut(String fileName)
	{
		File file = new File(fileName);
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		outs.push(out);
		System.setOut(out);
		return true;
	}

	public static void closeCurrentOut() {
		if (!outs.isEmpty())
			outs.pop().close();

		if(!outs.isEmpty())
			System.setOut(outs.peek());
	}
	
	public static void sparql2expression(String input, String output) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		boolean first;
		String line, query;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("^")) {
				for (int i = 0; i < 4; ++i)
					line = reader.readLine();
				first = true;
				query = "";
				while ((line = reader.readLine()) != null && !line.startsWith("}"))
					if (first) {
						first = false;
						query = expression(line.trim());
					}
					else query += ", " + expression(line.trim());
				writer.write(query);
				writer.newLine();
			}
		}
		reader.close();
		writer.close();
	}

	private static String expression(String line) {
		String[] parts = line.split(" ");
		if (parts[1].equals("rdf:type")) {
			return parts[2] + "(?" + variableIndex(parts[0]) + ")";
		}
		else return parts[1] + "(?" + variableIndex(parts[0]) + ",?" + variableIndex(parts[2]) + ")";
	}
	
	private static int variableIndex(String exp) {
		char var = exp.charAt(1);
		return (int)var - asciiX;
	}
	
	public static String readLine(BufferedReader reader) throws IOException {
		String line = reader.readLine();
		if (line == null)
			return null;
		return line.trim();
	}

	public static String getTextfromFile(String fileName) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(fileName));
		String program = scanner.useDelimiter("\\Z").next();
		scanner.close();
		return program;
	}

	public static String[] getPattern(BufferedReader answerReader) throws IOException {
		String lastLine = readLine(answerReader), line;
		while ((line = readLine(answerReader)) != null && !line.startsWith("---------"))
			lastLine = line;
		return lastLine.split(" ");
	}

	public static void removeRecursively(File file) {
		if (!file.exists()) return;

		if (file.isDirectory())
			for (File tFile: file.listFiles())
				removeRecursively(tFile);
		file.delete();
	}

	public static void removeRecursively(String fileName) {
		removeRecursively(new File(fileName));
	}

	public static Collection<String> getQueryTexts(String fileName) throws IOException {
		BufferedReader queryReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		String line;
		Collection<String> queryTexts = new LinkedList<String>();
		while (true) {
			while((line = queryReader.readLine()) != null && ((line = line.trim()).isEmpty() || line.startsWith("#"))) ;
			if (line == null) {
				queryReader.close();
				return queryTexts;
			}

			StringBuffer query = new StringBuffer();
			if (!line.startsWith("^["))
				query.append(line).append(LINE_SEPARATOR);

			while((line = queryReader.readLine()) != null && !line.trim().endsWith("}"))
				query.append(line).append(LINE_SEPARATOR);
			query.append(line);
			queryTexts.add(query.toString());
		}
	}

	/**
	 *
	 * @param answerReader
	 * @return all lines before the next empty line
	 * @throws IOException
	 */
	public static Collection<String> getLines(BufferedReader answerReader) throws IOException {
		Collection<String> answerTuples = new LinkedList<String>();
		String line;
		while ((line = answerReader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				break;
			answerTuples.add(line);
		}
		return answerTuples;
	}

	private static String getLogMessage(Object[] messages) {
		if (messages.length == 1) return messages[0].toString(); 
		else {
			logMessage.setLength(0);
			for (int i = 0; i < messages.length; ++i) { 
				if (logMessage.length() != 0)
					logMessage.append(LINE_SEPARATOR); 
				logMessage.append(messages[i]); 
			}
			return logMessage.toString(); 		
		}

	}

	public static void setLogLevel(Level level) {
		LOGS.setLevel(level);
	}

	public static void logInfo(Object... messages) {
		if (LOGS != null)
			LOGS.info(getLogMessage(messages)); 
	}
	
	public static void logTrace(Object... messages) {
		if (LOGS != null)
			LOGS.trace(getLogMessage(messages)); 
	}
	
	public static void logDebug(Object... messages) {
		if (LOGS != null)
			LOGS.debug(getLogMessage(messages));
	}
	
	public static void logError(Object... messages) {
		if (LOGS != null)
			LOGS.error(getLogMessage(messages));
	}

	public static String toFileIRI(String path) {
		String iri; 
		if (path.startsWith(FILE_SEPARATOR)) iri = "file:" + path; 
		else iri = "file:\\\\\\" + path;
		return iri.replace(FILE_SEPARATOR, JAVA_FILE_SEPARATOR).replace(" ", "%20");
	}

}
