package miTree;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MemoryPage<K extends Comparable<K>, V> {
	private int PageSize = 1024; // TODO xd
	private ObjectOutputStream writer;
	private FileOutputStream out;
	private int pageID;

	public void setWriter(String filename) {
		try {
			out = new FileOutputStream(filename + ".BIN");
			writer = new ObjectOutputStream(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MemoryPage(ArrayList<Node<K, V>> nodes, int height, int pageID) {
		this.pageID = pageID;
		setWriter(Integer.toString(pageID));

		int nodeLength = 0;
		int nodeSize = (int) (PageSize / (Math.pow(2, height)));
		// musi byæ podzielne

		if (nodes.size() > height) {
			System.out.println("Error, to many nodes, not enough height");
		}
		try {
			if (nodes.size() < height) {
				for (int i = 1; i < height - nodes.size(); i++)
					nodeSize *= 2;
				write(nodeSize);
			}
			for (int i = 0; i < nodes.size(); i++) {
				nodeLength += write(nodes.get(i)); // zapisuje obiekt

				if (nodeLength < nodeSize) // zapycha reszte
					write(nodeSize - nodeLength);
				else
					System.out.println("Error, Out Of Memory");
				nodeSize *= 2;
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] serialize(Object obj) throws IOException {
		// zeby poznac dlugosc serializacji
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}

	public int write(Node<K, V> node) throws IOException {
		int nodeLength = 0;
		byte[] obj = serialize(node);
		nodeLength = obj.length;
		writer.writeObject(node);
		return nodeLength;
	}

	public void write(int offset) throws IOException {
		byte[] buf=new byte[offset];
		writer.write(buf);
	}

	public Node<K, V> read(int offset) {
		Node<K,V> node=null;
		try {
			FileInputStream in = new FileInputStream(Integer.toString(pageID) + ".BIN");
			ObjectInputStream reader=new ObjectInputStream(in);
			reader.skip(offset);
			node=(Node<K,V>) reader.readObject();
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
