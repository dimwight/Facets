package applicable.textart;

import static facets.core.app.avatar.PlaneViewWorks.*;
import facets.core.app.PathSelection;
import facets.core.app.SViewer;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.AvatarPolicy;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.DragPolicy;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.SSelection;
import facets.facet.FacetFactory;
import facets.util.ArrayPath;
import facets.util.ItemList;
import facets.util.NumberPolicy;
import facets.util.ValueProxy;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.shade.Shades;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
/**
Avatar painting and drag policies for {@link TextArt} content. 
 */
public class TextArtAvatarPolicies extends AvatarPolicies {

	//Defines possible drag styles 
	enum DragStyle {
		SHIFT {
			public String toString() { return "Shift";}
		},
		TURN{
			public String toString() { return "Turn";}
		},
		SIZE{
			public String toString() { return "Size";}
		}
	}
	
	private final ProvidingCache painterCache=new ProvidingCache(20,null);

	private final NumberPolicy 
		anglePolicy,
		xPolicy, 
		yPolicy;
	private final SIndexing gridSnap;
	private final SToggling limits;
	final double 
		limitWidth,
		limitHeight;

	/**
	Implements interface method. 
	@see facets.core.app.avatar.AvatarView
	 */
	@Override
	public Painter getBackgroundPainter(SViewer viewer, 
			final PainterSource p) {
		
		//Cast view reference
		final PlaneViewWorks linesView = (PlaneViewWorks) viewer.view();
		
		//Get grid size, safe line length, limits flag
		final double 
		gridGap = gridSnap == null || gridSnap.index() == 0? 0 
			:new Integer((String) gridSnap.indexed());
		final double gridLength = linesView.showWidth() + linesView.showHeight() * 50;
		
		//Get limits and grid flags
		final boolean 
		paintLimits=limits != null && limits.isSet();
		final boolean paintGrid=gridGap > 0 && ((TextArtView) linesView).gridShow.isSet();
		
		//Merge into key
		String key=paintLimits+"|"+paintGrid+"|"+gridGap+"|"+gridLength;
		
		//Matching painters in cache?
		Painter[] painters=new ItemProvider<Painter[]>(painterCache,this,
				TextArtAvatarPolicies.class.getSimpleName()){
			@Override
			public CancelStyle cancelStyle(){return CancelStyle.None;}
			@Override
			protected Painter[] newItem(){
				
				//Open list
				ItemList<Painter>list=new ItemList(Painter.class);
				
				//Create and add grid?
				if(paintGrid) 
					list.addItem(newGrid(gridGap, gridLength, p));
				
				//Create and add limits?
				if(paintLimits)
					list.addItems(newLimitsPainters(p));
				
				//Cache and return any painters
				return list.items();
			}
			
		}.getForValues(key);
		return p.bundle(painters);
	}

	/**
	Core constructor. 
	@param xPolicy
	@param yPolicy
	@param anglePolicy
	@param limitWidth
	@param limitHeight
	@param gridSnap
	@param limits
	 */
	public TextArtAvatarPolicies(NumberPolicy xPolicy, NumberPolicy yPolicy, 
			NumberPolicy anglePolicy, double limitWidth, double limitHeight, 
			SIndexing gridSnap, SToggling limits) {
		
		this.anglePolicy=anglePolicy;
		this.xPolicy=xPolicy;
		this.yPolicy=yPolicy;
		this.gridSnap=gridSnap;
		this.limits=limits;
		this.limitWidth=limitWidth;
		this.limitHeight=limitHeight;
	}

	/**
	Convenience constructor. 
	<p>Used for applet. 
	@param xPolicy
	@param yPolicy
	@param anglePolicy
	 */
	public TextArtAvatarPolicies(NumberPolicy xPolicy, NumberPolicy yPolicy, 
			NumberPolicy anglePolicy) {
		this(xPolicy, yPolicy, anglePolicy, -1, -1, null, null);
	}

	/**
	 Implements interface method. 
	 @see facets.core.app.avatar.AvatarPolicies
	 */
	public SSelection newAvatarSelection(SViewer viewer, SSelection viewable) {
		TypedNode[] nodes = ((TypedNode) viewable.content()).children();
		TextArt[] lines = new TextArt[nodes.length];
		for(int i=0;i<lines.length;i++)
			lines [i] = new TextArt((ValueNode) nodes[i]);
		Object[] selected = viewable.multiple();
		if (selected[0] == viewable.content()) 
			return PathSelection.newMinimal(lines);
		ArrayPath[] paths;
		paths=new ArrayPath[selected.length];
		for (int i = 0; i < paths.length; i++)
			paths[i] = new ArrayPath(lines, TextArt.sourceProxy(lines,selected[i]));
		return new PathSelection(lines,paths);
	}

