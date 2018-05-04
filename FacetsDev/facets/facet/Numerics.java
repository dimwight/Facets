package facets.facet;
import static facets.facet.FacetFactory.*;
import facets.core.superficial.Notifying;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.facet.kit.*;
import facets.util.NumberPolicy;
import facets.util.StringFlags;
final class Numerics{
	final static class ColorShader extends SimpleMaster{
	  private KField field;
	  ColorShader(StringFlags hints){
	  	super(hints);
	  }
	  public void attachedToFacet(){
	  	field=toolkit().colorShader(core(),hints);
	  }
		public void retargetedSingle(STarget target,Notifying.Impact impact){
			field.setValue(((SNumeric)target).value());
	  }
		public void retargetedMultiple(STarget[]targets, Notifying.Impact impact){
			double first=((SNumeric)targets[0]).value();
			boolean allSame=true;
			for(int i=0;allSame&&i<targets.length;i++)
				allSame&=((SNumeric)targets[i]).value()==first;
			if(allSame)field.setValue(first);
			field.setIndeterminate(!allSame);
		}
		protected void notifyingSingle(STarget target,Object msg){
			SNumeric n=(SNumeric)target;
			n.setValue(n.policy().validValue(n.value(),field.value()));
	  }
		KWrap lazyBaseWrap(){return field;}
	  KWrap[]lazyPartWraps(){return null;}
	}
	final static class Field extends SimpleMaster{
	  private KField field;
	  Field(StringFlags hints){
	  	super(hints);
	  }
	  public void attachedToFacet(){
	  	field=toolkit().numberField(core,((SNumeric)target()).policy(),hints);
	  	field.makeEditable();
	  }
		public void retargetedMultiple(STarget[]targets, Notifying.Impact impact){
			double first=((SNumeric)targets[0]).value();
			boolean allSame=true;
			for(int i=0;allSame&&i<targets.length;i++)
				allSame&=((SNumeric)targets[i]).value()==first;
			field.setIndeterminate(!allSame);
			if(allSame)field.setValue(first);
		}
		void notifyingMultiple(STarget[]targets,Object msg){
	  	for(int t=0;t<targets.length;t++){
				SNumeric n=(SNumeric)targets[t];
				n.setValue(n.policy().validValue(n.value(),field.value()));
	  	}
	  }
		KWrap lazyBaseWrap(){return field;}
	  KWrap[]lazyPartWraps(){return null;}
	}
	static class Nudger extends SimpleMaster.Buttons{
		final static int DOWN=0,UP=1,NUDGES=2;
		private final int usage;
	  Nudger(int usage,StringFlags hints){
	  	super(hints);
	  	this.usage=usage;
	  }
	  public void attachedToFacet(){
			String[]titles=((SNumeric)target()).policy().incrementTitles();
	  	buttons=core.newRegisteredButtons(KButton.Type.Fire,usage,titles,hints);
	  }
		public void retargetedMultiple(STarget[]targets, Notifying.Impact impact){
			double first=((SNumeric)targets[0]).value();
			boolean allSame=true;
			for(int i=0;allSame&&i<targets.length;i++)
				allSame&=((SNumeric)targets[i]).value()==first;
			for(int i=0;i<buttons.length;i++)buttons[i].setIndeterminate(!allSame);
		}
		void notifyingMultiple(STarget[]targets,Object msg){
	  	if(msg!=buttons[UP]&&msg!=buttons[DOWN])return;
	  	for(int t=0;t<targets.length;t++){
				SNumeric n=(SNumeric)targets[t];NumberPolicy p=n.policy();
				double value=n.value(),change=p.validIncrement(value,msg==buttons[UP]);
				n.setValue(value+change);
	  	}
	  }
		void setEnables(STarget target){
			STarget[]elements=target.elements();
			SNumeric n=(SNumeric)(core.singleTargetClass.isInstance(target)?target
					:elements.length==1?elements[0]:null);
			if(n==null)return;
			NumberPolicy p=n.policy();
			double value=n.value();boolean live=n.isLive();
	    buttons[UP].setEnabled(live&&p.validIncrement(value,true)!=0);
	    buttons[DOWN].setEnabled(live&&p.validIncrement(value,false)!=0);
	  }
		KWrap lazyBaseWrap(){
			return forMenu()?null:toolkit().wrapMount(core(),buttons,3,3,
					new StringFlags(HINT_GRID+HINT_BARE));
		}
	}
	static FacetCore newSlider(STargeter t,final int sliderWidth,
			final StringFlags hints,final KWrap label,final KWrap box,Toolkit kit){
		return new SimpleCore(t,new SimpleMaster(hints){
			public void retargetedSingle(STarget target,Notifying.Impact impact){
		  	if(impact==Notifying.Impact.MINI)return;
				KWidget widget=(KWidget)core().base();
				widget.setIndeterminate(false);
				((KTargetable)widget).retarget(target,impact);
			}
			public void retargetedMultiple(STarget[]targets,Notifying.Impact impact){
				double first=((SNumeric)targets[0]).value();
				boolean allSame=true;
				for(int i=0;allSame&&i<targets.length;i++)
					allSame&=((SNumeric)targets[i]).value()==first;
				KWidget widget=(KWidget)core().base();
				if(allSame)((KTargetable)widget).retarget(targets[0],impact);
				widget.setIndeterminate(!allSame);
			}
			void notifyingMultiple(STarget[] targets,Object msg){
				double value=((SNumeric)targets[0]).value();
				for(int i=1;i<targets.length;i++)
					((SNumeric)targets[i]).setValue(value);
			}
			KWrap lazyBaseWrap(){
				return toolkit().sliderPanel(core(),sliderWidth,label,box,hints);
			}
			KWrap[]lazyPartWraps(){
				return null;
			}
		},kit);
	}
}
