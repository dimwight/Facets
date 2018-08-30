package facets.facet;
import static facets.facet.FacetFactory.*;
import static facets.facet.kit.KButton.*;
import facets.core.superficial.Notifying;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SIndexing.Coupler;
import facets.core.superficial.STargeter;
import facets.facet.FacetFactory.ComboCoupler;
import facets.facet.kit.KButton;
import facets.facet.kit.KButton.Type;
import facets.facet.kit.KList;
import facets.facet.kit.KWidget;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.StringFlags;
final class Indexings{
	static abstract class PaneMultiple extends SimpleMaster{
	  private KList list;
		private String[]thenTitles;
		private int[]thenIndices;
		private final int width,rows;
		PaneMultiple(int width,int rows,StringFlags hints){
	    super(hints);
	    this.width=width;this.rows=rows;
	  }
	  public void attachedToFacet(){
	    core.registerPart(list=newListPane());
	  }
		protected abstract KList newListPane();
		public void retargetedMultiple(STarget[]targets, Notifying.Impact impact){
			Object first=((SIndexing)targets[0]).indexed();
			boolean allSame=true;
			for(int i=0;allSame&&i<targets.length;i++)
				allSame&=((SIndexing)targets[i]).indexed().equals(first);
		  list.setIndeterminate(!allSame);		
			if(!allSame)return;
	  	Coupler coupler=((SIndexing)targets[0]).coupler;
	    String[]titles=coupler.newIndexableTitles((SIndexing)targets[0]);
			boolean newTitles=thenTitles==null||thenTitles.length!=titles.length;
			for(int i=0;!newTitles&&i<titles.length;i++)
				newTitles=!titles[i].equals(thenTitles[i]);
			if(newTitles)list.setTitles(titles);
			int[]indices=((SIndexing)targets[0]).indices();
			boolean newIndices=thenIndices==null||thenIndices.length!=indices.length;
	    if(true||newTitles||newIndices)list.setIndices(indices);
	    thenIndices=indices;thenTitles=titles;
		}
	  void notifyingMultiple(STarget[]targets,Object msg){
		  final SIndexing i=(SIndexing)targets[0];
			int[]indices=list.indices();
			if(i==null)return;
			for(STarget target:targets)
				((SIndexing)target).setIndices(indices);
	  }
		KWrap lazyBaseWrap(){
	    return toolkit().wrapMount(core(),new KWrap[]{list},0,0,hints);
	  }
	  KWrap[]lazyPartWraps(){return null;}
	  public String toString(){return super.toString()+", "+list.indices();}
	}
	final static class PaneSingle extends SimpleMaster{
	  private KList list;
	  private KButton editButton;
	  private boolean isCombo,isPane;
		private String[]thenTitles;
		private int thenIndex=Integer.MAX_VALUE;
		private final int width,rows;
		PaneSingle(int width,int rows,StringFlags hints){
	    super(hints);
	    this.width=width;
	    this.rows=rows;
		}
		public void attachedToFacet(){
	    isCombo=((SIndexing)target()).coupler instanceof ComboCoupler;
	    isPane=rows>0;
	    core.registerPart(list=isPane?toolkit().listPane(core(),width,rows)
	    		:toolkit().dropdownList(core,isCombo,hints));
	    editButton=!isCombo?null:
	    	core.newRegisteredButtons(KButton.Type.Fire,KButton.USAGE_PANEL,
	    			new String[]{"Edit|<"},hints)[0];
	  }
		public void retargetedMultiple(STarget[]targets, Notifying.Impact impact){
			Object first=((SIndexing)targets[0]).indexed();
			boolean allSame=true;
			for(int i=0;allSame&&i<targets.length;i++)
				allSame&=((SIndexing)targets[i]).indexed().equals(first);
		  list.setIndeterminate(!allSame);		
			if(!allSame)return;
	  	Coupler coupler=((SIndexing)targets[0]).coupler;
	    if(isCombo)editButton.setEnabled
				(((ComboCoupler)coupler).indexedTitleEditable((SIndexing)targets[0]));
	    String[]titles=coupler.newIndexableTitles((SIndexing)targets[0]);
	    for(int i=0;i<titles.length;i++)
	    	titles[i]=titles[i].replaceAll("&","");
			boolean newTitles=thenTitles==null||thenTitles.length!=titles.length;
			for(int i=0;!newTitles&&i<titles.length;i++)
				newTitles=!titles[i].equals(thenTitles[i]);
			if(newTitles)list.setTitles(titles);
	    int index=((SIndexing)targets[0]).index();
	    if(newTitles||thenIndex!=index)list.setIndices(new int[]{index});
	    thenIndex=index;thenTitles=titles;
		}
		void notifyingMultiple(STarget[]targets,Object msg){
			if(false)trace(".notifyingMultiple: msg=",msg);
			if(msg==editButton){
			  list.setIndex(list.indices()[0],true);
			  return;
			}
		  final SIndexing i=(SIndexing)targets[0];
	    if(msg instanceof String){
	      ((ComboCoupler)i.coupler).indexedTitleEdited((String)msg);
	      return;
	    }
			int index=list.indices()[0];
			if(false)trace("Dropdown: ",index);
			if(i==null||index<0)return;
			for(int t=0;t<targets.length;t++)((SIndexing)targets[t]).setIndex(index);
	  }
	  public String toString(){return super.toString()+", "+list.indices();}
	  KWrap lazyBaseWrap(){
	    KWrap[]wraps=!isCombo?new KWrap[]{list}:new KWrap[]{list,editButton};
	    Toolkit kit=toolkit();
			return kit.wrapMount(core(),wraps,3,1,hints);
	  }
	  KWrap[]lazyPartWraps(){return null;}
	}
	static abstract class Buttons extends SimpleMaster.Buttons{
		private final boolean noSelection;
		final int usage;
		Buttons(StringFlags hints){
			super(hints);
			noSelection=hints.includeFlag(HINT_INDEXING_SELECT);
			usage=forMenu()?KButton.USAGE_MENU:
				hints.includeFlag(HINT_USAGE_PANEL)?KButton.USAGE_PANEL
					:KButton.USAGE_TOOLBAR;
		}
		public void attachedToFacet(){
			SIndexing i=(SIndexing)target();
			String[]titles=i.coupler.newIndexableTitles(i);
			buttons=core().newRegisteredButtons(noSelection?KButton.Type.Fire
					:KButton.Type.Radio,usage,titles,hints);
		}
		public void retargetedMultiple(STarget[]targets, Notifying.Impact impact){
			SIndexing indexing=(SIndexing)targets[0];
			Object first=indexing.indexed();
			boolean allSame=true;
			for(int i=0;allSame&&i<targets.length;i++)
				allSame&=((SIndexing)targets[i]).indexed().equals(first);
			if(!allSame){
		    for(int b=0;b<buttons.length;b++)for(int t=0;t<targets.length;t++)
		    		if(((SIndexing)targets[t]).index()==b){
							buttons[b].setSelected(true);
							if(false)buttons[b].setIndeterminate(true);
				}		
		    return;
			}
			SIndexing i=(SIndexing)targets[0];
			Coupler coupler=i.coupler;
		  int indexingIndex=i.index();
		  boolean[]liveSettings=i.coupler.liveStates(i);
		  for(int b=0;b<buttons.length;b++){
		    buttons[b].setSelected(!noSelection&&indexingIndex==b);
		    if(liveSettings!=null)buttons[b].setEnabled(liveSettings[b]);
		  }
		}
		void notifyingMultiple(STarget[]targets,Object msg){
			int index=-1;
	  	for(int b=0;b<buttons.length;b++){
				boolean selected=buttons[b]==msg;
				buttons[b].setSelected(selected);
				if(selected)index=b;
			}
			for(int t=0;t<targets.length;t++)((SIndexing)targets[t]).setIndex(index);
	  }
	}
	static class Iterator extends SimpleMaster.Buttons{
		private final static int BACK_OR_ICON=0,FORWARD=1;
		private final boolean forIcon;
	  static KitFacet newIcon(STargeter t,final Toolkit kit){
			return new SimpleCore(t,new Iterator(new StringFlags(HINT_USAGE_ICON+HINT_BARE)),kit);
		}
		Iterator(StringFlags hints){
	  	super(hints);
			forIcon=hints.includeFlag(HINT_USAGE_ICON);
		}
		public void attachedToFacet(){
			SIndexing i=(SIndexing)target();
			String[]titles=i.coupler.iterationTitles(i);
			int usage=forIcon?USAGE_ICON:forMenu()?USAGE_MENU:
				hints.includeFlag(HINT_USAGE_PANEL)?USAGE_PANEL
					:USAGE_TOOLBAR;
			buttons=core.newRegisteredButtons(KButton.Type.Fire,usage,
					forIcon?new String[]{"Icon"}:titles,hints);
		}
		public void retargetedMultiple(STarget[]targets,Impact impact){
			if(forIcon){
				if(targets.length>1)throw new RuntimeException("Not implemented in "+this);
				else buttons[BACK_OR_ICON].setMessage((String)((SIndexing)targets[0]).indexed());
				return;
			}
	    boolean forwardable=false,backwardable=false;
			for(int t=0;t<targets.length;t++){
				SIndexing i=(SIndexing)targets[t];
		    if(i.coupler.canCycle(i))return;
		    boolean live=i.isLive();
		    int index=i.index();
		    backwardable|=live&&index>0;
		    forwardable|=live&&index<i.indexables().length-1;
			}
			buttons[BACK_OR_ICON].setEnabled(backwardable);
			buttons[FORWARD].setEnabled(forwardable);
	  }
		void flashIterate(boolean forward){
			SIndexing.iterate((SIndexing)target(),forward);
			KitSwing.flashButton(buttons[forward&&!forIcon?FORWARD:BACK_OR_ICON]);
		}
		void notifyingMultiple(STarget[]targets,Object msg){
			boolean forward=buttons.length==1||msg==buttons[FORWARD];
			for(int t=0;t<targets.length;t++)SIndexing.iterate((SIndexing)targets[t],forward);
	  }
	  KWrap lazyBaseWrap(){
	    return forMenu()?super.lazyBaseWrap():
	    	toolkit().wrapMount(core(),buttons,0,0,hints);    				
	  }
	}
	static class Items extends Buttons{
	  Items(StringFlags hints){
			super(hints);
		}
	  protected boolean forMenu(){
			return true;
		}
	  void setEnables(STarget target){
			SIndexing i=(SIndexing)(target instanceof SIndexing?target
					:target.elements()[0]);
	  	String[]titles=i.coupler.newIndexableTitles(i);
			KWrap[]items=core().items();
			if(titles.length==0||items.length!=titles.length)return;
			if(false)trace(".setEnables: target=",target);
			if(titles.length>1){
				for(KButton b:buttons)b.setEnabled(target.isLive());
			}
			else if(false){
				((KWidget)items[0]).setEnabled(true);
				buttons[0].setEnabled(false);
			}
		}
		public void retargetedSingle(STarget target,Notifying.Impact impact){
	  	if(buttons==null||impact==Notifying.Impact.MINI)return;
			SIndexing i=(SIndexing)target;
	  	String[]titles=i.coupler.newIndexableTitles(i);
	  	boolean change=titles.length!=buttons.length;
	  	for(int t=0;!change&&t<titles.length;t++)
				change|=!titles[t].equals(buttons[t].message());
	  	if(change){
	  		SimpleCore core=core();
	  		core.deleteParts();
	  		buttons=core.newRegisteredButtons(KButton.Type.Radio,usage,
	  				titles,hints);
	    }
	    super.retargetedSingle(target, impact);
	  }
	}
}
