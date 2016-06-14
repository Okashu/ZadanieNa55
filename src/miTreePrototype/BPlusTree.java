package miTreePrototype;

/**
 * Klasa centralna drzewa, obsługuje wszystkie zapytania programu.
 * @author Kacper Kozerski, Adam Michalski, Rafał Muszyński
 * @param <K> Typ służący za klucze w drzewie. Musi implementować Comparable.
 * @param <V> Typ służący za wartości w drzewie.
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
	 * Ustawia wysokość drzewa.
	 * @param height Nowa wysokość.
	 */
	public void setHeight(int height){
		this.height = height;
		pageManager.setTreeHeight(height);
	}
	/**
	 * Zwraca wyskość drzewa.
	 * @return Wysokość drzewa.
	 */
	public int getHeight(){ return height; }
	
	/**
	 * Szuka liścia, który może zawierać dany klucz.
	 * @param key Szukany klucz.
	 * @return Liść, który może zawierać dany klucz.
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
	 * Wyszukuje węzeł z danym kluczem. Jeśli takiego nie ma,
	 * zwraca null.
	 * @param key Szukany klucz.
	 * @return Liść zawierający dany klucz. Jeśli takiego nie ma, to null.
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
	 * Dodaje nową wartość do drzewa.
	 * @param key Klucz wartości.	
	 * @param value Wartość.
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
	 * Dodaje wartość dodatkową do jednego z węzłów na drzewie,
	 * prechowującego podany klucz.
	 * @param nodeKey Klucz znajdujący się w szukanym węźle.
	 * @param value Dodatkowa wartość.
	 */
	public void insertNodeValue(K nodeKey,V value){
		int newPageID = pageManager.allocateNewPage();
		if(pageManager.getNodeFromPage(root, height).insertNodeValue(nodeKey, value, newPageID, pageManager, height)){
			root = newPageID;
		}
	}

	/**
	 * Usuwa wartość dodatkową z jednego z węzłów na drzewie,
	 * prechowującego podany klucz.
	 * @param nodeKey Klucz znajdujący się w szukanym węźle.
	 * @param value Dodatkowa wartość.
	 */
	public void deleteNodeValue(K nodeKey,V value){
		int newPageID = pageManager.allocateNewPage();
		if(pageManager.getNodeFromPage(root, height).deleteNodeValue(nodeKey, value, newPageID, pageManager, height)){
			root = newPageID;
		}
	}
	
	/**
	 * Zwraca wartość związaną z danym kluczem.
	 * @param key Klucz szukanej wartości.
	 * @return Wartość danego klucza.
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
	 * Usuwa wartość związaną z danym kluczem.
	 * @param key Klucz usuwanej wartości.
	 * @return true jeśli usunięto wartość, false jeśli jej nie było.
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
