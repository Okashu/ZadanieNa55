package miTreePrototype;

import BPlusTree.InnerNode;

public class BPlusTree<K extends Comparable<K>, V> {
	
	private int root;
	
	private final int ORDER;
	private int height;
	public miTree.PageManager<K, V> pageManager;

	public BPlusTree(int order, int pageSize) {
		ORDER = order;
		pageManager = new miTree.PageManager<K, V> (pageSize);
		pageManager.allocateNewPage();
		LeafNode<K, V> rootNode = new LeafNode<K, V>(ORDER);
		pageManager.writeNodeToPage(rootNode, 0, 1);
		root = 0;
		height = 1;
	}

	public void setHeight(int height){
		this.height = height;
		pageManager.setTreeHeight(height);
	}
	public int getHeight(){ return height; }
	
	private LeafNode<K,V> searchForNode(K key){
		Node<K,V> node = pageManager.getNodeFromPage(root, height);
		int currentLevel = height;
		while (node instanceof InnerNode){
			InnerNode innerNode = (InnerNode)node;
			node = innerNode.getChild(node.getKeyLocation(key), --currentLevel, pageManager);
		}
		return (LeafNode<K, V>)node;
	}
	
	private Node<K,V> find(K key){
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
		int newPageID = pageManager.allocateNewPage();
		Split<K,V> split = pageManager.getNodeFromPage(root,height).insert(key, value, newPageID, pageManager, height);
		if (split != null){
			setHeight(height + 1);
			int splitPageID = pageManager.allocateNewPage();
			InnerNode<K, V> rootNode = new InnerNode<K,V>(ORDER);
			rootNode.keys.add(split.key);
			rootNode.pageIDs.add(newPageID);
			rootNode.pageIDs.add(splitPageID);
			pageManager.writeNodeToPage(split.left, newPageID, height - 1);
			pageManager.writeNodeToPage(split.right, splitPageID, height - 1);
			pageManager.writeNodeToPage(rootNode, newPageID, height);
		}
	}
	
	public V retrieve(K key){
		LeafNode<K,V> leaf = searchForNode(key);
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
		pageManager.getNodeFromPage(root, height).dump("", height, pageManager);
	}
	
	/*private void checkForErrors(){
		root.checkForErrors(true);
	}*/ //DEBUG

}
