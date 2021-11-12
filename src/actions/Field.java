package actions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Field extends VXMLAction {
	
	private final String name;
	private final Map<String, List<VXMLAction>> options;
	private final String prePrompt;
	private final String safeStateID; // just for failsafe if the field is failing so ines doesnt crash, or something else
	
	public Field(String name, Map<String, List<VXMLAction>> options, String prePrompt, String safeStateID) {
		this.name = name;
		this.options = options;
		this.prePrompt = prePrompt;
		this.safeStateID = safeStateID;
	}
	
	private static int id = 0;
	
	@Override
	public String toString(boolean prettyPrint) {
		String t = "";
		String nl = "";
		if (prettyPrint) {
			t = "\t";
			nl = System.lineSeparator();
		}
		StringBuilder sb = new StringBuilder();
		sb
			.append("<form id=\"").append(name).append("\">").append(nl)
			.append(t).append("<field name=\"").append(id).append("\">").append(nl)
			.append(t).append(t).append("<prompt>").append(nl)
			.append(t).append(t).append(t).append(prePrompt).append(" <enumerate/>").append(nl)
			.append(t).append(t).append("</prompt>").append(nl);
		for (String s : options.keySet()) {
			sb.append(t).append(t).append("<option value=\"").append(s).append("\">").append(s).append("</option>").append(nl);
		}
		sb.append(t).append(t).append("<filled>").append(nl);
		for (Entry<String, List<VXMLAction>> e : options.entrySet()) {
			sb.append(t).append(t).append(t).append("<if cond=\"").append(id).append("=").append(e.getKey()).append("\">").append(nl);
			for (VXMLAction action : e.getValue()) {
				for(String line : action.toString(prettyPrint).split(System.lineSeparator())) {
					sb.append(t).append(t).append(t).append(t).append(line).append(nl);
				}
			}
			sb.append(t).append(t).append(t).append("</if>").append(nl);
		}
		sb.append(t).append(t).append("</filled>").append(nl);
		sb.append(t).append("</field>").append(nl);
		if (safeStateID != null) {
			sb
				.append(t).append("<block>").append(nl)
				.append(t).append(t).append("<goto next=\"#").append(safeStateID).append("\"/>").append(nl)
				.append(t).append("</block>").append(nl);
		}
		sb.append("</form>");
		id++;
		return null;
	}
	
}
