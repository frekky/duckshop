package tk.allele.duckshop.errors;

/**
 * Thrown when a string cannot be parsed, usually in a TradingSign.
 */
public class InvalidSyntaxException extends DuckShopException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1815800979215159539L;

	public InvalidSyntaxException() {
        super();
    }

    public InvalidSyntaxException(String message) {
        super(message);
    }
}
