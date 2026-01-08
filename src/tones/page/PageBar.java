package tones.page;
import facets.util.ItemList;
import java.util.HashMap;
import java.util.Map;
import tones.Clef;
import tones.Tone;
import tones.Voice;
import tones.Mark.ClefMark;
import tones.bar.Bar;
import tones.bar.Incipit;
public class PageBar extends PageItem{
	public final double pageX,pageYs[],staveGap,pageXScale,staveWidth;
	public final Bar content;
	private final Voice selectedVoice;
	private final Map<Voice,Clef>voiceClefs=new HashMap();
	public PageBar(Bar content,double pageX,double pageY,double staveGap,
			double pageXScale,Voice selectedVoice){
		this.content=content;
		this.pageX=pageX;
		this.staveGap=staveGap;
		this.pageXScale=pageXScale;
		this.selectedVoice=selectedVoice;
		staveWidth=content.width*pageXScale;
		pageYs=new double[]{pageY,pageY+STAVE_GRID+staveGap};
		for(Voice v:Voice.values())
			voiceClefs.put(v,Clef.forVoice(v));
	}
	private Clef clefForTone(Tone t){
		Voice v=t.voice;
		Clef clef=voiceClefs.get(v);
		ClefMark m=t.getMark(ClefMark.class);
		if(m==null)return clef;
		voiceClefs.put(v,clef=m.clef);
		return clef;
	}
	public PageItem[]newItems(){
		ItemList<PageIncipit>incipits=new ItemList(PageIncipit.class);
		for(Incipit i:content.incipits)
			incipits.addItem(new PageIncipit(i,pageX,pageXScale));
		ItemList<PageItem>items=new ItemList(PageItem.class);
		items.addItem(this);
		for(PageIncipit incipit:incipits)
			for(Tone t:incipit.content.tones){
				Clef clef=clefForTone(t);
				items.add(new PageNote(this,t,incipit,pageYs[clef.staveAt],clef,
						t.voice==selectedVoice));
			}
		if(false)trace(".newItems: items=",items.size());
		return items.items();
	}
	public String toString(){
		return super.toString()+", pageX="+pageX+", pageY="+pageYs;
	}
}
