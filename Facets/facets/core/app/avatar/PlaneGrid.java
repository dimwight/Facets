package facets.core.app.avatar;
import static java.lang.Double.*;
import facets.core.app.avatar.PdfCanvas.PdfPainter;
import facets.util.Debug;
import facets.util.Strings;
import facets.util.Tracer;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class PlaneGrid extends Tracer{
	private final PlaneGrid master;
	private final Vector insets,unitRatios;
	private int across=Integer.MIN_VALUE,down=across;
	private Vector sizeRatios;
	public PlaneGrid(int across,int down,Vector insets,Vector unitRatios){
		master=null;
		sizeRatios=new Vector(1,1);
		this.across=across;
		this.down=down;
		this.insets=insets;
		this.unitRatios=unitRatios;
	}
	protected PlaneGrid(PlaneGrid master){
		this.master=master;
		insets=null;
		unitRatios=null;
	}
	final public Painter backgroundFrames(PainterSource p,boolean scaled){
		if(master!=null)throw new IllegalStateException("Mastered "+Debug.info(this));
		double width=sizedX(across),height=sizedY(down);
		Vector outset=(true?new Vector(sizedX(1),sizedY(1)):insets()).scaled(0.5);
		Point outer=new Point(0,0).shifted(outset.scaled(-1));
		final List<Painter>painters=new ArrayList();
		if(scaled)painters.add(p.rectangle(outer.x(),outer.y(),width+outset.x*2,height+outset.y*2,
				"shadeFill=white"));
		if(true){
			List<Line>lines=new ArrayList();
			int grain=1;
			for(int across=0;across<this.across+grain;across+=grain){
				double x=sizedX(across);
				lines.add(new Line(new double[]{x,0,x,height}));
			}
			for(int down=0;down<this.down+grain;down+=grain){
				double y=sizedY(down);
				lines.add(new Line(new double[]{0,y,width,y}));
			}
			painters.addAll(Arrays.asList(new Painter[]{
					p.rectangle(0,0,width,height,"shadePen=lightGray"),
					p.backgroundLines(lines.toArray(new Line[0]),Shades.lightGray),
				}));
		}
		final int hash=Arrays.hashCode(new double[]{width,height});
		return new PdfPainter(){
			private final PdfCode code=new PdfCode(this);
			@Override
			public int hashCode(){
				return hash;
			}
			@Override
			public void paintInGraphics(Object g){
				for(Painter painter:painters){
					painter.paintInGraphics(g);
					if(painter instanceof PdfPainter)code.addCode((PdfPainter)painter);
				}
				code.closeCode();
			}
			@Override
			public PdfCode code(){
				return code;
			}
		};
	}
	protected Vector insets(){
		return master==null?insets:master.insets();
	}
	protected Vector unitRatios(){
		return master==null?unitRatios:master.unitRatios();
	}
	final public void sizeToViewer(Vector viewer){
		if(master!=null)throw new IllegalStateException("Mastered "+Debug.info(this));
		double viewerAcross=(viewer.x-insets().x*2)/unitRatios().x,
			viewerDown=(viewer.y-insets().y*2)/unitRatios().y;
		if(viewerShapesGrid()){
			throw new RuntimeException("Not implemented in "+this);
		}
		else sizeRatios=new Vector(viewerAcross/across,viewerDown/down);
		if(false)trace(".sizeToViewer: ",this);
	}
	final public Vector newShow(){
		if(master!=null)throw new IllegalStateException("Mastered "+Debug.info(this));
		else return new Vector(sizedX(across)+insets().x*2,sizedY(down)+insets().y*2);
	}
	final public double sizedX(double gridX){
		return master!=null?master.sizedX(gridX):gridX*unitRatios().x*sizeRatios.x;
	}
	final public double sizedY(double gridY){
		return master!=null?master.sizedY(gridY):gridY*unitRatios().y*sizeRatios.y;
	}
	final public Point unsized(Point p){
		return master!=null?master.unsized(p)
				:new Point(p.x()/unitRatios().x/sizeRatios.x,p.y()/unitRatios().y/sizeRatios.y);
	}
	@Override
	public String toString(){
		return master!=null?master.toString()
				:("grid="+new Vector(across,down)+" viewerSetsGrid="+viewerShapesGrid()+
					" sizeRatios="+sizeRatios+" show="+newShow());
	}
	public final int across(){
		return across;
	}
	public final int down(){
		return down;
	}
	final public double width(){
		return master!=null?master.width():sizedX(across);
	}
	final public double height(){
		return master!=null?master.height():sizedY(down);
	}
	protected boolean viewerShapesGrid(){
		if(master!=null)throw new IllegalStateException("Mastered "+Debug.info(this));
		else return false;
	}
}