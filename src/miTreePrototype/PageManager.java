package miTreePrototype;

import java.util.ArrayList;
import java.util.List;

/**
 *Klasa zarządzająca stronami pamięci
 * @author Kacper Kozerski, Adam Michalski, Rafał Muszyński
 * @param <K> typ Klucza
 * @param <V> typ wartości
 */
public class PageManager<K extends Comparable<K>, V> {
	private int pageSize;
	private int treeHeight;
	private List<MemoryPage<K, V>> pageList;
	private boolean[] isPageUsed;
	
	/**Tworzy nową stronę i dodaje do listy. 
	 * @return Identyfikator nowej strony.
	 */
	public int allocateNewPage(){
		int newPageID = pageList.size();
		pageList.add(new MemoryPage<K, V>(newPageID, pageSize));
		return newPageID;
	}
	
	/**Tworzy nową stronę, służącą do przechowywania wartości.
	 * @return Identyfikator nowej strony.
	 */
	public int allocateNewValuePage(){
		int newPageID = pageList.size();
		pageList.add(new ValuePage<K, V>(newPageID, pageSize));
		return newPageID;
	}
	
	/**Inicjalizujuje nowy menadżer stron
	 * @param pageSize Rozmiar tworzonych stron.
	 */
	public PageManager(int pageSize){
		this.pageSize = pageSize;
		pageList = new ArrayList<MemoryPage<K, V>>();
		treeHeight = 1;
	}
	
	public int getPageCount(){
		return pageList.size();
	}
	
	public void setTreeHeight(int height){
		treeHeight = height;
	}
	public int getTreeHeight(){
		return treeHeight;
	}
	
	/**Czyta Node z danej strony i poziomu.
	 * @param pageID Identyfikator strony.
	 * @param level Poziom.
	 * @return Szukany Node.
	 */
	public Node<K, V> getNodeFromPage(int pageID, int level){
		int size = (int)(pageSize / Math.pow(2, level));
		int offset = size;
		if(level == treeHeight){
			size = size * 2;
			offset = 0;
		}
		Node<K, V> retrievedNode = pageList.get(pageID).read(offset);
		if(retrievedNode != null){
			return retrievedNode;
		}
		else{
			System.out.println("Could not retrieve node at level " + level + " from page " + pageID);
			return null;
		}
		
	}
	public MemoryPage<K, V> getPage(int index){
		return pageList.get(index);
	}
	
	/**
	 * Zapisuje nowego node na danej stronie i poziomie.
	 */
	public void writeNodeToPage(Node<K, V> node, int pageID, int level){
		pageList.get(pageID).write(node, level, treeHeight);
	}
	
	/**Służy do liczenia ilości używanych stron
	 * @param pageID numer strony do ustawienia jako używaną.
	 */
	public void setPageUsed(int pageID){
		isPageUsed[pageID] = true;
	}
	
	public int getUsedPageCount(){
		int used = 0;
		for(boolean b : isPageUsed){
			if(b){
				used++;
			}
		}
		return used;
	}
	public int getUnUsedPageCount(){
		int unused = 0;
		for(boolean b: isPageUsed){
			if(!b){
				unused++;
			}
		}
		return unused;
	}
	public void resetUsedPagesCount(){
		isPageUsed = new boolean[pageList.size()];
	}
	
}
