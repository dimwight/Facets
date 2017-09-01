package pdft.inspect;

import static pdft.inspect.PdfContenter.*;
import facets.core.app.HtmlView;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.STextual.Coupler;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.Times;
import facets.util.app.AppValues;
import facets.util.tree.ValueNode;
import pdft.inspect.Texts.TextStyle;

final class PageTextView extends HtmlView.SmartView{
	static final int TARGET_COUNT=0,TARGET_WRAP=1;
	private final STarget codeCount=new STextual("Lines",new Coupler(){
			@Override
			protected String getText(STextual t){
				t.setLive(false);
				return""+lineCount;
			}
		}), 
		wrap;
	final TextStyle style;
	int lineCount;
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
	SSelection newViewerSelection(Texts texts,int pageAt){
		final String raw=texts.newHtml(pageAt,style),lines[]=raw.split("\\s*<p>");
		lineCount=lines.length;
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
					codeCount,
					wrap
				};
			}
		};
	}
}