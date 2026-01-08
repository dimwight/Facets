package tones.bar;
import static tones.Voice.*;
import facets.util.Debug;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tones.Tone;
import tones.Voice;
import tones.app.TonesViewable;
import tones.bar.Incipit.Soundings;
public final class Bars extends Tracer implements Titled{
	public static final boolean beatsCheck=false;
	private final List<Bar>bars=new ArrayList();
	private final Map<Voice,VoicePart> parts=new HashMap();
	private final TonesViewable viewable;
	private VoicePart selectedPart= new VoicePart("");
	private short barBeats=32;
	private Soundings soundings=Soundings.newEmpty(barBeats);
	public Bars(TonesViewable viewable, DataNode tree) {
		this.viewable=viewable;
		int barAt = 0;
		for(var barTree:tree.children()){
			List<Incipit>forBar=new ArrayList<>();
			for(var incipitTree:barTree.children()){
				forBar.add(new Incipit(incipitTree));
			}
			bars.add(new Bar(barAt++,
					Collections.unmodifiableList(forBar)));

		}
	}
	public Bars(TonesViewable viewable){
		this.viewable=viewable;
		TypedNode[] children=viewable.contentTree().children();
		for(TypedNode child:children){
			VoicePart part=new VoicePart((String)child.values()[0]);
			parts.put(part.voice,part);
		}
		int barAt=0;
		while(true){
			Bar bar=newPartsBar(barAt++);
			if(bar!=null)bars.add(bar);
			else break;
		}
	}

	private Bar newPartsBar(int barAt){
		Map<Integer,Incipit> incipits=new HashMap();
		for(VoicePart part:parts.values()){
			List<Tone> partTones=part.getBarTones(barAt);
			if(false&&part.voice==Bass)
				trace(".newPartsBar: partTones="+partTones.size()+" barAt="+barAt);
			int barBeatsNow=partTones.isEmpty()?barBeats
					:(beatsCheck?partTones.remove(0):partTones.get(0)).beats;
			if(partTones.isEmpty()) continue;
			if(beatsCheck&&barBeats!=0&&barBeatsNow!=barBeats)
				throw new IllegalStateException("New barBeats="+barBeats
						+", barBeatsNow="+barBeatsNow+" in "+Debug.info(part));
			else if (false)
				barBeats=(short)barBeatsNow;
			if(soundings==null)
				soundings=Soundings.newEmpty(barBeats);
			int beatAt=0;
			for(Tone tone:partTones){
				Incipit i;
				if((i=incipits.get(beatAt))==null)
					incipits.put(beatAt,i=new Incipit((short)beatAt));
				i.addTone(tone);
				beatAt+=tone.beats;
			}
		}
		for(Incipit i:incipits.values())
			soundings=i.readSoundings(soundings);
		List<Incipit>forBar=new ArrayList(incipits.values());
		Collections.sort(forBar);
		return incipits.isEmpty()?null
				:new Bar(barAt, Collections.unmodifiableList(forBar)
		);
	}
	public int barCount(){
		return bars.size();
	}
	public List<Bar> barsFrom(int at){
		return bars.subList(at,bars.size());
	}
	public String title(){
		return viewable.title();
	}
	public DataNode newDataTree(int start, int stop){
		NodeList barsList=new NodeList(newDataRoot(getClass(),title()),true);
		for(Bar bar:barsFrom(start)){
			if(stop>0&&bar.at==stop) break;
			NodeList barList=new NodeList(
					newDataRoot(Bar.class,"at="+bar.at+(true?"":" width="+bar.width)),
					true);
			barsList.add(barList.parent);
			List<Incipit> incipits=new ArrayList<Incipit>(bar.incipits);
			Collections.sort(incipits);
			for(Incipit incipit:incipits)
				barList.add(incipit.newDataTree());
		}
		return barsList.parent;
	}
	static public DataNode newDataRoot(Class type, String title, Object...values){
		return new DataNode(type.getSimpleName(),title,values);
	}
}
