package miTree;

import java.util.ArrayList;
import java.util.List;

public class Node<K extends Comparable<K>, V> {
	
	//TODO wkleiæ tutaj BPT
	public List<K> keys;
	public List<V> values;
	private final boolean isLeaf;
	
	public Node(int order, boolean isLeaf){
		this.isLeaf = isLeaf;
		keys = new ArrayList<K>(order);
		values = new ArrayList<V>(order + 1);
	}
}
