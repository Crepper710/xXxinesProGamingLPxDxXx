public class Main {
	
	public static void main(String[] args) {
		String script = "\"stuff\"" + System.lineSeparator()
				+ "if ne:" + System.lineSeparator()
				+ "\t\"test\"" + System.lineSeparator()
				+ "\t\tif jo:" + System.lineSeparator()
				+ "\t\tend";
	}
	
	public static Option process(String script) {
		
	}
	
	public class Option {
		
		public Option() {
			
		}
		
	}
	
	public static class Action {
		
		private final String action;
		private final ActionType type;
		
		public Action(String action, ActionType type) {
			this.action = action;
			this.type = type;
		}
		
		public enum ActionType {
			
			VAR,
			GOTO,
			PRINT,
			END;
			
		}
		
	}
	
}
