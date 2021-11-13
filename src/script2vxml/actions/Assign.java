package script2vxml.actions;

import script2vxml.VXMLAction;

public class Assign extends VXMLAction {

	private final String name;
	private final String expr;
	
	public Assign(String name, String expr) {
		this.name = name;
		this.expr = expr;
	}
	
	@Override
	public String toString(boolean prettyPrint) {
		StringBuilder sb = new StringBuilder();
		sb.append("<assign name\"").append(name).append("\" expr=\"").append(expr).append("\"/>");
		return sb.toString();
	}
	
}
