package facets.facet;
import static facets.facet.FacetFactory.*;
import facets.core.app.MenuFacets;
import facets.core.app.MountFacet;
import facets.core.superficial.Notifying;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.facet.FacetMaster.Simple;
import facets.facet.Indexings.Buttons;
import facets.facet.Indexings.Items;
import facets.facet.Spacer.Filler;
import facets.facet.kit.KButton;
import facets.facet.kit.KField;
import facets.facet.kit.KList;
import facets.facet.kit.KMount;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.StringFlags;
import facets.util.Tracer;
import facets.util.app.AppWatcher;
import facets.util.app.WatcherCoupler;
import facets.util.shade.Shade;
class FacetsCore extends Tracer{
	static final int TOGGLE_CHECK=0,NUMERIC_TEXT=2,
  	NUMERIC_SLIDER=3,NUMERIC_SPINNER=4,NUMERIC_NUDGE=5,TEXTUAL=6,
  	FIRE_TRIGGER=7,
  	COMBO_INDEXING=0,COMBO_TEXTUAL=1,COMBO_TRIGGER=2;
  public final Toolkit kit;
	protected FacetsCore(Toolkit kit){
		if((this.kit=kit)==null)throw new IllegalArgumentException(
				"Null toolkit in "+Debug.info(this));
	}
	/**
	Creates a facet defining a ribbon tab. 
	 */
	public final SFacet ribbonTab(STargeter t,final String title,final SFacet...panels){
		return new FacetCore(t.target(),kit){
			@Override
			KWrap[]lazyParts(){
				return null;
			}
			@Override
			KWrap lazyBase(){
				return kit.ribbonTab(this,Objects.newTyped(KitFacet.class,panels));
			}
			@Override
			public String title(){
				return title;
			}
		};
	}
	/**
	 Creates an area facet defining groups of facets. 
	 @param t will usually be retargeted on a frame
	 @param contents tool-type facets created using {@link FacetFactory}
	 */
	final public SFacet toolGroups(final STargeter t,String hints,final SFacet...contents){
		return new RowPanel(t,kit,contents,0,0,
				new StringFlags((false?HINT_PANEL_MIDDLE:"")+hints));
	}
	/**
	Creates a facet defining a menu for attachment to a menu bar. 
	<p>The menu will check {@link MenuFacets#getFacets()} on each retargeting
	and update its items accordingly. 
	 @param content content supplying menu members and title
	 */
	public final SFacet menuRoot(MenuFacets content){
		return new MenuRoot(content.targeter,kit,content);
	}
	/**
	Creates a facet defining a menu for attachment to a menu bar. 
	<p>Calls {@link #menuRoot(MenuFacets)} with a trivial subclass constructed
	from the parameters. 
	 */
	public final SFacet menuRoot(STargeter t,String title,final SFacet...facets){
		return new MenuRoot(t,kit,new MenuFacets(t,title){
			public SFacet[]getFacets(){
				return facets;
			}
		});
	}
	/**
	 Creates an area facet defining a containing panel.  
	 <p>The panel uses a flow-type layout for each row, creating a new row for
	 each occurrence in <code>contents</code> of class constant <code>BREAK</code>.   
	 @param t will usually be retargeted on a frame
	 @param hgap horizontal gap between panels abstracted by <code>contents</code>
	 @param vgap vertical gap between panels abstracted by <code>contents</code>
	 @param hints one or more <code>Facets HINT</code> constants concatenated 
	 @param contents panel-type facet created using {@link FacetFactory}, 
	 with new rows denoted by <code>BREAK</code> 
	 */
	final public SFacet rowPanel(STargeter t,int hgap,int vgap,
			String hints,SFacet...contents){
		return new RowPanel(t,kit,contents,hgap,vgap,new StringFlags(hints));
	}
	/**
	 Creates an area facet defining
	 a containing panel with special layout behaviour. 
	 <p>The panel uses a flow-type layout for each row, creating a new row for
	 each occurrence in <code>contents</code> of class constant <code>BREAK</code>.   
	 <p>Panels abstracted by <code>contents</code> are aligned left, with
	 zero horizontal and vertical separation.  
	 @param t will usually be retargeted on a frame
	 @param contents panel-type facet created using {@link FacetFactory}, 
	 with new rows denoted by <code>BREAK</code> 
	 */
	final public SFacet rowPanel(STargeter t,SFacet...contents){
		return new RowPanel(t,kit,contents,0,0,StringFlags.EMPTY);
	}
	/**
	Wraps facet arrays in row panels. 
	@param facets the arrays to wrap
	@param t will control all panels
	@param hints can set alignment
	 */
	public final SFacet[]rowPanels(SFacet[][]facets,STargeter t,String hints){
		SFacet[]wraps=new SFacet[facets.length];
		for(int i=0;i<wraps.length;i++)wraps[i]=
			new RowPanel(t,kit,facets[i],0,0,new StringFlags(hints));
		return wraps;
	}
	/**
	 Creates a facet defining a single-line immutable text label based on an {@link SIndexing}.  
	 @param t must be retargeted on a {@link SIndexing} 
	 @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	public final SFacet targetLabel(STargeter t,final String hints){
		final StringFlags flags=new StringFlags(hints);
		return new SimpleCore(t,new SimpleMaster(flags){
			KWrap lazyBaseWrap(){
				return kit.textLabel(core,hints);
			}
			public void retargetedSingle(STarget target,Impact impact){
				((KField)core.base()).setText(
						((KitCore)kit).decoration(target.title(),flags).caption);
			}
			public void retargetedMultiple(STarget[]targets,Impact impact){
				retargetedSingle(targets.length==1?targets[0]:(STarget)targets[0].notifiable(),
						impact);
			}
			KWrap[] lazyPartWraps(){
				return null;
			}
		},kit);
	}
	public final AppWatcher coupleAppWatcher(WatcherCoupler coupler){
		AppWatcher watcher=kit instanceof KitSwing?((KitSwing)kit).watcher:
			new AppWatcher(){
				@Override
				protected boolean checkBusy(){
					return false;
				}};
		if(watcher!=null)watcher.setCoupler(coupler);
		return watcher;
	}
	/**
  Creates a facet enabling the user to define an RGB shade. 
   @param rgb must be retargeted on a {@link SNumeric}, whose value is 
   interpreted as defining a {@link Shade}. 
   */
  final public SFacet colorChooser(STargeter rgb){
		return new SimpleCore(rgb,new Numerics.ColorShader(new StringFlags(HINT_NONE)),
				kit);
	}
	/**
	Creates a facet defining an empty panel to fill spare space in a row panel.
	<p>Used after {@link FacetFactory#BREAK} it fills the bottom of the panel.   
	 */
	final public SFacet fill(){
		return new Filler(kit);
	}
	/**
	Creates a facet defining a sub-menu containing 
	either a single or a pair of iterating action menu items.  
	 @param t must be targeted on an {@link SIndexing}; its coupler defines the items 
	@param hints 
	 */
	final public SFacet indexingIteratorMenu(STargeter t,String hints){
    return new SimpleCore(t,new Indexings.Iterator(new StringFlags(hints)){
      protected boolean forMenu(){return true;}
      protected boolean isMenu(){return true;}
    },kit);
  }
	/**
	Creates a facet defining a pair of iterating pushbuttons.  
	 @param t must be targeted on an {@link SIndexing}; its coupler defines the items 
	 @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	final public SFacet indexingIteratorButtons(STargeter t,String hints){
		return new SimpleCore(t,new Indexings.Iterator(
				new StringFlags(hints+HINT_GRID)),kit);
	}
	/**
	Creates a facet defining either a single or a pair of 
	iterating action menu items. 
	 @param t must be targeted on an {@link SIndexing}; its coupler defines the items 
	 */
	final public SFacet indexingIteratorItems(STargeter t){
    return new SimpleCore(t,new Indexings.Iterator(StringFlags.EMPTY){
      protected boolean forMenu(){return true;}
    }, kit);
  }
	/**
	Creates a facet defining a dropdown list.  
	 <p>Editable behaviour of the selected item can be defined by creating
	 the indexing target with a <code>Facets.ComboCoupler</code>. 
	 @param t must be targeted on an {@link SIndexing}
	 @param hints one or more {@link FacetFactory} <code>HINT</code> 
	 constants concatenated 
	 */
	final public SFacet indexingDropdownList(STargeter t,String hints){
		return new SimpleCore(t,
				new Indexings.PaneSingle(0,0,new StringFlags(hints)),kit);
	}
	/**
	 Creates a facet defining a single-selection list pane.  
	 @param t must be targeted on an {@link SIndexing}
	 @param width pixel width
	 @param rows visible items count
	 @param hints one or more {@link FacetFactory} <code>HINT</code> 
	 constants concatenated 
	 */
	public final SFacet indexingPaneSingle(STargeter t,int width,int rows,String hints){
		return new SimpleCore(t,
				new Indexings.PaneSingle(width,rows,new StringFlags(hints)),kit);
	}
	/**
	 Creates a facet defining a list pane of items selected by checkboxes.  
	 @param t must be targeted on an {@link SIndexing}
	 @param width pixel width
	 @param rows visible items count
	 @param hints one or more {@link FacetFactory} <code>HINT</code> 
	 constants concatenated 
	 */
	public final SFacet indexingPaneChecked(STargeter t,final int width,
			final int rows,String hints){
		return new SimpleCore(t,
				new Indexings.PaneMultiple(width,rows,new StringFlags(hints)) {
					protected KList newListPane(){{
						return toolkit().listPaneChecked(core,width,rows);
					}
				}
		},kit);
	}
	/**
	 Creates a facet defining a multiple-selection list pane.  
	 @param t must be targeted on an {@link SIndexing}
	 @param width pixel width
	 @param rows visible items count
	 @param hints one or more {@link FacetFactory} <code>HINT</code> 
	 constants concatenated 
	 */
	public final SFacet indexingPaneMultiple(STargeter t,final int width,
			final int rows,String hints){
		return new SimpleCore(t,
				new Indexings.PaneMultiple(width,rows,new StringFlags(hints)) {
					protected KList newListPane(){{
						return toolkit().listPaneMultiple(core,width,rows);
					}
				}
		},kit);
	}
	/**
	Creates a facet defining a radio-button sub-menu.  
	@param t must be retargeted on an {@link SIndexing}; its indexables
	will appear in the menu 
	 */
  final public SFacet indexingRadioButtonMenu(STargeter t,String hints){
		STargeter[]elements=t.elements();
		if(elements.length>0&&!(elements[0].target()instanceof SIndexing))
			t=elements[COMBO_INDEXING];
    return new SimpleCore(t,new Items(new StringFlags(hints)){
			protected boolean isMenu(){
				return true;
			}
		},kit);
  }
	/**
	Creates a facet defining a radio-button items group.  
	 @param t must be retargeted on an {@link SIndexing}; its indexables will appear as the
	 items 
  @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	public final SFacet indexingRadioButtonMenuItems(STargeter t,String hints){
		return new SimpleCore(t,new Items(new StringFlags(hints)),kit);
	}
	/**
	Creates a facet defining a ribbon panel with a push-button for each indexable.  
	@param t must be retargeted on an indexing 
	@param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	final public SFacet indexingRibbonButtons(STargeter t,final String hints){
		return new SimpleCore(t,new Buttons(new StringFlags(hints+HINT_INDEXING_SELECT+HINT_GRID)){
		  KWrap lazyBaseWrap(){
				return kit.wrapMount(core(),buttons,0,0,hints);
			}
		},kit);
	}
	/**
	Creates a facet defining a radio-button panel.  
	@param t must be retargeted on an indexing 
	@param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	final public SFacet indexingRadioButtons(STargeter t,final String hints){
		return new SimpleCore(t,new Buttons(new StringFlags(hints)){
		  KWrap lazyBaseWrap(){
				return kit.wrapMount(core(),buttons,0,0,hints);
			}
		},kit);
	}
	/**
  Creates facets each defining a single radio-button.  
  @param t must be retargeted on an {@link SIndexing} 
	 * @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
   */
  final public SFacet[]indexingRadioButtonSingles(STargeter t,String hints){
  	SFacet[]singles=new SFacet[((SIndexing)t.target()).indexables().length];
  	for(int at=0;at<singles.length;at++){
  		final int singleAt=at;
  		singles[at]=new SimpleCore(t,new Buttons(new StringFlags(hints)){
  		  KWrap lazyBaseWrap(){
					return kit.wrapMount(core(),new KWrap[]{buttons[singleAt]},0,0,hints);
  			}
  		},kit);
  	}
  	if(true)return singles;
  	ItemList<SFacet>facets=new ItemList(SFacet.class);
  	if(false)facets.addItems(new SFacet[]{
			targetLabel(t,HINT_BARE),BREAK,
		});
  	for(SFacet single:singles)facets.addItems(new SFacet[]{
  			spacerWide(5),single,BREAK,
		});
  	facets.addItem(fill());
		return facets.items();
  }
	/**
	Creates a facet defining 
	one or more numeric text fields 
	exposing the target of <code>t</code> or those of its elements.  
	@param t must be retargeted on a numeric or a grouping of numerics
	@param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	final public SFacet numericFields(STargeter t,final String hints){
	  STargeter[]elements=t.elements();
		if(elements.length==0)return new SimpleCore(t,
    		new Numerics.Field(new StringFlags(hints)),kit);
		elements=new STargeter[]{t};
		final KWrap[]boxes=new KWrap[elements.length];
		for(int i=0;i<elements.length;i++)
	    boxes[i]=new SimpleCore(elements[i],
	    		new Numerics.Field(new StringFlags(hints)),kit).base();      
		return new FacetCore(t.target(),kit){
			final public KWrap lazyBase(){
				return kit.wrapMount(this,boxes,3,2,new StringFlags(hints+HINT_BARE));
			}
		  protected KWrap[]lazyParts(){return null;}
		};
	}
	/**
	 Creates a facet defining 
	 one or more pairs of nudge buttons 
	 exposing the target of <code>t</code> or those of its elements.  
	 @param t must be retargeted on a numeric or a grouping of numerics
	 @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	final public SFacet numericNudgeButtons(STargeter t,String hints){
		return NumericPanel.nudging(t,new StringFlags(hints),kit);
	}
  /**
	 Creates a facet attached to <code>t</code> defining 
	 a pair of action items.  
	 @param t must be retargeted on a numeric or a grouping of numerics 
	 * @param hints 
	 */
	public final SFacet numericNudgeMenu(STargeter t,String hints){
		return newNudgeMenusOrItems(t,true,new StringFlags(hints))[0];
	}
	/**
	 Creates nudge menu items exposing a numeric.  
	 <p>Facets attached to the <code>elements</code> of <code>t</code> 
	 each define a pair of action items.  
	 @param t must be retargeted on a numeric or a grouping of numerics 
	 * @param hints 
	 */
	final public SFacet[]numericNudgeMenuItems(STargeter t,String hints){
		return newNudgeMenusOrItems(t,false,new StringFlags(hints));
	}
	/**
	 Creates nudge menus exposing a numeric.
	 <p>Facets attached to the <code>elements</code> of <code>t</code> 
	 each define sub-menu containing a pair of action items.  
	 @param t must be retargeted on a numeric or a grouping of numerics 
	 * @param hints 
	 */
	public final SFacet[]numericNudgeMenus(STargeter t,String hints){
		return newNudgeMenusOrItems(t,true,new StringFlags(hints));
	}
	/**
	 Creates a facet defining 
	 one or more sliders 
	 exposing the target of <code>t</code> or those of its elements.  
	 @param t must be retargeted on a numeric or a grouping of numerics
	 @param width the width of each slider 
	 @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	final public SFacet numericSliders(STargeter t,final int width,
			String hints){
		return NumericPanel.sliding(t,width,new StringFlags(hints),kit);
	}
	/**
	 Creates a facet defining an empty panel for use as a vertical spacer. 
	 @param gap the vertical gap required
	 */
	final public SFacet spacerTall(int gap){
		return new Spacer(0,gap,kit);
	}
	/**
	 Creates a facet defining an empty panel for use as a horizontal spacer. 
	 @param gap the horizontal gap required
	 */
	final public SFacet spacerWide(int gap){
		return new Spacer(gap,kit);
	}
	/**
	 Creates a facet mount that can switch its visible contents. 
	 <p>Set the contents with {@link MountFacet#setFacets(SFacet...)}; 
	 and a coupler for the indexing with 
	 {@link #switchMountIndexing(SFacet, facets.core.superficial.SIndexing.Coupler)}. 
	 @param title will be used for the controlling indexing
	 */
	final public MountFacet switchMount(String title){
		return new SwitchMount(title,kit);
	}
	/**
	Creates an indexing setting the facet to be displayed by the facet passed. 
	@param mount must have been created using <code>switchMount</code>.  
	@param coupler is attached to the indexing
	 */
	final public static SIndexing switchMountIndexing(SFacet mount,
			SIndexing.Coupler coupler){
	  if(mount==null||coupler==null)throw new IllegalArgumentException(
	  		"Null mount or coupler");
	  return((SwitchMount)mount).swapReferences(coupler);
	}
	/**
	 Creates a facet that can hide its contents. 
	 */
	final private SFacet hideMount(final STargeter targeter,final SFacet contents,
			final String hints){
		return new FacetCore(targeter.target(),kit){
			private KMount mount;
			KWrap lazyBase(){
				targeter.attachFacet(this);
				mount=kit.hideMount(this);
				mount.setItem(((KitFacet)contents).base());
				return mount;
			}
			public void retarget(STarget target,Impact impact){
				super.retarget(target,impact);
				if(impact==Notifying.Impact.MINI)return;
				hideMountSetHidden(this,!target.isLive());
			};
			KWrap[]lazyParts(){
				return null;
			}
		};
	}
	final public void hideMountSetHidden(SFacet mount,boolean hidden){
		((KMount)((KitFacet)mount).base()).setHidden(hidden);
	}
	final public SFacet tabMount(STargeter targeter,SFacet[]facets,
			final String[]titles){
		if(facets.length!=titles.length)throw new IllegalArgumentException(
				"Bad facet or titles in "+Debug.info(this));
	  final KWrap[]items=new KWrap[facets.length];
	  for(int i=0;i<items.length;i++)
	    items[i]=((KitFacet)facets[i]).base();
		return new FacetCore(targeter.target(),kit){
			KWrap lazyBase(){
				return kit.tabMount(items,titles);
			}
			KWrap[]lazyParts(){return null;}
		};
	}
	/**
	 Creates a facet defining a single-line variable text label.  
	 @param t must be retargeted on a textual 
	 @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	public final SFacet textualLabel(STargeter t,final String hints){
		return new SimpleCore(t,new SimpleMaster(new StringFlags(hints)){
			KWrap lazyBaseWrap(){
				return kit.textLabel(core,hints);
			}
			public void retargetedSingle(STarget target,Impact impact){
				((KField)core.base()).setText(((STextual)target).text());
			}
			public void retargetedMultiple(STarget[]targets,Impact impact){
				retargetedSingle(targets[0],impact);
			}
			KWrap[]lazyPartWraps(){return null;}
		},kit);
	}
	/**
	 Creates a facet defining a toggle button or button panel.  
	 @param t must be retargeted on a toggling or a grouping of togglings 
	 @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	final public SFacet togglingButtons(STargeter t,String hints){
		StringFlags flags=new StringFlags(hints);
		return newLinkMount(t,flags,newLinkFacets(t,new int[]{TOGGLE_CHECK},
						KButton.USAGE_TOOLBAR,flags));
	}
  /**
	Creates a facet defining one or more pushbuttons. 
	@param t must be retargeted on a trigger or a grouping of triggers 
	@param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	final public SFacet triggerButtons(STargeter t,String hints){
		StringFlags flags=new StringFlags(hints);
		int usage=flags.includeFlag(HINT_USAGE_PANEL)?KButton.USAGE_PANEL
				:KButton.USAGE_TOOLBAR;
		return newLinkMount(t,flags,newLinkFacets(t,new int[]{FIRE_TRIGGER},usage,flags));
	}
	private SFacet triggerIcons_(STargeter t,String hints){
		StringFlags flags=new StringFlags(HINT_BARE+HINT_USAGE_ICON);
		return newLinkMount(t,flags,newLinkFacets(t,new int[]{FIRE_TRIGGER},
				KButton.USAGE_ICON,flags));
	}
	final class LinkMount extends FacetCore{
		final SFacet[]facets;
		private final StringFlags hints;
		LinkMount(STarget target,Toolkit kit,SFacet[]facets,StringFlags hints){
			super(target,kit);
			this.hints=hints;
			this.facets=facets;
		}
		protected KWrap lazyBase(){
			final KWrap[]contents=newBaseWraps(facets);
			for(int i=0;i<contents.length;i++)registerPart(contents[i]);
			return kit.wrapMount(this,contents,0,0,hints);
		}
		protected KWrap[]lazyParts(){
			return null;
		}
	}
	private SFacet newLinkMount(STargeter t,final StringFlags hints,SFacet...facets){
		FacetCore core=new LinkMount(t.target(),kit,facets,hints);
		t.attachFacet(core);
		return core;
	}
	private KitFacet[]newLinkFacets(STargeter t,int[]types,int usage,StringFlags hints){
		boolean multiType=types.length>1;
		if(multiType)throw new RuntimeException("Not implemented in "+Debug.info(this));
		STargeter[]elements=t.elements();
		if(elements.length==0)elements=new STargeter[]{t};
		KitFacet[]facets=new KitFacet[!multiType?elements.length:types.length];
		for(int i=0;i<facets.length;i++)facets[i]=newLinkFacet(
				elements[i],types[multiType?i:0],usage,hints);
		return facets;
	}
	private KitFacet newLinkFacet(STargeter t,int type,final int usage,StringFlags hints){
		SimpleMaster master=type==NUMERIC_TEXT?new Numerics.Field(StringFlags.EMPTY)
				:type==TOGGLE_CHECK?new Togglings.Button(usage,hints){
					protected boolean forMenu(){
						return usage==KButton.USAGE_MENU;
					}
				}
				:type==FIRE_TRIGGER?new TriggerButton(usage,hints){
					protected boolean forMenu(){
						return usage==KButton.USAGE_MENU;
					}
				}
				:null;
		if(master==null)throw new IllegalStateException("Null master in "+Debug.info(this));
		return new SimpleCore(t,master,kit);
	}
	/**
	 Creates a facet defining a checkbox or checkbox panel.  
	 @param t must be retargeted on a toggling or a grouping of togglings 
	 @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	final public SFacet togglingCheckboxes(STargeter t,String hints){
		StringFlags flags=new StringFlags(hints);
		return newLinkMount(t,flags,newLinkFacets(t,new int[]{TOGGLE_CHECK},
						KButton.USAGE_PANEL,flags));
	}
  /**
	 Creates a facet defining a checkbox sub-menu.  
	 @param t must be retargeted on a toggling or a grouping of togglings 
	 * @param hints 
	 */
	final public SFacet togglingCheckboxMenu(STargeter t, String hints){
		StringFlags flags=new StringFlags(hints);
		return newMenuItems(t,newLinkFacets(t,new int[]{TOGGLE_CHECK},
				KButton.USAGE_MENU,flags),true,flags);
	}
	/**
	Creates a facet defining an action menu. 
	@param t must be retargeted on a trigger or a grouping of triggers 
	 * @param hints 
	 */
	final public SFacet triggerMenu(STargeter t,String hints){
		StringFlags flags=new StringFlags(hints);
		return newMenuItems(t,newLinkFacets(t,new int[]{FIRE_TRIGGER},
				KButton.USAGE_MENU,flags),true,flags);
	}
	/**
	 Creates a facet defining one or more checkbox menu items.  
	 @param t must be retargeted on a toggling or a grouping of togglings 
   * @param hints 
	 */
	final public SFacet togglingCheckboxMenuItems(STargeter t, String hints){
		StringFlags flags=new StringFlags(hints);
		return newMenuItems(t,newLinkFacets(t,new int[]{TOGGLE_CHECK},
				KButton.USAGE_MENU,flags),false,flags);
	}
  /**
	Creates a facet defining one or more action menu items. 
	@param t must be retargeted on a trigger or a grouping of triggers 
	 */
	final public SFacet triggerMenuItems(STargeter t,String hints){
		return newMenuItems(t,newLinkFacets(t,new int[]{FIRE_TRIGGER},
				KButton.USAGE_MENU,new StringFlags(hints)),false,new StringFlags(hints));
	}
	final public String toString(){
		return Debug.info(this)+"-"+Debug.info(kit);
	}
  private SFacet newMenuItems(final STargeter t,final KitFacet[]facets,
  		final boolean asMenu,final StringFlags hints){
  	return new SimpleCore(t,new SimpleMaster(hints){
      KWrap lazyBaseWrap(){return null;}
  		KWrap[]lazyPartWraps(){
  			KWrap[]parts=new KWrap[facets.length];
  			for(int i=0;i<parts.length;i++)parts[i]=facets[i].base();
  	    return !isMenu()?parts
  	    	:new KWrap[]{kit.menu(core(),t.title(),parts,hints)};
  	  }
  		public void retargetedSingle(STarget target, Notifying.Impact impact){}
  		boolean isMenu(){return asMenu;}
  	},kit);
  }
  private final SFacet[]newNudgeMenusOrItems(STargeter t,final boolean asMenu,
  		StringFlags hints){
		STargeter[]elements=t.elements();
		if(elements.length==0)
			elements=new STargeter[]{t};
		SFacet[]nudgers=new SFacet[elements.length];
		for(int i=0;i<nudgers.length;i++)
			nudgers[i]=new SimpleCore(elements[i],
					new Numerics.Nudger(KButton.USAGE_MENU,hints){
				protected boolean forMenu(){
					return true;
				}
				protected boolean isMenu(){
					return asMenu;
				}
			},kit);
		return nudgers;
	}
  /**
	Creates a simple facet with a custom panel. 
	<p>The master passed is cast to an appropriate base class for the 
	toolkit used by the {@link FacetFactory}. 
	@param t must be retargeted on an appropriate target
	@param master must build a panel capable of exposing a target of <code>t</code>
	 */
	final public SFacet simpleMastered(STargeter t,Simple master){
		return new SimpleCore(t,(SwingSimpleMaster)master,kit);
	}
	/**
	 Creates a facet defining a single-line text field.  
	 @param t must be retargeted on a textual 
	 @param hints one or more {@link FacetFactory} <code>HINT</code> constants concatenated 
	 */
	public final SFacet textualField(STargeter t,int cols,String hints){
		return new SimpleCore(t,new TextualField(cols,new StringFlags(hints)),kit);
	}
	/**
	 Joins two facet arrays.
	 */
	public static final SFacet[]join(SFacet[]front,SFacet[]back){
		return Objects.join(SFacet.class,front,back);
	}
	final static KWrap[]newBaseWraps(SFacet[]facets){
		KWrap[]wraps=new KWrap[facets.length];
		for(int i=0;i<wraps.length;i++)wraps[i]=((KitFacet)facets[i]).base();
		return wraps;
	}
}
