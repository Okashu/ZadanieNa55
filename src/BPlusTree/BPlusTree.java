package BPlusTree;


public class BPlusTree<K extends Comparable<K>, V> {
	
	private Node<K,V> root;
	
	private final int ORDER;
	private int height;

	public BPlusTree(int order) {
		ORDER = order;
		root = new LeafNode<K,V>(ORDER);
		height = 1;
	}
	
	public int getHeight(){ return height; }
	
	private LeafNode<K,V> searchForNode(K key){
		Node<K,V> node = root;
		while (node instanceof InnerNode){
			InnerNode<K,V> inner = (InnerNode<K,V>)node;
			node = inner.getChild(inner.getKeyLocation(key));
		}
		LeafNode<K,V> leaf = (LeafNode<K,V>) node;
		return leaf;
	}
	
	private LeafNode<K,V> find(K key){
		LeafNode<K,V> leaf = searchForNode(key);
		if(leaf != null){
			int loc = leaf.getExactKeyLocation(key);
			if (loc >= 0){
				return leaf;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public void insert(K key, V value){
		Split<K,V> split = root.insert(key, value);
		if (split != null){
			root = new InnerNode<K,V>(ORDER);
			root.keys.add(split.key);
			((InnerNode<K,V>)root).children.add(split.left);
			((InnerNode<K,V>)root).children.add(split.right);
			height++;
		}
	}
	
	public V retrieve(K key){
		LeafNode<K,V> leaf = searchForNode(key);
		if(leaf != null){
			int loc = leaf.getExactKeyLocation(key);
			if (loc >= 0){
				return leaf.getValue(loc);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public boolean remove(K key){
		if(find(key) == null){
			return false;
		}
		boolean rootEmptyKeys = root.remove(key, null);
		if (rootEmptyKeys){
			//root ma 0 kluczy i jednego potomka
			//root przechodzi na swojego potomka, wysokosc drzewa zmniejszona o 1
			root = ((InnerNode<K,V>)root).getChild(0); //tu bedzie pageNumber
			height--;
		}
		//checkForErrors(); //DEBUG
		return true;
	}
	
	public void dump(){
		root.dump("");
	}
	
	/*private void checkForErrors(){
		root.checkForErrors(true);
	}*/ //DEBUG

}
