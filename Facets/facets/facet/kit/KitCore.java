package facets.facet.kit;
import static facets.core.app.AppConstants.*;
import static facets.facet.app.FacetConstants.*;
import static facets.util.Util.*;
import facets.core.app.AppConstants;
import facets.core.app.FeatureHost;
import facets.core.app.HideableHost;
import facets.core.app.Dialogs.ExceptionTexts;
import facets.core.app.StatefulViewable.ClipperSource;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifiable;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STextual;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STextual.Coupler;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.facet.AreaFacets.PaneLinking;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.KButton.Type;
import facets.facet.kit.KWrap.ItemSource;
import facets.util.Debug;
import facets.util.NumberPolicy;
import facets.util.StringFlags;
import facets.util.Strings;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.app.ProvidingCache;
import facets.util.tree.DataNode;
import facets.util.tree.ValueNode;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/**
Base implementation of {@link Toolkit}.  
<p>{@link KitCore} is declared <code>abstract</code> despite
providing implementations of all methods from {@link Toolkit}, as these are 
mostly invalid stubs to facilitate definition of partially valid subclasses.   
<p>{@link KitCore} also provides a range of concrete methods for use in subclasses and their helper classes 
</ul>
 */
public abstract class KitCore extends Tracer implements Toolkit{
	public final static Object DECORATIONS_NO_KEY="No key specified";
	public final static class FlashTextNotice extends Notice{
		public FlashTextNotice(Object source,Notifiable monitor){
			super(new STextual(Debug.info(source),"",new Coupler()),Impact.MINI);
			textual().setNotifiable(monitor);
		}
		public void setTextAndNotify(String text){
			STextual t=textual();
			t.setText(text);
			t.notifiable().notify(this);
		}
		public String flashText(){
			return textual().text();
		}
		private STextual textual(){
			return((STextual)sources.get(0));
		}
	}
	public static abstract class TextPaneUpdate{
		private final SSelection selection;
		public TextPaneUpdate(SSelection selection){
			this.selection=selection;
		}
		public final void updatePane(){
			Object selection=this.selection.single();
			String lines[]=selection instanceof DataNode?((DataNode)selection).values()
				:selection instanceof String?new String[]{(String)selection}:null;
			if(lines==null)throw new IllegalStateException("Not text: selection="+Debug.info(selection));
			String text=Strings.linesString(lines);
			updatePaneText(text);
		}
		protected abstract void updatePaneText(String now);
	}
	private final ClassLoader classLoader=getClass().getClassLoader();
	public static int widgets;
	final private Map<String,Object>icons=new HashMap(),disable=new HashMap();
	final private Map<String,String>titles=new HashMap();
	final Map<String,String>rubrics=new HashMap();
	protected final transient Map<String,Integer>keyCodes=new HashMap();
	private ProvidingCache providingCache=new ProvidingCache(50,null);
	public final Object getDecorationIcon(String key,boolean disable){
		if(key==null)throw new IllegalArgumentException("Null key in "+Debug.info(this));
	  key=key.trim();
		Object icon=disable?this.disable.get(key):icons.get(key);
		if(!disable||icon!=null||key.indexOf(".gif")<0||key.endsWith("..."))return icon;
		icon=newFileIcon(key);
		if(icon!=null)icons.put(key.toLowerCase(),icon);
		return icon;
	}
	final public void readDecorationValues(AppValues app){
		if(app==null) {
			Util.printOut("KitCore.readDecorationValues: No app values");
			return;
		}
		Object[][]values=app.decorationValues();
		ValueNode nature=app.nature();
		if(values==null||nature==null)
			throw new IllegalArgumentException("Null values or nature in "+Debug.info(this));
		String iconPath=nature.getString(NATURE_ICON_PATH);
		boolean iconAware=!iconPath.equals("*"),mayCopy;
		File icons=new File(iconPath),store=new File(nature.getString(NATURE_ICON_STORE_PATH));
		mayCopy=false;
		if(mayCopy)try{
			icons.mkdir();
			mayCopy=icons.canWrite()&&store.canRead();
		}catch(Exception e){
			mayCopy=false;
		}
		final String disableDir="disable";
		if(false&&!mayCopy)trace(".readDecorationValues: mayCopy="+mayCopy+
				" icons="+icons.canWrite()+" store="+store.canRead());
		else new File(icons,disableDir).mkdir();
		for(Object[]row:values){
			if(row.length==0)continue;
			String key=row[DECORATION_KEY].toString(),
				title=(String)row[DECORATION_TITLE],
				icon=row.length<=DECORATION_ICON?"":((String)row[DECORATION_ICON]).trim(),
				rubric=row.length<=DECORATION_RUBRIC?"":((String)row[DECORATION_RUBRIC]);
			Integer keyCode=row.length<=DECORATION_KEYCODE?0:(Integer)row[DECORATION_KEYCODE];
			if(key==null||key.equals(""))throw new IllegalArgumentException(
					"Null or empty key in "+Debug.info(this));
			if(!title.equals(""))titles.put(key,title);
			if(!rubric.equals(""))rubrics.put(key,rubric);
			if(keyCode!=0)keyCodes.put(key,keyCode);
			if(!iconAware||icon.equals(""))continue;
			for(Boolean disables:new Boolean[]{false,true}){
				if(disables)icon=disableDir+"/"+icon;
				String iconResource=iconPath+"/"+icon;
				if(key.contains("active"))trace(".readDecorationValues: key="+key+" iconResource=",iconResource);
				URL url=classLoader.getResource(iconResource);
				if(url==null&&mayCopy){
					File storeFile=new File(store,icon);
					if(storeFile.exists())try{
						printOut("Kit.readDecorationValues : ",storeFile);
						copyFile(storeFile,new File(iconResource));
					}catch(Exception e){
						printOut("Kit.readDecorationValues : ",e);
					}
					url=classLoader.getResource(iconResource);
				}
				Map<String,Object>iconStore=disables?disable:this.icons;
				if(url!=null)iconStore.put(key,newDecorationIcon(url));
				else if(true&&!disables)
					printOut("Kit.readDecorationValues : no icon for key="+key+" icon="+icon);
			}
		}
	}
	public final Object getAppIcon(String title){
		Object icon=getDecorationIcon(title,false);
		if(icon==null)icon=getDecorationIcon(AppConstants.NATURE_APP_ICON_LARGE,false);
		return icon!=null?icon:getDecorationIcon(AppConstants.NATURE_APP_ICON_SMALL,false);
	}
	protected Object newDecorationIcon(URL url){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	protected Object newFileIcon(String fileSpec){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	final public Decoration decoration(String title,StringFlags hints){
		return new Decoration(title,this,hints);
	}
	final public Decoration decoration(String title){
		return decoration(title,StringFlags.EMPTY);
	}
	public final String getDecorationText(String title,boolean clean){
		String core=title.replaceAll("(.+)\\Q...\\E$","$1"),
			ellipsis=title.replace(core,""),got=titles.get(core);
		got=(got==null||got.equals("")?core:got);
		if(false&&!ellipsis.equals(""))trace(": clean="+clean+" title="+title+" core="+core+" got="+got);
		return (!clean?got:got.replace("&","").replaceAll("([^|]+).*","$1"))+ellipsis;
	}
	protected Object getDecorationKeyStroke(String title){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public final String decodeTitleText(String text){
		String decoded="";
		for(String line:text.split("\\|"))decoded+=(getDecorationText(line,true));
		return decoded;
	}
	final public KButton button(KitFacet facet,Type type,int usage,
			String titles,StringFlags hints){
		KButton button=newButton(facet,type,usage);
		button.redecorate(decoration(titles,hints));
		return button;
	}
	protected KButton newButton(KitFacet facet,Type type,int usage){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	final public KWrap menu(KitFacet facet,String title,final KWrap[]items,
			StringFlags hints){
		ItemSource itemSource=new ItemSource(){
			public KWrap[]getItems(){
				return items;
			}
		};
		return menu(facet,title,itemSource,hints);
	}
	final public void setCache(ProvidingCache cache){
		this.providingCache=cache;
	}
	final public ProvidingCache providingCache(){
		return providingCache;
	}
	final static java.awt.Point pointAWT(int[]ints){
		final int X=0,Y=1,COORDS=2;
		return new java.awt.Point(ints[X],ints[Y]);
	}
	@Override
	public void adjustMenuMnemonics(KWrap[]items){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public ClipperSource statefulClipperSource(boolean useSystemClipboard){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KList dropdownList(KitFacet facet,boolean asCombo,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KWrap filler(KitFacet facet){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KWrap label(KitFacet facet,String title,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KWrap menu(KitFacet facet,String title,ItemSource itemSource,
			StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KMount switchMount(KitFacet facet,KWrap[]items,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KMount appMultiMount(KitFacet facet,FacetAppSurface app){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KWrap ribbonTab(KitFacet tab, KitFacet[] panels){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KMount rowMount(KitFacet facet,int hgap,int vgap,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public HideableHost newOrphanDialogHost(AppValues values){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KMount spreadMount(KitFacet facet,boolean inset){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KMount packedMount(KitFacet facet,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KTargetable sliderPanel(KitFacet facet,int width,KWrap label,
			KWrap box,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KWrap spacer(KitFacet facet,int width,int height){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KMount splitMount(KitFacet facet,boolean wide,SNumeric ratio){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KField textField(KitFacet facet,int cols,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KField numberField(KitFacet facet,NumberPolicy policy,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KWrap viewerTabs(SFacet[]viewers){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KWrap wrapMount(KitFacet facet,KWrap[]contents,int hgap,int vgap,
			StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KWrap nudgersPanel(KitFacet facet,KWrap[]buttons,KWrap[]boxes,
			KWrap[]labels,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KField colorShader(KitFacet facet,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KMount areaTabs(SFacet[]areas,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KMount paneTabs(SFacet[] areas,SFacet active,KWrap control){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public double layoutFactor(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KList listPane(KitFacet facet,int width,int rows){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KList listPaneMultiple(KitFacet facet,int width,int rows){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KList listPaneChecked(KitFacet facet,int width,int rows){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KMount tabMount(KWrap[]items,String[]titles){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KField textLabel(KitFacet facet,StringFlags hints){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public KViewer masteredViewer(KitFacet facet,ViewerAreaMaster vam,SView view,
			ValueNode stateNode){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public void warningCritical(ExceptionTexts tt,Exception e,boolean inOpen){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public FeatureHost newAppletHost(int width,int height,String name){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public KMount hideMount(KitFacet facet){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public KMount paneLinksGroup(PaneLinking panes){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}
