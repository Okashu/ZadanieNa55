package miTree;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MemoryPage<K extends Comparable<K>, V> {
	protected int pageSize;
	protected String fileName;

	public MemoryPage(int pageID, int pageSize) {
		this.pageSize = pageSize;
		fileName = Integer.toString(pageID) + ".BIN";
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			write(out, pageSize); // tworzy plik i zape³nia niczym
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void write(miTreePrototype.Node<K, V> node, int lvl, int height) {
		if(this instanceof ValuePage){
			System.out.println("ERROR: attempt to write node to value page.");
			return;
		}
		File file = new File(fileName);
		File temp = new File("temp.BIN");
		int beginning = 0;	//liczba bitów przed node'em do wpisania
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
				System.out.println("ERROR: not enough memory");
				// zrobiæ coœ bardziej zauwazalnego, jakieœ exception
			}
			
			in.skip(nodeSize);
			if (lvl != 1) // nie liœæ, kopiowanie reszty
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected int serializationLength(Object obj) throws IOException {
		// zeby poznac dlugosc serializacji
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray().length;
	}

	private int write(FileOutputStream out, miTreePrototype.Node<K, V> node) throws IOException {
		int nodeLength = 0;
		ObjectOutputStream writer = new ObjectOutputStream(out);
		nodeLength = serializationLength(node);
		writer.writeObject(node);
		return nodeLength;
	}

	private void write(FileOutputStream out, int offset) throws IOException {
		if (offset > 0) {			//dla bezpieczenstwa
			byte[] buf = new byte[offset];
			out.write(buf);
		}

	}
	
	public miTreePrototype.Node<K, V> read(int offset) {
		if(this instanceof ValuePage){
			System.out.println("ERROR: Attempting to read node from value page");
			int a = 1/0;
			return null;
		}
		miTreePrototype.Node<K, V> node = null;
		try {
			FileInputStream in = new FileInputStream(fileName);
			in.skip(offset);
			ObjectInputStream reader = new ObjectInputStream(in);
			
			//reader.skipBytes(offset);
			node = (miTreePrototype.Node<K, V>)reader.readObject();
			//System.out.println(node.toString());
			//System.out.println(node.getClass().toString());
			//System.out.println(node.pageIDs);
			in.close();
		} catch (IOException e) {
			System.out.println("IOException w read" + offset + " " + fileName);
			e.printStackTrace();
			System.exit(0);
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException w read");
			e.printStackTrace();
		}
		return node;
	}
}
