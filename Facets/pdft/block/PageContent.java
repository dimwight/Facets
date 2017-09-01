package pdft.block;
import facets.core.app.PathSelection;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.Regex;
import facets.util.ArrayPath;
import facets.util.Bytes;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.geom.Point;
import facets.util.shade.Shades;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import java.awt.Graphics;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import org.apache.pdfbox.util.TextPositionComparator;
import pdft.block.PdfContenter.CosDisposer;
import pdft.block.TextBlock.Blocks;
import facets.util.app.DirCache;
import applicable.TextAvatar;
final class PageContent extends Tracer implements TextAvatar{
	static final String P_CHARS="<p chars=";
	public enum TextStyle{Extracted,Stream;
		public String title(){
			return this==Extracted?"E&xtracted":"Stream";
		}
	}
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	final int pageAt;
	final PagePainters pagePainters;
	final BoundsBox drawBox;
	boolean inBlockEdit;
	private final List<TextAvatar>avatars=new ArrayList();
	private final List<PageChar>plainChars=new ArrayList();
	private List<PageChar>selectionChars=new ArrayList();
	private final FacetAppSurface app;
	private final COSDictionary cos;
	private final boolean empty;
	private final Blocks blocks;
	private String extracted;
	PageContent(CosDisposer doc,COSDictionary cos,int pageAt,FacetAppSurface app,
			DirCache disk,Blocks blocks){
		this.cos=cos;
		this.pageAt=pageAt;
		empty=(this.app=app)==null;
		this.blocks=blocks;
		PDPage page=new PDPage(cos);
		int rotation=page.findRotation();
		PDRectangle media=page.findMediaBox();
		BoundsBox pageBox=new BoundsBox(0,0,media.getWidth(),media.getHeight());
		boolean flip=rotation==90||rotation==270;
		drawBox=flip?new BoundsBox(0,0,pageBox.height,pageBox.width):pageBox;
		CharFonts fonts=false?null:new CharFonts(pageAt);
		class PageStripper extends PDFTextStripper{
			final List<TextPosition>texts=new ArrayList();
			PageStripper(int docPage)throws IOException{
				super();
				setStartPage(docPage);
				setEndPage(docPage);
			}
			@Override
			public boolean getSortByPosition(){
				return true;
			}
			@Override
			public void writeText(PDDocument doc,Writer outputStream)
					throws IOException{
				if(false)Times.printElapsed("PageContent.PageStripper.writeText");
				super.writeText(doc,outputStream);
			  Collections.sort(texts,new TextPositionComparator());
			  if(false)Times.printElapsed("PageContent.PageStripper.writeText texts="+texts.size());
			}
			@Override
			protected void processTextPosition(TextPosition text){
				super.processTextPosition(text);
				if(!text.getCharacter().trim().equals(""))texts.add(text);
			}
		}
		try{
			if(false)Times.printElapsed("PageContent.PageContent pageAt="+pageAt);
      StringWriter outputStream = new StringWriter();
			PageStripper stripper=new PageStripper(pageAt+1);
      stripper.writeText(new PDDocument(doc.disposable()),outputStream);
			extracted=outputStream.toString();
			for(TextPosition text:stripper.texts)
				plainChars.add(new PageChar(fonts,text,rotation,0));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		pagePainters=new PagePainters(app,page,drawBox,pageBox,rotation,fonts,
				plainChars,disk,pageAt);
		if(false)Times.printElapsed("PageContent.PageContent~ plainChars="+plainChars.size()+
				" extracted="+extracted.length());
	}
	SSelection newViewSelection(SView view,final Object selected){
		if(!(view	instanceof AvatarView))
			return((PageTextView)view).newHtmlSelection(this);
		((PageAvatarPolicies)((AvatarView)view).avatars()).pageSelected(this);
		avatars.clear();
		avatars.add(this);
		TextBlock selectedBlock=selected instanceof TextBlock?(TextBlock)selected:null;
		if(blocks!=null){
			DataNode pageBlocks=blocks.pageBlocks(pageAt);
			if(selectedBlock!=null&&!new NodeList(pageBlocks,true).contains(selectedBlock.data))
				selectedBlock=null;
			int boxAt=1;
			for(TypedNode data:pageBlocks.children()){
				data.setTitle("Text Box "+boxAt+++" on Page "+(pageAt+1));
				avatars.add(selectedBlock!=null&&data.stateEquals(selectedBlock.data)?
						selectedBlock:new TextBlock(data,!inBlockEdit));
			}
		}
		if(selectedBlock!=null)selectedBlock.setChars(inBlockEdit?plainChars:null);
		AvatarContent[]content=avatars.toArray(new AvatarContent[]{});
		return new PathSelection(content,new ArrayPath(content,
		selectedBlock!=null?selectedBlock:this));
	}
	@Override
	public Painter newViewPainter(final boolean selected){
		if(false)app.debugWatch(getClass().getSimpleName(),true,true);
		return new Painter(){
			@Override
			public void paintInGraphics(Object graphics){
				if(empty)return;
				Graphics g=(Graphics)graphics;
				for(PageChar draw:selectionChars)
					draw.newViewPainter(selected).paintInGraphics(g.create());
			}
			@Override
			public int hashCode(){
				return Arrays.hashCode(new Object[]{pageAt,selectionChars.size()});
			}
		};
	}
	@Override
	public Painter[]newPickPainters(final boolean selected){
		return new Painter[]{};
	}
	@Override
	public Object checkCanvasHit(Point at,double hitGap){
		if(inBlockEdit)return null;
		double atX=at.x(),atY=at.y();
		if(!drawBox.contains((float)atX,(float)atY))return null;
		if(blocks!=null) 
			for(TextAvatar a:avatars)
			if(a==this)continue;
			else{
				TextAvatar hitBlock=(TextAvatar)a.checkCanvasHit(at,hitGap);
				if(hitBlock!=null)return null;
		}
		else if(plainChars!=null)
			for(PageChar c:plainChars)if(c.boundsContain(at))return c;
		return blocks!=null?this:null;
	}
	Painter[]newDragPainters(Point anchor,Point drag,PainterSource p){
		List<Painter>select=new ArrayList();
		selectionChars.clear();
		if(anchor.equals(drag))return new Painter[]{};
		if(blocks==null){
			Object hit=checkCanvasHit(anchor,0);
			if(hit==null)return new Painter[]{};
			PageChar anchorChar=(PageChar)hit;
			boolean checkBefore=anchorChar.isAfter(drag);
			int anchorAt=plainChars.indexOf(anchorChar),charCount=plainChars.size();
			if(plainChars==null)throw new IllegalStateException(
					"Null plainChars in "+Debug.info(this));
			else for(PageChar check:plainChars.subList(
					checkBefore?0:anchorAt,checkBefore?anchorAt:charCount))
				if((checkBefore&&check.isAfter(drag))||(!checkBefore&&!check.isAfter(drag))){
					selectionChars.add(check);
					select.add(check.newViewPainter(true));
				}
		}
		else if(p!=null){
			BoundsBox box=TextBlock.newDragBox(anchor,drag);
			for(PageChar check:plainChars)
				if(box.contains(check.getBounds()))
					select.add(check.newViewPainter(true));
			select.add(p.marquee(anchor,drag,Shades.lightGray));
		}
		return select.toArray(new Painter[]{});
	}
	TextBlock newDraggedBlock(Point anchor,Point drag){
		TextBlock block=blocks.addBlock(pageAt);
		block.setBounds(anchor.boxValues(drag));
		return block;
	}
	@Override
	public String getText(){
		if(extracted==null)throw new IllegalStateException(
				"Null extracted in "+Debug.info(this));
		return extracted;
	}
	String getHtml(TextStyle style){
		final boolean extracted=style==TextStyle.Extracted;
		if(false)Times.printElapsed("PageContent.getHtml extracted="+extracted);
		final int basePts=FacetFactory.fontSizes[FacetFactory.fontSizeAt];
		String html=new ItemProvider<String>(app.ff.providingCache(),this,
				getClass().getSimpleName()+".getHtml"){
			@Override
			protected String newItem(){
				return new HtmlBuilder(){
					final double unitPts=12;
					protected String buildPageStyles(double points,double para){
						return extracted?
				("p{font-family:\"Times New Roman\",serif;font-size:"+usePts(14)+"pt}\n")
						:("p{font-family:\"Courier New\",Courier;font-size:"+usePts(12)+"pt;" +
								"margin-bottom:" +usePts(3)+"pt;}\n")+
				"i{color:gray}";
					}
					private double usePts(int pt){
						return Util.sf(basePts*pt/unitPts);
					}
					@Override
					public String newPageContent(){
						if(extracted)return"<p>"+getText().replace("\n","\n<p>");
						String text;
						try{
							text=new PDPage(cos).getContents().getInputStreamAsString();
						}catch(IOException e){
							throw new RuntimeException(e);
						}
						return P_CHARS+text.split("\\s+").length+">"
							+(Regex.replaceAll(text,new String[]{
								"\n","\n<p>",
								"\\(([^\\)]+)\\)","(<i>$1</i>)"
							}));
					}
				}.buildPage();
			}
			@Override
			protected long buildByteCount(){
				return 0;
			};
			@Override
			protected long finalByteCount(String item){
				return item.getBytes().length;
			}
			protected boolean passThrough(){
				return false;
			};
		}.getForValues(pageAt,extracted,basePts);
		if(false)Times.printElapsed("PageContent.getHtml html="+html.length());
		return html==null?"[Operation not completed]":html;
	}
	void clearSelection(){
		selectionChars.clear();
	}
	long estimateSize(){
		int size=plainChars.size();
		if(false){
			Times.printElapsed("PageContent.estimateSize "+size);
			if(size>0)Times.printElapsed("PageContent.estimateSize~: "
					+(Bytes.serialize((Serializable)plainChars).length/size));
		}
		return size*PageChar.SIZE_ESTIMATE;
	}
	@Override
	public BoundsBox getBounds(){
		return drawBox;
	}
	@Override
	public String toString(){
		return Debug.info(this)+": Page #"+pageAt;
	}
}
