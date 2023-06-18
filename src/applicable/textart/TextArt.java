package applicable.textart;
import static applicable.textart.TextArtConstants.*;
import static facets.util.tree.TypedNode.*;
import facets.core.app.Fonted;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerContenter.ContentSource;
import facets.core.app.avatar.AvatarContent;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.Util;
import facets.util.ValueProxy;
import facets.util.geom.Angle;
import facets.util.shade.Shade;
import facets.util.shade.Shaded;
import facets.util.shade.Shades;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;

/**
 Content type for spike application.  
 <p>{@link TextArt} defines a line of text that can be drawn at a specified
 position and angle, in a specified font and colour. 
 <p>Constructed from a {@link ValueNode} that can be exposed by a 
 tree-type structure viewer. 
 */
public final class TextArt extends ValueProxy implements Fonted, AvatarContent{	
	
	//Value keys
	private static final String KEY_ANGLE = "angle",
		KEY_X = "x",
		KEY_Y = "y",
		KEY_TEXT = "text",
		KEY_SHADE_RGB = "shadeRGB",
		KEY_SHADE_TITLE = "shadeTitle",
		KEY_FONT_FACE = "fontName",
		KEY_FONT_SIZE = "fontSize",
		KEY_FONT_BOLD= "fontBold",
		KEY_FONT_ITALIC = "fontItalic",
		KEY_CHANGE_UNMERGEABLE= "Unmergeable";
	final static String KEY_UNCHANGED = "No change";

	public TextArt(ValueNode node) {
		super(node);
	}

	/**
	Returns a key specifying the value difference between two {@link TextArt}s. 
	<p>Returns {@link #KEY_UNCHANGED} if no change is found, otherwise a private
	key interpretable by {@link #stripToMergeableValue(String)}. 
	<p>(Used for edit storage by {@link TextArtViewable}.)
	@param other to compare for change
	 */
	String valueChangeKey(TextArt other) {
		
		//Return appropriate key
		return 
			//Moved by mouse?
			atX() != other.atX() && atY() != other.atY() ? KEY_CHANGE_UNMERGEABLE
					
			//Must be facet move
			: atX() != other.atX() ? KEY_X
			: atY() != other.atY() ? KEY_Y
					
			//Could be either!
			: atAngle() != other.atAngle() ? KEY_ANGLE
			
			//Any other edit
			: !sourceNode().stateEquals(other.source) ? KEY_CHANGE_UNMERGEABLE
					
			//Not edited
			: KEY_UNCHANGED;
	}

	ValueNode sourceNode(){
		return(ValueNode)source;
	}

	/**
	Prepares for testing by {@link #changeValuesMergeable(TextArt)}. 
	<p>(Used for edit storage by {@link TextArtViewable}.)
	@param key was returned by {@link #valueChangeKey(TextArt)}
	 */
	void stripToMergeableValue(String key) {
		
		if(key==null)throw new IllegalArgumentException("Null key in "+Debug.info(this));
		else if (key == KEY_UNCHANGED || key == KEY_CHANGE_UNMERGEABLE) return; 
		
		ValueNode sourceNode = sourceNode();
		
		//Get mergeable value to keep, do sanity check
		Object value = null;
		if(key == KEY_X || key == KEY_Y) value = sourceNode.getInt(key);
		else if(key == KEY_ANGLE)value = sourceNode.getInt(key);
		if(value==null)throw new IllegalStateException("Null change in "+Debug.info(this));
		if(false)Util.printOut("TextLine.stripToMergeableValue: ",Debug.info(value));
		
		//Remove all values, restore mergeable value 
		sourceNode.setValues(new Object[]{});
		sourceNode.put(key, value);
		if(false)Util.printOut("~TextLine.stripToMergeableValue: ",sourceNode);
	}

	/**
	Checks to see if change values can be merged. 
	<p>(Used for edit storage by {@link TextArtViewable}.)
	@param other for checking
	 */
	boolean changeValuesMergeable(TextArt other){
		
		//Not values for same content line?
		if (!sourceNode().title().equals(other.sourceNode().title())) return false;
		
		//Get values as keypairs
		String[] these=sourceNode().values(),
		  those=other.sourceNode().values();
		
		//Obviously wrong?
		if (these.length > 1 || those.length > 1) return false;
		
		//Strip out keys
		String findKey="([^=]+)=.*",
			thisKey=these[0].replaceAll(findKey,"$1"),
			thatKey=those[0].replaceAll(findKey,"$1");
		
		//Return equality
		return thisKey.equals(thatKey);
	}

