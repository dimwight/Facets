package facets.core.app.avatar;
import static facets.core.app.avatar.PdfCode.Segment.*;
import static facets.util.Strings.*;
import static facets.util.Util.*;
import facets.core.app.avatar.PdfCanvas.PdfPainter;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.Util;
import java.util.Arrays;
public class PdfCode extends Tracer{
	public enum Segment{MOVETO("m",2),LINETO("l",2),QUADTO("?",4),CUBICTO("?",6),CLOSE("h",0);
		private final String code;
		private final int coordCount;
		private Segment(String code,int coordCount){
			this.code=code;
			this.coordCount=coordCount;
		}
		String coordsCode(double[]coords){
			return coordsString(Arrays.copyOf(coords,coordCount))+" "+code+" ";
		}
	}
	private final StringBuilder code=new StringBuilder();
	private final PdfPainter painter;
	private boolean create=PdfCanvas.pdf.exists();
	private StringBuilder path;
	protected PdfCode(PdfPainter painter){
		this.painter=painter;
	}
	final public void addCode(PdfPainter painter){
		trace(painter.code().getCode());
	}
	@Override
	protected final void traceOutput(String msg){
		if(!create)return;
		code.append(msg);
		code.append('\n');
	}
	public final void closeCode(){
		if(false&&create)Util.printOut("PdfCode: code=\n",getCode());
		create=false;
	}
	public final String getCode(){
		if(create&&code.length()==0)throw new IllegalStateException(
				"Code not created for "+Debug.info(painter));
		else return code.toString();
	}
	final protected void setTransform(double[]coords){
		trace("%setTransform\nQ q "+coordsString(coords)+" cm");
	}
	final protected void addPathSegment(Segment segment,double[]coords){
		if(segment==MOVETO)path=new StringBuilder();
		path.append(segment.coordsCode(coords));
		if(segment==CLOSE)path.append("\n");
	}
	final protected void fillPath(double[]color){ 
		trace("%fillPath\n"+path+coordsString(color)+" rg f");
	}
	final protected void strokePath(double[]color,double width){ 
		trace("%strokePath\n"+path+coordsString(color)+" RG "+sf(width)+" w s");
	}
	private static String coordsString(double[]coords){
		return fxString(coords).replace(',',' ').replaceAll("\\.0\\b","");
	}
	final protected void drawText(String text,String font,float size,boolean isBold,
			boolean isItalic,double[]color){
		trace("%drawText\n");
		trace("/F1 "+sf(size)+" Tf "+coordsString(color)+" rg\n"
		+"1 0 0 -1 0 0 cm BT ("+text+")Tj ET 1 0 0 -1 0 0 cm");
	}
}