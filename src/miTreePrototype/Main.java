package miTreePrototype;


public class Main {

	public static final int PAGESIZE = 4096;
	
	public static void main(String[] args){
		
	
		BPlusTree<Integer, Integer> tree2 = new BPlusTree<Integer, Integer>(4, PAGESIZE);
		for(int i=200; i>=0; i--){
			if (i%2 == 0){
				tree2.insert(i, i);
			}
		}
		tree2.dump();

	/*	for(int i=0; i<=200; i++){
			System.out.println(i + ": " + tree2.retrieve(i));
		}
		tree2.dump();
		System.out.println(tree2.getHeight());
		
		for(int i=200; i>=0; i--){
			if (i%4 == 0){
				System.out.println(i);
		//		tree2.remove(i);
			}
		}
		tree2.dump();
		for(int i=0; i<=200; i++){
			System.out.println(i + ": " + tree2.retrieve(i));
		}
		System.out.println(tree2.getHeight());
		
		for(int i=200; i>=0; i--){
			if (i%8 == 2){
				System.out.println(i);
			//	tree2.remove(i);
			}
		}
		tree2.dump();
		for(int i=0; i<=200; i++){
			System.out.println(i + ": " + tree2.retrieve(i));
		}
		tree2.dump();
		System.out.println(tree2.getHeight());
		
		for(int i=200; i>=0; i--){
			if (i%16 == 6){
				System.out.println(i);
			//	tree2.remove(i);
			}
		}
		tree2.dump(); //powinny zostac sie liscie postaci 14+16k
		tree2.insert(15, 15);
		tree2.insert(33, 33);
	//	tree2.remove(14);
		System.out.println("!");
		tree2.dump();
		
		BPlusTree<Integer, Integer> tree3 = new BPlusTree<Integer, Integer>(4, PAGESIZE);
		tree3.insert(3,3);
		tree3.insert(2, 2);
		tree3.dump();
	//	tree3.remove(2);
		tree3.dump();
	//	tree3.remove(3);
		tree3.dump();
		tree3.insert(2, 2);
		tree3.dump();*/
		
	}

}
