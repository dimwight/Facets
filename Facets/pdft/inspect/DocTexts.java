package pdft.inspect;
import static facets.util.Util.*;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
class DocTexts extends Tracer{
	public PageChars getChars(int pageAt){
		stripper.setStartPage(pageAt+1);
		stripper.setEndPage(pageAt+1);
		try{
			String text=stripper.getText(doc);
			final List<TextPosition>textChars=new ArrayList();
			Iterator<TextPosition>chars=stripChars.iterator();
			boolean debug=false;
			if(debug)trace(": text=\n",text);
			for(int charAt=0;charAt<text.length();charAt++){
				String textNext=text.substring(charAt,charAt+1);
				if(textNext.matches("\\s+")){
					textChars.add(null);
					if(debug)trace(": space ");
					continue;
				}else{
					if(!chars.hasNext())break;
					TextPosition match=chars.next();
					String matchChar=match.getCharacter();
					int matchCount=matchChar.length();
					if(matchChar.matches("\\s+")){
						charAt--;
						continue;
					}
					else if(matchCount>1){
						textNext=text.substring(charAt,charAt+matchCount);
						charAt+=matchCount-1;
					}
					String msg="matchChar="+matchChar+" textNext="+textNext;
					if(!matchChar.equals(textNext))throw new IllegalStateException(
							"No match "+msg);
					else if(debug)trace(": matched ",msg);
					textChars.add(match);
				}
			}
			return new PageChars(pages.get(pageAt),Collections.unmodifiableList(textChars));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	final public class PageChars extends Tracer{
		public final PDPage page;
		public final List<TextPosition>textChars;
		PageChars(PDPage page,List<TextPosition>textChars){
			this.page=page;
			this.textChars=textChars;
		}
		void markPage(String toMark,String text){
			for(int textAts=0;textAts<text.length();){
				int markStart=text.indexOf(toMark,textAts);
				if(markStart<0)break;
				TextPosition charStart=this.textChars.get(markStart);
				trace(".markPage: markStart=",traceTextChar(charStart));
				PDAnnotationTextMarkup hilite=new PDAnnotationTextMarkup(
						PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
				float x=charStart.getX(),y=charStart.getY();
				hilite.setQuadPoints(new float[]{x,y,x+charStart.getWidth(),
						y+charStart.getHeight()});
				try{
					page.getAnnotations().add(hilite);
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		}
		protected final Object traceTextChar(TextPosition tc){
			return tc.getCharacter()+" y="+sf(tc.getY())+" x="+sf(tc.getX());
		}
	}
	public final PDDocument doc;
	public final List<PDPage>pages;
	private final PDFTextStripper stripper;
	private final List<TextPosition>stripChars=new ArrayList();
	protected DocTexts(COSDocument cosDoc){
		if(cosDoc==null)
			throw new IllegalArgumentException("Null cos in "+Debug.info(this));
		doc=new PDDocument(cosDoc);
		try{
			stripper=new PDFTextStripper(){
				private final boolean test=false;
				private int testCharAt=-1;
				public boolean getSortByPosition(){
					return!test;
				}
				@Override
				public void processEncodedText(byte[]string)throws IOException{
					if(test)trace(".processEncodedText: text=",new String(string));
					super.processEncodedText(string);
				}
				@Override
				protected void processTextPosition(TextPosition text){
					if(test){
						trace(".processTextPosition: text=",text.getCharacter());
						testCharAt++;
						stripChars.add(text);
					}
					super.processTextPosition(text);
				}
				protected void writePage()throws IOException{
					if(!test)stripChars.clear();
					else trace(".writePage: charsRaw=",stripChars.size());
					super.writePage();
					if(!test)stripChars.addAll(charactersByArticle.get(0));
					else trace(".writePage: charsRaw="+stripChars.size()+" charAt="+testCharAt);
				}
			};
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		pages=new PDDocument(cosDoc).getDocumentCatalog().getAllPages();
	}
	public String newExtracted(int pageAt){
		try{
			stripper.setStartPage(pageAt+1);
			stripper.setEndPage(pageAt+1);
			return stripper.getText(doc);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	public String getStreamText(int pageAt){
		try{
			return pages.get(pageAt).getContents().getInputStreamAsString();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	public static void markDocPages(COSDocument doc,String toMark){
		for(Object each:new PDDocument(doc).getDocumentCatalog().getAllPages())
			throw new RuntimeException("Not implemented for "+Debug.info(each));
	}
}/*
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

