package facets.util.shade;
import facets.util.Debug;
import facets.util.Identified;
import facets.util.Strings;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.Util;
import java.awt.Color;
import java.io.Serializable;
/**
Named abstract colour. 
<p>{@link Shade} defines an immutable 24-bit RGB colour that is guaranteed to 
have a human-readable name. Methods mostly return properties in RGB and HSB terms,
 and new shades of the colour. 
 */
public final class Shade extends Tracer
		implements Serializable,Identified,Titled,Comparable{
	/**Index into values array. */
  final public static int H=0,S=1,B=2,R=0,G=1;
  /**Default title for custom shades.  */
  public static final String TITLE_CUSTOM="Custom";
  private static int customs;
  /**Brightening/darkening factor. */
  public final static double FACTOR_B=1.2; 
  /**HSB threshold value*/
  public final static double THRESHOLD_S=0.3,THRESHOLD_B=.7; 
  static final String BRIGHTER="[brighter]",DARKER="[darker]";
  private final int rgb;
  private static int identities;
  private final int identity=identities++;
  private final String title;
	/**
	Convenience constructor accepting HSB values. 
	@param hsb is converted to RGB equivalent
	@param title is passed to core constructor 
	 */
	public Shade(double[]hsb,String title){
		this(hsb[H],hsb[S],hsb[B],title);
	}
  /**
  Core constructor. 
  @param rgb is interpreted as a 24-bit RGB color value. 
  @param title must be non-<code>null</code> and conform to the contract of 
  {@link facets.util.Titled}. 
   */
  public Shade(int rgb,String title){
    this.rgb=rgb&0xFFFFFF;
    if(title==null)throw new IllegalArgumentException("Null title in "+Debug.info(this));
    if(false&&Shades.CORE_SHADES!=null)for(int i=0;i<Shades.CORE_SHADES.length;i++)
      if(this.rgb==Shades.CORE_SHADES[i].rgb)title=Shades.CORE_SHADES[i].title;
    if(title.equals(""))title=true?TITLE_CUSTOM:TITLE_CUSTOM+(++customs);
    this.title=title;
    if(false&&title.startsWith(TITLE_CUSTOM))Util.printOut("Shade: ",this);
  }
	private Shade(double h,double s,double b,String title){
		this(Color.HSBtoRGB((float)h,(float)s,(float)b),title);
	}
	/**
	The 24-bit RGB value for the shade. 
	 */
	public int rgb(){return rgb;}
	/**
  The title passed to the constructor.
  <p>If the shade was created using a <i>newXXXed</i> method, the title will be
  {@link #TITLE_CUSTOM} (which can be used for any 'anonymous' shade). 
<p>Note that the title does not form part of the shade's identity. Different shades 
can share a title (especially {@link #TITLE_CUSTOM}); different titles may be 
returned by shades for which <code>equals</code> returns <code>true</code>. 
   */
  public String title(){return title;}
  /**
  True if HSB brightness is higher than <code>THRESHOLD_B</code>. 
   */
  public boolean isBright(){return valuesHSB()[B]>THRESHOLD_B;}
  /**
	Creates a brighter version of the shade.	
	<p>HSB brightness will be higher than the source shade by {@link #FACTOR_B}, 
	up to a maximum of 1.0.
	Title will be {@link #TITLE_CUSTOM}). 
	 */
	public Shade brighter(){
    double hsb[]=valuesHSB(),shaded=hsb[B]*FACTOR_B;
    return new Shade(hsb[H],hsb[S],shaded<1?shaded:1,title+BRIGHTER);
  }
  /**
	Creates a darker version of the shade.	
	<p>HSB brightness will be lower than the source shade by 1/{@link #FACTOR_B}.
	Title will be {@link #TITLE_CUSTOM}). 
	 */
  public Shade darker(){
    double[]hsb=valuesHSB();
    return new Shade(hsb[H],hsb[S],hsb[B]/FACTOR_B,title+DARKER);
  }
	/**
  True if HSB saturation is higher than <code>THRESHOLD_S</code>. 
	 */
	public boolean isSaturated(){return valuesHSB()[S]>THRESHOLD_S;}	
  /**
  Creates a new version of the shade with HSB saturation of either 1.0 or
  below {@link #THRESHOLD_S}. 
  @param saturated switches saturation on if <code>true</code>
   */
  public Shade resaturated(boolean saturated){
    double[]hsb=valuesHSB();
    hsb[S]=saturated?1f:THRESHOLD_S*.99f;
    String newTitle=TITLE_CUSTOM+" "+title+(saturated?", saturated":", unsaturated");
    return new Shade(hsb,true?"":newTitle);
  }
	/**
	The RGB components of the shade.  
	<p>Returned as an <code>int[3]</code> with values in range 0 to 255. 
	 */
	public int[]valuesRGB(){
	  Color color=new Color(rgb);
  	return new int[]{color.getRed(),color.getGreen(),color.getBlue()};
  }
	/**
	The HSB description of the shade.  
	<p>Returned as a <code>double[3]</code> with values in range 0.0 to 1.0. 
	 */
	public double[]valuesHSB(){
	  Color color=new Color(rgb);
  	float[]hsb=Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),null);
  	return new double[]{hsb[H],hsb[S],hsb[B]}; 
  }
  /**

  The RGB color value to a specified reduced accuracy.  
  <p>Components are truncated to fit the complete value 
  within <code>bitLength</code>. So-called because its intended use is to 
  determine whether one shade can reasonably be 'snapped' to another.  
  @param bitLength the bit length to be used; must be divisible by 3 
   */
  public int snapRGB(int bitLength){
    if(bitLength%3!=0)throw new IllegalArgumentException("Bad bit length in "+Debug.info(this));
    int shift=(24-bitLength)/3,maskR=0xFF0000,maskG=0xFF00,maskB=0xFF, 
    	fullR=rgb&maskR,fullG=rgb&maskG,fullB=rgb&maskB,
    	snapR=(fullR>>shift)&maskR,snapG=(fullG>>shift)&maskG,snapB=(fullB>>shift)&maskB,
    	snap=(snapR>>2*shift)+(snapG>>1*shift)+(snapB>>0*shift);
    if(false)Util.printOut("Shade: ",Strings.hexString(snap));
    return snap;
  }
  /**
	Compares the <code>rgb</code> values of two shades. 
	<p>Note that <code>title</code> is ignored. 
	 */
	public int compareTo(Object o){return title.compareTo(((Shade)o).title);}
  /**
	Checks the <code>rgb</code> values of two shades. 
	<p>Note that <code>title</code> is ignored. 
   */
  public boolean equals(Object o){return this==o||rgb==((Shade)o).rgb;}
  /**
	Returns the <code>rgb</code> value. 
	<p>Note that <code>title</code> is ignored. 
   */
  public int hashCode(){return rgb;}
  public Object identity(){return identity;}
  public String toString(){
    String extra=Strings.hexString(rgb)+" "+Strings.hexString(snapRGB(12));
    return Debug.info(this)+" "+extra;
  }
}
