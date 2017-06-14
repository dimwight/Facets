package facets.util.shade;
import static facets.util.shade.Shade.*;
import facets.util.NumberPolicy;
import facets.util.NumberPolicy.Ticked;
import facets.util.tree.ValueNode;
import java.awt.Color;
/**
Shade constants and helper classes. 
<p>In addition to helper subclasses of {@link facets.util.NumberPolicy}, 
this class includes the following shade set constants:</p>
<ul>
<li>{@link #CORE_SHADES } - a {@link Shade}[] containing shades black, blue, brown, cyan, gray, 
green, magenta, orange, red, yellow.</li>
<li> {@link #HTML_SET } - a {@link ShadeSet} initialised with the set of HTML colors. 
</li></ul>
 */
final public class Shades{
  public static Shade named(String name){
  	if(name.trim().equals(""))return null;
  	name=name.toLowerCase().replace(" ","");
		for(Shade s:false?fullSet:CORE_SHADES){
			if(!s.title().toLowerCase().replace(" ","").startsWith(name.replaceAll("\\[.+","")
					))continue;
			return name.contains(BRIGHTER)?s.brighter():
				name.contains(DARKER)?s.darker():s;
		}
		throw new RuntimeException("No shade found for name="+name);
	}
	/**
	Policy for shade HSB values. 
	<p><code>HSBPolicy</code> is a {@link facets.util.NumberPolicy.Ticked} 
	with range 0.00 to 1.00.
	Much of its policy depends on the likely scale size - larger for hue.  
	 */
	public static class HSBPolicy extends NumberPolicy.Ticked{
    private final boolean forHue;
    public HSBPolicy(boolean forHue){
      super(0,1);
      this.forHue=forHue;
    }
    public boolean canCycle(){return forHue;}
    public int format(){return FORMAT_DECIMALS_2;}
    public int labelSpacing(){return forHue?5:10;}
    public int snapType(){return forHue?SNAP_NONE:SNAP_TICKS;}
    public int tickSpacing(){return 5;}
  }
  /**
	Policy for shade RGB values. 
  <p><code>RGBPolicy</code>is a {@link facets.util.NumberPolicy.Ticked} 
  with range 0 to 256.  
   */
  public static class RGBPolicy extends NumberPolicy.Ticked{
    public RGBPolicy(){super(0,256);}
    public int columns(){return 2;}
    public int labelSpacing(){return 4;}
    public int snapType(){return SNAP_NONE;}
    public int tickSpacing(){return 16;}
  }
	/**
	 Core shade.  
	 */
	final public static Shade 
	  blue=new Shade(0x0000FF,"Blue"),
	  brown=new Shade(0xA52A2A,"Brown"),
	  cyan=new Shade(0x00FFFF,"Cyan"),
	  black=new Shade(0x000000,"Black"),
		lightGray=new Shade(0xCCCCCC,"Light Gray"),
	  gray=new Shade(0x808080,"Gray"),
	  green=new Shade(0x008000,"Green"),
	  magenta=new Shade(0xFF00FF,"Magenta"),
	  orange=new Shade(0xFFA500,"Orange"),
	  red=new Shade(0xFF0000,"Red"),
	  yellow=new Shade(0xFFFF00,"Yellow"),
	  white=new Shade(0xFFFFFF,"White");
		/**
		The set of core shades. 
		 */
		final public static Shade CORE_SHADES[]={
	    black,
	    blue,
	    brown,
	    cyan,
	    gray,
	    lightGray,
	    green,
	    magenta,
	    orange,
	    red,
	    yellow,
	    white,
		};
	final private static Shade
	  aliceBlue=new Shade(0xF0F8FF,"Alice Blue"),
	  antiqueWhite=new Shade(0xFAEBD7,"Antique White"),
	  aquamarine=new Shade(0x7FFFD4,"Aquamarine"),
	  azure=new Shade(0xF0FFFF,"Azure"),
	  beige=new Shade(0xF5F5DC,"Beige"),
	  bisque=new Shade(0xFFE4C4,"Bisque"),
	  blanchedAlmond=new Shade(0xFFEBCD,"Blanched Almond"),
	  blueViolet=new Shade(0x8A2BE2,"Blue Violet"),
	  burlyWood=new Shade(0xDEB887,"Burly Wood"),
	  cadetBlue=new Shade(0x5F9EA0,"Cadet Blue"),
	  chartreuse=new Shade(0x7FFF00,"Chartreuse"),
	  chocolate=new Shade(0xD2691E,"Chocolate"),
	  coral=new Shade(0xFF7F50,"Coral"),
	  cornflowerBlue=new Shade(0x6495ED,"Cornflower Blue"),
	  cornsilk=new Shade(0xFFF8DC,"Cornsilk"),
	  crimson=new Shade(0xDC143C,"Crimson"),
	  darkBlue=new Shade(0x00008B,"Dark Blue"),
	  darkCyan=new Shade(0x008B8B,"Dark Cyan"),
	  darkGoldenRod=new Shade(0xB8860B,"Dark Golden Rod"),
	  darkGray=new Shade(0xA9A9A9,"Dark Gray"),
	  darkGreen=new Shade(0x006400,"Dark Green"),
	  darkKhaki=new Shade(0xBDB76B,"Dark Khaki"),
	  darkMagenta=new Shade(0x8B008B,"Dark Magenta"),
	  darkOliveGreen=new Shade(0x556B2F,"Dark Olive Green"),
	  darkOrange=new Shade(0xFF8C00,"Dark Orange"),
	  darkOrchid=new Shade(0x9932CC,"Dark Orchid"),
	  darkRed=new Shade(0x8B0000,"Dark Red"),
	  darkSalmon=new Shade(0xE9967A,"Dark Salmon"),
	  darkSeaGreen=new Shade(0x8FBC8F,"Dark Sea Green"),
	  darkSlateBlue=new Shade(0x483D8B,"Dark Slate Blue"),
	  darkSlateGray=new Shade(0x2F4F4F,"Dark Slate Gray"),
	  darkTurquoise=new Shade(0x00CED1,"Dark Turquoise"),
	  darkViolet=new Shade(0x9400D3,"Dark Violet"),
	  deepPink=new Shade(0xFF1493,"Deep Pink"),
	  deepSkyBlue=new Shade(0x00BFFF,"Deep Sky Blue"),
	  dimGray=new Shade(0x696969,"Dim Gray"),
	  dodgerBlue=new Shade(0x1E90FF,"Dodger Blue"),
	  feldspar=new Shade(0xD19275,"Feldspar"),
	  fireBrick=new Shade(0xB22222,"Fire Brick"),
	  floralWhite=new Shade(0xFFFAF0,"Floral White"),
	  forestGreen=new Shade(0x228B22,"Forest Green"),
	  fuchsia=new Shade(0xFF00FF,"Fuchsia"),
	  gainsboro=new Shade(0xDCDCDC,"Gainsboro"),
	  ghostWhite=new Shade(0xF8F8FF,"Ghost White"),
	  gold=new Shade(0xFFD700,"Gold"),
	  goldenRod=new Shade(0xDAA520,"Golden Rod"),
	  greenYellow=new Shade(0xADFF2F,"Green Yellow"),
	  honeyDew=new Shade(0xF0FFF0,"Honey Dew"),
	  hotPink=new Shade(0xFF69B4,"Hot Pink"),
	  indianRed=new Shade(0xCD5C5C ,"Indian Red"),
	  indigo=new Shade(0x4B0082 ,"Indigo"),
	  ivory=new Shade(0xFFFFF0,"Ivory"),
	  khaki=new Shade(0xF0E68C,"Khaki"),
	  lavender=new Shade(0xE6E6FA,"Lavender"),
	  lavenderBlush=new Shade(0xFFF0F5,"Lavender Blush"),
	  lawnGreen=new Shade(0x7CFC00,"Lawn Green"),
	  lemonChiffon=new Shade(0xFFFACD,"Lemon Chiffon"),
	  lightBlue=new Shade(0xADD8E6,"Light Blue"),
	  lightCoral=new Shade(0xF08080,"Light Coral"),
	  lightCyan=new Shade(0xE0FFFF,"Light Cyan"),
	  lightGoldenRodYellow=new Shade(0xFAFAD2,"Light Golden Rod Yellow"),
	  lightGrey=new Shade(0xD3D3D3,"Light Grey"),
	  lightGreen=new Shade(0x90EE90,"Light Green"),
	  lightPink=new Shade(0xFFB6C1,"Light Pink"),
	  lightSalmon=new Shade(0xFFA07A,"Light Salmon"),
	  lightSeaGreen=new Shade(0x20B2AA,"Light Sea Green"),
	  lightSkyBlue=new Shade(0x87CEFA,"Light Sky Blue"),
	  lightSlateBlue=new Shade(0x8470FF,"Light Slate Blue"),
	  lightSlateGray=new Shade(0x778899,"Light Slate Gray"),
	  lightSteelBlue=new Shade(0xB0C4DE,"Light Steel Blue"),
	  lightYellow=new Shade(0xFFFFE0,"Light Yellow"),
	  lime=new Shade(0x00FF00,"Lime"),
	  limeGreen=new Shade(0x32CD32,"Lime Green"),
	  linen=new Shade(0xFAF0E6,"Linen"),
	  maroon=new Shade(0x800000,"Maroon"),
	  mediumAquaMarine=new Shade(0x66CDAA,"Medium Aqua Marine"),
	  mediumBlue=new Shade(0x0000CD,"Medium Blue"),
	  mediumOrchid=new Shade(0xBA55D3,"Medium Orchid"),
	  mediumPurple=new Shade(0x9370D8,"Medium Purple"),
	  mediumSeaGreen=new Shade(0x3CB371,"Medium Sea Green"),
	  mediumSlateBlue=new Shade(0x7B68EE,"Medium Slate Blue"),
	  mediumSpringGreen=new Shade(0x00FA9A,"Medium Spring Green"),
	  mediumTurquoise=new Shade(0x48D1CC,"Medium Turquoise"),
	  mediumVioletRed=new Shade(0xC71585,"Medium Violet Red"),
	  midnightBlue=new Shade(0x191970,"Midnight Blue"),
	  mintCream=new Shade(0xF5FFFA,"Mint Cream"),
	  mistyRose=new Shade(0xFFE4E1,"Misty Rose"),
	  moccasin=new Shade(0xFFE4B5,"Moccasin"),
	  navajoWhite=new Shade(0xFFDEAD,"Navajo White"),
	  navy=new Shade(0x000080,"Navy"),
	  oldLace=new Shade(0xFDF5E6,"Old Lace"),
	  olive=new Shade(0x808000,"Olive"),
	  oliveDrab=new Shade(0x6B8E23,"Olive Drab"),
	  orangeRed=new Shade(0xFF4500,"Orange Red"),
	  orchid=new Shade(0xDA70D6,"Orchid"),
	  paleGoldenRod=new Shade(0xEEE8AA,"Pale Golden Rod"),
	  paleGreen=new Shade(0x98FB98,"Pale Green"),
	  paleTurquoise=new Shade(0xAFEEEE,"Pale Turquoise"),
	  paleVioletRed=new Shade(0xD87093,"Pale Violet Red"),
	  papayaWhip=new Shade(0xFFEFD5,"Papaya Whip"),
	  peachPuff=new Shade(0xFFDAB9,"Peach Puff"),
	  peru=new Shade(0xCD853F,"Peru"),
	  pink=new Shade(0xFFC0CB,"Pink"),
	  plum=new Shade(0xDDA0DD,"Plum"),
	  powderBlue=new Shade(0xB0E0E6,"Powder Blue"),
	  purple=new Shade(0x800080,"Purple"),
	  rosyBrown=new Shade(0xBC8F8F,"Rosy Brown"),
	  royalBlue=new Shade(0x4169E1,"Royal Blue"),
	  saddleBrown=new Shade(0x8B4513,"Saddle Brown"),
	  salmon=new Shade(0xFA8072,"Salmon"),
	  sandyBrown=new Shade(0xF4A460,"Sandy Brown"),
	  seaGreen=new Shade(0x2E8B57,"Sea Green"),
	  seaShell=new Shade(0xFFF5EE,"Sea Shell"),
	  sienna=new Shade(0xA0522D,"Sienna"),
	  silver=new Shade(0xC0C0C0,"Silver"),
	  skyBlue=new Shade(0x87CEEB,"Sky Blue"),
	  slateBlue=new Shade(0x6A5ACD,"Slate Blue"),
	  slateGray=new Shade(0x708090,"Slate Gray"),
	  snow=new Shade(0xFFFAFA,"Snow"),
	  springGreen=new Shade(0x00FF7F,"Spring Green"),
	  steelBlue=new Shade(0x4682B4,"Steel Blue"),
	  tan=new Shade(0xD2B48C,"Tan"),
	  teal=new Shade(0x008080,"Teal"),
	  thistle=new Shade(0xD8BFD8,"Thistle"),
	  tomato=new Shade(0xFF6347,"Tomato"),
	  turquoise=new Shade(0x40E0D0,"Turquoise"),
	  violet=new Shade(0xEE82EE,"Violet"),
	  violetRed=new Shade(0xD02090,"Violet Red"),
	  wheat=new Shade(0xF5DEB3,"Wheat"),
	  whiteSmoke=new Shade(0xF5F5F5,"White Smoke"),
	  yellowGreen=new Shade(0x9ACD32,"Yellow Green"),
	  fullSet[]={
    aliceBlue,
    antiqueWhite,
    aquamarine,
    azure,
    beige,
    bisque,
    black,
    blanchedAlmond,
    blue,
    blueViolet,
    brown,
    burlyWood,
    cadetBlue,
    chartreuse,
    chocolate,
    coral,
    cornflowerBlue,
    cornsilk,
    crimson,
    cyan,
    darkBlue,
    darkCyan,
    darkGoldenRod,
    darkGray,
    darkGreen,
    darkKhaki,
    darkMagenta,
    darkOliveGreen,
    darkOrange,
    darkOrchid,
    darkRed,
    darkSalmon,
    darkSeaGreen,
    darkSlateBlue,
    darkSlateGray,
    darkTurquoise,
    darkViolet,
    deepPink,
    deepSkyBlue,
    dimGray,
    dodgerBlue,
    feldspar,
    fireBrick,
    floralWhite,
    forestGreen,
    fuchsia,
    gainsboro,
    ghostWhite,
    gold,
    goldenRod,
    gray,
    green,
    greenYellow,
    honeyDew,
    hotPink,
    indianRed,
    indigo,
    ivory,
    khaki,
    lavender,
    lavenderBlush,
    lawnGreen,
    lemonChiffon,
    lightBlue,
    lightCoral,
    lightCyan,
    lightGoldenRodYellow,
    lightGrey,
    lightGreen,
    lightPink,
    lightSalmon,
    lightSeaGreen,
    lightSkyBlue,
    lightSlateBlue,
    lightSlateGray,
    lightSteelBlue,
    lightYellow,
    lime,
    limeGreen,
    linen,
    magenta,
    maroon,
    mediumAquaMarine,
    mediumBlue,
    mediumOrchid,
    mediumPurple,
    mediumSeaGreen,
    mediumSlateBlue,
    mediumSpringGreen,
    mediumTurquoise,
    mediumVioletRed,
    midnightBlue,
    mintCream,
    mistyRose,
    moccasin,
    navajoWhite,
    navy,
    oldLace,
    olive,
    oliveDrab,
    orange,
    orangeRed,
    orchid,
    paleGoldenRod,
    paleGreen,
    paleTurquoise,
    paleVioletRed,
    papayaWhip,
    peachPuff,
    peru,
    pink,
    plum,
    powderBlue,
    purple,
    red,
    rosyBrown,
    royalBlue,
    saddleBrown,
    salmon,
    sandyBrown,
    seaGreen,
    seaShell,
    sienna,
    silver,
    skyBlue,
    slateBlue,
    slateGray,
    snow,
    springGreen,
    steelBlue,
    tan,
    teal,
    thistle,
    tomato,
    turquoise,
    violet,
    violetRed,
    wheat,
    white,
    whiteSmoke,
    yellow,
    yellowGreen,
  };
	/**
	 {@link ShadeSet} initialised with the set of HTML colors. 
	 */
	final public static ShadeSet HTML_SET=new ShadeSet(fullSet,null);
}
