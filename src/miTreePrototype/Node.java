package miTreePrototype;

import java.util.*;

public abstract class Node<K extends Comparable<K>, V> implements java.io.Serializable {

	protected int ORDER;
	public List<K> keys;
	public List<Integer> pageIDs;
	public List<V> nodeValueList;

	/**Ustawia rz¹d wêz³a
	 * @param order rz¹d wêz³a
	 */
	public void setOrder(int order){
		this.ORDER = order;
	}

	/**Tworzy nowy wêze³ drzewa
	 * @param order rz¹d wêz³a
	 */
	Node(int order) {
		ORDER = order;
		keys = new ArrayList<K>(ORDER);
		pageIDs = new ArrayList<Integer>(ORDER + 1);
		nodeValueList = new ArrayList<V>(1);
	}

	/**Zwraca indeks danego klucza
	 * @param key klucz
	 * @return indekst klucza
	 */
	public int getKeyLocation(K key) {
		int i = 0;
		while (i < keys.size() && keys.get(i).compareTo(key) <= 0) {
			i++;
		}
		return i;
	}

	/**
	 * @return true jeœli wymagany jest split
	 */
	public boolean needsToBeSplit() {
		return keys.size() > ORDER;
	}

	/**
	 * @return true jeœli nale¿y po³¹czyæ dwa wêz³y 
	 */
	public boolean needsToBeMerged() {
		return keys.size() < (Math.ceil((double) (ORDER + 1) / 2));
	}

	public boolean canLendAKey() {
		return keys.size() > Math.ceil((double) (ORDER + 1) / 2);
	}

	// zwraca true, jesli zmieniono ilosc kluczy rodzica
	public boolean handleMerger(InnerNode<K, V> parent, int childIndex, int pageID, PageManager<K, V> pageManager,
			int currentLevel) {
		if (parent == null) {
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			return false; // node jest rootem, wiec nie ma braci
		}
		Node<K, V> leftSibling = parent.getChildsLeftSibling(childIndex, currentLevel, pageManager);
		if (leftSibling != null && leftSibling.canLendAKey()) {
			// pozycz od lewego brata
			K splitKey = parent.getChildSplitKey(childIndex, true);
			K pushedKey = borrowKeys(leftSibling, true, splitKey);

			pageManager.writeNodeToPage(this, pageID, currentLevel);
			int temp = pageManager.allocateNewPage();
			pageManager.writeNodeToPage(leftSibling, temp, currentLevel);
			// zmiana kluczy w rodzicu
			parent.setChildSplitKey(childIndex, true, pushedKey);
			parent.setChild(childIndex, pageID);
			parent.setChild(childIndex - 1, temp);

			return false;
		}
		Node<K, V> rightSibling = parent.getChildsRightSibling(childIndex, currentLevel, pageManager);
		if (rightSibling != null && rightSibling.canLendAKey()) {
			// pozycz od prawego brata
			K splitKey = parent.getChildSplitKey(childIndex, false);
			K pushedKey = borrowKeys(rightSibling, false, splitKey);

			pageManager.writeNodeToPage(this, pageID, currentLevel);
			int temp = pageManager.allocateNewPage();
			pageManager.writeNodeToPage(rightSibling, temp, currentLevel);
			// zmiana kluczy w rodzicu
			parent.setChildSplitKey(childIndex, false, pushedKey);
			parent.setChild(childIndex, pageID);
			parent.setChild(childIndex + 1, temp);
			return false;
		}
		// nie mozna pozyczyc klucza
		// trzeba polaczyc braci
		if (leftSibling != null) {
			// polacz z lewym
			K splitKey = parent.getChildSplitKey(childIndex, true);
			this.mergeWith(leftSibling, true, splitKey);

			pageManager.writeNodeToPage(this, pageID, currentLevel);
			// usuniecie kluczy w rodzicu
			parent.removeChildSplitKey(childIndex, true);
			parent.setChild(childIndex - 1, pageID);

			return true;
		} else if (rightSibling != null) {
			// polacz z prawym
			K splitKey = parent.getChildSplitKey(childIndex, false);
			this.mergeWith(rightSibling, false, splitKey);

			pageManager.writeNodeToPage(this, pageID, currentLevel);
			// usuniecie kluczy w rodzicu
			parent.removeChildSplitKey(childIndex, false);
			parent.setChild(childIndex, pageID);

			return true;
		}
		return false;
	}


