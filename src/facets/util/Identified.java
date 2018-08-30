package facets.util;
/**
Specifies a unique identity. 
 */
public interface Identified{
  /**
  Return an object that is as far as possible unique for the implementation. 
  <p>This will usually be a class instance counter.  
   */
  Object identity();
}
