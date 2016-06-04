package BPlusTree;

public class UpdateKeyRemoveResult<K extends Comparable<K>, V> extends RemoveResult<K, V> {

	public final K key;
	public final boolean changedLeft;
	
	public UpdateKeyRemoveResult(K key, boolean changedLeft) {
		this.key = key;
		this.changedLeft = changedLeft;
	}
	
}
