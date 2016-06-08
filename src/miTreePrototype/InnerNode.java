package miTreePrototype;

import java.io.Serializable;
import java.util.ArrayList;


public class InnerNode<K extends Comparable<K>, V> extends Node<K, V> implements Serializable {

	
	public InnerNode(int order) {
		super(order);
		pageIDs = new ArrayList<Integer>(ORDER+1);
	}
	
	//powoduje zmniejszenie minimalnego rozmiaru inner-node'ów, zapobiega to
	//przepełnianiu drzewa przy pożyczaniu kluczy od braci
	public boolean canLendAKey(){
		return keys.size() > Math.ceil((double)(ORDER+1)/2-1);
	}
	
	public Node<K, V> getChild(int index, int myLevel, PageManager<K, V> pageManager){
		return pageManager.getNodeFromPage(pageIDs.get(index), myLevel - 1);
	}
	
	public void setChild(int index, int pageNumber){ //pageNumber
		pageIDs.set(index, pageNumber);
	}

	public Split<K, V> insert(K key, V value, Integer pageID, PageManager<K, V> pageManager, Integer currentLevel){
		
		int i = getKeyLocation(key);
		Split<K,V> split = getChild(i,currentLevel,pageManager).insert(key, value,pageID,pageManager, currentLevel - 1);
		
		if (split != null){
			int j = getKeyLocation(split.key);
			keys.add(j, split.key);
			pageIDs.set(j, pageID);
			pageManager.writeNodeToPage(split.left, pageID, currentLevel-1);
			int temp = pageManager.allocateNewPage();
			pageIDs.add(j+1, temp);
			pageManager.writeNodeToPage(split.right, temp, currentLevel-1);
			
			if(needsToBeSplit()){
				return this.split();
			}else{
				pageManager.writeNodeToPage(this, pageID, currentLevel);
			}
		} else {
			pageIDs.set(i, pageID);
			pageManager.writeNodeToPage(this, pageID, currentLevel);
		}
		return null;
	}

	public Split<K, V> split() {
		int mid = keys.size()/2;
		InnerNode<K,V> rightSibling = new InnerNode<K,V>(ORDER);
		
		// srodkowy przechodzi calkowicie na wyzszy node
		K middleKey = this.keys.get(mid);
		
		rightSibling.keys = new ArrayList<K>(keys.subList(mid + 1, keys.size()));
		rightSibling.pageIDs = new ArrayList<Integer>(pageIDs.subList(mid + 1, pageIDs.size()));
		this.keys = new ArrayList<K>(keys.subList(0, mid));
		this.pageIDs =  new ArrayList<Integer>(pageIDs.subList(0, mid + 1));
		
		return new Split<K,V>(middleKey, this, rightSibling);
	}

	public void dump(String prefix, int myLevel, PageManager<K, V> pageManager) {
		System.out.println(prefix + "Inner Node");
		for(int i=0; i<pageIDs.size(); i++){
			getChild(i, myLevel, pageManager).dump(prefix + "    ", myLevel - 1, pageManager);
			if(i<keys.size()){
				System.out.println(prefix + "+Key: " + keys.get(i));
			}
		}
	}
	
	// zwraca lewego brata podanego dziecka
	public Node<K,V> getChildsLeftSibling(int childIndex, int childLevel, PageManager<K, V> pageManager){
		if(childIndex > 0){
			return getChild(childIndex-1, childLevel+1, pageManager);
		}
		return null;
	}
	
	// zwraca prawego brata podanego dziecka
	public Node<K,V> getChildsRightSibling(int childIndex, int childLevel, PageManager<K, V> pageManager){
		if(childIndex < pageIDs.size()-1){
			return getChild(childIndex+1, childLevel+1, pageManager);
		}
		return null;
	}

	public boolean remove(K key, InnerNode<K,V> parent, int childIndex, int pageID, PageManager<K, V> pageManager, int currentLevel) {
		int i = getKeyLocation(key);
		boolean changedKeysSize = getChild(i, currentLevel, pageManager).remove(key, this, i, pageID, pageManager, currentLevel - 1);
		if (changedKeysSize){
			// syn zmienil liczbe kluczy rodzica
			
			if(parent == null){ // jesli to root
				if(keys.size() == 0){
					// usunieto wszystkie klucze, wiec zostalo sie jedno dziecko
					// dziecko staje sie nowym root'em
					return true;
				} else {
					pageManager.writeNodeToPage(this, pageID, currentLevel);
					return false;
				}
			} else {
				// poniewaz usunieto klucz, kluczy moze byc za malo
				if(this.needsToBeMerged()){
					return handleMerger(parent, childIndex, pageID, pageManager, currentLevel);
				} else {
					pageManager.writeNodeToPage(this, pageID, currentLevel);
					return false;
				}
			}

		} else {
			setChild(i, pageID);
			pageManager.writeNodeToPage(this, pageID, currentLevel);
		}
		
		return false;
	}
	