	/**
	Remaining methods retrieve and set properties. 
	 */
	public String text() {
		return sourceNode().getString(KEY_TEXT);
	}

	public void setText(String text) {
		if (text().equals(text)) return;
		sourceNode().put(KEY_TEXT, text);
	}

	public int atX() {
		int value=sourceNode().getInt(KEY_X);
		if(value==ValueNode.NO_INT)throw new IllegalStateException(
				"No value in"+Debug.info(this));
		return value;
	}

	public int atY() {
		int value=sourceNode().getInt(KEY_Y);
		if(value==ValueNode.NO_INT)throw new IllegalStateException(
				"No value in"+Debug.info(this));
		return value;
	}

	public void setAt(int x, int y) {
		if (false&&atX() == x && atY() == y) return;
		sourceNode().put(KEY_X, x);
		sourceNode().put(KEY_Y, y);
	}

	public int atAngle() {
		int value=sourceNode().getInt(KEY_ANGLE);
		if(value==ValueNode.NO_INT)throw new IllegalStateException(
				"No value " +value+" in"+Debug.info(this));
		return value;
	}

	public void setAngle(int degrees) {
		if (false&&atAngle() == degrees) return;
		if(false)Util.printOut("TextLine.setAngle: ", Debug.info(this)+
				" "+degrees);
		sourceNode().put(KEY_ANGLE, degrees);
	}

	@Override
	public String fontFace() {
		return sourceNode().getString(KEY_FONT_FACE);
	}

	@Override
	public void setFontFace(String fontFace){
		if (fontFace() == fontFace) return;
		sourceNode().put(KEY_FONT_FACE, fontFace);
	}

	@Override
	public int fontSize() {
		int value=sourceNode().getInt(KEY_FONT_SIZE);
		if(value==ValueNode.NO_INT)throw new IllegalStateException(
				"No value in"+Debug.info(this));
		return value;
	}

	@Override
	public void setFontSize(int fontSize) {
		if (false&&fontSize() == fontSize) return;
		sourceNode().put(KEY_FONT_SIZE, fontSize);
	}

	@Override
	public boolean fontIsBold() {
		return sourceNode().getBoolean(KEY_FONT_BOLD);
	}

	@Override
	public void setFontBold(boolean bold) {
		if (fontIsBold() == bold) return;
		sourceNode().put(KEY_FONT_BOLD, bold);
	}

	@Override
	public boolean fontIsItalic() {
		return sourceNode().getBoolean(KEY_FONT_ITALIC);
	}

	@Override
	public void setFontItalic(boolean italic) {
		if (fontIsItalic() == italic) return;
		sourceNode().put(KEY_FONT_ITALIC, italic);
	}

	@Override
	public Shade shade() {
		return new Shade(sourceNode().getInt(KEY_SHADE_RGB),
				sourceNode().getString(KEY_SHADE_TITLE));
	}

	@Override
	public void setShade(Shade shade) {
		if (shade() == shade) return;
		sourceNode().put(KEY_SHADE_RGB, shade.rgb());
		sourceNode().put(KEY_SHADE_TITLE, shade.title());
	}
	
	/**
	 Unique constructor. 
	 @param text text string to be drawn
	 @param atX X start position
	 @param atY Y start position
	 @param atAngle rotation of line in degrees
	 @param shade colour of line
	 @param fontFace name of font
	 @param fontSize size of font
	 @param fontBold contentStyle of font
	 @param fontItalic contentStyle of font
	 @param nodeTitle 
	 */
	public TextArt(String text, int atX, int atY, int atAngle,
			Shade shade, String fontFace, int fontSize, boolean fontBold,
			boolean fontItalic, String nodeTitle) {
		
		//Create loaded node with instance count
		this(new ValueNode("TextLine", !nodeTitle.equals("")?nodeTitle:UNTITLED
				, new Object[]{
				KEY_TEXT + "=" + text,
				KEY_X + "=" + atX,
				KEY_Y + "=" + atY,
				KEY_ANGLE + "=" + atAngle,
				KEY_FONT_FACE + "=" + fontFace,
				KEY_FONT_SIZE + "=" + fontSize,
				KEY_FONT_BOLD + "=" + fontBold,
				KEY_FONT_ITALIC + "=" + fontItalic,
				KEY_SHADE_RGB + "=" + shade.rgb(),
				KEY_SHADE_TITLE + "=" + shade.title(),				
		}));
	}

