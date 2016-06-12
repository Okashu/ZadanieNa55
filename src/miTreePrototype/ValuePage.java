package miTreePrototype;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Klasa specjalnych stron pami�ci. Ka�da z nich przechowuje jedn� zmienn� typu V i nic wi�cej.
 * (w przeciwie�stwie do normalnych MemoryPage, kt�re przechowuj� po kilka Node'�w)
 */
public class ValuePage<K extends Comparable<K>, V> extends MemoryPage<K, V> {
	
	/**
	 * Tworzy pust� stron� pami�ci.
	 * @param pageID Numer strony w powi�zanym z ni� pageManagerze.
	 * @param pageSize Rozmiar strony w bajtach.
	 */
	public ValuePage(int pageID, int pageSize){
		super(pageID, pageSize);
	}
	
	/**
	 * Zapisuje podan� warto�� na stronie (do pliku).
	 * @param value Warto�� do zapisania.
	 */
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
	/**
	 * Odczytuje zapisan� na stronie warto��.
	 * @return Odczytana warto��.
	 */
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
