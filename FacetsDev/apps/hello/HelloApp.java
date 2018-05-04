package apps.hello;
import facets.core.app.FeatureHost;
import facets.core.app.SurfaceStyle;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.kit.Toolkit;
import facets.util.Debug;
import facets.util.app.AppValues;
import java.awt.Dimension;
import apps.DemoApplet;
public abstract class HelloApp{
	public final AppValues values;
	//Parameter key
	protected HelloApp(AppValues values,String[]args){
		//${string_prompt:Hello[Type]:Label Field List All Choose Spaces Select Limit Commit }
		(this.values=values).readValues(args);
	}
	final public void openApp(){
		String type=readSurfaceType();
		Dimension size=sizeForType(type);
		openInAppletWindow(size,type);
	}
	protected abstract void openInAppletWindow(Dimension size,String title);
	final public SSurface newSurface(Toolkit kit,SHost host,Class contenterClass){
		FacetFactory ff=kit==null?null:FacetFactory.newAppletCore(kit,surfaceStyle());
		return HelloContenter.newSurface(readSurfaceType(),ff,host,contenterClass);
	}
	protected SurfaceStyle surfaceStyle(){
		return SurfaceStyle.BROWSER;
	}
	public String readSurfaceType(){
		return values.nature().get(DemoApplet.PARAM_TYPE);
	}
	public final Dimension sizeForType(String type){
		Dimension untyped=new Dimension(100,100);
		return type==null||type.trim().equals("")?untyped
			:"Label".contains(type)?new Dimension(150,150)
			:"Field|FieldBase".contains(type)?true?new Dimension(354,198):new Dimension(317,85)
			:"Commit".contains(type)?new Dimension(317,121)
			:"Limit".contains(type)?new Dimension(383,130)
			:"Spaces".contains(type)?new Dimension(314,130)
			:"Select".contains(type)?new Dimension(410,251)
			:"Choose".contains(type)?new Dimension(311,233)
			:"All".contains(type)?new Dimension(385,324)
			:"List".contains(type)?new Dimension(375,327)
			:untyped;
	}
}
