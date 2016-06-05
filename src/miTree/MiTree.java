package miTree;

public class MiTree<K extends Comparable<K>, V> {
	
	private PageManager<K, V> pageManager;
	private int pageSize; //todo xddd
	private Node<K, V> root;
	private final int ORDER;
	
	public MiTree(int order, int pageSize){
		this.ORDER = order;
		this.pageSize = pageSize;
		pageManager = new PageManager<K, V>(pageSize);
		root = new Node<K, V>(ORDER, true);
	}
	
	
	
	public static void main(String[] args){
		int order, pageSize;
		MiTree<Integer, Integer> miTree;
		if(args.length < 2){
			System.out.println("Please specify the order of the tree and the height of a page as the first"
					+ "and second launch parameters respectively.");
			return;
		}
		
		try{
			order = Integer.parseInt(args[0]);
			pageSize = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException e){
			e.printStackTrace();
			return;
		}
		miTree = new MiTree<Integer, Integer>(order, pageSize);
	}

}
