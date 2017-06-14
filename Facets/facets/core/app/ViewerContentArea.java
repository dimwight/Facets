package facets.core.app;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SelectingFrame;
import facets.core.superficial.app.SContentAreaTargeter.ContentArea;
final class ViewerContentArea extends ContentArea{
  private String useTitle=superTitle();
	ViewerContentArea(String title,STarget[]viewers,SContenter contenter){
  	super(title,contenter,viewers);
  }
	final void setUseTitle(String title){
		useTitle=title;
	}
	final String superTitle(){
		return super.title();
	}
	final public String title(){
		if(contenter==null)return superTitle();
		String contentTitle=contenter.title().replace("&","");
		if(!useTitle.startsWith(contentTitle))
			useTitle=useTitle.replaceAll("^[^:]+",contentTitle);
		return useTitle;
	}
}