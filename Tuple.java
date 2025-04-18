/**
 * Represents a pair of values of possibly different types.
 * <br><br>
 * A simple data structure to hold two related values together.
 * This class is generic so it can hold two objects of different types.
 * 
 * @param <X> The type of the first value
 * @param <Y> The type of the second value
 */
public class Tuple<X, Y> {
  /** The first element of the tuple */
  public final X x;
  /** The second element of the tuple */
  public final Y y;

  /**
   * Constructs a new tuple with the specified values.
   * 
   * @param x The first value
   * @param y The second value
   */
  public Tuple(X x, Y y) {
    this.x = x;
    this.y = y;
  }
}