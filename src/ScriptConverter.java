import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ScriptConverter {
	
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Please put 2 arguments (first in file, then out file)");
			System.exit(-1);
		}
		String script = new String(Files.readAllBytes(Paths.get(new File(args[0]).toURI())), StandardCharsets.UTF_8);
		List<Action.Context> actions = preProcess(script);
	}
	
	//testing only
	public static void print(Object obj) {
		if (obj instanceof Action.Context) {
			System.out.println(((Action.Context)obj).getAction());
			print(((Action.Context)obj).getContext());
		}
		if (obj instanceof List) {
			for (Object o : ((List) obj)) {
				print(o);
			}
		}
		if (obj instanceof String) {
			System.out.println(obj);
		}
		if (obj instanceof Object[]) {
			print(((Object[])obj)[0]);
			print(((Object[])obj)[1]);
		}
		if (obj instanceof Map) {
			for (Object o : ((Map) obj).entrySet()) {
				print(o);
			}
		}
		if (obj instanceof Entry) {
			print(((Entry) obj).getKey());
			print(((Entry) obj).getValue());
		}
	}
	
	public static List<Action.Context> preProcess(String script) {
		script = script.trim();
		String[] lines = script.split(System.lineSeparator());
		ObjectInteger currLine = new ObjectInteger(0);
		try {
			return readNextBlock(lines, 0, currLine);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("failed to read script");
	}
	
	public static Action.Context readStatment(String[] lines, HashMap<String, List<Action.Context>> currIfTree, int currTab, ObjectInteger currLine) throws Exception {
		if (currLine.get() < lines.length) {
			String nextLine = lines[currLine.get()];
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
				if (!nextLine.endsWith("\"")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
					throw new IllegalArgumentException();
				}
				action = Action.PRINT.context(nextLine.substring(1, nextLine.length() - 1));
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
				action = Action.END.context("end");
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
				List<Action.Context> actions = readNextBlock(lines, currTab + 1, currLine);
				if (nextLine.startsWith("(") && nextLine.endsWith("):")) {
					action = Action.IF.context(new Object[] {nextLine.substring(1, nextLine.length() - 2), actions});
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
				List<Action.Context> actions = readNextBlock(lines, currTab + 1, currLine);
				action = Action.FIELD.context(new Object[] {nextLine, actions});
			}break;
			default:
				System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
				throw new IllegalArgumentException();
			}
			return action;
		}
		throw new Exception("Failed to read line: " + currLine.get());
	}
	
	public static List<Action.Context> readNextBlock(String[] lines, int currTab, ObjectInteger currLine) throws Exception {
		List<Action.Context> result = new ArrayList<>();
		HashMap<String, List<Action.Context>> currIfTree = new HashMap<>();
		while(currLine.get() < lines.length) {
			String tabPattern = Arrays.asList(new String[currTab]).stream().map(s -> "\t").collect(Collectors.joining());
			String nextLine = lines[currLine.get()];
			if ((!nextLine.startsWith(tabPattern)) || nextLine.startsWith(tabPattern + "\t")) {
				break;
			}
			Action.Context action = readStatment(lines, currIfTree, currTab, currLine);
			if (action.getAction() != Action.IF_PLACEHOLDER) {
				if (currIfTree.size() != 0) {
					action = Action.IF_TREE.context(currIfTree);
					currIfTree = new HashMap<>();
				}
				result.add(action);
			}
		}
		if (currIfTree.size() != 0) {
			result.add(Action.IF_TREE.context(currIfTree));
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