package facets.util.shade;
/**
Has a {@link Shade} that can be read and set. 
<p>{@link Shaded} allows the colour of an object to be to read and set 
independently of its implementation type.   
 */
public interface Shaded{
  /**
  The current colour of the object as a {@link Shade}. 
   */
  Shade shade();
  /**
  Set the current colour of the object as a {@link Shade}. 
  @param shade must be non-<code>null</code> (to preserve contract semantics)
   */
  void setShade(Shade shade);
}