package apps.svg;
import facets.core.app.SViewer;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.AvatarPolicy;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PainterSource.Transform;
import facets.util.Debug;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import java.util.ArrayList;
import java.util.List;
import applicable.SvgShape;
import apps.svg.PathContenter.Paths;
final class PathAvatars extends AvatarPolicies{
	private final PathContenter pc;
	PathAvatars(PathContenter pc){
		this.pc=pc;
	}
	@Override
	public AvatarPolicy viewerPolicy(SViewer viewer, AvatarContent content,
									 final PainterSource p){
		final Paths paths=(Paths)content;
		if(paths==null)throw new IllegalStateException("Null paths in "+Debug.info(this));
		return new AvatarPolicy(){
			public Painter[]newViewPainters(boolean selected,boolean active){
				if(false)Debug.printStackTrace(5);
				return newTransformedPainters(selected?Shades.magenta:Shades.red);
			}
			public Painter[]newPickPainters(Object hit,boolean selected){
				return newTransformedPainters(Shades.blue);
			}
			private Painter[]newTransformedPainters(Shade shade){
				double[]values=pc.getPathValues(PathAvatars.this.pc.valuesKey);
				double pathX=-paths.paths.length/2d*values[PathContenter.VALUE_SPACING];
				Object renderValue=PathAvatars.this.pc.render.indexed();
				List<Painter>rawPainters=new ArrayList();
				for(SvgShape path:paths.paths){
					Painter painter=p.mastered(path.newOutlined(
							renderValue.equals(PathContenter.RENDER_PEN)?null:shade,
							renderValue.equals(PathContenter.RENDER_FILL)?null:shade.darker(),
							shade==Shades.red&&paths.paths.length==1));
					p.applyTransforms(new Transform[]{
						p.transformAt(pathX+=values[PathContenter.VALUE_SPACING],0),	
					},true,painter);
					rawPainters.add(painter);
				}
				Painter[]painters=rawPainters.toArray(new Painter[]{});
				p.applyTransforms(new Transform[]{
						p.transformAt(values[PathContenter.VALUE_X],values[PathContenter.VALUE_Y]),	
						p.transformScale(values[PathContenter.VALUE_SCALE],values[PathContenter.VALUE_SCALE]),
					},true,painters);
				return painters;
			}
		};
	}
	@Override
	public Painter getBackgroundPainter(SViewer viewer,
			PainterSource p){
		if(false||!pc.box.isSet())return Painter.EMPTY;
		Painter painter=p.mastered(
				pc.unitBox.newOutlined(Shades.lightGray.brighter(),null,false));
		return painter;
	}
}