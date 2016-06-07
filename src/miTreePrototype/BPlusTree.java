package miTreePrototype;


public class BPlusTree<K extends Comparable<K>, V> {
	
	private Node<K,V> root;
	
	private final int ORDER;
	private int height;
	public miTree.PageManager<K, V> pageManager;

	public BPlusTree(int order, int pageSize) {
		ORDER = order;
		pageManager = new miTree.PageManager<K, V> (pageSize);
		root = new LeafNode<K,V>(ORDER);
		height = 1;
	}
	
	public int getHeight(){ return height; }
	
	private Node<K,V> searchForNode(K key){
		Node<K,V> node = root;
		while (node.isLeaf == false){
			node = node.getChild(node.getKeyLocation(key), pageManager);
		}
		return node;
	}
	
	private Node<K,V> find(K key){
		Node<K,V> leaf = searchForNode(key);
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
		Integer newPageNumber = pageManager.allocateNewPage();
		Split<K,V> split = root.insert(key, value, newPageNumber, pageManager, new Integer(1));
		if (split != null){
			root = new InnerNode<K,V>(ORDER);
			root.keys.add(split.key);
			((InnerNode<K,V>)root).children.add(split.left);
			((InnerNode<K,V>)root).children.add(split.right);
			height++;
		}
	}
	
	public V retrieve(K key){
		Node<K,V> leaf = searchForNode(key);
		if(leaf != null){
			int loc = leaf.getExactKeyLocation(key);
			if (loc >= 0){
				return leaf.getValue(loc, pageManager);
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
		int newPage = pageManager.allocateNewPage();
		boolean rootEmptyKeys = root.remove(key, null, newPage);
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
