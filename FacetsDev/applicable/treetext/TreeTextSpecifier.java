package applicable.treetext;
import static facets.facet.app.FacetPreferences.*;
import facets.core.app.FeatureHost;
import facets.core.app.PagedContenter;
import facets.core.app.SContenter;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.util.FileSpecifier;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlPolicy;
import facets.util.tree.XmlSpecifier;
import java.io.File;
public abstract class TreeTextSpecifier extends FacetAppSpecifier{
	final XmlPolicy xmlPolicy=new XmlPolicy(){
		@Override
		protected boolean dataUsesAttributes(){
			return false;
		}
		public XmlSpecifier[]fileSpecifiers(){
			return TreeTextSpecifier.this.fileSpecifiers(xmlPolicy);
		};
	};
	protected int contents;
	public TreeTextSpecifier(Class appClass){
		super(appClass);
	}
	@Override
	public PagedContenter[]adjustPreferenceContenters(SSurface surface,
			PagedContenter[]contenters){
		return false?contenters:new PagedContenter[]{
			contenters[PREFERENCES_TRACE],
			contenters[PREFERENCES_GRAPH],
			contenters[PREFERENCES_VALUES],
			contenters[PREFERENCES_VIEW],
		};
	}
	@Override
	public boolean headerIsRibbon(){
		return args().getOrPutBoolean(ARG_RIBBON,false);
	}
	@Override
	final protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host){
		return new FacetAppSurface(this,ff){
			@Override
			public FileSpecifier[]getFileSpecifiers(){
				return xmlPolicy.fileSpecifiers();
			}
			@Override
			protected Object getInternalContentSource(){
				return((TreeTextSpecifier)spec).getInternalContentSource();
			}
			@Override
			protected SContenter newContenter(Object source){
				return TreeTextSpecifier.this.newContenter(source,this);
			}
		};
	}
	protected XmlSpecifier[]fileSpecifiers(XmlPolicy policy){
		return new XmlSpecifier[]{
			new XmlSpecifier("txt.xml","Text in XML",policy),
		};
	}
	protected Object getInternalContentSource(){
		return false?new File("Test.txt.xml")
				:new ValueNode("xml","Content"+contents++,new Object[]{
						new ValueNode("TextTree",new Object[]{
							new ValueNode("TextLine",new Object[]{"First line"}),
							new ValueNode("TextLine",new Object[]{"Second line"})})});
	}
	protected TreeTextContenter newContenter(Object source,FacetAppSurface app){
		return new TreeTextContenter(source,app){};
	}
}
