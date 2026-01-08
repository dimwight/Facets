package tones.page;
import facets.util.Debug;
import tones.Tone;
import tones.bar.Bar;
import tones.bar.Incipit;
public final class PageIncipit extends PageItem{
  public final Incipit content;
  private final double scaledPageX;
  PageIncipit(Incipit content,double barPageX,double pageXScale){
    this.content=content;
    scaledPageX=(int)(barPageX+content.gridAt*pageXScale);
  }
  double tonePageX(Tone tone){
    if(scaledPageX<0)throw new IllegalStateException(
        "Invalid scaledPageX in "+Debug.info(this));
    else return scaledPageX+tone.getOffset();
  } 
}
