package applicable.treecode;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.tree.TypedNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**
Defines the {@link TreeCodeType}s available to a {@link TreeCoded} framework.
 */
public abstract class TreeCodeTypes extends Tracer{
	protected abstract TreeCodeType[]types();
}