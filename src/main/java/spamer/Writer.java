package spamer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Writer {

	File file = new File("sent.txt");
	DataOutputStream data;

	public Writer(){
		try {
			data = new DataOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (file.exists()) {
			file.delete();
		}
	}

	public void write(String email){
		try {
			data.writeBytes(email + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			data.flush();
			data.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
