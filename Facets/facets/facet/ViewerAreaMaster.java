package facets.facet;
import static facets.facet.FacetFactory.*;
import facets.core.app.TypeKeyable;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.FacetMaster.Viewer;
import facets.facet.kit.swing.ViewerMaster;
import facets.util.StringFlags;
import facets.util.Util;
/**
	Parameter object for viewer creation methods in {@link AreaFacets}. 
	<p>{@link ViewerAreaMaster} has several roles. It 
<ul>
	<li>encapsulates parameters required when
	creating viewer facets and attaching them to the area target tree. 
	<li>can supply a custom {@link ViewerMaster}
<li>can supply a closure that creates a local viewer toolbar.
<li>defines text suitable for use in state keys
	</ul>	 
	*/
	public abstract class ViewerAreaMaster implements TypeKeyable{
		private final String typeKey=newTypeKey(this);
		public final String newTypeKey(Object keyable){
			if((keyable instanceof String)){
				String text=(String)keyable;
				if(text.length()>0)return text;
				else keyable=this;
			}
			return Util.shortTypeNameKey(keyable);
		}
		public final StringFlags hints(){
			return new StringFlags(hintString());
		}
		/**
		May return custom {@link FacetMaster.Viewer}. 
		<p>Default returns <code>null</code>, signalling that type be determined by 
		the surface builder based on view. 
		<p>Non-<code>null</code> returns should be of the 
	 extension class for the builder.
		 */
		public Viewer viewerMaster(){
			return null;
		}
		public String typeKey(){
			return typeKey;
		}
		/**
		Return concatenated hint strings. 
		<p>Called from {@link #hints()} 
		@return by default {@link FacetFactory#HINT_NONE}. 
		 */
		protected String hintString(){
			return HINT_NONE;
		}
		/**
		May return custom local toolbar.
		@param viewTargeter targeting the {@link ViewerTarget#viewFrame()}
		@return <code>null</code> by default
		 */
		protected SFacet newViewTools(STargeter viewTargeter){
			return null;
		}
		final ViewerAreaMaster[]childMasters(SAreaTarget area){
			final ViewerAreaMaster parent=this;
		  SAreaTarget[]areaChildren=(SAreaTarget[])area.indexableTargets();
		  ViewerAreaMaster[]childMasters=new ViewerAreaMaster[areaChildren.length];
		  for(int i=0;i<areaChildren.length;i++){
		  	ViewerAreaMaster master=newChildMaster(areaChildren[i]);
		  	childMasters[i]=master!=null?master:
					new ViewerAreaMaster(){
						public Viewer viewerMaster(){
							return parent.viewerMaster();
						}
						public String typeKey(){
							return parent.typeKey();
						}
						protected String hintString(){
							return parent.hintString();
						}
						protected SFacet newViewTools(STargeter viewTargeter){
							return parent.newViewTools(viewTargeter);
						};
			  	};
		  }
			return childMasters;
		}
		/**
		May return specific master for the area passed. 
		@return by default  <code>null</code>.
		 */
		protected ViewerAreaMaster newChildMaster(SAreaTarget child){
			return null;
		}
	}