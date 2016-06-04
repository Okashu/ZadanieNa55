package BPlusTree;

public class RemoveSplitKeyResult<K extends Comparable<K>, V> extends RemoveResult<K, V> {
	
	public final boolean changedLeft;
	
	public RemoveSplitKeyResult(boolean changedLeft) {
		this.changedLeft = changedLeft;
	}

}