	// zwraca klucz oddzielajacy dwoch braci
	public K getChildSplitKey(int childIndex, boolean leftSibling){
		if(leftSibling && childIndex > 0){
			return keys.get(childIndex-1);
		} else if(childIndex < pageIDs.size()){
			return keys.get(childIndex);
		}
		return null;
	}
	
	// zmienia klucz oddzielajacy dwoch braci
	public void setChildSplitKey(int childIndex, boolean leftSibling, K key){
		if(leftSibling && childIndex > 0){
			keys.set(childIndex-1, key);
		} else if(childIndex < pageIDs.size()){
			keys.set(childIndex, key);
		}
	}
	
	/*public int getChildsId(Node<K,V> child, int childLevel, PageManager<K, V> pageManager){
		for(int i=0; i < pageIDs.size(); i++){
			if (getChild(i, childLevel+1, pageManager) == child){
				return pageIDs.get(i);
			}
		}
		return -1;
	}*/
	
	// usuwa klucz oddzielajacy pierwotnie oddzielajacy dwoch polaczonych braci
	public void removeChildSplitKey(int childIndex, boolean leftSibling){
		if(leftSibling && childIndex > 0){
			keys.remove(childIndex-1);
			pageIDs.remove(childIndex-1);
		} else if(childIndex < pageIDs.size()){
			keys.remove(childIndex);
			pageIDs.remove(childIndex+1);
		}
	}

	protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey) {
		// polacz klucze i dzieci, dodatkowo wstaw klucz dzielacy braci z rodzica
		if (mergeToLeft){
			keys.add(0, splitKey);
			keys.addAll(0, mergingNode.keys);
			pageIDs.addAll(0, ((InnerNode<K,V>)mergingNode).pageIDs);
		} else {
			keys.add(splitKey);
			keys.addAll(mergingNode.keys);
			pageIDs.addAll(((InnerNode<K,V>)mergingNode).pageIDs);
		}	
	}

	protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey) {
		K pushedKey = null;
		int lenderIndex = (borrowFromLeft) ? (lender.keys.size() - 1) : 0;
		if(borrowFromLeft){
			// pozycza z lewej
			// w tym celu bierze takze klucz z rodzica, zas klucz z brata przechodzi na rodzica
			keys.add(0, splitKey);
			pageIDs.add(0,((InnerNode<K,V>)lender).pageIDs.get(lenderIndex + 1));
			pushedKey = lender.keys.get(lenderIndex);
			
			lender.keys.remove(lenderIndex);
			((InnerNode<K,V>)lender).pageIDs.remove(lenderIndex + 1);
		} else {
			// pozycza z prawej
			// w tym celu bierze takze klucz z rodzica, zas klucz z brata przechodzi na rodzica
			keys.add(splitKey);
			pageIDs.add(((InnerNode<K,V>)lender).pageIDs.get(lenderIndex));
			pushedKey = lender.keys.get(lenderIndex);
			
			lender.keys.remove(lenderIndex);
			((InnerNode<K,V>)lender).pageIDs.remove(lenderIndex);	
		}
		return pushedKey;
	}

	/*public void checkForErrors(boolean root) {
		if(needsToBeSplit() && !root){
			System.out.println("cos sie nie rozdzielilo");
		}
		if(children.size() != (keys.size()+1)){
			System.out.println("cos sie rozjechalo");
		}
		for(int i=0; i<children.size(); i++){
			children.get(i).checkForErrors(false);
			if(i<ORDER && i<keys.size()){
				if (keys.get(i) == null){
					System.out.println("cos sie znullowalo");
				}
				if(children.get(i) instanceof LeafNode){
					if (i >= 0 && ((LeafNode<K,V>)children.get(i)).keys.get(0).compareTo(((LeafNode<K,V>)children.get(i+1)).keys.get(0)) >= 0){
						System.out.println("cos jest nie po kolei");
					}
				}

			}
		}
	}*/ //DEBUG

}
