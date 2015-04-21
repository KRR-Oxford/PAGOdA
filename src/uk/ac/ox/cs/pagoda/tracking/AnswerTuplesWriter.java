package uk.ac.ox.cs.pagoda.tracking;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import uk.ac.ox.cs.pagoda.query.AnswerTuples;

public class AnswerTuplesWriter {

	private static final String QUERY_SEPARATOR = "---------------------------------------------";

	BufferedWriter writer = null; 
	
	public AnswerTuplesWriter(String fileName) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	public void write(String[] answerVariables, Collection<String> answerTuples)throws IOException {
		for (int i = 0; i < answerVariables.length; ++i)
			writer.write(answerVariables[i] + "\t");
		writer.newLine();
		writer.write(QUERY_SEPARATOR); 
		writer.newLine();
		
		if (answerTuples == null) {
			writer.newLine(); 
			return ;
		}
		
		for (String answerTuple: answerTuples) {
			writer.write(answerTuple); 
			writer.newLine(); 
		}
		writer.newLine();
	}
	
	public void close() {
		if (writer != null)
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void write(String[] answerVariables, AnswerTuples answerTuples) {
		// TODO Auto-generated method stub
		
	}
	
}
