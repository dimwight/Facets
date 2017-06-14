package facets.core.superficial.app;
import java.io.Serializable;
/**
Selection within application content. 
<p>{@link SSelection} encapsulates the two key features of a 
selection (potentially multiple) within content:
<ul>
<li>the selectable content itself
<li>the currently selected portion(s) of the content
</ul>
<p>Implementors should provide a valid implementation of at least one of
{@link #single()} and {@link #multiple()}; 
instances are intended to be immutable, each defining a specific selection.  
 */
public interface SSelection extends Serializable{
	/**
	The content within which to select. 
	<p>This will generally be the content to display within a viewer; and thus
	itself a portion of some larger content.  
	 */
	Object content();
	/**
	Single object defining the currently selected portion(s) of {@link #content()}. 
	 */
	Object single();
	/**
	Array defining the currently selected portion(s) of {@link #content()}. 
	 */
	Object[]multiple();
}