	/**dodaje dan¹ wartoœæ do tego wêz³a, wartoœæ nie jest zwi¹zana z drzewem
	 * @param key klucz szukanego wêz³a
	 * @param value dodawana wartoœæ
	 * @param pageID identyfikator nowej strony
	 * @param pageManager Menadzer stron
	 * @param currentLevel poziom aktualnego wêz³a
	 * @return true jeœli dodano wartoœæ do jakiegoœ wêz³a
	 */
	public boolean insertNodeValue(K key, V value, int pageID, PageManager<K, V> pageManager, int currentLevel) {
		int i = getKeyLocation(key);
		if (i > 0 && keys.get(i-1).compareTo(key) == 0){ // czy drzewo zawiera dany klucz
			nodeValueList.add(value);
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			return true;
		} else {
			if (this instanceof InnerNode){
				boolean gotInserted = pageManager.getNodeFromPage(pageIDs.get(i), currentLevel - 1).insertNodeValue(key, value,
						pageID, pageManager, currentLevel - 1);
				if(gotInserted){
					pageIDs.set(i, pageID);
					pageManager.writeNodeToPage(this, pageID, currentLevel);
				}
				return gotInserted;
			}
			return false;
		}
	}
	

	/**usuwa dan¹ wartoœæ z tego wêz³a, wartoœæ nie jest zwi¹zana z drzewem
	 * @param key klucz szukanego wêz³a
	 * @param value dodawana wartoœæ
	 * @param pageID identyfikator nowej strony
	 * @param pageManager Menadzer stron
	 * @param currentLevel poziom aktualnego wêz³a
	 * @return true jeœli usuniêto wartoœæ z jakiegoœ wêz³a
	 */
	public boolean deleteNodeValue(K key, V value, int pageID, PageManager<K, V> pageManager, int currentLevel) {
		int i = getKeyLocation(key);
		if (i > 0 && keys.get(i-1).compareTo(key) == 0){ // czy drzewo zawiera dany klucz
			nodeValueList.remove(value);
			pageManager.writeNodeToPage(this, pageID, currentLevel);
			return true;
		} else {
			if (this instanceof InnerNode){
				boolean gotDeleted = pageManager.getNodeFromPage(pageIDs.get(i), currentLevel - 1).deleteNodeValue(key, value,
						pageID, pageManager, currentLevel - 1);
				if(gotDeleted){
					pageIDs.set(i, pageID);
					pageManager.writeNodeToPage(this, pageID, currentLevel);
				}
				return gotDeleted;
			}
			return false;
		}
	}


	/**
	 * Wypisuje wartoœci trzymane w node
	 */
	public void writeNodeValues() {
		for (int i = 0; i < nodeValueList.size(); i++)
			System.out.print(nodeValueList.get(i).toString() + " ");
	}

	public abstract Split<K, V> insert(K key, V value, int pageID, PageManager<K, V> pageManager, int currentLevel);

	public abstract Split<K, V> split();

	public abstract boolean remove(K key, InnerNode<K, V> parent, int childIndex, int pageID,
			PageManager<K, V> pageManager, int currentLevel);	// przekazuje
															 	// rodzica, aby
																// miec dostep
																// do braci

	abstract protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey);

	abstract protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey);

	abstract public void dump(String prefix, int myLevel, PageManager<K, V> pageManager, int myPageID);

	// abstract public void checkForErrors(boolean root); //DEBUG

}
