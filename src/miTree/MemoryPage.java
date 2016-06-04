package miTree;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MemoryPage<K extends Comparable<K>, V> {
	private ObjectOutputStream writer;
	private int PageSize = 1024; // TODO xd
	private ObjectInputStream reader;			

	public void setWriter(String filename) {
		try {
			writer = new ObjectOutputStream(new FileOutputStream(filename + ".BIN"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MemoryPage(ArrayList<Node<K, V>> nodes, int height, int pageID) {

		setWriter(Integer.toString(pageID));

		int nodeLength = 0;
		int nodeSize = (int) (PageSize / (Math.pow(2, height)));
		// musi byæ podzielne
															
		if (nodes.size() > height) {
			System.out.println("Error, to many nodes, not enough height");
		}
		if (nodes.size() < height) {
			for (int i = 1; i < height - nodes.size(); i++) 
				nodeSize *= 2;
			write(nodeSize);
		}
		for (int i = 0; i < nodes.size(); i++) {
			nodeLength += write(nodes.get(i)); // zapisuje obiekt

			if (nodeLength <= nodeSize) 	// zapycha reszte
				write(nodeSize - nodeLength);
			else
				System.out.println("Error, Out Of Memory");
			nodeSize *= 2;
		}
	}

	public byte[] serialize(Object obj) throws IOException { 			
		// super na okolo
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}

	public int write(Node<K, V> node) {
		int nodeLength = 0;

		try {
			byte[] obj = serialize(node);
			nodeLength = obj.length;
			writer.write(ByteBuffer.allocate(4).putInt(nodeLength).array()); 
			// 4 pierwsze bity do dlugosc serializacji obiektu
			writer.writeObject(node);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodeLength;
	}

	public void write(int offset) {
		for (int i = 0; i < offset; i++)
			try {
				writer.write(ByteBuffer.allocate(offset).array());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public Node<K, V> read(int offset) {
		// TODO ????
		return null;
	}
}
