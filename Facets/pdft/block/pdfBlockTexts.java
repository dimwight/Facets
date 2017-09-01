package pdft.block;
import facets.core.app.AppSurface.ContentStyle;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
final class pdfBlockTexts extends PdfApp{
	public pdfBlockTexts(){
		super(pdfBlockTexts.class);
	}
	@Override
	protected boolean jarReady(){
		return false;
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	@Override
	protected void addStateDefaults(ValueNode root){
		super.addStateDefaults(root);
		Nodes.guaranteedChild(root,TextBlock.TYPE_PATH,DataNode.UNTITLED);
	}
	public static void main(String[]args){
		new pdfBlockTexts().buildAndLaunchApp(args);
	}
	@Override
	public boolean offersHelp(){
		return true;
	}
}
