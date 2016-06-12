package miTreePrototype;


/**
 * Klasa odpowiada obs³ugê dzielania siê wêz³ów na dwa
 * @param <K> typ kluczy w wêzle
 * @param <V> typ wartoœci w wêzle
 */
public class Split<K extends Comparable<K>, V> {
	
	public final K key;
	public final Node <K, V> left;
	public final Node <K, V> right;

	/**tworzy nowy obiekt split
	 * @param key klucz miêdzy wêz³ami
	 * @param left lewy wêze³
	 * @param right prawy wêze³
	 */
	public Split(K key, Node <K, V> left, Node <K, V> right) {
		this.key = key;
		this.left = left;
		this.right = right;
	}

}
