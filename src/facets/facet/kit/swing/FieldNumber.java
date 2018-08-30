package facets.facet.kit.swing;
import facets.facet.FacetFactory;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.NumberPolicy;
import facets.util.StringFlags;

import java.text.DecimalFormat;
import java.text.NumberFormat;
final class FieldNumber extends FieldText{
	private final NumberFormat formatter;
	public FieldNumber(KitFacet facet,NumberPolicy policy,KitCore toolkit,
			StringFlags hints){
	  super(facet,policy.columns(),toolkit,hints,true);
		int format=policy.format();
		if(format>=NumberPolicy.FORMAT_DECIMALS_0){
		  formatter=DecimalFormat.getInstance();
	    formatter.setMaximumFractionDigits(format);
			formatter.setMinimumFractionDigits(format);
			formatter.setGroupingUsed(!hints.includeFlag(FacetFactory.HINT_NUMERIC_UNGROUPED));
		}
		else if(format==NumberPolicy.FORMAT_HEX)formatter=null;
		else 
			{formatter=null;throw new RuntimeException("Format not implemented in "+Debug.info(this));}
	}
	public void setValue(double value){
	  String formatted=formatter!=null?formatter.format(value):
	    Integer.toHexString((int)value).toUpperCase();
    setText(formatted);
	}
	public double value(){
		if(formatter!=null)
		  try{return Double.valueOf(text()).doubleValue();}
		catch(NumberFormatException e){return Double.valueOf(lastSetText).doubleValue();}
		else 
		  try{return Integer.parseInt(text(),16);}
			catch(NumberFormatException e){return Integer.parseInt(lastSetText,16);}
	}
}
