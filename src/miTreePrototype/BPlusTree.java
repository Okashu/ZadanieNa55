package miTreePrototype;


public class BPlusTree<K extends Comparable<K>, V> {
	
	private int root;
	
	private final int ORDER;
	private int height;
	public PageManager<K, V> pageManager;

	public BPlusTree(int order, int pageSize) {
		if (order < 3){
			throw new IllegalArgumentException();
		}
		ORDER = order;
		pageManager = new PageManager<K, V> (pageSize);
		root = pageManager.allocateNewPage();
		LeafNode<K, V> rootNode = new LeafNode<K, V>(ORDER);
		height = 1;
		pageManager.writeNodeToPage(rootNode, root, height);
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
			InnerNode<K, V> innerNode = (InnerNode<K, V>)node;
			node = innerNode.getChild(node.getKeyLocation(key), currentLevel, pageManager);
			currentLevel--;
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
		root = newPageID;
		if (split != null){
			setHeight(height + 1);
			int splitPageID = pageManager.allocateNewPage();
			InnerNode<K, V> rootNode = new InnerNode<K,V>(Math.max(3, ORDER / (int)Math.pow(2, height - 1) )); //kazdy wyzszy node jest mniejszy o polowe
			rootNode.keys.add(split.key);
			rootNode.pageIDs.add(newPageID);
			rootNode.pageIDs.add(splitPageID);
			pageManager.writeNodeToPage(split.left, newPageID, getHeight() - 1);
			pageManager.writeNodeToPage(split.right, splitPageID, getHeight() - 1);
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
		int newPageID = pageManager.allocateNewPage();
		Node<K,V> rootNode = pageManager.getNodeFromPage(root,height);
		boolean rootEmptyKeys = rootNode.remove(key, null, -1, newPageID, pageManager, height);
		root = newPageID;
		if (rootEmptyKeys){
			//root ma 0 kluczy i jednego potomka
			//root przechodzi na swojego potomka, wysokosc drzewa zmniejszona o 1
			//root = ((InnerNode<K,V>)root).getChild(0); //tu bedzie pageNumber
			
			rootNode = ((InnerNode<K,V>)rootNode).getChild(0, height, pageManager);
			setHeight(height - 1);
			rootNode.setOrder(Math.max(3, ORDER / (int)Math.pow(2, height - 1) ));
			
			pageManager.writeNodeToPage(rootNode, newPageID, height);
		}
		//checkForErrors(); //DEBUG
		return true;
	}
	
	public void dump(){
		System.out.println("miTree of height " + height );
		pageManager.getNodeFromPage(root, height).dump("", height, pageManager, root);
		System.out.println("Used pages: " + (pageManager.getPageCount() - pageManager.getUnUsedPageCount()));
		System.out.println("Unused pages: "+pageManager.getUnUsedPageCount());
		pageManager.resetUsedPagesCount();
	}
	
	/*private void checkForErrors(){
		root.checkForErrors(true);
	}*/ //DEBUG

}
