package apps.hello;

import facets.core.app.AppContenter;
import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost;
import facets.core.app.MenuFacets;
import facets.core.app.MountFacet;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SContenter;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STrigger;
import facets.core.superficial.STrigger.Coupler;
import facets.core.superficial.app.IndexingFrame;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SelectingFrame;
import facets.facet.FacetFactory;
import facets.facet.FacetFactory.AppletFeatures;
import facets.util.Debug;
import facets.util.Util;
import java.io.Serializable;
import java.util.List;
import apps.DemoSurface;

/**
Creates a {@link SSurface} for {@link List} content 
with itself as {@link SContenter}. 
<p>{@link HelloListContenter} extends its superclass 
for use by a private {@link DemoSurface}.
 */
public abstract class HelloListContenter extends HelloContenter{
	
	/**
	{@link IndexingFrame} that exposes actions on its content. 
	 */
	private final class ListFrame extends IndexingFrame {

		//Flag set by edits, accessed by contenter
		boolean hasChanged;

		//Shared coupler for actions on list
		private final Coupler forTriggers = new STrigger.Coupler() {
			
			@Override
			public void fired(STrigger t) {
				
				//Get index, cast reference, get current item
				int index = indexing().index();
				List<Serializable> list = ((List) framed);
				Serializable indexed = list.get(index);
				
				//Respond to suit trigger fired
				if(t == deleteAction) {
					
					//Remove item
					list.remove(indexed);
				}
				else 
				if (t == duplicateAction) {
					
					//Copy, insert and adjust index
					Serializable copy = Util.deserializedCopy(indexed);
					list.add(index++, copy);
				}
				else
				if (t == upAction) {
					
					//Remove, replace at new index
					list.remove(indexed);
					list.add(--index, indexed);					
				}
				else
				if (t == downAction) {
					
					//Remove, replace at new index
					list.remove(indexed);
					list.add(++index, indexed);					
				}

				//Update to match content and index
				int size = list.size();
				if(index == size) index--;
				updateIndexing(list.toArray(), index);		
				
				//Set live states and flag
				setActionLiveStates(size, index);
				hasChanged = true;
			}
		};		

		//Triggers sharing coupler
		private final STarget 
		deleteAction = new STrigger("Delete", forTriggers),
		duplicateAction = new STrigger("Duplicate", forTriggers),
		upAction = new STrigger("Up", forTriggers),
		downAction = new STrigger("Down", forTriggers);

		//Sets all live states based on state of list and indexing
		private void setActionLiveStates(int itemsCount, int index) {
			
			//Create flag
			boolean singleItem = itemsCount == 1;

			//Set action live states to match index, count and flag
			deleteAction.setLive(!singleItem);
			upAction.setLive(!singleItem && index > 0);
			downAction.setLive(!singleItem && index < itemsCount - 1);
		}

		//Set indexing with content and index passed
		private void updateIndexing(Object[] content, int index) {
			setIndexing(new SIndexing("Content", content, index, 
					
					//Coupler updates live states to match index
					new SIndexing.Coupler() {
				
				@Override
				public void indexSet(SIndexing i) {
					
					//Call general method
					setActionLiveStates(i.indexables().length, i.index());
				}
			}));
		}

		//Construct from source list
		ListFrame(String title, List<Serializable> list) {
			 
			//Pass content to superclass
			super(title, list);
			
			//Construct and set indexing
			updateIndexing(list.toArray(), 0);
		}

		//Implement by delegation
		@Override
		protected SFrameTarget newIndexedFrame(Object indexed) {	
			
			//Delegate to enclosing class with cast to content type
			return newSelectionFrame((Serializable) indexed);
		}

		//Reimplementation
		@Override
		protected STarget[] lazyElements() {
			
			//Return actions
			return new STarget[]{deleteAction, duplicateAction, upAction, downAction};
		}
	}

	//Immutable reference		
	private final SFrameTarget contentFrame;

