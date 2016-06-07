package miTreePrototype;

import java.util.*;

public class InnerNode<K extends Comparable<K>, V> extends Node<K, V> {

	
	public InnerNode(int order) {
		super(order);
	}
	
	//powoduje zmniejszenie minimalnego rozmiaru inner-node'ów, zapobiega to
	//przepełnianiu drzewa przy pożyczaniu kluczy od braci
	public boolean canLendAKey(){
		return keys.size() > Math.ceil((double)(ORDER+1)/2-1);
	}
	
	public Node<K, V> getChild(int index, int myLevel, miTree.PageManager pageManager){
		return pageManager.getNodeFromPage(index, myLevel);
	}
	
	public void setChild(int index, int pageNumber){ //pageNumber
		pageIDs.set(index, pageNumber);
	}

	public Split<K, V> insert(K key, V value, int level) {
		int i = getKeyLocation(key);
		Split<K,V> split = getChild(i).insert(key, value, level - 1);
		
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

	public Split<K, V> split() {
		int mid = keys.size()/2;
		InnerNode<K,V> rightSibling = new InnerNode<K,V>(ORDER);
		
		// srodkowy przechodzi calkowicie na wyzszy node
		K middleKey = this.keys.get(mid);
		
		rightSibling.keys = new ArrayList<K>(keys.subList(mid + 1, keys.size()));
		rightSibling.children = new ArrayList<Node<K,V>>(children.subList(mid + 1, children.size()));
		this.keys = new ArrayList<K>(keys.subList(0, mid));
		this.children =  new ArrayList<Node<K,V>>(children.subList(0, mid + 1));
		
		return new Split<K,V>(middleKey, this, rightSibling);
	}

	public void dump(String prefix) {
		System.out.println(prefix + "Inner Node");
		for(int i=0; i<children.size(); i++){
			getChild(i).dump(prefix + " ");
			if(i<keys.size()){
				System.out.println(prefix + "+Key: " + keys.get(i));
			}
		}
	}
	
	// zwraca lewego brata podanego dziecka
	public Node<K,V> getChildsLeftSibling(Node<K,V> child){
		for(int i=1; i < children.size(); i++){
			if (getChild(i) == child){
				return getChild(i-1);
			}
		}
		return null;
	}
	
	// zwraca prawego brata podanego dziecka
	public Node<K,V> getChildsRightSibling(Node<K,V> child){
		for(int i=0; i < children.size()-1; i++){
			if (getChild(i) == child){
				return getChild(i+1);
			}
		}
		return null;
	}

	public boolean remove(K key, InnerNode<K,V> parent) {
		int i = getKeyLocation(key);
		boolean changedKeysSize = getChild(i).remove(key, this);

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
	
	// zwraca klucz oddzielajacy dwoch braci
	public K getChildSplitKey(Node<K,V> child, boolean leftSibling){
		if(!leftSibling && getChild(0) == child){
			return keys.get(0);
		}
		for(int i=1; i < children.size(); i++){
			if (getChild(i) == child){
				if(leftSibling){
					return keys.get(i-1);
				} else {
					return keys.get(i);
				}
			}
		}
		return null;
	}
	
	// zmienia klucz oddzielajacy dwoch braci
	public K setChildSplitKey(Node<K,V> child, boolean leftSibling, K key){
		if(!leftSibling && getChild(0) == child){
			keys.set(0, key);
		}
		for(int i=1; i < children.size(); i++){
			if (children.get(i) == child){
				if(leftSibling){
					return keys.set(i-1, key);
				} else {
					return keys.set(i, key);
				}
			}
		}
		return null;
	}
	
	// usuwa klucz oddzielajacy pierwotnie oddzielajacy dwoch polaczonych braci
	public K removeChildSplitKey(Node<K,V> child, boolean leftSibling){
		if(!leftSibling && children.get(0) == child){
			keys.remove(0);
			children.remove(1);
		}
		for(int i=1; i < children.size(); i++){
			if (getChild(i) == child){
				if(leftSibling){
					keys.remove(i-1);
					children.remove(i-1);
				} else {
					keys.remove(i);
					children.remove(i+1);
				}
			}
		}
		return null;
	}

	protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey) {
		// polacz klucze i dzieci, dodatkowo wstaw klucz dzielacy braci z rodzica
		if (mergeToLeft){
			keys.add(0, splitKey);
			keys.addAll(0, mergingNode.keys);
			children.addAll(0, ((InnerNode<K,V>)mergingNode).children);
		} else {
			keys.add(splitKey);
			keys.addAll(mergingNode.keys);
			children.addAll(((InnerNode<K,V>)mergingNode).children);
		}	
	}

	protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey) {
		K pushedKey = null;
		int lenderIndex = (borrowFromLeft) ? (lender.keys.size() - 1) : 0;
		if(borrowFromLeft){
			// pozycza z lewej
			// w tym celu bierze takze klucz z rodzica, zas klucz z brata przechodzi na rodzica
			keys.add(0, splitKey);
			children.add(0,((InnerNode<K,V>)lender).getChild(lenderIndex + 1));
			pushedKey = lender.keys.get(lenderIndex);
			
			lender.keys.remove(lenderIndex);
			((InnerNode<K,V>)lender).children.remove(lenderIndex + 1);
		} else {
			// pozycza z prawej
			// w tym celu bierze takze klucz z rodzica, zas klucz z brata przechodzi na rodzica
			keys.add(splitKey);
			children.add(((InnerNode<K,V>)lender).getChild(lenderIndex));
			pushedKey = lender.keys.get(lenderIndex);
			
			lender.keys.remove(lenderIndex);
			((InnerNode<K,V>)lender).children.remove(lenderIndex);	
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
