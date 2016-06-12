package miTreePrototype;


/**
 * Klasa przechowuj�ca informacj� o podziale dw�ch w�z��w.
 * U�ywana w trakcie dodawania element�w do drzewa.
 * 
 * @param <K> Typ s�u��cy za klucze w drzewie. Musi implementowa� Comparable.
 * @param <V> Typ s�u��cy za warto�ci w drzewie.
 */
public class Split<K extends Comparable<K>, V> {
	
	public final K key;
	public final Node <K, V> left;
	public final Node <K, V> right;

	/**
	 * Tworzy nowy obiekt Split.
	 * @param key Klucz oddzielaj�cy nowo powasta�e w�z�y.
	 * @param left Lewy w�ze�.
	 * @param right Prawy w�ze�.
	 */
	public Split(K key, Node <K, V> left, Node <K, V> right) {
		this.key = key;
		this.left = left;
		this.right = right;
	}

}
