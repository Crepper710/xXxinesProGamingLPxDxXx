package script2vxml;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import script2vxml.actions.Assign;
import script2vxml.actions.Goto;
import script2vxml.actions.If;
import script2vxml.actions.Prompt;

public class ScriptConverter {
	
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("Please put 3 arguments (first in file, then out file, then (true/false) for pretty print)");
			System.exit(-1);
		}
		String script = new String(Files.readAllBytes(Paths.get(new File(args[0]).toURI())), StandardCharsets.UTF_8);
		List<Action.Context> actions = preProcess(script);
		VXMLDocument vxml = convert(actions);
		Files.write(Paths.get(new File(args[1]).toURI()),vxml.toString(Boolean.parseBoolean(args[2])).getBytes(StandardCharsets.UTF_8));
	}
	
	//testing only
	@SuppressWarnings("rawtypes")
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
	
	public static Action.Context readStatment(String[] lines, TreeMap<String, List<Action.Context>> currIfTree, int currTab, ObjectInteger currLine) throws Exception {
		if (currLine.get() < lines.length) {
			String nextLine = lines[currLine.get()];
			{
				int commentIndex = nextLine.indexOf("//");
				if (commentIndex != -1) {
					nextLine = nextLine.substring(0, commentIndex);
				}
			}
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
			nextLine = nextLine.substring(currTab).trim();
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
				if (!(nextLine.startsWith("if ") && nextLine.endsWith(":"))) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
					throw new IllegalArgumentException();
				}
				nextLine = nextLine.substring(2, nextLine.length() - 1).trim();
				if (nextLine.contains(" ") || nextLine.contains("\t")) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" invalid syntax");
					throw new IllegalArgumentException();
				}
				currLine.set(currLine.get() + 1);
				List<Action.Context> actions = readNextBlock(lines, currTab + 1, currLine);
				action = Action.IF.context(new Object[] {nextLine.replace('"', '\''), actions});
			}break;
			case 'o':{
				if (!(nextLine.startsWith("option ") && nextLine.endsWith(":"))) {
					System.err.println((currLine.get() + 1) + ": \"" + nextLine + "\" unknown command");
					throw new IllegalArgumentException();
				}
				nextLine = nextLine.substring(6, nextLine.length() - 1).trim();
				currLine.set(currLine.get() + 1);
				List<Action.Context> actions = readNextBlock(lines, currTab + 1, currLine);
				currIfTree.put(nextLine.replace('"', '\''), actions);
				action = Action.OPTION_PLACEHOLDER.context(null);
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
		TreeMap<String, List<Action.Context>> currIfTree = new TreeMap<>();
		while(currLine.get() < lines.length) {
			String tabPattern = Arrays.asList(new String[currTab]).stream().map(s -> "\t").collect(Collectors.joining());
			String nextLine = lines[currLine.get()];
			if ((!nextLine.startsWith(tabPattern)) || nextLine.startsWith(tabPattern + "\t")) {
				break;
			}
			Action.Context action = readStatment(lines, currIfTree, currTab, currLine);
			if (action.getAction() != Action.OPTION_PLACEHOLDER) {
				if (currIfTree.size() != 0) {
					action = Action.OPTION_TREE.context(currIfTree);
					currIfTree = new TreeMap<>();
				}
				result.add(action);
			}
		}
		if (currIfTree.size() != 0) {
			result.add(Action.OPTION_TREE.context(currIfTree));
		}
		return result;
	}
	
	public static String preProcessOptionExpression(String expr, List<String> options) {
		StringBuilder sb = new StringBuilder();
		StringBuilder currentOption = new StringBuilder();
		boolean passThroughMode = false;
		for (char c : expr.toCharArray()) {
			if (c == '$') {
				if (passThroughMode) {
					sb.append("$id$=='").append(currentOption.toString()).append("'");
					if (!options.contains(currentOption.toString())) {
						options.add(currentOption.toString());
					}
					currentOption = new StringBuilder();
				}
				passThroughMode = !passThroughMode;
				continue;
			}
			if (!passThroughMode) {
				sb.append(c);
			} else {
				currentOption.append(c);
			}
		}
		return sb.toString();
	}
	
	public static VXMLDocument convert(List<Action.Context> actions) {
		boolean preMode = true;
		List<VXMLAction> preActions = new ArrayList<>();
		List<VXMLForm> forms = new ArrayList<>();
		String firstID = "";
		Set<String> varNames = new HashSet<>();
		for (Action.Context action : actions) {
			if (preMode) {
				if (action.getAction() == Action.FIELD) {
					preMode = false;
				} else {
					preActions.add(toAction(action, "start", "start", forms, varNames));
				}
			}
			if (!preMode) {
				if (action.getAction() != Action.FIELD) {
					new IllegalArgumentException("Wrong fieldorder");
				}
				Object[] temp_a = (Object[])action.getContext();
				String temp_s1 = (String) temp_a[0];
				@SuppressWarnings("unchecked")
				List<Action.Context> temp_s2 = (List<Action.Context>) temp_a[1];
				if (firstID.isEmpty()) {
					firstID = temp_s1;
				}
				forms.add(toForm(temp_s2, temp_s1, temp_s1, temp_s1, forms, varNames));
			}
		}
		return new VXMLDocument(preActions, forms, varNames.stream().collect(Collectors.toList()), firstID);
	}
	
	public static int countIfTree(List<Action.Context> actions) {
		int result = 0;
		for (Action.Context action : actions) {
			if (action.getAction() == Action.OPTION_TREE) {
				result++;
			}
		}
		return result;
	}
	
	public static int indexOfIfTree(List<Action.Context> actions) {
		for (int i = 0; i < actions.size(); i++) {
			if (actions.get(i).getAction() == Action.OPTION_TREE) {
				return i;
			}
		}
		throw new IllegalArgumentException("contains no if tree");
	}
	
	@SuppressWarnings("unchecked")
	public static VXMLForm toForm(List<Action.Context> actions, String id, String parentId, String fieldId, List<VXMLForm> allForms, Set<String> varNames) {
		if (countIfTree(actions) != 1) {
			throw new IllegalArgumentException(id + " is no form (" + countIfTree(actions) + ")");
		}
		int index = indexOfIfTree(actions);
		List<VXMLAction> temp_o1 = new ArrayList<>();
		List<VXMLAction> temp_o2 = new ArrayList<>();
		for (Action.Context a : actions.subList(0, index)) {
			temp_o1.add(toAction(a, id, fieldId, allForms, varNames));
		}
		for (Action.Context a : actions.subList(index + 1, actions.size())) {
			temp_o2.add(toAction(a, id, fieldId, allForms, varNames));
		}
		List<String> options = new ArrayList<>();
		Map<String, List<VXMLAction>> ifs = new TreeMap<>();
		int counter = 0;
		for (String key : ((TreeMap<String, List<Action.Context>>) actions.get(index).getContext()).navigableKeySet()) {
			List<Action.Context> value = ((TreeMap<String, List<Action.Context>>) actions.get(index).getContext()).get(key);
			int i = countIfTree(value);
			List<VXMLAction> temp = new ArrayList<>();
			if (i == 0) {
				for (Action.Context a : value) {
					temp.add(toAction(a, id, fieldId, allForms, varNames));
				}
			} else if (i == 1) {
				String subId = parentId + "_s" + (++counter) + "_" + getUUID();
				allForms.add(toForm(value, subId, id, fieldId, allForms, varNames));
				temp.add(new Goto(subId));
			} else {
				new IllegalArgumentException("field is no form");
			}
			ifs.put(preProcessOptionExpression(key, options), temp);
		}
		return new VXMLForm(id, options, ifs, temp_o1, fieldId, temp_o2);
	}
	
	public static VXMLAction toAction(Action.Context action, String parentId, String fieldId, List<VXMLForm> allForms, Set<String> varNames) {
		switch (action.getAction()) {
		case GOTO:
			return new Goto((String)action.getContext());
		case PRINT:
			return new Prompt((String)action.getContext());
		case END:
			return new Goto("end");
		case VAR:
			String[] var = ((String)action.getContext()).split("=");
			varNames.add(var[0]);
			return new Assign(var[0], var[1]);
		case IF:
			Object[] objs = (Object[]) action.getContext();
			String condition = (String) objs[0];
			@SuppressWarnings("unchecked") List<Action.Context> actions = (List<Action.Context>) objs[1];
			int i = countIfTree(actions);
			List<VXMLAction> temp = new ArrayList<>();
			if (i == 0) {
				for (Action.Context a : actions) {
					temp.add(toAction(a, parentId, fieldId, allForms, varNames));
				}
			} else if (i == 1) {
				String subId = parentId + "_s_" + getUUID();
				allForms.add(toForm(actions, subId, parentId, fieldId, allForms, varNames));
				temp.add(new Goto(subId));
			} else {
				new IllegalArgumentException("field is no form");
			}
			return new If(condition, temp);
		default:
			throw new IllegalArgumentException(action.getAction() + String.valueOf(Character.toChars(0x1F928)));
		}
	}
	
	private static int counter = 0;
	
	public static String getUUID() {
		byte[] a = ByteBuffer.allocate(4).putInt((++counter)).array();
		return bytesToHex(a);
	}
	
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	public static enum Action {
		
		GOTO,
		PRINT,
		END,
		VAR,
		OPTION_TREE,
		FIELD,
		OPTION_PLACEHOLDER,
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
