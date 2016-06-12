package miTreePrototype;

import java.util.ArrayList;
import java.util.List;

public class PageManager<K extends Comparable<K>, V> {
	private int pageSize;
	private int treeHeight;
	private List<MemoryPage<K, V>> pageList;
	private boolean[] isPageUsed;
	
	public int allocateNewPage(){
		int newPageID = pageList.size();
		pageList.add(new MemoryPage<K, V>(newPageID, pageSize));
		return newPageID;
	}
	
	public int allocateNewValuePage(){
		int newPageID = pageList.size();
		pageList.add(new ValuePage<K, V>(newPageID, pageSize));
		return newPageID;
	}
	
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
	
	public void writeNodeToPage(Node<K, V> node, int pageID, int level){
		pageList.get(pageID).write(node, level, treeHeight);
	}
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
