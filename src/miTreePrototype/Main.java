package miTreePrototype;

import java.util.StringTokenizer;

public class Main {

	public static final int PAGESIZE = 2048;
	
	public static void main(String[] args){
		if(System.console() == null){
			oldMain();
			return;
		}
		
		System.out.print("Welcome to the miTree testing prorgam. Please choose an option:\n"
				+ "1. Run an example program, creating and modifying some trees\n"
				+ "2. Run a simple interactive program, allowing you to create and modify miTrees yourself\n"
				+ "Choose an option: ");
		
		String input = System.console().readLine();
		
		int option;
		try{
			option = Integer.parseInt(input);
			if(option == 1){
				System.out.println("\n");
				oldMain();
			}
			else if(option == 2){
				System.out.print("\n");
				testingProgram();
			}
			else{
				throw new NumberFormatException();
			}
		}
		catch(NumberFormatException e){
			System.out.println("Invalid option!");
			return;
		}
	}
	
	private static void testingProgram(){
		System.out.print("In this program, you'll be able to create and modify Integer mitrees.\n"
				+ "Please specify the tree ORDER: ");

		int order;
		String input = System.console().readLine();
		System.out.print("\n");
		try{
			order = Integer.parseInt(input);
			if(order < 3){
				throw new NumberFormatException();
			}
		}
		catch(NumberFormatException e){
			System.out.println("Invalid order!");
			return;
		}
		
		BPlusTree<Integer, Integer> bpt = new BPlusTree<Integer, Integer>(order, PAGESIZE);
		while(true){
			System.out.print("Your options are: (I)nsert <NUMBER>, (R)emove <NUMBER>, (D)raw.\n"
					+ "Choose an option: ");
			
			input = System.console().readLine();
			try{
				if(input == null){
					System.out.println("ERROR: no input!");
					return;
				}
				StringTokenizer st = new StringTokenizer(input);
				if(!st.hasMoreTokens()){
					throw new NumberFormatException();
				}
				String option = st.nextToken().toLowerCase();
				if(option.startsWith("i")){
					if(!st.hasMoreTokens()){
						throw new NumberFormatException();						
					}
					int argument = Integer.parseInt(st.nextToken());
					bpt.insert(argument, argument);
				}
				else if(option.startsWith("r")){
					if(!st.hasMoreTokens()){
						throw new NumberFormatException();
					}
					int argument = Integer.parseInt(st.nextToken());
					bpt.remove(argument);
				}
				else if(option.startsWith("d")){
					bpt.dump();
				}
			}
			catch(NumberFormatException e){
				System.out.println("Invalid command!");
			}
			
		}
	}
	
	private static void oldMain(){

		BPlusTree<Integer, Integer> tree2 = new BPlusTree<Integer, Integer>(32, PAGESIZE);
		for(int i=200; i>=0; i--){
			if (i%2 == 0){
				tree2.insert(i, i);
			}
		}
		tree2.dump();

		for(int i=0; i<=200; i++){
			System.out.println(i + ": " + tree2.retrieve(i));
		}
		tree2.dump();
		System.out.println(tree2.getHeight());
		
		for(int i=200; i>=0; i--){
			if (i%4 == 0){
				System.out.println(i);
				tree2.remove(i);
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
				tree2.remove(i);
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
				tree2.remove(i);
			}
		}
		tree2.dump(); //powinny zostac sie liscie postaci 14+16k
		tree2.insert(15, 15);
		tree2.insert(33, 33);
		tree2.remove(14);
		System.out.println("!");
		tree2.dump();
		
		int j;
		for(int i=100; i<300; i++){ // dla order=32, pagesize=2048 dochodzi do ok 550
			j = i;
			switch (i%3){ // lekko poprzekrecana kolejnosc dodawania
			case 0:
				j = i+1; break;
			case 1:
				j = i-1; break;
			}

			System.out.println(j);
			tree2.insert(j, j);

		}
		tree2.dump();
		
		
		BPlusTree<Integer, Integer> tree1000 = new BPlusTree<Integer,Integer>(32, PAGESIZE);
		for(int i = 1000; i < 1480; i++){
			System.out.println(i);
			tree1000.insert(i, i);
		}
		
		tree1000.dump();
		
		//tree1000.remove(1200);
		
		tree1000.insertNodeValue(1100, 34);
		tree1000.insertNodeValue(1098, 14);
		tree1000.insertNodeValue(1098, 16);
		tree1000.insertNodeValue(1018, 36);
		tree1000.deleteNodeValue(1098, 16);
		tree1000.dump();
		
		/*for(int i = 1005; i < 1475; i++){
				System.out.println(i);
				tree1000.remove(i);

		}
		
		tree1000.dump();*/
		/*BPlusTree<Integer, Integer> tree3 = new BPlusTree<Integer, Integer>(4, PAGESIZE);
		tree3.insert(3,3);
		tree3.insert(2, 2);
		tree3.dump();
		tree3.remove(2);
		tree3.dump();
		tree3.remove(3);
		tree3.dump();
		tree3.insert(2, 2);
		tree3.dump();*/
	}

}
