package pdft.block;
import static pdft.block.PageContent.*;
import static pdft.block.pdfInspect.*;
import facets.core.app.HtmlView;
import facets.core.app.HtmlView.SmartView;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.STextual.Coupler;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.Regex;
import facets.util.Times;
import facets.util.app.AppValues;
import facets.util.tree.ValueNode;
import pdft.block.PageContent.TextStyle;
final class PageTextView extends SmartView{
	static final int TARGET_LINES=0,TARGET_CHARS=1,TARGET_WRAP=2;
	private final Coupler counts=new Coupler(){
		@Override
		protected String getText(STextual t){
			t.setLive(false);
			return""+(t==lines?lineCount:charCount);
		}
	};
	private final STarget lines=new STextual("Lines",counts),
		chars=new STextual("Chars",counts),
		wrap;
	final TextStyle style;
	int lineCount,charCount;
	PageTextView(TextStyle style,AppValues values){
		super(style.title());
		this.style=style;
		final ValueNode nature=values.nature();
		wrap=new SToggling("Wrap &Lines",
				nature.getOrPutBoolean(ARG_WRAP,false),
				new SToggling.Coupler(){
			@Override
			public void stateSet(SToggling t){
				nature.put(ARG_WRAP,t.isSet());
			}
		});
	}
  @Override
	protected void traceOutput(String msg){
		if(false)Times.printElapsed(Debug.info(this)+msg);
		else if(false)super.traceOutput(msg);
	}
	SSelection newHtmlSelection(PageContent page){
		final String raw=page.getHtml(style),lines[]=raw.split("\\s*<p>");
		lineCount=lines.length;
		if(style==TextStyle.Stream)
			charCount=Integer.valueOf(Regex.find(raw,P_CHARS+"\\d+").replace(P_CHARS,""));
		return new SSelection(){
			public Object content(){
				return quickLineHeight()>0?lines:raw;
			}
			public Object[]multiple(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			public Object single(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
  @Override
  public int quickLineHeight(){
  	return style==TextStyle.Stream?17:-1;
  }
  @Override
  public boolean wrapLines(){
		return style==TextStyle.Extracted||((SToggling)wrap).isSet();
  }
	SFrameTarget newFramed(){
		return new SFrameTarget(this){
			@Override
			protected STarget[]lazyElements(){
				return style!=TextStyle.Stream?super.lazyElements():new STarget[]{
					lines,chars,wrap
				};
			}
		};
	}
}