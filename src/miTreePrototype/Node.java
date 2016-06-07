package miTreePrototype;

import java.util.*;


import miTree.PageManager;

public abstract class Node<K extends Comparable<K>, V> {

	protected final int ORDER;
	public List<K> keys;
	public List<Integer> pageIDs;
	public boolean isLeaf;
	private int level;
	
	Node(int order){
		ORDER = order;
		keys = new ArrayList<K>(ORDER);
		pageIDs = new ArrayList<Integer>(ORDER + 1);
	}
	
	
	public Node<K, V> getChild(int index, PageManager pageManager){
		return pageManager.getNodeFromPage(pageIDs.get(index), level);
	}
	
	public void setChild(int index, Integer pageNumber){ //pageNumber
		pageIDs.set(index, pageNumber);
	}
	
	public V getValue(int index, PageManager pageManager){
	//TODO odczyt wartoœci	
		return null;
	}
	
	public int getKeyLocation(K key){
		int i=0;
		while(i < keys.size() && keys.get(i).compareTo(key) <= 0){
			i++;
		}
		return i;
	}
	
	public int getExactKeyLocation(K key){
		int i = getKeyLocation(key);
		if (i>0 && i<=keys.size() && keys.get(i-1).equals(key)){
			return i-1;
		} else {
			return -1;
		}
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
	
	public Split<K, V> insert(K key, V value, Integer pageID, PageManager<K, V> pageManager, Integer currentLevel) {
		if(!isLeaf){			
			int i = getKeyLocation(key);
			Split<K,V> split = getChild(i, pageManager).insert(key, value, pageID, pageManager, currentLevel);
			pageIDs.set(i, pageID);
			pageManager.writeNodeToPage(this, pageID, currentLevel.intValue());
			currentLevel += 1;
			
			if (split != null){
				int j = getKeyLocation(split.key);
				keys.add(j, split.key);
				children.add(j+1, split.right);
				
				if(needsToBeSplit()){
					return this.split();
				}
			}
			return null;
		}
		else{
			if(keys.size() == 0){
				keys.add(key);
				int newPage = pageManager.allocateNewPage();
				pageManager.getPage(newPage).writeValue(value);
				pageIDs.add(newPage);
				pageManager.writeNodeToPage(this, pageID.intValue(), currentLevel.intValue());
				currentLevel += 1;
				return null;
			} else {
				int i = getKeyLocation(key);
				keys.add(i, key);
				int newPage = pageManager.allocateNewPage();
				pageManager.getPage(newPage).writeValue(value);
				pageIDs.add(i, newPage);
				pageManager.writeNodeToPage(this, pageID.intValue(), currentLevel.intValue());
				
				if(needsToBeSplit()){
					return this.split();
				} else {
					return null;
				}
			}
		}
	}
	
	public Split<K, V> split() {
		int mid = keys.size()/2;
		Node<K,V> rightSibling = new InnerNode<K,V>(ORDER);
		
		// srodkowy przechodzi calkowicie na wyzszy node
		K middleKey = this.keys.get(mid);
		
		rightSibling.keys = new ArrayList<K>(keys.subList(mid + 1, keys.size()));
		rightSibling.pageIDs = new ArrayList<Integer>(pageIDs.subList(mid + 1, pageIDs.size()));
		this.keys = new ArrayList<K>(keys.subList(0, mid));
		this.pageIDs =  new ArrayList<Integer>(pageIDs.subList(0, mid));
		
		return new Split<K,V>(middleKey, this, rightSibling);
	}
	
	
	public boolean remove(K key, Node<K,V> parent, int pageID){	//przekazuje rodzica, aby miec dostep do braci
		if(isLeaf){
			int i = getExactKeyLocation(key);
			if (i < 0){
				return false;
			}
			keys.remove(i);
			pageIDs.remove(i);
			if(this.needsToBeMerged()){
				return this.handleMerger(parent);
			} else {
				return false;
			}
		}
		else{
			int i = getKeyLocation(key);
			boolean changedKeysSize = getChild(i).remove(key, this, pageID);

			if (changedKeysSize){
				// syn zmienil liczbe kluczy rodzica
				
				if(parent == null){ // jesli to root
					if(keys.size() == 0){
						// usunieto wszystkie klucze, wiec zostalo sie jedno dziecko
						// dziecko staje sie nowym root'em
						return true;
					} else {
						return false;
					}
				} else {
					// poniewaz usunieto klucz, kluczy moze byc za malo
					if(this.needsToBeMerged()){
						return handleMerger(parent);
					} else {
						return false;
					}
				}
			}
			return false;
		}
	}
	abstract protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey);
	abstract protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey);
	abstract public void dump(String prefix);

	//abstract public void checkForErrors(boolean root); //DEBUG

}
