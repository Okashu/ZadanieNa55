package miTreePrototype;

import java.util.*;

/**
 * Klasa abstrakcyjna węzłów drzewa.
 * @param <K> Typ kluczy (musi implementować Comparable<K>).
 * @param <V> Typ wartości.
 */
 
public abstract class Node<K extends Comparable<K>, V> implements java.io.Serializable {

	protected int ORDER;
	
	/**
	 * Klucze węzła.
	 */
	public List<K> keys;
	
	/**
	 * Numery stron przechowywane w węźle.
	 * W przypadku liścia, są to numery z wartościami.
	 * W przypadku węzła wewnętrznego, są to numery stron,
	 * na których znajdują się niższe węzły drzewa.
	 */
	public List<Integer> pageIDs;
	
	/**
	 * Lista dodatkowych wartości przechowywanych w węźle.
	 */
	public List<V> nodeValueList;

	/**
	 * Tworzy nowy węzeł drzewa o podanym rzędzie (ilości kluczy).
	 * @param order Rząd węzła.
	 */
	Node(int order) {
		ORDER = order;
		keys = new ArrayList<K>(ORDER);
		pageIDs = new ArrayList<Integer>(ORDER + 1);
		nodeValueList = new ArrayList<V>(1);
	}
	
	/**
	 * Zmienia rząd węzła.
	 * @param order Nowy rząd węzła.
	 */
	public void setOrder(int order){
		this.ORDER = order;
	}

	/**
	 * Zwraca indeks odpowiadający danemu kluczowy.
	 * Odpowiada on numerowi strony wskazującej na węzeł
	 * mogący zawierać dany klucz lub wartość odpowiadającą danemu
	 * kluczowi.
	 * W szczególności, gdy szukany klucz jest mniejszy od wszystkich
	 * kluczy (lub równy pierwszemu kluczowi), funkcja zwraca 0.
	 * W przeciwnym wypadku, funkcja zwraca indeks najmniejszego klucza
	 * większego od szukanego klucza.
	 * @param key Szukany klucz.
	 * @return Indeks odpowiadający kluczowi.
	 */
	public int getKeyLocation(K key) {
		int i = 0;
		while (i < keys.size() && keys.get(i).compareTo(key) <= 0) {
			i++;
		}
		return i;
	}

	/**
	 * Funkcja sprawdzająca, czy węzeł musi zostać podzielony
	 * na dwa, tj. czy ilość kluczy jest większa od rzędu
	 * drzewa.
	 * @return true, jeśli węzeł musi zostać podzielony.
	 */
	public boolean needsToBeSplit() {
		return keys.size() > ORDER;
	}

	/**
	 * Funkcja sprwadzająca, czy węzeł musi zostać połączony
	 * z sąsiadującym węzłem, tj. iłość kluczy w węźle jest
	 * mniejsza od połowy rzędu drzewa.
	 * @return true, jeśli musi zostać połączony z sąsiednim.
	 */
	public boolean needsToBeMerged() {
		return keys.size() < (Math.ceil((double) (ORDER + 1) / 2));
	}

	/**
	 * Funkcja sprawdzająca, czy węzeł jest w stanie pożyczyć klucz
	 * sąsiadującemu węzłowi, tj. ilość kluczy jest większa od
	 * połowy rzędu drzewa.
	 * @return true, jeśli węzeł może pożyczyć klucz.
	 */
	public boolean canLendAKey() {
		return keys.size() > Math.ceil((double) (ORDER + 1) / 2);
	}

	/**
	 * Funkcja zajmująca się łączeniem węzła z sąsiednim, w przypadku
	 * kiedy podczas usuwania klucza w węźle znajdzie się zbyt mała liczba
	 * kluczy (jeśli needsToBeMerged() zwraca true).
	 * 
	 * Jeśli węzeł nie posiada rodzica (tj. jest rootem), to funkcja nie
	 * wykonuje niczego.
	 * 
	 * Problem zbyt małej liczby kluczy jest rozwiązywany na dwa sposoby:
	 * <ol>
	 * <li>Jeśli jeden z sąsiednich węzłów jest w stanie pożyczyć klucz,
	 * to od tego sąsiada pożyczany jest pojedynczy klucz.</li>
	 * <li>Jeśli żaden z sąsiednich węzłów nie może pożyczyć żadnego klucza,
	 * to węzeł zostaje połączony razem ze swoim sąsiadem w jeden węzeł</li>
	 * </ol>
	 * 
	 * Łączenie dwóch węzłów może spowodować zmianę ilości kluczy rodzica.
	 * Wtedy funkcja zwraca wartość true, w przeciwnym razie zwraca wartość false.
	 * @param parent Rodzic węzła. null, jeśli węzeł jest rootem.
	 * @param childIndex Indeks w liście pageIDs rodzica, pod którym znajduje sie wskazanie na ten węzeł.
	 * @param pageID Numer strony, na której zapisywane są zmienione dane.
	 * @param pageManager Menadżer stron drzewa.
	 * @param currentLevel Poziom węzła.
	 * @return true, jeśli zmieniła się liczba kluczy rodzica.
	 */
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

