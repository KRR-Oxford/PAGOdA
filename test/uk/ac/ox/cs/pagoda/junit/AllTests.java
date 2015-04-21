package uk.ac.ox.cs.pagoda.junit;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import uk.ac.ox.cs.data.WriteIntoTurtle;

@RunWith(Suite.class)
@SuiteClasses({ WriteIntoTurtle.class, PagodaUOBM.class
	})
public class AllTests {

	public static void copy(String source, String dest) {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	        is.close();
	        os.close();
	        
		    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source))); 
		    writer.write("");
		    writer.close();
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	    
//	    File src = new File(source);
//	    src.delete(); 
	}
	
}
