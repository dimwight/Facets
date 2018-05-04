package facets.core.app.avatar;
import facets.core.app.SView;
import facets.core.app.SViewer;
/**
Policy for a viewer that uses custom avatars. 
<p>{@link AvatarView} defines 
<ul>
  <li>policies that define the appearance and behaviour of avatars
  <li>commonly-required values specifying avatar picking behaviour
</ul>
 */
public interface AvatarView extends SView{
	public enum Direction{
		N(true,false,false,false),NW(true,false,false,true),NE(true,false,true,false),
		S(false,true,false,false),SE(false,true,true,false),SW(false,true,false,true),
		E(false,false,true,false),W(false,false,false,true),
		None(false,false,false,false);
		public final boolean isNorthern,isSouthern,isEastern,isWestern;
		Direction(boolean isNorthern, boolean isSouthern, boolean isEastern,boolean isWestern){
			this.isNorthern=isNorthern;
			this.isSouthern=isSouthern;
			this.isEastern=isEastern;
			this.isWestern=isWestern;
	}
	}
  /**
	Style for view background. 
	<p>Can specify colour, texture etc.  
	 */
	Object backgroundStyle();
	/**
	Specifies the pixel gap that counts as a hit when picking. 
	@return a positive integer
	 */
	int pickHitPixels();
	/**
	Specifies the pixel size of a point mark. 
	@return a positive integer
	 */
	int markPixels();
	/**
	Specifies the pixel gap that should be leapt to a point mark. 
	@return a positive integer
	 */
	int markLeapPixels();
	/**
	Does the view participate in the GUI drag-and-drop system?. 
	 */
	boolean doesDnD();
	AvatarPolicies avatars();
}

