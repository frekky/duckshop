package tk.allele.duckshop.errors;

/**
 * An exception somehow relating to DuckShop.
 */
public class DuckShopException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6972167855015884049L;

	public DuckShopException() {
        super();
    }

    public DuckShopException(String message) {
        super(message);
    }
}
