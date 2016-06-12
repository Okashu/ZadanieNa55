package miTreePrototype;


/**
 * Klasa przechowuj¹ca informacjê o podziale dwóch wêz³ów.
 * U¿ywana w trakcie dodawania elementów do drzewa.
 * 
 * @param <K> Typ s³u¿¹cy za klucze w drzewie. Musi implementowaæ Comparable.
 * @param <V> Typ s³u¿¹cy za wartoœci w drzewie.
 */
public class Split<K extends Comparable<K>, V> {
	
	public final K key;
	public final Node <K, V> left;
	public final Node <K, V> right;

	/**
	 * Tworzy nowy obiekt Split.
	 * @param key Klucz oddzielaj¹cy nowo powasta³e wêz³y.
	 * @param left Lewy wêze³.
	 * @param right Prawy wêze³.
	 */
	public Split(K key, Node <K, V> left, Node <K, V> right) {
		this.key = key;
		this.left = left;
		this.right = right;
	}

}
