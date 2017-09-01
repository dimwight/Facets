package pdft.inspect;
import static facets.core.superficial.app.SViewer.*;
import static pdft.inspect.PdfContenter.*;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.AvatarPolicy;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.DragPolicy;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.Painter.Style;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PlaneView;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.app.avatar.AvatarContent.State;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableFrame;
import facets.util.Debug;
import facets.util.app.AppValues;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
final class PageAvatarPolicies extends AvatarPolicies{
	static final int DISPLAY_TEXT=0,DISPLAY_STANDARD=1,DISPLAY_BEST=2;
	private static final double MARGINS=40;
	final SIndexing views;
	private PagePainters page;
	final static class PageRenderView extends PlaneViewWorks{
		PageRenderView(PageAvatarPolicies avatars){
			super("Re&ndering",0,0,new Vector(0,0),avatars);
		}
		@Override
		public Object backgroundStyle(){
			return Shades.gray;
		}
		void setToPageRotation(PDPage page){
			boolean rotated=page.findRotation()!=0;
			Dimension size=page.findMediaBox().createDimension();
			double across=rotated?size.height:size.width,down=rotated?size.width
					:size.height;
			setShowValues(across+MARGINS,down+MARGINS,new Vector(MARGINS,MARGINS).scaled(0.5),1);
		}
	}
	PageAvatarPolicies(AppValues values){
		final ValueNode nature=values.nature();
		views=new SIndexing("Page Render",new Object[]{"Text Only","Text and &Graphics"},
			nature.getOrPutBoolean(ARG_RENDER,false)?DISPLAY_STANDARD:DISPLAY_TEXT,
			new SIndexing.Coupler(){
			@Override
			public void indexSet(SIndexing i){
				nature.put(ARG_RENDER,i.index()==DISPLAY_STANDARD);
			}
		});
	}
	SFrameTarget newFramedView(ViewableFrame viewable){
		PageRenderView view=new PageRenderView(this);
		view.setToPageRotation(new PDPage((COSDictionary)viewable.selection().single()));
		return new SFrameTarget(view);
	}
	SSelection newViewerSelection(final PagePainters painters){
		if(painters==null)throw new IllegalStateException(
				"Null painters in "+Debug.info(this));
		if(this.page!=painters)painters.clearSelection();
		this.page=painters;
		return new SSelection(){
			public Object content(){
				return new AvatarContent[]{painters};
			}
			public Object single(){
				return content();
			}
			public Object[]multiple(){
				return(Object[])content();
			}
		};
	}
	@Override
	public AvatarPolicy avatarPolicy(SViewer viewer,AvatarContent content,
			final PainterSource p){
		return new AvatarPolicy(){
			@Override
			public Object stateCursor(State state){
				return state==Style.PickedSelected?SViewer.CURSOR_TEXT:
					SViewer.CURSOR_DEFAULT;
			}
			@Override
			public Painter[]newViewPainters(boolean selected,boolean active){
				return new Painter[]{page.newTextPainter(views.index()),
						page.newSelectionPainter()};
			}
			@Override
			public Painter[]newPickPainters(Object hit,boolean selected){
				return true?new Painter[]{}
					:new Painter[]{p.pointMark((Point)hit,Shades.red,false)};
			}
		};
	}
	@Override
	public Painter getBackgroundPainter(SViewer viewer,PainterSource p){
		PlaneView view=(PlaneView)viewer.view();
		return p.bar(0,0,view.showWidth()-MARGINS,view.showHeight()-MARGINS,
				Shades.white,false);
	}
	@Override
	public DragPolicy dragPolicy(AvatarView view,final AvatarContent[]content,
			Object hit,final PainterSource p){
		page.clearSelection();
		return new DragPolicy(){
			@Override
			public Object stateCursor(State state){
				return CURSOR_TEXT;
			}
			public Painter[]newDragPainters(Point anchorAt,Point dragAt){
				return false?new Painter[]{p.line(new Line(anchorAt,dragAt),Shades.red,0,false)}
					:page.newDragSelection(anchorAt,dragAt);
			}
			@Override
			public Object[]newDragDropEdits(Point anchorAt,Point dragAt){
				page.setDragSelection(anchorAt,dragAt);
				return content;
			}
		};
	}
}/*
This file forms part of Version 0.3.62 of pdfInspect 
http://pdfinspector.sourceforge.net
Copyright (C) 2011  David M Wright 
This library is free software; you can redistribute it and/or modify it under 
the terms of the GNU Lesser General Public License as published by the 
Free Software Foundation; either release 3 of the License, or (at your 
option) any later release.
This library is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
details.
You should have received a copy of the GNU Lesser General Public License along 
with this library; if not, write to the Free Software Foundation, Inc., 
59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
