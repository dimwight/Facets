package apps.svg;
import static applicable.SvgPath.*;
import static applicable.SvgShape.*;
import static facets.core.app.AppConstants.*;
import static facets.util.Doubles.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.AppSurface;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.FeatureHost;
import facets.core.app.PagedContenter;
import facets.core.app.SContenter;
import facets.core.superficial.SIndexing;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPreferences;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.TextLines;
import facets.util.Util;
import facets.util.app.HostBounds;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import applicable.SvgPath;
final class SvgPathView extends FacetAppSpecifier{
	enum ReadValues{
		SF("Significant &Figures","readSigFigs",new Integer[]{3,4,5,6,7,8},4),
		SHIFT("S&hift","readShift",new Integer[]{3,2,1,0,-1,-2,-3},2);
		final public static String TITLE="SVG Read";
		final public String title,key;
		final private Integer[]range;
		final Integer defaultVal;
		private ReadValues(String title,String key,Integer[]range,Integer defaultVal){
			this.title=title;
			this.key=key;
			this.range=range;
			this.defaultVal=defaultVal;
		}
		public SIndexing newIndexing(final ValueNode values){
			return new SIndexing(title,range,(Integer)useValue(values),
			new SIndexing.Coupler(){
				public void indexSet(SIndexing i){	
					values.put(key,(int)(Integer)i.indexed());
				}
			});
		}
		public int useValue(ValueNode values){
			return values.getOrPutInt(key,defaultVal);
		}
	}
	private final File svg;
	static boolean trace=false;
	SvgPathView(){
		super(SvgPathView.class);
		svg=new File(userDir(),"PathView.svg");				
		if(false){
			trace=true;
			for(SvgPath values:findPaths(
					newScaledFileRoot(svg,ReadValues.SF.defaultVal,
							ReadValues.SHIFT.defaultVal),null));
		}
	}
	protected void addNatureDefaults(ValueNode root){
		super.addNatureDefaults(root);
		mergeContents(root,new Object[]{
				HostBounds.NATURE_SIZE_MIN+"=150,150",
				HostBounds.NATURE_SIZER_DEFAULT+"=700,650",
				NATURE_RUN_WATCHED+"=false",
		 });
	}
	@Override
	public void adjustValues(){
		super.adjustValues();
		state().put(PathContenter.KEY_NO_ASK,nature().getBoolean(PathContenter.KEY_NO_ASK));
	}
	@Override
	public PagedContenter[]adjustPreferenceContenters(
			SSurface surface,PagedContenter[]contenters){
		PathContenter content=(PathContenter)((AppSurface)surface).findActiveContent();
		ValueNode working=(ValueNode)content.state.copyState();
		return new PagedContenter[]{
				contenters[FacetPreferences.PREFERENCES_VALUES],
				content.preferences.newReadContenter(working,true),
				content.preferences.newRenderContenter(working),
			};
	}
	@Override
	public boolean isFileApp(){
		return false;
	}
	@Override
	public ContentStyle contentStyle(){
		return ContentStyle.SINGLE;
	}
	@Override
	protected FacetAppSurface newApp(FacetFactory ff, FeatureHost host){
		return new FacetAppSurface(this,ff){
			@Override
			public FileSpecifier[]getFileSpecifiers(){
				return new FileSpecifier[]{file};
			}
			@Override
			public Object getInternalContentSource(){
				return svg;
			}
			@Override
			protected SContenter newContenter(Object source){
				try{
					return new PathContenter((File)source,this);
				}catch(Exception e){
					return findActiveContent();
				}		
			}
		};
	}
	static ValueNode newScaledFileRoot(File file,int sigFigs,int shift){
		TextLines.setDefaultEncoding(true);
		ValueNode svg=new ValueNode(TYPE_SVG,file.getName());
		new XmlDocRoot(svg,XML_POLICY).readFromSource(file);
		ValueNode rule=(ValueNode)descendantTitled(svg,PathContenter.TITLE_RULE);
		if(rule==null)throw new IllegalStateException("No " +PathContenter.TITLE_RULE+" in svg="+svg);
		double x1=rule.getDouble("x1"),x2=rule.getDouble("x2"),
			y1=rule.getDouble("y1"),y2=rule.getDouble("y2");
		boolean equalX=x1==x2,equalY=y1==y2;
		if(!equalX&&!equalY)throw new IllegalStateException("Oblique line in "+rule);
		double ruleLength=Math.abs(equalX?y2-y1:x2-x1),factor=Math.pow(10,shift);
		final int sf=DIGITS_SF=sigFigs;
		final Pattern _value=Pattern.compile("\\-?\\d+\\.?[\\d\\-e]*");
		NodeList paths=new NodeList(new ValueNode(PathContenter.TYPE_PATHS,svg.title()),true);
		paths.clear();
		paths.addAll(descendantsTyped(svg,TYPE_PATH));
		for(TypedNode each:paths){
			if(each.title().equals(TypedNode.UNTITLED))throw new IllegalStateException(
					"No title in "+Debug.info(each));
			ValueNode path=(ValueNode)each;
			String data=path.getString(KEY_DATA),check="",scaled="";
			Matcher m=_value.matcher(data);int dataAt=0;
			for(int valueAt=0;m.find(dataAt);dataAt=m.end(),valueAt++){
				String value=m.group();
				check+=data.substring(dataAt,data.indexOf(value,dataAt))+value;
				scaled+=data.substring(dataAt,data.indexOf(value,dataAt))+
				Util.sf((Double.valueOf(value)-(valueAt==0?x1:valueAt==1?y1:0))
							*factor/ruleLength);
			}
			check+=data.substring(dataAt);scaled+=data.substring(dataAt);
			if(!check.equals(data))throw new IllegalStateException("Bad data check="+check);
			path.put(KEY_SHIFT,shift);
			path.put(KEY_DATA,scaled);
			path.put(KEY_RAW,data);
		}
		DIGITS_SF=sf;
		paths.add(0,rule);
		return false?svg:(ValueNode)paths.parent;
	}
	static SvgPath[]findPaths(TypedNode tree,Map<String,SvgPath>cache){
		Collection<SvgPath>paths=new ArrayList();
		for(TypedNode each:tree.type().equals(TYPE_PATH)?new TypedNode[]{tree}
				:descendantsTyped(Nodes.ancestry(tree)[0],TYPE_PATH)){
			ValueNode values=(ValueNode)each;
			String id=values.title();
			SvgPath path=cache==null?null:cache.get(id);
			if(path==null)path=new SvgPath(values);
			if(cache!=null)cache.put(id,path);
			paths.add(path);
		}
		return paths.toArray(new SvgPath[]{});
	}
	public static void main(String[]args){
		new SvgPathView().buildAndLaunchApp(args);
	}
}
