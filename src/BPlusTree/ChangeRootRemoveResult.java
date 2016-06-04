package BPlusTree;

public class ChangeRootRemoveResult<K extends Comparable<K>, V> extends RemoveResult<K, V> {

	public final Node<K,V> newRoot;
	
	public ChangeRootRemoveResult(Node<K,V> newRoot) {
		this.newRoot = newRoot;
	}
	
}
