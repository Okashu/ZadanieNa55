package miTreePrototype;

import java.io.Serializable;
import java.util.ArrayList;


public class LeafNode<K extends Comparable<K>, V> extends Node<K, V> implements Serializable {

	public LeafNode(int order) {
		super(order);
	}
	
	public V getValue(int index, miTree.PageManager<K, V> pageManager){
		miTree.MemoryPage<K, V> memoryPage = pageManager.getPage(pageIDs.get(index));
		if(! (memoryPage instanceof miTree.ValuePage)){
			System.out.println("ERROR: Tried to get value from a node page!");
			System.exit(-1);
		}
		miTree.ValuePage<K, V> valuePage = (miTree.ValuePage<K, V>)(pageManager.getPage(pageIDs.get(index)));
		return (V)(valuePage.readValue());
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
	public Split<K, V> insert(K key, V value, Integer pageID, miTree.PageManager<K, V> pageManager, Integer currentLevel){
		
		if(keys.size() == 0){
			keys.add(key);
			int newPageNumber=pageManager.allocateNewValuePage();
			((miTree.ValuePage<K,V>) pageManager.getPage(newPageNumber)).writeValue(value);
			pageIDs.add(newPageNumber);
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			
			return null;
		} else {
			int i = getKeyLocation(key);
			keys.add(i, key);
			int newPageNumber=pageManager.allocateNewValuePage();
			((miTree.ValuePage<K,V>) pageManager.getPage(newPageNumber)).writeValue(value);
			pageIDs.add(i, newPageNumber);
				
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			
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
		rightSibling.pageIDs = new ArrayList<Integer>(pageIDs.subList(mid, keys.size()));
		this.keys = new ArrayList<K>(keys.subList(0, mid));
		this.pageIDs = new ArrayList<Integer>(pageIDs.subList(0, mid));
		
		return new Split<K,V>(rightSibling.keys.get(0), this, rightSibling);
	}

	public void dump(String prefix, int myLevel, miTree.PageManager<K, V> pageManager) {
		System.out.println(prefix + "Leaf Node ");
		for(int i=0; i<keys.size(); i++){
			System.out.println(prefix + getValue(i, pageManager).toString());
		}
	}

/*	public boolean remove(K key, InnerNode<K,V> parent) {
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