	/**
	 * Dodaje dodatkową wartość do węzła posiadającego podany klucz.
	 * Jeśli węzeł nie posiada tego klucza, klucz szukany jest rekurencyjnie
	 * w niższych węzłach drzewa.
	 * @param key Klucz znajdujący się w węźle, do którego dodawana jest wartość.
	 * @param value Dodawana dodatkowa wartość
	 * @param pageID Numer strony, na której zapisywane są zmienione dane.
	 * @param pageManager Menadżer stron drzewa.
	 * @param currentLevel Poziom węzła.
	 * @return true, jeśli znaleziono węzeł o danym kluczu i dodano do niego wartość.
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
	
	/**
	 * Usuwa podaną dodatkową wartość z węzła posiadającego podany klucz.
	 * Jeśli węzeł nie posiada tego klucza, klucz szukany jest rekurencyjnie
	 * w niższych węzłach drzewa.
	 * @param key Klucz znajdujący się w węźle, z którego usuwana jest wartość.
	 * @param value Usuwana dodatkowa wartość
	 * @param pageID Numer strony, na której zapisywane są zmienione dane.
	 * @param pageManager Menadżer stron drzewa.
	 * @param currentLevel Poziom węzła.
	 * @return true jeśli usunięto wartość z jakiegoś węzła
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
	 * Funkcja wypisująca dodatkowe wartości przechowywane w węźle.
	 */
	public void writeNodeValues() {
		for (int i = 0; i < nodeValueList.size(); i++)
			System.out.print(nodeValueList.get(i).toString() + " ");
	}

	/**
	 * Rekurencyjnie dodaje nową wartość do drzewa.
	 * @param key Klucz wartości.
	 * @param value Wartość
	 * @param pageID Numer strony, na której zapisywane są zmienione dane.
	 * @param pageManager Menadżer stron drzewa.
	 * @param currentLevel Poziom węzła.
	 * @return Dane o podziale węzła, jeśli taki nastąpił.
	 */
	public abstract Split<K, V> insert(K key, V value, int pageID, PageManager<K, V> pageManager, int currentLevel);

	/**
	 * Dzieli węzeł na dwa. Zwraca informacje o nowo powstałym
	 * węźle lewym, prawym, oraz o kluczy dzielącym te dwa węzły
	 * (najmniejszy klucz prawego węzła).
	 * @return Dane o podziale węzła.
	 */
	public abstract Split<K, V> split();

	/**
	 * Rekurencyjnie usuwa wskazany klucz z drzewa.
	 * @param key Klucz do usunięcia.
	 * @param parent Rodzic węzła. null, jeśli węzeł jest rootem.
	 * @param childIndex Indeks w liście pageIDs rodzica, pod którym znajduje sie wskazanie na ten węzeł.
	 * @param pageID Numer strony, na której zapisywane są zmienione dane.
	 * @param pageManager Menadżer stron drzewa.
	 * @param currentLevel Poziom węzła.
	 * @return true, jeśli zmieniła się liczba kluczy rodzica.
	 */
	public abstract boolean remove(K key, InnerNode<K, V> parent, int childIndex, int pageID,
			PageManager<K, V> pageManager, int currentLevel);	// przekazuje
															 	// rodzica, aby
																// mieć dostęp
																// do braci

	/**
	 * Łączy węzeł z podanym sąsiadem.
	 * @param mergingNode Węzeł, który łączony jest z danym węzłem.
	 * @param mergeToLeft Czy dołączany węzeł jest lewym sąsiadem.
	 * @param splitKey Klucz dzielący węzły.
	 */
	abstract protected void mergeWith(Node<K, V> mergingNode, boolean mergeToLeft, K splitKey);

	/**
	 * Pożycza klucz od wskazanego sąsiada.
	 * @param lender Wskazany sąsiad.
	 * @param borrowFromLeft Czy wskazany węzeł jest lewym sąsiadem.
	 * @param splitKey Klucz dzielący węzły.
	 * @return Klucz przepchany do rodzica w wyniku zmiany kluczy w węzłach.
	 */
	abstract protected K borrowKeys(Node<K, V> lender, boolean borrowFromLeft, K splitKey);

	/**
	 * Wypisuje wartości z drzewa.
	 * @param prefix Prefiks znajdujący się przed wypisywanymi wartościami.
	 * @param pageID Numer strony, na której zapisywane są zmienione dane.
	 * @param pageManager Menadżer stron drzewa.
	 * @param currentLevel Poziom węzła.
	 */
	abstract public void dump(String prefix, int pageID, PageManager<K, V> pageManager, int currentLevel);

}
