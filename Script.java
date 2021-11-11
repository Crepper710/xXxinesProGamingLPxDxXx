import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Script {
	
	public static void main(String[] args) throws IOException {
		String script = new String(Files.readAllBytes(Paths.get("C:\\Users\\thor\\Desktop\\skript.txt")), StandardCharsets.UTF_8);
		preProcess(script);
	}
	
	public static void preProcess(String script) {
		script = script.trim();
		String[] lines = script.split(System.lineSeparator());
		HashSet<String> varNames = new HashSet<>();
		HashMap<String, Action.Context> fields = new HashMap<>();
		ObjectInteger currLine = new ObjectInteger(0);
		try {
			readNextBlock(lines, varNames, fields, 0, currLine);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Action.Context readStatment(String[] lines, HashSet<String> varNames, HashMap<String, Action.Context> fields, HashMap<String, List<Action.Context>> currIfTree,int currTab, ObjectInteger currLine) throws Exception {
		if (currLine.get() < lines.length) {
			String nextLine = lines[currLine.get()];
			System.out.println(nextLine);
			{
				String tabPattern = Arrays.asList(new String[currTab]).stream().map(s -> "\t").collect(Collectors.joining());
				if (!nextLine.startsWith(tabPattern)) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" invalid tab length");
					throw new IllegalArgumentException();
				}
				if (nextLine.startsWith(tabPattern + "\t")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" invalid tab length");
					throw new IllegalArgumentException();
				}
			}
			nextLine = nextLine.substring(currTab);
			char firstChar = nextLine.charAt(0);
			Action.Context action = null;
			switch (firstChar) {
			case '"':{
				action = Action.PRINT.context(nextLine);
				currLine.set(currLine.get() + 1);
			}break;
			case 'g':{
				if (!nextLine.startsWith("goto: ")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
					throw new IllegalArgumentException();
				}
				nextLine = nextLine.substring(5).trim();
				if (nextLine.contains(" ") || nextLine.contains("\t")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" invalid syntax");
					throw new IllegalArgumentException();
				}
				action = Action.GOTO.context(nextLine);
				currLine.set(currLine.get() + 1);
			}break;
			case 'e':{
				if (!nextLine.equals("end")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
					throw new IllegalArgumentException();
				}
				nextLine = nextLine.substring(3).trim();
				if (!nextLine.isEmpty()) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" invalid syntax");
					throw new IllegalArgumentException();
				}
				action = Action.END.context(null);
				currLine.set(currLine.get() + 1);
			}break;
			case 'v':{
				if (!nextLine.startsWith("var: ")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
					throw new IllegalArgumentException();
				}
				nextLine = nextLine.substring(4).trim();
				if (nextLine.contains(" ") || nextLine.contains("\t")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" invalid syntax");
					throw new IllegalArgumentException();
				}
				action = Action.VAR.context(nextLine);
				currLine.set(currLine.get() + 1);
			}break;
			case 'i':{
				if (!nextLine.startsWith("if ")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
					throw new IllegalArgumentException();
				}
				nextLine = nextLine.substring(2).trim();
				if (nextLine.contains(" ") || nextLine.contains("\t")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" invalid syntax");
					throw new IllegalArgumentException();
				}
				currLine.set(currLine.get() + 1);
				List<Action.Context> actions = readNextBlock(lines, varNames, fields, currTab + 1, currLine);
				if (nextLine.startsWith("(") && nextLine.endsWith(")")) {
					action = Action.IF.context(actions);
				} else {
					currIfTree.put(nextLine, actions);
					action = Action.IF_PLACEHOLDER.context(null);
				}
			}break;
			case 'f':{
				if (!nextLine.startsWith("field: ")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
					throw new IllegalArgumentException();
				}
				nextLine = nextLine.substring(6).trim();
				if (nextLine.contains(" ") || nextLine.contains("\t")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" invalid syntax");
					throw new IllegalArgumentException();
				}
				currLine.set(currLine.get() + 1);
				List<Action.Context> actions = readNextBlock(lines, varNames, fields, currTab + 1, currLine);
				action = Action.FIELD.context(actions);
			}break;
			default:
				System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
				throw new IllegalArgumentException();
			}
			return action;
		}
		throw new Exception("Failed to read line: " + currLine.get());
	}
	
	public static List<Action.Context> readNextBlock(String[] lines, HashSet<String> varNames, HashMap<String, Action.Context> fields, int currTab, ObjectInteger currLine) throws Exception {
		List<Action.Context> result = new ArrayList<>();
		HashMap<String, List<Action.Context>> currIfTree = new HashMap<>();
		while(currLine.get() < lines.length) {
			String tabPattern = Arrays.asList(new String[currTab]).stream().map(s -> "\t").collect(Collectors.joining());
			String nextLine = lines[currLine.get()];
			if ((!nextLine.startsWith(tabPattern)) || nextLine.startsWith(tabPattern + "\t")) {
				break;
			}
			Action.Context action = readStatment(lines, varNames, fields, currIfTree, currTab, currLine);
			if (action.getAction() != Action.IF_PLACEHOLDER) {
				if (currIfTree.size() != 0) {
					action = Action.IF_TREE.context(currIfTree);
					currIfTree = new HashMap<>();
					result.add(action);
				}
			} else {
				result.add(action);
			}
		}
		return result;
	}
	
	public static enum Action {
		
		GOTO,
		PRINT,
		END,
		VAR,
		IF_TREE,
		FIELD,
		IF_PLACEHOLDER,
		IF;
		
		public Context context(Object context) {
			return new Context(this, context);
		}
		
		public static class Context {
			
			private final Action action;
			private final Object context;
			
			public Context(Action action, Object context) {
				this.action = action;
				this.context = context;
			}
			
			public Action getAction() {
				return action;
			}
			
			public Object getContext() {
				return context;
			}
			
		}
		
	}
	
	public static class ObjectInteger {
		
		private int value = 0;
		
		public ObjectInteger() {}
		
		public ObjectInteger(int value) {
			this.value = value;
		}
		
		public void set(int value) {
			this.value = value;
		}
		
		public int get() {
			return value;
		}
		
	}
	
}