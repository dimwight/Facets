package pdft.inspect;
import static facets.core.app.HtmlView.*;
import static facets.facet.FacetFactory.*;
import facets.core.app.avatar.AvatarView;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectionView;
import facets.core.superficial.app.ViewableFrame;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.NumberPolicy;
import facets.util.Times;
import facets.util.app.ProvidingCache;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import pdft.inspect.PageAvatarPolicies.PageRenderView;
class PdfPages extends ViewableFrame{
	static final int COS_GO_TO_PAGE=0,COS_PAGE_COUNT=1,COS_FONTS=2,
		COS_LAST=COS_FONTS;
	private final ProvidingCache appCache;
	private final Texts texts;
	private final List<COSDictionary>cosPages;
	private final int pageCount;
	private final SNumeric goToPage;
	private int viewPageAt;
	PdfPages(String title,COSDocument cosDoc,FacetAppSurface app){
		super(title,cosDoc);
		appCache=app.ff.providingCache();
		texts=new Texts(cosDoc,app);
		cosPages=new ArrayList();
		for(Object each:new PDDocument(cosDoc).getDocumentCatalog().getAllPages())
			cosPages.add(((PDPage)each).getCOSDictionary());
		pageCount=cosPages.size();
		int pageAt=true||!title.startsWith("Default")?1
				:Integer.valueOf(title.replaceAll("[^\\d]+",""));
		defineSelection(cosPages.get(pageAt-1));
		goToPage=new SNumeric("Page",pageAt,new SNumeric.Coupler(){
			@Override
			public void valueSet(SNumeric n){
				COSDictionary pageNow=cosPages.get((int)n.value()-1);
				if(pageNow!=selection().single())defineSelection(pageNow);
			}
			@Override
			public NumberPolicy policy(SNumeric n){
				return new NumberPolicy(1,cosPages.size()){
					@Override
					public String[]incrementTitles(){
						return new String[]{PAGE_PREVIOUS,PAGE_NEXT};
					}
				};
			}
		});
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		SelectionView view=(SelectionView)viewer.view();
		final int pageAt=(int)goToPage.value()-1;
		if(viewPageAt!=pageAt&&view instanceof AvatarView){
			view.updateStateStamp();
			viewPageAt=pageAt;
		}
		return view instanceof AvatarView?
				((PageAvatarPolicies)((AvatarView)view).avatars()).newViewerSelection(
						texts.getPainters(pageAt))
			:view instanceof PageTextView?
					((PageTextView)view).newViewerSelection(texts,pageAt)
			:view.newViewerSelection(viewer,selection());
	}
	@Override
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		defineSelection(selection.single());
	}
	@Override
	public SSelection defineSelection(Object definition){
		final COSDictionary cos=definition instanceof COSDictionary?
			(COSDictionary)definition:cosPages.get((int)goToPage.value()-1);
		SSelection set=setSelection(new SSelection(){
			@Override
			public Object content(){
				return framed;
			}
			@Override
			public Object single(){
				return cos;
			}
			@Override
			public Object[]multiple(){
				return new Object[]{cos};
			}
		});
		if(goToPage!=null)goToPage.setValue(cosPages.indexOf(cos)+1);
		return set;
	}
	@Override
	protected STarget[]lazyElements(){
		return new STarget[]{
				goToPage,
				new STextual("pageCount","/  "+pageCount,new STextual.Coupler()),
				FacetFactory.fontIndexing
			};
	}
	public void setFramedState(Object stateSpec,boolean interim){}
	void setPageViewToRotation(PageRenderView view){
		view.setToPageRotation(new PDPage((COSDictionary)selection().single()));
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
