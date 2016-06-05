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
	private int PageSize = 1024; // TODO xd
	private String fileName;

	public MemoryPage(int pageID) {
		fileName = Integer.toString(pageID) + ".BIN";
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			write(out, PageSize); // tworzy plik i zape³nia niczym
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void write(Node<K, V> node, int lvl, int height) {
		File file = new File(fileName);
		File temp = new File("temp.BIN");
		int begining = 0;
		int nodeSize = 0;
		int nodeLength = 0;
		if (lvl != height)
			begining = (int) (PageSize / Math.pow(2, lvl));
		nodeSize = PageSize - begining;
		try {
			FileInputStream in = new FileInputStream(fileName);
			FileOutputStream out = new FileOutputStream("temp.BIN");
			for (int i = 0; i < begining; i++) { // kopiowanie do momentu noda
				out.write(in.read());
			}
			nodeLength = write(out, node);	// zapisywanie noda
			if (nodeSize >= nodeLength) { 	//dopychanie 
				write(out, nodeSize - nodeLength);
			} else
				System.out.println("ERROR, not enough memory");
			// zrobiæ coœ bardziej zauwazalnego, jakieœ exception
			
			in.skip(nodeSize);
			if (lvl != 1) // nie liœæ, kopiowanie reszty
			{
				for (int i = begining + nodeSize; i < PageSize; i++)
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

	private int serializationLength(Object obj) throws IOException {
		// zeby poznac dlugosc serializacji
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray().length;
	}

	private int write(FileOutputStream out, Node<K, V> node) throws IOException {
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

	public Node<K, V> read(int offset) {
		Node<K, V> node = null;
		try {
			FileInputStream in = new FileInputStream(fileName);
			ObjectInputStream reader = new ObjectInputStream(in);
			reader.skip(offset);
			node = (Node<K, V>) reader.readObject();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
	}
}
