package miTreePrototype;

/**
 * Klasa centralna drzewa, obs³uguje wszystkie zapytania programu.
 *
 * @param <K> Typ s³u¿¹cy za klucze w drzewie. Musi implementowaæ Comparable.
 * @param <V> Typ s³u¿¹cy za wartoœci w drzewie.
 */
public class BPlusTree<K extends Comparable<K>, V> {
	
	private int root;
	
	private final int ORDER;
	private int height;
	public PageManager<K, V> pageManager;

	/**
	 * Tworzy nowe drzewo o zadanych parametrach.
	 * @param order Maksymalna liczba kluczy w drzewie.
	 * @param pageSize Rozmiar strony w drzewie.
	 */
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
	
	/**
	 * Ustawia wysokoœæ drzewa.
	 * @param height Nowa wysokoœæ.
	 */
	public void setHeight(int height){
		this.height = height;
		pageManager.setTreeHeight(height);
	}
	/**
	 * Zwraca wyskoœæ drzewa.
	 * @return Wysokoœæ drzewa.
	 */
	public int getHeight(){ return height; }
	
	/**
	 * Szuka liœcia, które mo¿e zawieraæ dany klucz.
	 * @param key Szukany klucz.
	 * @return Liœæ, który mo¿e zawieraæ dany klucz.
	 */
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
	
	/**
	 * Wyszukuje wêz³a z danym kluczem. Jeœli takiego nie ma,
	 * zwraca null.
	 * @param key Szukany klucz.
	 * @return Liœæ zawieraj¹cy dany klucz. Jeœli takiego nie ma, to null.
	 */
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
	
	/**
	 * Dodaje now¹ wartoœæ do drzewa.
	 * @param key Klucz wartoœci.	
	 * @param value Wartoœæ.
	 */
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
	
	/**
	 * Dodaje wartoœæ dodatkow¹ do jednego z wêz³ów na drzewie,
	 * prechowuj¹cego podany klucz.
	 * @param nodeKey Klucz znajduj¹cy siê w szukanym wêŸle.
	 * @param value Dodatkowa wartoœæ.
	 */
	public void insertNodeValue(K nodeKey,V value){
		int newPageID = pageManager.allocateNewPage();
		if(pageManager.getNodeFromPage(root, height).insertNodeValue(nodeKey, value, newPageID, pageManager, height)){
			root = newPageID;
		}
	}

	/**
	 * Usuwa wartoœæ dodatkow¹ z jednego z wêz³ów na drzewie,
	 * prechowuj¹cego podany klucz.
	 * @param nodeKey Klucz znajduj¹cy siê w szukanym wêŸle.
	 * @param value Dodatkowa wartoœæ.
	 */
	public void deleteNodeValue(K nodeKey,V value){
		int newPageID = pageManager.allocateNewPage();
		if(pageManager.getNodeFromPage(root, height).deleteNodeValue(nodeKey, value, newPageID, pageManager, height)){
			root = newPageID;
		}
	}
	
	/**
	 * Zwraca wartoœæ zwi¹zan¹ z danym kluczem.
	 * @param key Klucz szukanej wartoœci.
	 * @return Wartoœæ danego klucza.
	 */
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
	
	/**
	 * Usuwa wartoœæ zwi¹zan¹ z danym kluczem.
	 * @param key Klucz usuwanej wartoœci.
	 * @return true jeœli usuniêto wartoœæ, false jeœli jej nie by³o.
	 */
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
			
			rootNode = ((InnerNode<K,V>)rootNode).getChild(0, height, pageManager);
			setHeight(height - 1);
			rootNode.setOrder(Math.max(3, ORDER / (int)Math.pow(2, height - 1) ));
			
			pageManager.writeNodeToPage(rootNode, newPageID, height);
		}
		return true;
	}
	
	public void dump(){
		pageManager.resetUsedPagesCount();
		System.out.println("miTree of height " + height );
		pageManager.getNodeFromPage(root, height).dump("", root, pageManager, height);
		System.out.println("Used pages: " + pageManager.getUsedPageCount());
		System.out.println("Unused pages: " + pageManager.getUnUsedPageCount());
		pageManager.resetUsedPagesCount();
	}

}
