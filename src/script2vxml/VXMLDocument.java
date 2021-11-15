package script2vxml;

import java.util.List;

public class VXMLDocument {

	private final List<VXMLAction> preActions;
	private final List<VXMLForm> forms;
	private final List<String> varNames;
	private final String firstID;
	
	public VXMLDocument(List<VXMLAction> preActions, List<VXMLForm> forms, List<String> varNames, String firstID) {
		this.preActions = preActions;
		this.forms = forms;
		this.varNames = varNames;
		this.firstID = firstID;
	}
	
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
		StringBuilder sb = new StringBuilder();
		sb.append("<vxml>").append(nl);
		for (String varName : varNames) {
			sb.append(t).append("<var name=\"").append(varName).append("\"/>").append(nl);
		}
		sb
			.append(t).append("<form id=\"start\">").append(nl)
			.append(t).append(t).append("<block>").append(nl);
		for (VXMLAction action : preActions) {
			for(String line : action.toString(prettyPrint).split(System.lineSeparator())) {
				sb.append(t).append(t).append(t).append(line).append(nl);
			}
		}
		sb
			.append(t).append(t).append(t).append("<goto next=\"#").append(firstID).append("\"/>").append(nl)
			.append(t).append(t).append("</block>").append(nl)
			.append(t).append("</form>").append(nl);
		for (VXMLForm form : forms) {
			for(String line : form.toString(prettyPrint).split(System.lineSeparator())) {
				sb.append(t).append(line).append(nl);
			}
		}
		sb
			.append(t).append("<form id=\"end\">").append(nl)
			.append(t).append(t).append(nl)
			.append(t).append("</form>").append(nl)
			.append("</vxml>");
		return sb.toString();
	}
	
}
