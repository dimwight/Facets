package tones.bar;
import static java.lang.Math.*;
import static tones.Tone.*;
import static tones.bar.Bar.*;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tones.Interval;
import tones.Tone;
import tones.Tone.Dissonance;
import tones.Voice;
public final class Incipit extends Tracer implements Comparable<Incipit>{
	public Incipit(TypedNode tree) {
		String[] split = tree.title().split(" ");
		beatAt=Short.valueOf(split[1]);
		gridAt=Short.valueOf(split[3]);
		for (var forTone:tree.children())
			addTone(new Tone(forTone));
	}

    static class Soundings{
		private final Map<Voice,Tone> soundings;
		private final short beatAt,barBeats;
		private Soundings(short barBeats,short beatAt,
				Map<Voice,Tone> soundings){
			this.beatAt=beatAt;
			this.barBeats=barBeats;
			this.soundings=soundings;
		}
		static Soundings newEmpty(short barBeats){
			return new Soundings(barBeats,(short)0,new HashMap());
		}
		Soundings newUpdated(Incipit i){
			Map<Voice,Tone> nowTones=new HashMap(),nowSoundings=new HashMap();
			for(Tone t:i.tones)
				if(!t.isRest()) nowTones.put(t.voice,t);
			int incipitAt=i.beatAt;
			for(Voice v:Voice.values()){
				Tone now=nowTones.get(v),s=soundings.get(v);
				if(now!=null) nowSoundings.put(v,now);
				else if(s!=null){
					int trim=(incipitAt>0?incipitAt:barBeats)-beatAt;
					s=s.newSounding(trim);
					if(s.beats%barBeats>0) nowSoundings.put(v,s);
				}
			}
			return new Soundings(barBeats,(short)incipitAt,nowSoundings);
		}
		DataNode newDataTree(){
			NodeList nodes=new NodeList(Bars.newDataRoot(getClass(),""//+soundings.size()+" "
					+(false?"":beatAt)),true);
			List<Tone> values=new ArrayList(soundings.values());
			Collections.sort(values,new Comparator<Tone>(){
				@Override
				public int compare(Tone t1,Tone t2){
					return t1.voice.compareTo(t2.voice);
				}
			});
			nodes.parent.setValues(Objects.toLines(values.toArray()).split("\n"));
			return nodes.parent;
		}
	}
	int close(int gridAtNext){
		gridAt=gridAtNext;
		int offset=0;
		for(Tone t:tones)
			offset=max(offset,t.checkBarOffset(this,WIDTH_NOTE));
		rise=6;
		staveGap=10;
		fall=6; 
		if(false&&offset>0)trace(".close: "+this+" +"+offset);
		return gridAt+offset*2/3;
	}
	public final Set<Tone>tones=new HashSet();
	public final short beatAt;
	public int gridAt=-1;
	int rise,staveGap,fall;
	final private Map<Tone,Collection<Dissonance>>againsts=new HashMap();
	private Soundings soundings=Soundings.newEmpty((short) 0);
	Incipit(short beatAt){
		this.beatAt=beatAt;
		if(false)trace(": ",this);
	}
	public void addTone(Tone tone){
		tones.add(tone);
	}
	Soundings readSoundings(Soundings then){
		soundings=then.newUpdated(this);
		return soundings;
	}
	public Soundings soundings(){
		if(soundings==null)
			throw new IllegalStateException("Null soundings in "+this);
		return soundings;
	}
	public Collection<Dissonance>getDissonances(Tone t){
		Set<Dissonance> set=new HashSet();
		for(Voice v:Voice.values()){
			Tone sounding=soundings.soundings.get(v);
			if(v!=t.voice&&sounding!=null){
				Interval i=Interval.between(t,sounding);
				if(i.isDissonant(sounding))
					set.add(new Dissonance(i,sounding));
			}
		}
		return set;
	}
	public int hashCode(){
		return Arrays.hashCode(intValues());
	}
	public boolean equals(Object obj){
		Incipit that=(Incipit)obj;
		return this==that||Arrays.equals(intValues(),that.intValues());
	}
	@Override
	public int compareTo(Incipit i){
		return Integer.valueOf(beatAt).compareTo(Integer.valueOf(i.beatAt));
	}
	private int[]intValues(){
		return new int[]{beatAt,fall,staveGap,rise,gridAt};
	}
	public String toString(){
		return //Debug.info(this)+
				"b "+beatAt+" g "+gridAt
				//+" tones:"+tones.size()
		;
	}
	DataNode newDataTree(){
		List<Tone> sortTones=new ArrayList<>(tones);
		sortTones.sort((t1, t2) -> t1.voice.compareTo(t2.voice));
		NodeList nodes=new NodeList(Bars.newDataRoot(getClass(),toString()),true);
		for(Tone tone:sortTones){
			if(tone.isRest()) continue;
			DataNode add=tone.newDataTree();
			nodes.add(add);
			Collection<Dissonance> got=againsts.get(tone);
			int count=got==null?0:got.size();
			String values=got==null?"":Objects.toLines(got.toArray());
			TypedNode clashes=true?null:Bars.newDataRoot(Dissonance.class,""+count,
					values.split("\n"));
			if(clashes!=null&&clashes.values().length>1)
				Nodes.appendChild(add,clashes);
		}
		if (false) nodes.add(soundings.newDataTree());
		return nodes.parent;
	}
}
