package facets.facet;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.facet.kit.Decoration;
import facets.facet.kit.KWidget;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.shade.Shade;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
/**
Builds and manages Swing panel for a simple facet. 
<p>{@link SwingSimpleMaster} descends from {@link facets.facet.FacetMaster.Simple}
via an internal superclass used by  the {@link FacetFactory} facet builder. 
<p>Custom facet can be created by passing concrete subclasses to
	 {@link facets.facet.FacetFactory#simpleMastered(facets.core.superficial.STargeter,facets.facet.FacetMaster.Simple)}.
*/
public abstract class SwingSimpleMaster extends SimpleMaster{
	private JComponent swingWrappable;
	public final Map<String,JComponent>swingMap=new HashMap();	
	public SwingSimpleMaster(){
		super(StringFlags.EMPTY);		
	}
	/**
	Create a panel facet base for attachment to the GUI. 
	@return a {@link JComponent} which may include others
	 */
	protected abstract JComponent lazySwingWrappable();
	/**
	The component returned by {@link #lazySwingWrappable()}. 
	 */
	final public JComponent facetBaseWrapped(){
		return(JComponent)core.base().wrapped();
	}
	@Override
	public abstract void retargetedSingle(STarget target,Impact impact);
	@Override
	public void retargetedMultiple(STarget[]targets,Impact impact){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	final KWrap lazyBaseWrap(){
		swingWrappable=lazySwingWrappable();
		return new KWidget(){
			public Object wrapped(){
				return swingWrappable;
			}
			public KitFacet facet(){
				return core();
			}
			public Object newWrapped(Object parent){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			public void redecorate(Decoration decorations){
				throw new RuntimeException("Not implemented in "+Debug.info(this));				
			}
			public void setEnabled(boolean enabled){}
			public void setIndeterminate(boolean on){
				throw new RuntimeException("Not implemented in "+Debug.info(this));				
			}
			public Map<String,?>components(){
				return swingMap;
			}
		};
	}
	final KWrap[]lazyPartWraps(){
		return null;
	}
}
