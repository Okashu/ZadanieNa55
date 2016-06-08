package miTreePrototype;

import java.util.*;


import miTree.PageManager;

public abstract class Node<K extends Comparable<K>, V> {

	protected final int ORDER;
	public List<K> keys;
	public List<Integer> pageIDs;
	private int level;
	
	Node(int order){
		ORDER = order;
		keys = new ArrayList<K>(ORDER);
		pageIDs = new ArrayList<Integer>(ORDER + 1);
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
	
	//zwraca true, jesli zmieniono ilosc kluczy rodzica
	public boolean handleMerger(InnerNode<K,V> parent){
		if (parent == null){
			return false; //node jest rootem, wiec nie ma braci
		}
		Node<K,V> leftSibling = parent.getChildsLeftSibling(this);
		if (leftSibling != null && leftSibling.canLendAKey()){
			//pozycz od lewego brata
			K splitKey = parent.getChildSplitKey(this, true);
			K pushedKey = borrowKeys(leftSibling, true, splitKey);
			//zmiana kluczy w rodzicu
			parent.setChildSplitKey(this, true, pushedKey);
			return false;
		}
		Node<K,V> rightSibling = parent.getChildsRightSibling(this);
		if (rightSibling != null && rightSibling.canLendAKey()){
			//pozycz od prawego brata
			K splitKey = parent.getChildSplitKey(this, false);
			K pushedKey = borrowKeys(rightSibling, false, splitKey);
			//zmiana kluczy w rodzicu
			parent.setChildSplitKey(this, false, pushedKey);
			return false;
		}
		//nie mozna pozyczyc klucza
		//trzeba polaczyc braci
		if (leftSibling != null){
			//polacz z lewym
			K splitKey = parent.getChildSplitKey(this, true);
			this.mergeWith(leftSibling, true, splitKey);
			//usuniecie kluczy w rodzicu
			parent.removeChildSplitKey(this, true);
			return true;
		} else if (rightSibling != null){
			//polacz z prawym
			K splitKey = parent.getChildSplitKey(this, false);
			this.mergeWith(rightSibling, false, splitKey);
			//usuniecie kluczy w rodzicu
			parent.removeChildSplitKey(this, false);
			return true;
		}
		return false;
	}
	
	public abstract Split<K, V> insert(K key, V value, Integer pageID, PageManager<K, V> pageManager, Integer currentLevel);	
	public abstract Split<K, V> split();
	public abstract boolean remove(K key, int parentPageID); //przekazuje rodzica, aby miec dostep do braci
	abstract protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey);
	abstract protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey);
	abstract public void dump(String prefix, int myLevel, miTree.PageManager pageManager);

	//abstract public void checkForErrors(boolean root); //DEBUG

}
