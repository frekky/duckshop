package tk.allele.duckshop.items;

import tk.allele.duckshop.errors.InvalidSyntaxException;
import tk.allele.duckshop.trading.TradeAdapter;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents money as a floating point value.
 */
public class Money extends Item {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4584614856143319254L;

	/**
     * The format for money: a dollar sign, then a floating point number.
     */
    private static final Pattern moneyPattern = Pattern.compile("\\$((\\d*\\.)?\\d+)");

    private final double amount;

    /**
     * Create a new Money instance.
     * <p>
     * The item string is not parsed; it is simply kept so it can be
     * later retrieved by {@link #getOriginalString()}.
     */
    public Money(final double amount, @Nullable final String itemString) {
        super(itemString);
        this.amount = amount;
    }

    /**
     * Create a new Money instance.
     *
     * @throws IllegalArgumentException if the amount is zero.
     */
    public Money(final double amount) {
        this(amount, null);
    }

    /**
     * Parse a Money instance from a String.
     *
     * @throws InvalidSyntaxException if the string cannot be parsed.
     */
    public static Money fromString(final String itemString)
            throws InvalidSyntaxException {
        if (nothingAliases.contains(itemString.toLowerCase())) {
            return new Money(0.0, itemString);
        } else {
            Matcher matcher = moneyPattern.matcher(itemString);
            if (matcher.matches()) {
                double amount = Double.parseDouble(matcher.group(1));
                return new Money(amount, itemString);
            } else {
                throw new InvalidSyntaxException();
            }
        }
    }

    @Override
    public int countAddTo(TradeAdapter adapter) {
        return adapter.countAddMoney(this);
    }

    @Override
    public int countTakeFrom(TradeAdapter adapter) {
        return adapter.countSubtractMoney(this);
    }

    @Override
    public void addTo(TradeAdapter adapter) {
        adapter.addMoney(this);
    }

    @Override
    public void takeFrom(TradeAdapter adapter) {
        adapter.subtractMoney(this);
    }

    /**
     * Get the amount of money.
     */
    public double getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Money) {
            Money other = (Money) obj;
            return (this.amount == other.amount);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // See <http://docs.oracle.com/javase/6/docs/api/java/lang/Double.html#hashCode()>
        long v = Double.doubleToLongBits(amount);
        return (int) (v ^ (v >>> 32));
    }

    @Override
    public String toString() {
        if (amount == 0.0) {
            return nothingAliases.iterator().next();
        } else {
            return "$" + amount;
        }
    }
}
