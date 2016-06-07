package miTreePrototype;

import java.util.*;


public class LeafNode<K extends Comparable<K>, V> extends Node<K, V> {
	

	public LeafNode(int order) {
		super(order);
	}
	
	public V getValue(int index, miTree.PageManager<K, V> pageManager){
		return (V)(pageManager.getPage(index).readValue());
	}
	
	// zwraca dokladna lokalizacje klucza, -1 gdy liść go nie posiada
	public int getExactKeyLocation(K key){
		int i = getKeyLocation(key);
		if (i>0 && i<=keys.size() && keys.get(i-1).equals(key)){
			return i-1;
		} else {
			return -1;
		}
	}

	public Split<K, V> insert(K key, V value, int level) {
		if(keys.size() == 0){
			keys.add(key);
			values.add(value);
			return null;
		} else {
			int i = getKeyLocation(key);
			keys.add(i, key);
			values.add(i, value);
			
			if(needsToBeSplit()){
				return this.split();
			} else {
				return null;
			}
		}
	}

	public Split<K, V> split() {
		int mid = (int)Math.ceil((double)keys.size()/2);
		LeafNode<K,V> rightSibling = new LeafNode<K,V>(ORDER);
		
		rightSibling.keys = new ArrayList<K>(keys.subList(mid, keys.size()));
		rightSibling.values = new ArrayList<V>(values.subList(mid, keys.size()));
		this.keys = new ArrayList<K>(keys.subList(0, mid));
		this.values = new ArrayList<V>(values.subList(0, mid));
		
		return new Split<K,V>(rightSibling.keys.get(0), this, rightSibling);
	}

	public void dump(String prefix) {
		System.out.println(prefix + "Leaf Node ");
		for(int i=0; i<keys.size(); i++){
			System.out.println(prefix + getValue(i).toString());
		}
	}

	public boolean remove(K key, InnerNode<K,V> parent) {
		int i = getExactKeyLocation(key);
		if (i < 0){
			return false;
		}
		keys.remove(i);
		values.remove(i);
		if(this.needsToBeMerged()){
			return this.handleMerger(parent);
		} else {
			return false;
		}
	}

	protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey) {
		//polacz tablice kluczy i wartosci
		if (mergeToLeft){
			keys.addAll(0, mergingNode.keys);
			values.addAll(0, ((LeafNode<K,V>)mergingNode).values);
		} else {
			keys.addAll(mergingNode.keys);
			values.addAll(((LeafNode<K,V>)mergingNode).values);
		}
	}

	protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey) {
		//pozycz jeden klucz i wartosc z konca lewego brata lub poczatku prawego brata
		int borrowerIndex = (borrowFromLeft) ? 0 : (this.keys.size());
		int lenderIndex = (borrowFromLeft) ? (lender.keys.size() - 1) : 0;
		keys.add(borrowerIndex, lender.keys.get(lenderIndex));
		values.add(borrowerIndex, ((LeafNode<K,V>)lender).values.get(lenderIndex));
		
		lender.keys.remove(lenderIndex);
		((LeafNode<K,V>)lender).values.remove(lenderIndex);
		
		return (borrowFromLeft) ? this.keys.get(0) : lender.keys.get(0);
	}

	//DEBUG
	/*public void checkForErrors(boolean root) {
		for(int i=0; i<keys.size(); i++){
			if (keys.get(i) == null){
				System.out.println("cos sie znullowalo w lisciu");
			}
		}
	}*/

}