	/**
	Implements interface method. 
	 @see facets.core.app.avatar.AvatarPolicies
	 */
	@Override
	public AvatarPolicy viewerPolicy(SViewer viewer, final AvatarContent content,
									 final PainterSource p) {
		
		//Define, create and return policy
		return new AvatarPolicy() {
		
			//To create and transform painters
			private TextArtPainters linePainters;

			@Override
			public Painter[] newViewPainters(boolean selected, boolean active) {
				
				//Create new painter for possibly changed content
				linePainters = new TextArtPainters((TextArt) content, p);
				
				//Define flag to pass
				boolean showSelection = selected && (active || markInactiveSelection());
				
				//Create and return appropriate painters
				return linePainters.newViewPainters(showSelection);
			}
		
			@Override
			public Painter[] newPickPainters(Object hit, boolean selected) {
				
				//Hit is guaranteed to be represented by a point
				Point hitAt = (Point) hit;
				
				//Create and return appropriate painters
				return linePainters.newPickPainters(hitAt, selected);
			}
		};
	}

	/**
	Should any selection be marked when the view is inactive? 
	 */
	protected boolean markInactiveSelection() {
		return true;
	}

	/**
	 Implements interface method. 
	 <p>Delegates largely to class methods of {@link TextArtDragPolicy}.
	 @see facets.core.app.avatar.AvatarPolicies
	 */
	@Override
	public DragPolicy dragPolicy(AvatarView view, AvatarContent[] content,
			Object hit, PainterSource p) {
		
		//Hit is guaranteed to be represented by a point
		Point hitAt = (Point) hit;
		
		//Which policy to return?
		DragStyle style = new TextArtPainters((TextArt) content[0],
				p).decideDragStyle(hitAt);
		
		//Create and return as appropriate
		return style == DragStyle.TURN ? 
				TextArtDragPolicy.newTurnPolicy(content, p, 
						anglePolicy) 
				: style == DragStyle.SIZE ? 
						TextArtDragPolicy.newSizePolicy(content, p)
						: TextArtDragPolicy.newShiftPolicy(content, p, 
								xPolicy, yPolicy);
	}

	/**
	Called by {@link #getBackgroundPainter(SViewer,PainterSource)}. 
	@param gridGap spacing of gridlines
	@param gridLength length of gridlines
	@param painters from {@link FacetFactory} facet builder
	 */
	private Painter newGrid(double gridGap, double gridLength,
			PainterSource painters) {
			
		//Initialise, create two sets of lines, create grid
		int gridCount = (int) (gridLength / gridGap);
		Line[] gridLines = new Line[gridCount * 2];
		double startX = -gridLength / 2, 
			startY = -gridLength / 2, 
			x = startX, 
			y = startY;
		for (int i = 0; i < gridCount; i++)
			gridLines[i] = new Line(new Point(x += gridGap, y), 
					new Point(x,y + gridLength));
		x = startX;
		for (int i = gridCount; i < gridLines.length; i++)
			gridLines[i] = new Line(new Point(x, y += gridGap),
					new Point(x + gridLength, y));
		
		return painters.backgroundLines(gridLines, Shades.lightGray.brighter());
	}

	/**
	Called by {@link #getBackgroundPainter(SViewer,PainterSource)}. 
	@param p from {@link FacetFactory} facet builder
	 */
	private Painter[] newLimitsPainters(PainterSource p) {
		
		//Calculate dimensions, create start point
		double shrink = .95,
			width = limitWidth * shrink, 
			height = limitHeight * shrink;
		Point startAt = new Point((limitWidth - width) * 3 / 4,
				(limitHeight + height) / 2);
		
		//Create box using utility method
		Line[] lines = newLineSets(startAt, width, height,LINES_SINGLE)[0];
		
		//Create and return painters
		Painter[] limitPainters = new Painter[lines.length];
		for(int i=0;i<limitPainters.length;i++)
			limitPainters[i]=p.line(lines[i], Shades.magenta.resaturated(false), 
					5, false);
		return limitPainters;
	}
}