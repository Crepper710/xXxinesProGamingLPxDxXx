package actions;

public abstract class VXMLAction {
	
	@Override
	public String toString() {
		return this.toString(true);
	}
	
	public abstract String toString(boolean prettyPrint);
	
}
