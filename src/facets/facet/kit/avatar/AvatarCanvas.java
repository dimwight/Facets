package facets.facet.kit.avatar;
import static java.awt.event.MouseEvent.*;
import facets.core.app.PathSelection;
import facets.core.app.SViewer;
import facets.core.app.ViewerTarget;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PickPainter;
import facets.core.app.avatar.Pickable;
import facets.core.superficial.app.SSelection;
import facets.facet.FacetFactory;
import facets.facet.kit.KitFacet;
import facets.facet.kit.KitCore.FlashTextNotice;
import facets.util.ArrayPath;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.OffsetPath;
import facets.util.StringFlags;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.geom.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
abstract class AvatarCanvas extends Tracer{
	interface CanvasHost{
	  void setAndPaintPainters(Painter background,Painter[]viewPainters,
	  		Painter[]motionPainters);
	  PainterSource newPainterSource();
		void launchDnD();
		void setCanvasCursor(int type);
	}
	static final int MOUSE_DRAG_NOTIFY=MOUSE_LAST+1;
	private final class MouseHandler{
  	private final AvatarCanvas canvas=AvatarCanvas.this;
  	private Painter[]painters;
    private Drag drag;	
    private Pick thenPick,lastClick,nullPick;
		boolean handleEvent(int id,Point at,int mods){
		 	if(!canPick())return false;
		  switch(id){
		  case MOUSE_MOVED:
		  	boolean thenPainters=painters!=null;
		    painters=checkAvatarsPick(id,at);
		    if(!thenPainters&&painters==null)return true;
		    else break;
		  case MOUSE_EXITED:restoreDefaults(true);break;
		  case MOUSE_CLICKED:
		    if(thenPick!=null)updateSelection((lastClick=thenPick).picked);
		    painters=checkAvatarsPick(MOUSE_MOVED,at);
		    break;
		  case MOUSE_PRESSED:
			  if(lastClick==null||thenPick==null)return false;
			  Object thenPicked=thenPick.picked;
		  	if(!(thenPicked==lastClick.picked||
		      (thenPicked instanceof Avatar&&((Avatar)thenPicked).isSelected())))
		  		return false;
		    drag=thenPicked instanceof Avatar?mousePressedNewDrag(thenPick,at):null;
		  case MOUSE_DRAGGED:
		    if(drag==null)return false;
		    painters=Objects.newTyped(Painter.class,
					drag.respondToMouse(canvas,at,MOUSE_DRAGGED+mods,painterSource));
		    break;
		  case MOUSE_DRAG_NOTIFY:
		    if(drag!=null)mouseDragEdited(drag.avatars,
		    		drag.respondToMouse(canvas,at,MOUSE_DRAG_NOTIFY,painterSource),
		    		true);
		  	break;
		  case MOUSE_RELEASED:
		    if(drag!=null)mouseDragEdited(drag.avatars,
		    		drag.respondToMouse(canvas,at,id,painterSource),false);
		    restoreDefaults(false);
		    checkAvatarsPick(MOUSE_MOVED,at);
		    break;
		  }
		  host.setAndPaintPainters(background,viewPainters,painters);
			return true;
		}
	  private void mouseDragEdited(Avatar[]dragged,Object[]edits,boolean interim){
	    if(edits==null)return;
	    if(edits.length!=dragged.length)
	    	throw new IllegalArgumentException("Non-matching edits length in "+Debug.info(this));
	    viewer().selectionEdited(null,edits,interim);
	  }
		private Drag mousePressedNewDrag(Pick pick,Point at){
			updateSelection(pick.picked);
	    ItemList<Avatar>selection=new ItemList(Avatar.class);
	    Avatar picked=(Avatar)pick.picked;
			for(Avatar a:avatarPickables)
				if(a.content.equals(picked.content))picked=a;
			selection.addItem(picked);
			for(Avatar a:avatarPickables)
	      if(a.isSelected()&&a!=picked)selection.addItem(a);
			Drag drag=new Drag((AvatarView)viewer().view(),selection.items(),
					at,checkPickables(new Pickable[]{picked},at,hitGap()).hit,
					painterSource);
			if(drag.hasPolicy)return drag;
			restoreDefaults(false);
			return null;
		}
	  private void updateSelection(Object picked){
		  boolean extending=viewer().view().allowMultipleSelection();
		  for(Avatar a:avatarPickables)
		    if(extending&&picked instanceof Avatar)
		    	a.setSelected(a.isSelected()||a==picked);
		    else
		    	a.setSelected(a==picked);
		  SSelection selection;
		  if(picked instanceof Avatar){
		    ItemList<AvatarContent>selections=new ItemList(AvatarContent.class);
			  for(Avatar a:avatarPickables)
		      if(a.isSelected())selections.addItem(a.content);
		  	AvatarContent[]selected=selections.items();
		    OffsetPath[]paths=new OffsetPath[selected.length];
		    for(int p=0;p<paths.length;p++)
		    	paths[p]=new ArrayPath(selected,selected[p]);
		    selection=new PathSelection(selected,paths);
		  }
			else selection=PathSelection.newMinimal(picked);
			viewer().selectionChanged(selection);    
		}
		private boolean canPick(){
			return viewer().isActive()&&viewer().view().isLive();
		}
		void reset(){
			painters=null;
			drag=null;
			thenPick=nullPick;
		}
		private PickPainter[]checkAvatarsPick(int eventType,Point at){
		  if(!canPick()||avatarPickables==null)return null;
		  if(nullPick==null)thenPick=nullPick=Pick.newNullPick(viewer().view());
		  ItemList<Pickable>pickables=new ItemList(Pickable.class);
		  for(Avatar a:avatarPickables)if(a.isSelected())pickables.addItem(a);
		  for(Avatar a:avatarPickables)if(!a.isSelected())pickables.addItem(a);
		  Pick pick=checkPickables(pickables.items(),at,hitGap()),
		  	nowPick=pick!=null?pick:nullPick;
		  if(nowPick.picked==thenPick.picked)
		    return nowPick.respondToMouse(canvas,at,eventType);
		  else{
		    thenPick.respondToMouse(canvas,at,MOUSE_EXITED);
		    return(thenPick=nowPick).respondToMouse(canvas,at,
		    		MOUSE_ENTERED);
		  }
		}
	}
	protected boolean consumeMouseEvent(int id,double atX,double atY,int mods){
	  Point at=new Point(atX,atY);
	  if(flash!=null&&!at.equals(atThen))flash.setTextAndNotify(at.toString());
	  atThen=at;
	  return mouseHandler.handleEvent(id,at,mods);
	}
	protected final KitFacet facet;
  CanvasHost host;
  protected Collection<Avatar>avatarPickables;
  private final MouseHandler mouseHandler=new MouseHandler();
  private final FlashTextNotice flash;
  private Painter viewPainters[],background;
  private PainterSource painterSource;
	private Object atThen;
	AvatarCanvas(KitFacet facet,StringFlags hints){
		this.facet=facet;
		if(!(viewer().view()instanceof AvatarView))throw new IllegalArgumentException(
				"Non-avatar viewer in "+Debug.info(this));
		flash=hints.includeFlag(FacetFactory.HINT_NO_FLASH)?null
				:new FlashTextNotice(this,facet.target());
	}
  @Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	protected void refreshViewPainters(){
		if(false)Times.printElapsed("AvatarCanvas.refreshViewPainters");
		restoreDefaults(false);
	  if(painterSource==null)painterSource=host.newPainterSource();
		SViewer viewer=viewer();
		AvatarPolicies policies=((AvatarView)viewer.view()).avatars();
		List<Avatar>below=new ArrayList(),above=new ArrayList();		
		avatarPickables=new ArrayList();
		SSelection selection=viewer.selection();
		Object[]selected=selection.multiple();
		for(AvatarContent content:(AvatarContent[])selection.content()){
			Avatar add=new Avatar(this,content,policies.viewerPolicy(viewer,content,painterSource));
			if(policies.isContentSelectable(content))avatarPickables.add(add);
	  	for(Object s:selected)if(content==s)add.setSelected(true);
	    if(add.isSelected())above.add(add);
			else below.add(add);
		}
	  ItemList<Painter>viewList=new ItemList(Painter.class);
		for(Avatar add:below)viewList.addItems(add.getPainters());
		for(Avatar add:above)viewList.addItems(add.getPainters());
	  host.setAndPaintPainters(background=policies.getBackgroundPainter(viewer,painterSource),
	  		viewPainters=viewList.items(),null);
		if(false)Times.printElapsed("AvatarCanvas.refreshViewPainters~");
	}
	final Pick checkPickables(Pickable[]pickables,Point at,double hitGap){
		Pick pick=null;
		for(int i=0;i<pickables.length&&pick==null;i++)
		  pick=(Pick)pickables[i].checkCanvasHit(at,hitGap);
		return pick;
	}
	final void restoreDefaults(boolean exiting){
		boolean launchDnD=((AvatarView)viewer().view()).doesDnD()&&exiting&&
			mouseHandler.drag!=null;
		mouseHandler.reset();
		host.setCanvasCursor(SViewer.CURSOR_DEFAULT);
		if(launchDnD)host.launchDnD();
	}
	final SViewer viewer(){
		return((ViewerTarget)facet.target());
	}
	protected double markSize(){
		return((AvatarView)viewer().view()).markPixels();
	}
	protected double hitGap(){
		return((AvatarView)viewer().view()).pickHitPixels();
	}
	protected void paneSizeSet(int width,int height){}
}