	/**
	Creates default {@link TextArt} content.  
	@see facets.core.app.ViewerContenter.ContentSource
	 */
	public final static ViewerContenter.ContentSource LINES_SOURCE = 
		new ViewerContenter.ContentSource() {		

		//Instance counters
		private int jumbles, lineSets;

		//Create a line with jumbled properties
			private TextArt newJumbleLine(String text) {
				return new TextArt(text, 
						atXs[jumbles % atXs.length],
						atYs[jumbles % atYs.length], 
						toDegrees(atRadians[jumbles % atRadians.length]),
						SHADES[shadesAt[jumbles % shadesAt.length]], 
						FONT_FACES[facesAt[jumbles % facesAt.length]],
						FONT_SIZES[sizesAt[jumbles % sizesAt.length]], 
						bolds[jumbles % bolds.length],
						italics[jumbles % italics.length],
						"#" + jumbles++);
			}

			//Implement interface method
			public Object newContent() { 

				//Create three (probably jumbled) text line objects
				boolean jumble = true;
				TextArt line0 = jumble ? newJumbleLine(texts[0])
							: new TextArt(texts[0],
						50,
						70,
						toDegrees(-.2),
						Shades.red,
						FONT_FACES[1],
						FONT_SIZES[3],
						true, false,""),
						
					line1 = jumble ? newJumbleLine(texts[1])
							: new TextArt(texts[1],
						70,
						100,
						toDegrees(.2),
						Shades.blue,
						FONT_FACES[0],
						FONT_SIZES[4],
						false, true,""),
						
					line2 = jumble ? newJumbleLine(texts[2])
							: new TextArt(texts[2],
						100,
						170,
						0,
						Shades.magenta,
						FONT_FACES[2],
						FONT_SIZES[2],
						false, false,""),
						
						lineTest = new TextArt(texts[0],
							5,
							9,
							0,
							Shades.red,
							FONT_FACES[1],
							FONT_SIZES[3],
							true, false,"");
			
				//Create line set with children
				ValueNode lineSet = new ValueNode("lines", "Lines" + (++lineSets), 
						new DataNode[]{
					line0.sourceNode(), line1.sourceNode(), line2.sourceNode()
				});
				
				boolean minimal=true;
				//Add opening constraints settings
				lineSet.setValues(new Object[]{
						KEY_ANGLE_SNAP + "=" + ANGLE_SNAPS[minimal?0:1],
						KEY_GRID_SNAP + "=" + GRID_SNAPS[minimal?0:1],
						KEY_LIMITS + "=" + !minimal,
						KEY_LIMIT_WIDTH + "=300",
						KEY_LIMIT_HEIGHT + "=200"
				});
			
				//Return node
				return lineSet;
			}
			
		//(Non-jumblable) texts
		final String[] texts = {
				"The quick brown fox",
				"jumped over the lazy dog",
				"And the cow...?"
		};
		
		//Values for jumbling
		final int[] atXs = {
				50, 
				70,
				100,
				90,
				40,
				80, 
				60, 
				100,
				70, 
			},
			atYs = {
				70,
				100,
				170,
				60,
				100, 
				70, 
				140,
				90, 
				110,
				130, 
				80,
			},
		facesAt = {
				1, 
				0, 
				2, 
				0, 
				1, 
				1, 
				2				
			},
		sizesAt = {
				2, 
				3, 
				0,				
				4, 
				5, 
				1, 
				6, 
			},
		shadesAt = {
				0,
				2, 
				1,
				3, 
				0,				
				4, 
				1, 
				3,
				2,
				0,
			};
		final double[] atRadians = {
				-.2, 
				.2, 
				0, 
				-.4,
				.1,
				-.5,
				0.4,
				.3,
				.5
			};
		final boolean[] bolds = {
				true, 
				false,
				false,
				true,
				false,
			},
			italics = {
				false, 
				true, 
				false,
				true, 
			};

		private int toDegrees(double radians){
			return (int)(radians/Math.PI*180);
		}
	};

	@Override
	protected Object[] lazyValues(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}

	public static ValueNode[]getProxySourceNodes(Object[]proxies){
		ValueNode[]sources=new ValueNode[proxies.length];
		for(int i=0;i<sources.length;i++)sources[i]=(ValueNode)((ValueProxy)proxies[i]).source;
		return sources;
	}
}