package facets.core.app.avatar;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.geom.Vector;
import facets.util.shade.Shade;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import java.net.URL;
import java.util.Arrays;
/**
Abstracts the features required to paint {@link AvatarContent}.
 */
public abstract class PainterMaster{
	public final ValueNode values;
	private Integer hash;
	protected PainterMaster(ValueNode values){
		this.values=values;
	}
	@Override
	final public int hashCode(){
		return hash!=null?hash:Arrays.hashCode(Objects.join(Object.class,
			new Object[]{values==null?"No values":false?values:Nodes.valuesAsLine(values,0)},
			lazyHashables()));
	}
	protected abstract Object[]lazyHashables();
	/**
	Abstracts outline features required to paint {@link AvatarContent}.
	 */
	public static abstract class Outlined extends PainterMaster{
		public static final Object PEN_HAIRLINE="Hairline";
		public final Shade fill,pen;
		public final boolean pickable;
		private final Object[]hashables;
		public Outlined(Shade fill,Shade pen,boolean pickable){
			super(null);
			if(fill==null&&pen==null)throw new IllegalArgumentException(
					"Null fill and pen in "+Debug.info(this));
			this.fill=fill;
			this.pen=pen;
			this.pickable=pickable;
			hashables=new Object[]{fill==null?"No fill":fill,pen==null?"No Pen":pen,pickable};
		}
		@Override
		final protected Object[]lazyHashables(){
			return Objects.join(Object.class,hashables,lazySubHashables());
		}
		protected Object[]lazySubHashables(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		public Shade getFill(){
			return fill;
		}
		public Shade getPen(){
			return pen;
		}
		public boolean isPickable(){
			return pickable;
		}
		public abstract Object getOutline();
		public Object penStyle(){
			return PEN_HAIRLINE;
		}
		public Scaling scaling(){
			return Scaling.NONE;
		}
		public Vector bounds(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
	/**
	Abstracts text features required to paint {@link AvatarContent}.
	 */
	public final static class Textual extends PainterMaster{
		/** Key for debug value. */
		final public static String KEY_TEXT_ALIASING="textAntiAlias";
		public final String text;
		public final double x,y;
		public Textual(ValueNode values,String text,double x,double y){
			super(values);
			this.text=text;
			this.x=x;
			this.y=y;
		}
		@Override
		protected Object[]lazyHashables(){
			return new Object[]{text,x,y};
		}
	}
	/**
	Defines how an {@link Outlined} should scale its pen.
	 */
	public enum Scaling{NONE,OUTLINE,PEN}
	/**
	Abstracts raster features required to paint {@link AvatarContent}.
	 */
	public final static class Rastered extends PainterMaster{
		public final URL url;
		public Rastered(URL url){
			super(null);
			this.url=url;
		}
		@Override
		protected Object[]lazyHashables(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
}