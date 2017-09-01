package pdfb;
import facets.util.ItemList;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
public final class CleanPubPs{
  private final static String start="[/NamespacePush pdfmark_5",stop=
    		"[/NamespacePop pdfmark_5";
	private final boolean debug=false;
	public void clean(File fileIn,File fileOut)throws IOException{
		int from=-1,to=from;
    BufferedReader reader=new BufferedReader(new InputStreamReader(
 				new FileInputStream(fileIn)));	
		PrintWriter writer=new PrintWriter(new FileWriter(fileOut),true);
 		String line;
 		for(int i=0;(line=reader.readLine())!=null;i++){
			if(from<0&&line.startsWith(start)){
				from=i;
				if(debug)Util.printOut("CleanPubPs.clean: " +i+":"+line);
			}
			else if(from>=0&&to<0){
				if(line.startsWith(stop))to=i+1;
				if(debug)Util.printOut("CleanPubPs.clean: " +i+":"+line);
			}
			else{
				writer.println(line);
				if(i%100000==0)Util.printOut("CleanPubPs.clean: "  +i/1000+"K lines ");
			}
		}
 		reader.close();
		writer.close();
		Util.printOut(from<0||to<0?"CleanPubPs.clean: not needed"
				:"CleanPubPs.clean: cleaned from "+from+" to "+to);
  }
	public static void main(String[]args){
		File fileIn=new File("C:/Tray/3002312_2.ps"),
	  	fileOut=new File("C:/Tray/ant.ps");
		try{
			new CleanPubPs().clean(fileIn,fileOut);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
}
