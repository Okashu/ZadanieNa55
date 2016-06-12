package miTreePrototype;


/**
 * Klasa odpowiada obs�ug� dzielania si� w�z��w na dwa
 * @param <K> typ kluczy w w�zle
 * @param <V> typ warto�ci w w�zle
 */
public class Split<K extends Comparable<K>, V> {
	
	public final K key;
	public final Node <K, V> left;
	public final Node <K, V> right;

	/**tworzy nowy obiekt split
	 * @param key klucz mi�dzy w�z�ami
	 * @param left lewy w�ze�
	 * @param right prawy w�ze�
	 */
	public Split(K key, Node <K, V> left, Node <K, V> right) {
		this.key = key;
		this.left = left;
		this.right = right;
	}

}
