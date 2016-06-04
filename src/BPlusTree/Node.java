package BPlusTree;

import java.util.*;

public abstract class Node<K extends Comparable<K>, V> {

	protected final int ORDER;
	public List<K> keys;
	
	Node(int order){
		ORDER = order;
		keys = new ArrayList<K>(ORDER);
	}
	
	public int getKeyLocation(K key){
		int i=0;
		while(i < keys.size() && keys.get(i).compareTo(key) <= 0){
			i++;
		}
		return i;
	}
	
	public boolean needsToBeSplit(){
		return keys.size() > ORDER;
	}
	
	public boolean needsToBeMerged(){
		return keys.size() < (Math.ceil((double)(ORDER+1)/2));
	}
	
	public boolean canLendAKey(){
		return keys.size() > Math.ceil((double)(ORDER+1)/2);
	}
	
	public RemoveResult<K, V> handleMerger(InnerNode<K,V> parent){
		if (parent == null){
			return null; //node jest rootem, wiec nie ma braci
		}
		//checkForErrors(false); //DEBUG
		
		Node<K,V> leftSibling = parent.getChildsLeftSibling(this);
		if (leftSibling != null && leftSibling.canLendAKey()){
			//pozycz od lewego brata
			K splitKey = parent.getChildSplitKey(this, true);
			K pushedKey = borrowKeys(leftSibling, true, splitKey);
			//checkForErrors(false); //DEBUG
			return new UpdateKeyRemoveResult<K,V>(pushedKey, true);
		}
		
		Node<K,V> rightSibling = parent.getChildsRightSibling(this);
		if (rightSibling != null && rightSibling.canLendAKey()){
			//pozycz od prawego brata
			K splitKey = parent.getChildSplitKey(this, false);
			K pushedKey = borrowKeys(rightSibling, false, splitKey);
			//checkForErrors(false); //DEBUG
			return new UpdateKeyRemoveResult<K,V>(pushedKey, false);
		}
		
		//nie mozna pozyczyc klucza
		//trzeba polaczyc braci
		
		if (leftSibling != null){
			//polacz z lewym
			K splitKey = parent.getChildSplitKey(this, true);
			this.mergeWith(leftSibling, true, splitKey);
			//checkForErrors(false); //DEBUG
			return new RemoveSplitKeyResult<K,V>(true);
		} else if (rightSibling != null){
			K splitKey = parent.getChildSplitKey(this, false);
			//System.out.println("split:" + splitKey);
			this.mergeWith(rightSibling, false, splitKey);
			//checkForErrors(false); //DEBUG
			return new RemoveSplitKeyResult<K,V>(false);
		}
		
		return null;
	}
	
	abstract public Split<K,V> insert(K key, V value);
	abstract public Split<K,V> split();
	abstract public RemoveResult<K,V> remove(K key, InnerNode<K,V> parent); //przekazuje rodzica, aby miec dostep do braci
	abstract protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey);
	abstract protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey);
	abstract public void dump(String prefix);

	//abstract public void checkForErrors(boolean root); //DEBUG

}
