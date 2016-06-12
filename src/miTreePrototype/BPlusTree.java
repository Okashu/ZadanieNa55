package miTreePrototype;

/**
 * Klasa centralna drzewa, obs³uguje wszystkie zapytania programu.
 * @author Adam
 *
 * @param <K> Typ s³u¿¹cy za klucze w drzewie. Musi implementowaæ Comparable.
 * @param <V> Typ s³u¿¹cy za wartoœci w drzewie.
 */
public class BPlusTree<K extends Comparable<K>, V> {
	
	private int root;
	
	private final int ORDER;
	private int height;
	public PageManager<K, V> pageManager;

	/**Tworzy nowe drzewo o zadanych parametrach
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
	
	/**ustawia wysokoœæ drzewa
	 * @param height nowa wysokoœæ
	 */
	public void setHeight(int height){
		this.height = height;
		pageManager.setTreeHeight(height);
	}
	/**
	 * zwraca wyskoœæ drzewa
	 * 
	 * @return wysokoœæ drzewa
	 */
	public int getHeight(){ return height; }
	
	/**Szuka liœcia przechowuj¹cego dany klucz
	 * @return szukany liœæ
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
	
	/**Wyszukuje wêze³ z danym kluczem
	 * @param key klucz
	 * @return liœæ, wtedy i tylko wtedy gdy znajduje siê w nim klucz
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
	
	/**Dodaje now¹ wartoœæ do drzewa
	 * @param key klucz wartoœci	
	 * @param value nowa wartoœæ
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
	/**Dodaje now¹ wartoœæ do Node, wartoœæ ta 
	 * nie ma zwi¹zku z wartoœciami przechowywanymi na drzewie
	 * @param nodeKey
	 * @param value
	 */
	public void insertNodeValue(K nodeKey,V value){
		int newPageID = pageManager.allocateNewPage();
		if(pageManager.getNodeFromPage(root, height).insertNodeValue(nodeKey, value, newPageID, pageManager, height)){
			root = newPageID;
		}
	}

	/**Usuwa wartosc z Node, wartoœæ ta nie ma zwi¹zku
	 *  z wartœciami przechowywanymi na drzewie
	 * @param nodeKey
	 * @param value
	 */
	public void deleteNodeValue(K nodeKey,V value){
		int newPageID = pageManager.allocateNewPage();
		if(pageManager.getNodeFromPage(root, height).deleteNodeValue(nodeKey, value, newPageID, pageManager, height)){
			root = newPageID;
		}
	}
	
	/**Zwraca wartoœæ zwi¹zan¹ z danym kluczem
	 * @param key klucz szukanej wartoœci
	 * @return szukana wartoœæ
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
	
	/**Usuwa wartoœæ zwi¹zan¹ z danym kluczem
	 * @param key klucz usuwanej wartoœci
	 * @return true jeœli usuniêto wartoœæ, false jeœli jej nie by³o
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
			//root = ((InnerNode<K,V>)root).getChild(0); //tu bedzie pageNumber
			
			rootNode = ((InnerNode<K,V>)rootNode).getChild(0, height, pageManager);
			setHeight(height - 1);
			rootNode.setOrder(Math.max(3, ORDER / (int)Math.pow(2, height - 1) ));
			
			pageManager.writeNodeToPage(rootNode, newPageID, height);
		}
		//checkForErrors(); //DEBUG
		return true;
	}
	
	/**
	 * Wypisuje zawartoœæ drzewa
	 */
	public void dump(){
		pageManager.resetUsedPagesCount();
		System.out.println("miTree of height " + height );
		pageManager.getNodeFromPage(root, height).dump("", height, pageManager, root);
		System.out.println("Used pages: " + pageManager.getUsedPageCount());
		System.out.println("Unused pages: " + pageManager.getUnUsedPageCount());
		pageManager.resetUsedPagesCount();
	}
	
	/*private void checkForErrors(){
		root.checkForErrors(true);
	}*/ //DEBUG

}
