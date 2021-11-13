package script2vxml.actions;

import script2vxml.VXMLAction;

public class Goto extends VXMLAction {
	
	private final String target;
	
	public Goto(String target) {
		this.target = target;
	}

	@Override
	public String toString(boolean prettyPrint) {
		StringBuilder sb = new StringBuilder();
		sb.append("<goto next=\"#").append(target).append("\"/>");
		return sb.toString();
	}
	
}
