package miTreePrototype;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Klasa zajmuje si� obs�ug� strony pamieci.
 * Ka�da strona to plik binarny zapisywany w folderze memoryPages
 * @param <K> typ kluczy
 * @param <V> typ wartosci
 */
public class MemoryPage<K extends Comparable<K>, V> {
	protected int pageSize;
	protected String fileName;

	/**Tworzy now� stron� w nowym pliku binarnym [numer pageID].BIN
	 * @param pageID numer identyfikacyjny strony
	 * @param pageSize rozmiar strony
	 */
	public MemoryPage(int pageID, int pageSize) {
		this.pageSize = pageSize;
		
		File directory = new File("memoryPages");
		if(! directory.exists()){
			try{
				directory.mkdir();
			}
			catch(SecurityException e){
				System.out.println("ERROR: No file write access!");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		fileName = "memoryPages/" + Integer.toString(pageID) + ".BIN";
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			write(out, pageSize); // tworzy plik i zape�nia niczym
			out.close();
		} catch (IOException e) {
			System.out.println("ERROR: IOException while creating memory page!");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**Zapisuje danego Node na danym poziomie w stronie pami�ci
	 * @param node node kt�ry trzeba zapisac
	 * @param lvl poziom ma kt�rym go zapisujemy
	 * @param height wysoko�c drzewa
	 */
	public void write(Node<K, V> node, int lvl, int height) {
		if(this instanceof ValuePage){
			System.out.println("ERROR: attempt to write node to value page.");
			System.exit(-1);
		}
		File file = new File(fileName);
		File temp = new File("temp.BIN");
		int beginning = 0;	//liczba bit�w przed node'em do wpisania
		int nodeSize =  (int) (pageSize / Math.pow(2, lvl));
		int nodeLength = 0;
		beginning = (lvl == height) ? 0 : nodeSize;
		
		if(lvl == height)
			nodeSize *= 2;
		
		try {
			FileInputStream in = new FileInputStream(fileName);
			FileOutputStream out = new FileOutputStream("temp.BIN");
			for (int i = 0; i < beginning; i++) { // kopiowanie do momentu noda
				out.write(in.read());
			}
			nodeLength = write(out, node);	// zapisywanie noda
			if (nodeSize >= nodeLength) { 	//dopychanie 
				write(out, nodeSize - nodeLength);
			} else{
				in.close();
				out.close();
				throw new OutOfMemoryError();
			}
			
			in.skip(nodeSize);
			if (lvl != 1) // nie li��, kopiowanie reszty
			{
				for (int i = beginning + nodeSize; i < pageSize; i++)
					out.write(in.read());
				//uwazac na to czy nie wyleci poza pli i nie rzuci EOF
			}
			in.close();
			out.close();
			file.delete();
			temp.renameTo(file);

		} catch (FileNotFoundException e) {
			System.out.println("ERROR: File not found: " + fileName);
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("ERROR: IOException while writing node to file");
			e.printStackTrace();
			System.exit(-1);
		} catch(OutOfMemoryError e){
			System.out.println("ERROR: not enough memory");
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**Zwraca d�ugo�� serializacji danego obiektu
	 * @param obj obiekt serializowany
	 * @return d�ugo�� serializacji
	 * @throws IOException 
	 */
	protected int serializationLength(Object obj) throws IOException {
		// zeby poznac dlugosc serializacji
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray().length;
	}

	/**zapisuje Node w danym miejscu w pliku
	 * @param out
	 * @param node
	 * @return
	 * @throws IOException
	 */
	private int write(FileOutputStream out, Node<K, V> node) throws IOException {
		int nodeLength = 0;
		ObjectOutputStream writer = new ObjectOutputStream(out);
		nodeLength = serializationLength(node);
		writer.writeObject(node);
		return nodeLength;
	}

	/**Zapisuje puste miejsca po serializacji 
	 * by zachowa� okre�lone wielko�ci poziom�w w stronie pami�ci
	 * @param out
	 * @param offset
	 * @throws IOException
	 */
	private void write(FileOutputStream out, int offset) throws IOException {
		if (offset > 0) {			//dla bezpieczenstwa
			byte[] buf = new byte[offset];
			out.write(buf);
		}

	}
	
	/**Czyta Node z okre�lonego poziomu na stronie pami�ci
	 * @param offset poziom, ilo�� bit�w w pliku przed Nodem 
	 * @return Zczytany Node
	 */
	public Node<K, V> read(int offset) {
		if(this instanceof ValuePage){
			throw new IllegalArgumentException();
		}
		Node<K, V> node = null;
		try {
			FileInputStream in = new FileInputStream(fileName);
			in.skip(offset);
			ObjectInputStream reader = new ObjectInputStream(in);
			
			node = (Node<K, V>)reader.readObject();
			
			in.close();
		} catch (IOException e) {
			System.out.println("ERROR: IOException while reading offset: " + offset + " in file " + fileName);
			e.printStackTrace();
			System.exit(-1);
		} catch (ClassNotFoundException e) {
			System.out.println("ERROR: ClassNotFoundException while reading offset: " + offset);
			e.printStackTrace();
			System.exit(-1);
		} catch(IllegalArgumentException e){
			System.out.println("ERROR: Tried to read node from a value page!");
			e.printStackTrace();
			System.exit(-1);
		}
		return node;
	}
}
