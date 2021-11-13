package script2vxml.actions;

import java.util.List;

import script2vxml.VXMLAction;

public class If extends VXMLAction {
	
	private final String condition;
	private final List<VXMLAction> actions;
	
	public If(String condition, List<VXMLAction> actions) {
		this.condition = condition;
		this.actions = actions;
	}

	@Override
	public String toString(boolean prettyPrint) {
		String t = "";
		String nl = "";
		if (prettyPrint) {
			t = "\t";
			nl = System.lineSeparator();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<if cond=\"").append(condition).append("\">").append(nl);
		for (VXMLAction action : actions) {
			for(String line : action.toString(prettyPrint).split(System.lineSeparator())) {
				sb.append(t).append(line).append(nl);
			}
		}
		sb.append("</if>");
		return sb.toString();
	}
	
}
