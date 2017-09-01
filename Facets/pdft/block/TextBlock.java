package pdft.block;
import static facets.core.app.avatar.PlaneView.*;
import static facets.util.Doubles.*;
import static facets.util.shade.Shades.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.AppSpecifier;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.util.Debug;
import facets.util.Doubles;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.shade.Shade;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import applicable.TextAvatar;
import applicable.avatar.BoxValues;
final class TextBlock extends Tracer implements TextAvatar{
	public static final String TYPE="block",TYPE_PATH="blocks",KEY_BOX="box";
	static final class Blocks{
		DataNode docBlocks;
		Blocks(AppSpecifier spec,String title){
			docBlocks=guaranteedChild(spec.state(TYPE_PATH),TYPE_PATH,title);
		}
		DataNode pageBlocks(final int pageAt){
			return guaranteedChild(docBlocks,TYPE_PATH,""+pageAt);
		}
		void deleteBlock(TextBlock block,int pageAt){
			if(!new NodeList(pageBlocks(pageAt),true).remove(block.data))
				throw new IllegalArgumentException("Block not found "+block.data);
		}
		TextBlock addBlock(int pageAt){
			ValueNode data=new ValueNode(TYPE,DataNode.UNTITLED);
			new NodeList(pageBlocks(pageAt),true).add(data);
			return new TextBlock(data,true);
		}
	}
	PainterSource p;
	final ValueNode data;
	private final boolean pickable;
	private List<PageChar>chars;
	static BoundsBox newDragBox(Point anchor,Point drag){
		double[]box=anchor.boxValues(drag);
		return new BoundsBox(box[BoxL],box[BoxT],box[BoxR],box[BoxB]);
	}
	TextBlock(TypedNode data,boolean pickable){
		this.pickable=pickable;
		this.data=(ValueNode)data;
	}
	void setChars(List<PageChar>chars){
		this.chars=chars==null?null:new ArrayList();
		if(chars!=null)for(PageChar c:chars){
			BoundsBox bounds=getBounds();
			if(c.boundsWithin(bounds))this.chars.add(c);
		}
	}
	void setBounds(double[]bounds){
		data.put(KEY_BOX,bounds);
	}
	@Override
	public Object checkCanvasHit(Point at,double hitGap){
		if(!pickable||!getBounds().contains(at.x(),at.y()))return null;
		return this;
	}
	private Painter[]newBoundsPainters(boolean selected,boolean forPick){
		Shade shade=chars!=null?gray.darker():selected?red.darker().darker()
				:gray.brighter();
		BoundsBox r=getBounds();
		return forPick?newFramePainters(r,shade.brighter().brighter(),
					false,true) 
				:chars==null?newFramePainters(r,shade,true,false)
			:Objects.join(Painter.class,
				new Painter[]{p.bar(r.left,r.top,r.width,r.height,white,false)},
				newFramePainters(r,shade,true,false)
			);
	}
	private Painter[]newFramePainters(BoundsBox box,Shade shade,boolean pickable, 
			boolean corners){
		Point lt=new Point(box.left,box.top),rt=new Point(box.right,box.top),
		lb=new Point(box.left,box.bottom),rb=new Point(box.right,box.bottom);
		return false&&corners?new Painter[]{
				p.pointMark(lt,shade,pickable),p.pointMark(rt,shade,pickable),
				p.pointMark(lb,shade,pickable),p.pointMark(rb,shade,pickable),
				p.line(new Line(lt,rt),shade,0,pickable),
				p.line(new Line(rt,rb),shade,0,pickable),
				p.line(new Line(rb,lb),shade,0,pickable),
				p.line(new Line(lb,lt),shade,0,pickable),
			}
			:new Painter[]{
			p.line(new Line(lt,rt),shade,0,pickable),
			p.line(new Line(rt,rb),shade,0,pickable),
			p.line(new Line(rb,lb),shade,0,pickable),
			p.line(new Line(lb,lt),shade,0,pickable),
		};
	}
	@Override
	public Painter newViewPainter(final boolean selected){
		return new Painter(){
			@Override
			public void paintInGraphics(Object graphics){
				Graphics g=(Graphics)graphics;
				for(Painter p:newBoundsPainters(selected,false))
					p.paintInGraphics(g.create());
				if(chars!=null)for(PageChar c:chars)
					c.newViewPainter(true).paintInGraphics(g.create());
			}
		};
	}
	@Override
	public Painter[]newPickPainters(boolean selected){
		return newBoundsPainters(selected,true);
	}
	@Override
	public BoundsBox getBounds(){
		double[]bounds=data.getDoubles(KEY_BOX);
		return new BoundsBox(bounds[BoxL],bounds[BoxT],bounds[BoxR],
				bounds[BoxB]);
	}
	@Override
	public String getText(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}	
}
