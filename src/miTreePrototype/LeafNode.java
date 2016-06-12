package miTreePrototype;

import java.io.Serializable;
import java.util.ArrayList;



public class LeafNode<K extends Comparable<K>, V> extends Node<K, V> implements Serializable {

	public LeafNode(int order) {
		super(order);
	}
	
	public V getValue(int index, PageManager<K, V> pageManager){
		MemoryPage<K, V> memoryPage = pageManager.getPage(pageIDs.get(index));
		if(! (memoryPage instanceof ValuePage)){
			System.out.println("ERROR: Tried to get value from a node page!");
			System.exit(-1);
		}
		ValuePage<K, V> valuePage = (ValuePage<K, V>)memoryPage;
		return valuePage.readValue();
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
	
	public Split<K, V> insert(K key, V value, int pageID, PageManager<K, V> pageManager, int currentLevel){
		if(keys.size() == 0){
			keys.add(key);
			int newPageNumber=pageManager.allocateNewValuePage();
			((ValuePage<K,V>) pageManager.getPage(newPageNumber)).writeValue(value);
			pageIDs.add(newPageNumber);
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			
			return null;
		} else {
			int i = getKeyLocation(key);
			keys.add(i, key);
			int newPageNumber=pageManager.allocateNewValuePage();
			((ValuePage<K,V>) pageManager.getPage(newPageNumber)).writeValue(value);
			pageIDs.add(i, newPageNumber);
				
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			
			if(needsToBeSplit()){
				if(pageManager.getTreeHeight() == 1)
				{
					return this.splitAsRoot();
				}
				else{
					return this.split();
				}
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
	public Split<K, V> splitAsRoot(){
		int mid = (int)Math.ceil((double)keys.size()/2);
		LeafNode<K, V> leftSibling = new LeafNode<K, V>(Math.max(3, ORDER/2));
		LeafNode<K,V> rightSibling = new LeafNode<K,V>(Math.max(3, ORDER/2));
		rightSibling.keys = new ArrayList<K>(keys.subList(mid, keys.size()));
		rightSibling.pageIDs = new ArrayList<Integer>(pageIDs.subList(mid, keys.size()));
		leftSibling.keys = new ArrayList<K>(keys.subList(0, mid));
		leftSibling.pageIDs = new ArrayList<Integer>(pageIDs.subList(0, mid));
		
		return new Split<K,V>(rightSibling.keys.get(0), leftSibling, rightSibling);
	}

	public void dump(String prefix, int myLevel, PageManager<K, V> pageManager, int myPageID) {
		System.out.println(prefix + "Leaf Node on page " + myPageID + " - order: "+ ORDER);
		pageManager.setPageUsed(myPageID);
		if(!nodeValueList.isEmpty()){
			System.out.print(prefix + "Extra values: ");
			writeNodeValues();
			System.out.println("");
		}
		for(int i=0; i<keys.size(); i++){
			pageManager.setPageUsed(pageIDs.get(i));
			System.out.println(prefix + getValue(i, pageManager).toString()+ " - value on page " + pageIDs.get(i));
		}
	}

	public boolean remove(K key, InnerNode<K,V> parent, int childIndex, int pageID, PageManager<K, V> pageManager, int currentLevel) {
		int i = getExactKeyLocation(key);
		if (i < 0){
			return false;
		}
		keys.remove(i);
		pageIDs.remove(i);
		if(this.needsToBeMerged()){
			return this.handleMerger(parent, childIndex, pageID, pageManager, currentLevel);
		} else {
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			return false;
		}
	}

	protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey) {
		//polacz tablice kluczy i wartosci
		if (mergeToLeft){
			keys.addAll(0, mergingNode.keys);
			pageIDs.addAll(0, ((LeafNode<K,V>)mergingNode).pageIDs);
			nodeValueList.addAll(0, mergingNode.nodeValueList);
		} else {
			keys.addAll(mergingNode.keys);
			pageIDs.addAll(((LeafNode<K,V>)mergingNode).pageIDs);
			nodeValueList.addAll(mergingNode.nodeValueList);
		}
	}

	protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey) {
		//pozycz jeden klucz i wartosc z konca lewego brata lub poczatku prawego brata
		int borrowerIndex = (borrowFromLeft) ? 0 : (this.keys.size());
		int lenderIndex = (borrowFromLeft) ? (lender.keys.size() - 1) : 0;
		keys.add(borrowerIndex, lender.keys.get(lenderIndex));
		pageIDs.add(borrowerIndex, ((LeafNode<K,V>)lender).pageIDs.get(lenderIndex));
		
		lender.keys.remove(lenderIndex);
		((LeafNode<K,V>)lender).pageIDs.remove(lenderIndex);
		
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
