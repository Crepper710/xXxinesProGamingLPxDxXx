package actions;

public class Prompt extends VXMLAction {
	
	private final String text;
	
	public Prompt(String text) {
		this.text = text;
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
		sb
			.append("<prompt>").append(nl)
			.append(t).append(text).append(nl)
			.append("</prompt>");
		return sb.toString();
	}

}
