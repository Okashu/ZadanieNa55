package miTreePrototype;


/**
 * Klasa przechowująca informacje o podziale na dwa węzły.
 * Używana w trakcie dodawania elementów do drzewa.
 * @author Kacper Kozerski, Adam Michalski, Rafał Muszyński
 * @param <K> Typ służący za klucze w drzewie. Musi implementować Comparable.
 * @param <V> Typ służący za wartości w drzewie.
 */
public class Split<K extends Comparable<K>, V> {
	
	public final K key;
	public final Node <K, V> left;
	public final Node <K, V> right;

	/**
	 * Tworzy nowy obiekt Split.
	 * @param key Klucz oddzielający nowo powastałe węzły.
	 * @param left Lewy węzeł.
	 * @param right Prawy węzeł.
	 */
	public Split(K key, Node <K, V> left, Node <K, V> right) {
		this.key = key;
		this.left = left;
		this.right = right;
	}

}
