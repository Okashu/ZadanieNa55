package miTreePrototype;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Klasa Node'�w, kt�re znajduj� si� na samym dole drzewa.
 * Strony odpowiadaj�ce ich kluczom przechowuj� nie Node'y, ale warto�ci.
 *
 * @param <K> Typ s�u��cy za klucz w Nodzie (musi implementowa� Comparable).
 * @param <V> Typ s�u��cy za warto�� w Nodzie.
 */
public class LeafNode<K extends Comparable<K>, V> extends Node<K, V> implements Serializable {

	/**
	 * Tworzy pusty LeafNode o zadanej maksymalnej liczbie kluczy.
	 * @param order Maksymalna liczba kluczy w Nodzie.
	 */
	public LeafNode(int order) {
		super(order);
	}
	
	/**
	 * Zwraca warto�� przechowywan� we wskazanym miejscu w Nodzie.
	 * @param index Miejsce w nodzie, kt�re ma by� odczytane.
	 * @param pageManager Manager stron u�ywany w programie.
	 * @return
	 */
	public V getValue(int index, PageManager<K, V> pageManager){
		MemoryPage<K, V> memoryPage = pageManager.getPage(pageIDs.get(index));
		if(! (memoryPage instanceof ValuePage)){
			System.out.println("ERROR: Tried to get value from a node page!");
			System.exit(-1);
		}
		ValuePage<K, V> valuePage = (ValuePage<K, V>)memoryPage;
		return valuePage.readValue();
	}
	
	/**
	 * Szuka, gdzie w Nodzie znajduje si� dany klucz.
	 * @param key Klucz, kt�ry ma by� znaleziony.
	 * @return Miejsce, gdzie znajduje si� klucz. -1, je�li nie ma takiego klucza.
	 */
	public int getExactKeyLocation(K key){
		int i = getKeyLocation(key);
		if (i>0 && i<=keys.size() && keys.get(i-1).equals(key)){
			return i-1;
		} else {
			return -1;
		}
	}
	/**
	 * Wstawia zadan� par� K, V w tego Node'a
	 * 
	 * @param K Klucz odpowiadaj�cy danej warto�ci.
	 * @param V Warto�� do wstawienia do Node'a.
	 * @param pageID Numer strony pami�ci, na kt�r� ma by� zapisany Node po zmianach.
	 * @param pageManagaer Manager stron u�ywany w programie.
	 * @param currentLevel Obecny poziom wywo�ania rekurencyjnego (w tym momencie powinien zawsze by� r�wny 1, bo poziom li�cia jest zawsze r�wny 1).
	 * 
	 * @return obiekt typu Split, zawieraj�cy informacje o Node'ach, na kt�re nast�pi� podzia�
	 */
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

	/**
	 * Dzieli Node'a na dwa nowe Node'y. U�ywane, kiedy brakuje miejsca w Nodzie.
	 * @return Obiekt typu Split zawieraj�cy informacje o Node'ach, na kt�re si� podzieli� ten Node.
	 */
	public Split<K, V> split() {
		int mid = (int)Math.ceil((double)keys.size()/2);
		LeafNode<K,V> rightSibling = new LeafNode<K,V>(ORDER);
		
		rightSibling.keys = new ArrayList<K>(keys.subList(mid, keys.size()));
		rightSibling.pageIDs = new ArrayList<Integer>(pageIDs.subList(mid, keys.size()));
		this.keys = new ArrayList<K>(keys.subList(0, mid));
		this.pageIDs = new ArrayList<Integer>(pageIDs.subList(0, mid));
		
		return new Split<K,V>(rightSibling.keys.get(0), this, rightSibling);
	}
	/**
	 * Dzia�a podobnie jak split(), ale jest wywo�ywane, kiedy wysoko�� drzewa jest r�wna 1. Li�� dzieli si� na dwa, ale o mniejszym rozmiarze.
	 * Wynika to ze struktury miTree.
	 * 
	 * @return Obiekt typu Split zawieraj�cy informacje o Node'ach (2 razy mniejszych), na kt�re si� podzieli� ten Node.
	 */
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

	/**
	 * Wypisuje zawarto�� tego Node'a
	 * 
	 * @param prefix Tekst dodawany przed ka�d� lini� tekstu.
	 * @param myLevel Poziom tego Node'a (tutaj powinien by� zawsze 1, bo jest li�ciem).
	 * @param myPageID Numer strony, na kt�rej znajduje si� ten Node.
	 */
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
