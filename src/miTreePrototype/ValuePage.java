package miTreePrototype;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ValuePage<K extends Comparable<K>, V> extends MemoryPage<K, V> {
	
	public ValuePage(int pageID, int pageSize){
		super(pageID, pageSize);
	}
	
	
	public void writeValue(V value){
		File file = new File(fileName);
		try{
			if(serializationLength(value) > pageSize){
				System.out.println("ERROR: not enough memory");
			}
			FileOutputStream out = new FileOutputStream(file);
			ObjectOutputStream writer = new ObjectOutputStream(out);
			writer.writeObject(value);
			writer.close();
		}
		catch(FileNotFoundException e){
			System.out.println("ERROR: no such file: " + fileName);
		}
		catch(IOException e){
			e.printStackTrace();
		}

	}
	public V readValue(){
		File file = new File(fileName);
		try{
			FileInputStream in = new FileInputStream(file);
			
			ObjectInputStream reader = new ObjectInputStream(in);
			
			V value = (V)reader.readObject();
			reader.close();
			return value;
			
		}
		catch(FileNotFoundException e){
			System.out.println("ERROR: no such file + " + fileName);
			return null;
		}
		catch(IOException e){
			e.printStackTrace();
			return null;
		}
		catch(ClassNotFoundException e){
			e.printStackTrace();
			return null;
		}
	}
}
