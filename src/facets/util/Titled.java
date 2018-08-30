package facets.util;
/**
	Has a single-line, human-readable identifying text. 
	<p>The text may be shared with other objects to indicate
	common origin or purpose, or used as a persistable unique identifier.  
	<p>(<b>Note</b> A <i>title</i> is of course the same as a 
	<i>label</i> or <i>caption</i>; the distinct term was originally devised to 
	avoid collision in code with these widely-used terms). 
 */
public interface Titled{
  /**
	Return human-readable identifying text. 
	@return non-<code>null</code>, non-empty string with no structural whitespace   
   */
  String title();
}
