package pdft.inspect;
import static facets.util.Regex.*;
import static pdft.inspect.PdfContenter.*;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.HtmlBuilder;
import facets.util.Times;
import facets.util.Util;
import facets.util.app.ProvidingCache.ItemProvider;
import org.apache.pdfbox.cos.COSDocument;
final class Texts extends DocTexts{
	public enum TextStyle{Extracted,Stream;
		public String title(){
			return this==Extracted?"E&xtracted":"Stream";
		}
	}
	private final PagePainters emptyPainters=new PagePainters(this,0,null);
	private final FacetAppSurface app;
	Texts(COSDocument cosDoc,FacetAppSurface app){
		super(cosDoc);
		this.app=app;
	}
	PagePainters getPainters(final int pageAt){
		if(false)trace(".getPainters: pageAt=",pageAt);
		if(false)app.debugWatch("Texts.getPainters",false,true);
		PagePainters painters=new ItemProvider<PagePainters>(app.ff.providingCache(),doc,
				"Texts.getPainters"){
			@Override
			protected PagePainters newItem(){
				traceDebug(".getPainters:=",this);
				return new PagePainters(Texts.this,pageAt,app);
			}
			@Override
			protected long buildByteCount(){
				return 0;
			}
			protected long finalByteCount(PagePainters item){
				return 0;
			};
		}.getForValues(pageAt);
		if(false)trace(".getPainters: painters=",painters);
		return painters!=null?painters:emptyPainters;
	}
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	String newHtml(final int pageAt,TextStyle style){
		final boolean extracted=style==TextStyle.Extracted;
		if(false)Times.printElapsed("Texts..buildPageContent extracted="+extracted);
		final int basePts=FacetFactory.fontSizes[FacetFactory.fontSizeAt];
		String html=new ItemProvider<String>(app.ff.providingCache(),doc,
				Texts.class.getSimpleName()+".newHtml"){
			@Override
			protected String newItem(){
				return new HtmlBuilder(){
					final double unitPts=12;
					@Override
					protected String[]buildPageStyles(double points){
						return extracted?new String[]{
				"p{font-family:\"Times New Roman\",serif;font-size:"+usePts(14)+"pt}"
						}
						:new String[]{
				"p{font-family:\"Courier New\",Courier;font-size:"+usePts(12)+"pt;margin-bottom:" +
						usePts(3)+"pt;}",
				"i{color:gray}"
						};
					}
					private double usePts(int pt){
						return Util.sf(basePts*pt/unitPts);
					}
					@Override
					public String newPageContent(){
						return"<p>"+(extracted?newExtracted(pageAt).replace("\n","\n<p>")
								:replaceAll(getStreamText(pageAt),new String[]{
							"\n","\n<p>",
							"\\(([^\\)]+)\\)","(<i>$1</i>)"
						}));
					}
				}.buildPage();
			}
			@Override
			protected long buildByteCount(){
				return 0;
			}
			
			@Override
			protected long finalByteCount(String storeReady){
				return storeReady.getBytes().length;
			}
		}.getForValues(pageAt,extracted,basePts);
		if(false)Times.printElapsed("Texts..buildPageContent html="+html.length());
		return html==null?"[Operation not completed]":html;
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
