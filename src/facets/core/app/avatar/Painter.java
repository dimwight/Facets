package facets.core.app.avatar;
/**
Object to be used in custom avatar painting. 
<p>A {@link Painter} is a discrete object to be used by a custom avatar 
  when painting its content; it will often also be {@link Pickable}
<p>{@link Painter}s may be supplied by the facet builder used to create the
viewer to which they are passed; or implemented by client code calling a specific toolkit.
 */
public interface Painter{
	public static enum Style implements AvatarContent.State{
			HitCheck,Plain,Picked,Selected,PickedSelected,Dragging;
		boolean isPicked(){
			return this==Picked||this==PickedSelected;
		}
		boolean isSelected(){
			return this==Selected||this==PickedSelected;
		}
	}
	Painter EMPTY=new Painter(){
		@Override
		public void paintInGraphics(Object graphics){}
	};
	void paintInGraphics(Object graphics);
}
