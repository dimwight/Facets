package facets.util;
import java.io.Serializable;
/**
Container for flags defined using strings. 
<p>{@link StringFlags} offers an alternative to <code>int</code>
constants to encode flags, being
<ul>
 <li>type-safe - no chance of confusion with <code>int</code>s used as values</li>
 <li>equally compact to code
 <li>more intuitive - no need to combine by ORing and decode by ANDing</li>
 <li>infinitely extensible - not limited by bit space available</li> 
 <li>largely self-documenting - the type name says it all, no need to reserve
 identifiers such as 'flags', 'type' etc.</li> 
 <li>object-oriented - in the useful sense that the type itself provides all the
 support need to code it, not relying like <code>int</code>s on externally
 defined logical arcana.</li>
 <li>easier to debug - values are human-readable</li>  
</ul>  
<p>{@link StringFlags} defines flags as set or reset by the presence or 
absence in its (immutable) contained string of string constants which should be 
created with {@link #newFlag(String)}. Collisions between partial or complete 
flag strings are avoided by prefixing each with the {@link #PREFIX} constant 
and by numbering flags sequentially. 
 */
final public class StringFlags implements Serializable{
  final public static String PREFIX="|",FLAG_NONE=newFlag("None");
	public static final StringFlags EMPTY=new StringFlags(FLAG_NONE);
	private static int creations;
  private final String flags;
  /** 
	 Converts text into an appropriate flag.  
	 <p>The flag is guaranteed to be acceptable when passed to
	 the constructor either on its own or concatenated with others created by 
	 this method. Such flags are therefore suitable as constants for 
	 use by application code.  
	 @param text may be any string not including the {@link #PREFIX} constant
	 beyond its start and will define a single flag
	 */
	final public static String newFlag(String text){
		int markAt=text.indexOf(PREFIX);
		if(markAt>0)throw new IllegalArgumentException("Bad text contains flag mark: "+text);
		String flag=(markAt==0?"":PREFIX)+creations+++":"+text.trim();
		return flag;
	}
	/**
	Unique constructor. 
	@param flags must a concatenation of strings created using {@link #newFlag(String)}. 
	 */
	public StringFlags(String flags){
		if(flags.equals(""))flags=FLAG_NONE;
	  if(!flags.startsWith(PREFIX))
	  	throw new IllegalArgumentException("Bad flags "+flags);
	  this.flags=flags;
	  if(false)Util.printOut("\nStringFlags: \n",this);
	}
	/**
  Do the flags set include this flagged string? 
  @param flag must have been created with {@link #newFlag(String)}.
   */
  public boolean includeFlag(String flag){
  	if(false)Util.printOut("StringFlags.include: ",flag);
    if(!flag.startsWith(PREFIX))
    	throw new IllegalArgumentException("Bad flag in "+Debug.info(this));
    return flags.indexOf(flag)>=0;
  }
  /**
	Add a flag. 
	@return a {@link StringFlags} with <code>flag</code> appended to the
	flags if not already included 
  @param flag must have been created with {@link #newFlag(String)}.
	 */
	public StringFlags addFlag(String flag){
		return includeFlag(flag)?this:
			new StringFlags((includeFlag(FLAG_NONE)?"":flags)+flag);
	}
	/**
	Remove a flag. 
	@return a {@link StringFlags} with <code>flag</code> absent from the
	flags  
  @param flag must have been created with {@link #newFlag(String)}.
	 */
	public StringFlags removeFlag(String flag){
		return!includeFlag(flag)?this:new StringFlags(flags.replace(flag,""));
	}
	/**
	Lists the current set of flags. 
	 */
	public String toString(){return flags.trim().replaceAll("\\s+"," ");}
}