	/**
	Unique constructor. 
	@param list to be exposed in surface; must be {@link Serializable} to
	enable duplication. 
	 */
	protected HelloListContenter(List<Serializable> list){
		
		//Create content frame
		contentFrame = new ListFrame(title(), list);	
	}
			/**
	Implements interface method. 
	@see SContenter#newContentArea(boolean)
	 */
	final public SAreaTarget newContentArea(boolean faceted) {
		
		//Create root
		AreaRoot root = new SContentAreaTargeter.ContentArea(title(),this,
				new STarget[]{contentFrame}) {

			//Standard reimplementation
			@Override
			public STargeter newTargeter(){
				return new SContentAreaTargeter(getClass());
			}
		};
		
		//Attach mount facet and return
		ff.areas().mount(root,true);
		return root;
	}

	//Delegate to enclosing applet
	@Override
	public STarget[] newBasicTargets(){
		return newRootTargets();
	}
	/**
	Return a {@link SFrameTarget} whose {@link STarget} <code>elements</code> 
	represent the selection passed. 
	<p>Called by the private subclass of {@link IndexingFrame} created  
	in the constructor. 
	@param indexed the currently indexed member of the {@link List} 
	framed by {@link #contentFrame}
	 */
	protected abstract SFrameTarget newSelectionFrame(Serializable indexed);

	/**
	Return non-content {@link STarget}s to be exposed by the surface. 
	<p>For use by the {@link SAreaTarget} created in 
	{@link #newContentArea(boolean)}; default returns empty array. 
	 */
	protected STarget[] newRootTargets() {
		return new STarget[]{};
	}

	/**
	Implements interface method. 
	@see apps.DemoSurface.Contenter#newLayout(SHost,SContentAreaTargeter)
	 */
	final public FacetLayout newLayout(SHost host, SContentAreaTargeter area) {

		//Get references
		STargeter 
		list = area.content(),
		selection = area.selection(),
		indexing = ((IndexingFrame.FrameTargeter)area.content()).indexing();
		
		//Create, check and attach panel
		SFacet panel = newPanel(ff, area, list, indexing, selection),
			rootFacet=area.areaTarget().attachedFacet();
		if(panel == null)throw new IllegalStateException(
				"No panel in "+Debug.info(this));
		((MountFacet)rootFacet).setFacets(panel);
		
		//Get any menus
		SFacet[] menus = newMenus(ff, area, list, indexing, selection);
		
		//Create and return appropriate layout
		return ((FeatureHost) host).newLayout(rootFacet, 
				new AppletFeatures(null, menus, false));
	}

	/**
	Return panel with facet attached to the targeter (trees) passed. 
	@param ff passed from superclass
	@param root retargeted on the parent of any targets returned by
	{@link #newRootTargets()}
	@param list retargeted on {@link #contentFrame}
	@param indexing retargeted on the {@link SIndexing} setting the 
	selection 
	@param selection retargeted on the frame returned by 
	{@link #newSelectionFrame(Serializable)} 
	 */
	protected abstract SFacet newPanel(FacetFactory ff, STargeter root, 
			STargeter list, STargeter indexing, STargeter selection);

	/**
	Return menus with facet attached to the targeter (trees) passed. 
	<p>Parameters are as for {@link #newPanel(FacetFactory,STargeter,
	STargeter,STargeter,STargeter)} 
	 */
	protected abstract SFacet[] newMenus(FacetFactory ff, STargeter root, 
			STargeter list, STargeter indexing, STargeter selection);

	/**
	Convenience method for the currently indexed member of the list 
	framed by {@link #contentFrame}. 
	 */
	final public Object selection() {
		
		//Extract reference from frame
		return ((SelectingFrame) contentFrame()).selection().single();
	}
	
	/**
	Implements abstract method. 
	@see SContenter#contentFrame()
	 */
	final public SFrameTarget contentFrame() {
		
		//Return reference
		return contentFrame;
	}

	/**
	Empty implementation of interface method. 
	@see SContenter#areaRetargeted(SContentAreaTargeter)
	 */
	public void areaRetargeted(SContentAreaTargeter area) {}
	
	/**
	Implements interface method with invalid stub. 
	@see apps.DemoSurface.Contenter#getContextFacets()
	 */
	public MenuFacets getContextFacets() {
		throw new RuntimeException("Not implemented in "+this);
	}
}
