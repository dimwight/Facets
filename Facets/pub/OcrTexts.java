package pub;
import static pub.PubValues.*;
import facets.util.TextLines;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import applicable.refs.TextReferences;
import applicable.refs.TextReferences.ReadSourceProvider;
final class OcrTexts{
	private final File file;
	OcrTexts(File file){
		this.file=file;
	}
	String[]readAllPages()throws IOException{
		List<String>text=new ArrayList();
		if(false)for(String str:new TextLines(file).readLinesString().split("\\W+"))
			text.add(str);
		return text.toArray(new String[]{});
	}
}
