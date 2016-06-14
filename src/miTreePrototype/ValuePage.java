package miTreePrototype;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Klasa specjalnych stron pamięci. Każda z nich przechowuje jedną zmienną typu V i nic więcej.
 * (w przeciwieńtwie do normalnych MemoryPage, które przechowują po kilka Node'ów)
 * @author Kacper Kozerski, Adam Michalski, Rafał Muszyński
 */
public class ValuePage<K extends Comparable<K>, V> extends MemoryPage<K, V> {
	
	/**
	 * Tworzy pustą stronę pamięci.
	 * @param pageID Numer strony w powiązanym z nią pageManagerze.
	 * @param pageSize Rozmiar strony w bajtach.
	 */
	public ValuePage(int pageID, int pageSize){
		super(pageID, pageSize);
	}
	
	/**
	 * Zapisuje podaną wartość na stronie (do pliku).
	 * @param value Wartość do zapisania.
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
	 * Odczytuje zapisaną na stronie wartość.
	 * @return Odczytana wartość.
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
