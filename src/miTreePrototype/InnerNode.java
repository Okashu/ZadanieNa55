package miTreePrototype;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Klasa wewnętrznych węzłów drzewa, nie przechowują wartości lecz wskazania na kolejne węzły drzewa
 * @author Kacper Kozerski, Adam Michalski, Rafał Muszyński
 * @param <K> Typ służący za klucze w drzewie. Musi implementować Comparable.
 * @param <V> Typ służący za wartości w drzewie.
 */

public class InnerNode<K extends Comparable<K>, V> extends Node<K, V> implements Serializable {

	
	/**
	 * Tworzy pusty InnerNode o zadanej maksymalnej liczbie kluczy.
	 * @param order Maksymalna liczba kluczy w węźle.
	 */
	public InnerNode(int order) {
		super(order);
		pageIDs = new ArrayList<Integer>(ORDER+1);
	}
	
	// innerNode mają mniejszą minimalną liczbę kluczy, zapobiega to
	// przepełnianiu drzewa przy pożyczaniu kluczy od węzłów sąsiednich
	public boolean canLendAKey(){
		return keys.size() > Math.ceil((double)(ORDER+1)/2-1);
	}
	
	/**
	 * Zwraca węzeł-dziecko o podanym indeksie.
	 * @param index Indeks dziecka.
	 * @param currentLevel Poziom węzła.
	 * @param pageManager Menadżer stron drzewa.
	 * @return Węzeł-dziecko o podanym indeksie.
	 */
	public Node<K, V> getChild(int index, int currentLevel, PageManager<K, V> pageManager){
		return pageManager.getNodeFromPage(pageIDs.get(index), currentLevel - 1);
	}
	
	/**
	 * Ustawia wskazanie na dziecko na odpowiednim indeksie
	 * na danej stronie.
	 * @param index Indeks dziecka, którego numer strony jest zmieniony.
	 * @param pageNumber Numer strony dziecka.
	 */
	public void setChild(int index, int pageNumber){
		pageIDs.set(index, pageNumber);
	}

	/* (non-Javadoc)
	 * @see miTreePrototype.Node#insert(java.lang.Comparable, java.lang.Object, int, miTreePrototype.PageManager, int)
	 */
	public Split<K, V> insert(K key, V value, int pageID, PageManager<K, V> pageManager, int currentLevel){
		
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
				if(currentLevel == pageManager.getTreeHeight())
					return this.splitAsRoot();
				else{
					return this.split();
				}
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
	
	public Split<K, V> splitAsRoot(){
		int mid = keys.size()/2;
		InnerNode<K, V> leftSibling = new InnerNode<K, V>(Math.max(3, ORDER/2));
		InnerNode<K,V> rightSibling = new InnerNode<K,V>(Math.max(3, ORDER/2));
		
		// srodkowy przechodzi calkowicie na wyzszy node
		K middleKey = this.keys.get(mid);
		
		rightSibling.keys = new ArrayList<K>(keys.subList(mid + 1, keys.size()));
		rightSibling.pageIDs = new ArrayList<Integer>(pageIDs.subList(mid + 1, pageIDs.size()));
		leftSibling.keys = new ArrayList<K>(keys.subList(0, mid));
		leftSibling.pageIDs =  new ArrayList<Integer>(pageIDs.subList(0, mid + 1));
		leftSibling.nodeValueList = this.nodeValueList;
		
		return new Split<K,V>(middleKey, leftSibling, rightSibling);
	}

	public void dump(String prefix, int pageID, PageManager<K, V> pageManager, int currentLevel) {
		System.out.println(prefix + "Inner Node on page " + pageID + " - order: " + ORDER);
		pageManager.setPageUsed(pageID);
		if(!nodeValueList.isEmpty()){
			System.out.print(prefix + "Extra values: ");
			writeNodeValues();
			System.out.println("");
		}
		for(int i=0; i<pageIDs.size(); i++){
			pageManager.setPageUsed(pageIDs.get(i));
			getChild(i, currentLevel, pageManager).dump(prefix + "    ", pageIDs.get(i), pageManager, currentLevel - 1);
			if(i<keys.size()){
				System.out.println(prefix + "+Key: " + keys.get(i));
			}
		}
	}
	
	/**
	 * Zwraca lewego brata podanego dziecka. Jeśli nie istnieje, zwraca null.
	 * @param childIndex Indeks dziecka, którego brata szukamy.
	 * @param childLevel Poziom dziecka.
	 * @param pageManager Menadżer stron drzewa.
	 * @return Lewy brat, jeśli istnieje.
	 */
	public Node<K,V> getChildsLeftSibling(int childIndex, int childLevel, PageManager<K, V> pageManager){
		if(childIndex > 0){
			return getChild(childIndex-1, childLevel+1, pageManager);
		}
		return null;
	}
	
	/**
	 * Zwraca prawego brata podanego dziecka. Jeśli nie istnieje, zwraca null.
	 * @param childIndex Indeks dziecka, którego brata szukamy.
	 * @param childLevel Poziom dziecka.
	 * @param pageManager Menadżer stron drzewa.
	 * @return Prawy brat, jeśli istnieje.
	 */
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
	
	/**
	 * Funkcja zwracająca klucz oddzielający dwóch sąsiednich dzieci.
	 * @param childIndex Indeks jednego z dzieci.
	 * @param leftSibling Czy druge dziecko jest lewym bratem.
	 * @return Klucz oddzielający wskazane dzieci.
	 */
	public K getChildSplitKey(int childIndex, boolean leftSibling){
		if(leftSibling && childIndex > 0){
			return keys.get(childIndex-1);
		} else if(childIndex < pageIDs.size()){
			return keys.get(childIndex);
		}
		return null;
	}
	
	/**
	 * Funkcja zmieniająca klucz oddzielający dwóch sąsiednich dzieci.
	 * @param childIndex Indeks jednego z dzieci.
	 * @param leftSibling Czy druge dziecko jest lewym bratem.
	 * @param key Nowy klucz oddzielający wskazane dzieci.
	 */
	public void setChildSplitKey(int childIndex, boolean leftSibling, K key){
		if(leftSibling && childIndex > 0){
			keys.set(childIndex-1, key);
		} else if(childIndex < pageIDs.size()){
			keys.set(childIndex, key);
		}
	}
	
	/**
	 * Funkcja usuwająca klucz oddzielający dwóch sąsiednich dzieci wraz
	 * z dzieckiem będacym bratem pierwszego dziecka.
	 * @param childIndex Indeks jednego z dzieci.
	 * @param leftSibling Czy druge dziecko jest lewym bratem.
	 */
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
			nodeValueList.addAll(0, mergingNode.nodeValueList);
		} else {
			keys.add(splitKey);
			keys.addAll(mergingNode.keys);
			pageIDs.addAll(((InnerNode<K,V>)mergingNode).pageIDs);
			nodeValueList.addAll(mergingNode.nodeValueList);
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

}
