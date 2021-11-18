package script2vxml;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import script2vxml.actions.Prompt;

public class VXMLForm {
	
	public static String DEFAULT_PRE_PROMPT = "Wähle";
	
	private final String name;
	private final List<String> options;
	private final Map<String, List<VXMLAction>> ifs;
	private final List<VXMLAction> preOption;
	private final String safeStateID; // just for failsafe if the field is failing so ines doesnt crash, or something else
	private final List<VXMLAction> postOption;
	
	public VXMLForm(String name, List<String> options, Map<String, List<VXMLAction>> ifs, List<VXMLAction> preOption, String safeStateID, List<VXMLAction> postOption) {
		this.name = name;
		this.options = options;
		this.ifs = ifs;
		this.preOption = preOption;
		this.safeStateID = safeStateID;
		this.postOption = postOption;
	}
	
	private static int id = 0;
	
	@Override
	public String toString() {
		return this.toString(true);
	}
	
	public String toString(boolean prettyPrint) {
		String t = "";
		String nl = "";
		if (prettyPrint) {
			t = "\t";
			nl = System.lineSeparator();
		}
		
		Prompt prePrompt = null;
		
		for (VXMLAction action : preOption) {
			if (action instanceof Prompt) {
				prePrompt = (Prompt) action;
			}
		}
		
		if (prePrompt == null) {
			prePrompt = new Prompt(DEFAULT_PRE_PROMPT);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<form id=\"").append(name).append("\">").append(nl);
		
		List<VXMLAction> temp = preOption;
		temp.remove(prePrompt);
		
		if (temp.size() >= 1) {
			sb.append(t).append("<block>").append(nl);
			for (VXMLAction action : temp) {
				for(String line : action.toString(prettyPrint).split(System.lineSeparator())) {
					sb.append(t).append(t).append(line).append(nl);
				}
			}
			sb.append(t).append("</block>").append(nl);
		}
		
		sb
			.append(t).append("<field name=\"").append(convertId(id)).append("\">").append(nl)
			.append(t).append(t).append("<prompt>").append(nl)
			.append(t).append(t).append(t).append(prePrompt.getText()).append(" <enumerate/>").append(nl)
			.append(t).append(t).append("</prompt>").append(nl);
		for (String s : options) {
			sb.append(t).append(t).append("<option value=\"").append(s).append("\">").append(s).append("</option>").append(nl);
		}
		sb.append(t).append(t).append("<filled>").append(nl);
		for (Entry<String, List<VXMLAction>> e : ifs.entrySet()) {
			sb.append(t).append(t).append(t).append("<if cond=\"").append(e.getKey().replace("$id$", convertId(id))).append("\">").append(nl);
			for (VXMLAction action : e.getValue()) {
				for(String line : action.toString(prettyPrint).split(System.lineSeparator())) {
					sb.append(t).append(t).append(t).append(t).append(line).append(nl);
				}
			}
			sb.append(t).append(t).append(t).append("</if>").append(nl);
		}
		if (postOption.size() >= 1) {
			for (VXMLAction action : postOption) {
				for(String line : action.toString(prettyPrint).split(System.lineSeparator())) {
					sb.append(t).append(t).append(t).append(line).append(nl);
				}
			}
		}
		if (safeStateID != null) {
			sb.append(t).append(t).append(t).append("<goto next=\"#").append(safeStateID).append("\"/>").append(nl);
		}
		sb
			.append(t).append(t).append("</filled>").append(nl)
			.append(t).append("</field>").append(nl)
			.append("</form>");
		id++;
		return sb.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public static String convertId(int id) {
		return String.valueOf(id).replace('0', 'a').replace('1', 'b').replace('2', 'c').replace('3', 'd').replace('4', 'e').replace('5', 'f').replace('6', 'g').replace('7', 'h').replace('8', 'i').replace('9', 'j');
	}
	
}
