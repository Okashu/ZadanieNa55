package miTreePrototype;

import java.util.*;



public abstract class Node<K extends Comparable<K>, V> implements java.io.Serializable {

	protected int ORDER;
	public List<K> keys;
	public List<Integer> pageIDs;
	
	public Node(){
		ORDER = 3;
	}
	
	public void setOrder(int order){
		this.ORDER = order;
	}
	
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
	public boolean handleMerger(InnerNode<K,V> parent, int childIndex, int pageID, PageManager<K, V> pageManager, int currentLevel){
		if (parent == null){
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			return false; //node jest rootem, wiec nie ma braci
		}
		Node<K,V> leftSibling = parent.getChildsLeftSibling(childIndex, currentLevel, pageManager);
		if (leftSibling != null && leftSibling.canLendAKey()){
			//pozycz od lewego brata
			K splitKey = parent.getChildSplitKey(childIndex, true);
			K pushedKey = borrowKeys(leftSibling, true, splitKey);
			
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			int temp = pageManager.allocateNewPage();
			pageManager.writeNodeToPage(leftSibling, temp, currentLevel);
			//zmiana kluczy w rodzicu
			parent.setChildSplitKey(childIndex, true, pushedKey);
			parent.setChild(childIndex, pageID);
			parent.setChild(childIndex-1, temp);
			
			return false;
		}
		Node<K,V> rightSibling = parent.getChildsRightSibling(childIndex, currentLevel, pageManager);
		if (rightSibling != null && rightSibling.canLendAKey()){
			//pozycz od prawego brata
			K splitKey = parent.getChildSplitKey(childIndex, false);
			K pushedKey = borrowKeys(rightSibling, false, splitKey);
			
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			int temp = pageManager.allocateNewPage();
			pageManager.writeNodeToPage(rightSibling, temp, currentLevel);
			//zmiana kluczy w rodzicu
			parent.setChildSplitKey(childIndex, false, pushedKey);
			parent.setChild(childIndex, pageID);
			parent.setChild(childIndex+1, temp);
			return false;
		}
		//nie mozna pozyczyc klucza
		//trzeba polaczyc braci
		if (leftSibling != null){
			//polacz z lewym
			K splitKey = parent.getChildSplitKey(childIndex, true);
			this.mergeWith(leftSibling, true, splitKey);
			
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			//usuniecie kluczy w rodzicu
			parent.removeChildSplitKey(childIndex, true);
			parent.setChild(childIndex-1, pageID);
			
			return true;
		} else if (rightSibling != null){
			//polacz z prawym
			K splitKey = parent.getChildSplitKey(childIndex, false);
			this.mergeWith(rightSibling, false, splitKey);
			
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			//usuniecie kluczy w rodzicu
			parent.removeChildSplitKey(childIndex, false);
			parent.setChild(childIndex, pageID);
			
			return true;
		}
		return false;
	}
	
	public abstract Split<K, V> insert(K key, V value, int pageID, PageManager<K, V> pageManager, int currentLevel);	
	public abstract Split<K, V> split();
	public abstract boolean remove(K key, InnerNode<K, V> parent, int childIndex, int pageID, PageManager<K, V> pageManager, int currentLevel); //przekazuje rodzica, aby miec dostep do braci
	abstract protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey);
	abstract protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey);
	abstract public void dump(String prefix, int myLevel, PageManager<K, V> pageManager, int myPageID);

	//abstract public void checkForErrors(boolean root); //DEBUG

}
