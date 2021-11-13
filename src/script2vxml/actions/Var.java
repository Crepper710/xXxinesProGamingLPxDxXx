package script2vxml.actions;

import script2vxml.VXMLAction;

public class Var extends VXMLAction {
	
	private final String name;
	
	public Var(String name) {
		this.name = name;
	}

	@Override
	public String toString(boolean prettyPrint) {
		StringBuilder sb = new StringBuilder();
		sb.append("<var name=\"").append(name).append("\"/>");
		return sb.toString();
	}

}
