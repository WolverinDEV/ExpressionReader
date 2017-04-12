package dev.wolveringer.jee;

public class ExpressionException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public static class FunctionExpressionException extends ExpressionException {
		private static final long serialVersionUID = 1L;

		public FunctionExpressionException(String message) {
			super(message);
		}
	}
	
	public ExpressionException(String message) {
		super(message);
	}
}
