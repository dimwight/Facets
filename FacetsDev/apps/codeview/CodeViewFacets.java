package apps.codeview;

import static apps.codeview.CodeViewConstants.*;
import facets.core.app.MenuFacets;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SurfaceServices;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.facet.FacetFactory;
import facets.util.ItemList;


/**
 Extends facet builder API for use by {@link CodeViewContenter}. 
 <p>{@link CodeViewFacets} also implements {@link SurfaceServices} sufficiently
 to supply context menu facets. 
 */
public class CodeViewFacets extends FacetFactory 
		implements SurfaceServices{

	//References to targeter tree elements 
	private final STargeter 
	root,
	external,
	text,
	code,
	imports,
	html,
	javadoc;

	//Menu for external applet host window?
	private final boolean buildExternalMenu;

	//For lazily-created facet
	private MenuFacets contextFacets;

	/**
	Unique constructor. 
	@param ff core facet builder
	@param root retargeted on an {@link SAreaTarget} content root returned by
	{@link CodeViewContenter}
	 */
	public CodeViewFacets(FacetFactory ff, SContentAreaTargeter root) {
		
		//Build superclass from core
		super(ff);		
		
		//Get references to targeter tree elements 
		STargeter elements[] = (this.root=root).elements();

		//Set flag, calculate offsets
		buildExternalMenu = elements.length == 3;		
		int noExternal = buildExternalMenu ? 0 : 1;
		
		//Get references (possibly using offset)
		external = noExternal > 0 ? null : elements[AREA_EXTERNAL];
		text = elements[AREA_TEXT - noExternal];
		code = elements[AREA_CODE - noExternal];
		STargeter[] codeElements = code.elements();		
		imports = codeElements[CODE_IMPORTS];
		javadoc = codeElements[CODE_JAVADOC];
		html = codeElements[CODE_HTML];
	}

	/**
	 Creates content menus. 
	 <p>These may or may not include a menu for an external browser window. 
	 */
	public SFacet[] contentMenuRoots() {
		
		//Create and return menu layouts
		return buildExternalMenu ? 
			new SFacet[]{
				newExternalMenuRoot(),
				menuRoot(newTextMenuFacets()),
				menuRoot(newCodeMenuFacets())
			}
			:new SFacet[]{
				menuRoot(newTextMenuFacets()),
				menuRoot(newCodeMenuFacets())
			};
	}

	/**Creates text menu facets. */
	private MenuFacets newTextMenuFacets() {
		
		STargeter elements[] = text.elements(), 
			size = elements[TEXT_SIZE], 
			tabs = elements[TEXT_TABS], 
			syntax = elements[TEXT_SYNTAX];
		
		//Create menu items
		final SFacet[] items ={
			//togglingCheckboxMenuItems(html, HINT_NONE),
			indexingRadioButtonMenu(size,HINT_NONE), 
			indexingRadioButtonMenu(tabs,HINT_NONE), 
			togglingCheckboxMenuItems(syntax,HINT_NONE)
		};

		//Create and return menu facet
		return new MenuFacets(text, text.title()){
			@Override
			public SFacet[] getFacets() {
				return items;
			}
		};
	}

	/**Creates rendering menu facets. */
	private MenuFacets newCodeMenuFacets() {
		
		//Create menu items
		final SFacet 
			importsItem = togglingCheckboxMenuItems(imports, HINT_NONE),
			docsItem = togglingCheckboxMenu(javadoc, HINT_NONE);
		
		//Create and return menu facet
		return new MenuFacets(code, code.title()){
			@Override
			public SFacet[] getFacets() {
				return new SFacet[]{
						importsItem,
						docsItem,
				};
			}
		};
	}

	/**Creates viewer menu. */
	private SFacet newExternalMenuRoot() {
		
		//Create and return checkbox menu
		return menuRoot(external, "External", new SFacet[]{
			togglingCheckboxMenuItems(external,HINT_NONE)
		});
	}

	/**
	Implements interface method. 
	@see facets.core.app.SurfaceServices#getContextMenuFacets()
	 */
	public MenuFacets getContextMenuFacets() {
		
		//Only create once
		if(contextFacets != null)return contextFacets;
		
		//Open list
		ItemList<SFacet>list=new ItemList(SFacet.class);
		
		//Start with any external trigger
		if(external != null) {
			list.addItem(triggerMenuItems(
					((SContentAreaTargeter)root).viewer().elements()[0], HINT_NONE));		
			list.addItem(BREAK);			
		}
		
		//Standard elements, get array
		list.addItems(newTextMenuFacets().getFacets());
		list.addItem(BREAK);
		list.addItems(newCodeMenuFacets().getFacets());
		final SFacet[]facets =list.items();
		
		//Define, create and return context facet
		return contextFacets = new MenuFacets(root, "Context Menu"){
			@Override
			public SFacet[] getFacets() {
				return facets;
			}
		};
	}

	/**
	Invalid implementation of interface method. 
	@see facets.core.app.SurfaceServices#handleInvalidInput(facets.core.superficial.STarget, java.lang.Object)
	 */
	public void handleInvalidInput(STarget target, Object input) {
		throw new RuntimeException("Not implemented in "+this);		
	}
}