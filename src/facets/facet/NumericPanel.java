package facets.facet;
import static facets.facet.FacetFactory.*;
import facets.core.app.FrameGroup.Proxy;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargeterCore;
import facets.facet.kit.*;
import facets.util.StringFlags;
import facets.util.tree.Nodes;
abstract class NumericPanel extends FacetCore{
	protected final KWrap[]boxes;
	protected final String extraTitle;
	protected final KWrap[]labels;
	protected final STargeter[]links;
	private final StringFlags hints;
	private final int sliderWidth;
	private NumericPanel(STargeter t,StringFlags hints,Toolkit kit){
		this(t,hints,0, kit);
	}
	private NumericPanel(STargeter t,StringFlags hints,int sliderWidth,
			Toolkit kit){
		super(t.target(),kit);
		this.hints=hints;this.sliderWidth=sliderWidth;
		boolean	proxied=t.target()instanceof Proxy;
		STargeter[]elements=t.elements();
		links=elements.length==0||(proxied&&elements.length==1)?
				new STargeter[]{t}:elements;
		if(proxied&&links[0].elements().length>1)
			for(int i=0;i<links.length;i++)links[i]=links[i].elements()[0];
		STarget target=links[0].target();
		SNumeric numeric=(SNumeric)
			(!proxied?target:target.elements()[0]);
		String nudgeTitles[]=numeric.policy().incrementTitles();
		extraTitle=nudgeTitles.length==2||sliderWidth>0?null:nudgeTitles[2];
		labels=true||hints.includeFlag(FacetFactory.HINT_BARE)?null:
			new KWrap[links.length+(extraTitle==null?0:1)];
		boxes=!hints.includeFlag(FacetFactory.HINT_NUMERIC_FIELDS)?null:
			new KWrap[links.length];
		SimpleCore[]numerics=!(labels!=null||boxes!=null)?null
				:new SimpleCore[links.length];
		for(int i=0;i<links.length;i++){
		  if(labels!=null||boxes!=null)numerics[i]=new SimpleCore(links[i],
		  	new Numerics.Field(new StringFlags(FacetFactory.HINT_BARE)),kit);      
		  if(labels!=null)labels[i]=
		  	numerics[i].newRegisteredLabel(numerics[i].title(),hints);
		  if(boxes!=null)boxes[i]=numerics[i].base();
		}
	}
	protected KWrap[]lazyParts(){return null;}
	static SFacet nudging(STargeter t,final StringFlags hints,Toolkit kit){
		return new NumericPanel(t,hints,kit){
			final public KWrap lazyBase(){
				final int nudges=Numerics.Nudger.NUDGES;
				final KWrap[]buttons=new KWrap[links.length*nudges];
		  	int usage=hints.includeFlag(HINT_USAGE_PANEL)?KButton.USAGE_PANEL
		  			:KButton.USAGE_TOOLBAR;
				for(int i=0;i<links.length;i++){
					Numerics.Nudger master=new Numerics.Nudger(usage,hints);
					SimpleCore core=new SimpleCore(links[i],master,kit);
					if(extraTitle!=null)
						labels[labels.length-1]=core.newRegisteredLabel(extraTitle,hints);
					buttons[i*nudges]=master.buttons[Numerics.Nudger.DOWN];
					buttons[i*nudges+1]=master.buttons[Numerics.Nudger.UP];
				}
				return kit.nudgersPanel(this,buttons,boxes,labels,hints);
			}				
		};
	}
	static SFacet sliding(STargeter t,final int sliderWidth,
			final StringFlags hints,Toolkit kit){
		return new NumericPanel(t,hints,sliderWidth,kit){
			final public KWrap lazyBase(){
				final KWrap[]sliderWraps=new KWrap[links.length];
				for(int i=0;i<sliderWraps.length;i++)
					sliderWraps[i]=Numerics.newSlider(links[i],sliderWidth,hints,
						labels==null?null:labels[i],boxes==null?null:boxes[i],
								kit).base();
				return kit.wrapMount(this,sliderWraps,5,2,hints);
			}
		  protected KWrap[]lazyParts(){return null;}
		};
	}
}