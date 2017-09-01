package pub;
import static java.util.Collections.*;
import static pub.PubValues.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Regex;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
public final class IncBuild extends Tracer{
	static final String OPTION_TOP="<option value=\"",OPTION_VALUE_CLOSE="\">",
		OPTION_TAIL="</option>";
	private static final String ALLPUBS=false?"allpubs.inc":"pathsInc",CHANGES="changes.inc",
		CHECKFILES[]={"amplifiers.inc","combos.inc","vibrators.inc","pubspage.shtm"};
	private final class IncIssue implements Comparable{
		final String path,pub,issue,title;
		IncIssue(String path){
			this.path=path;
			String[]pubAndIssue=readPathPubAndIssue(path);
			title=pub=pubAndIssue[0];
			issue=pubAndIssue[1];
		}
		IncIssue(String[]pathAndTitle){
			if(false)trace(".IncIssue: pathAndTitle=",pathAndTitle);
			path=pathAndTitle[0];
			title=pathAndTitle[1];
			String[]pubAndIssue=readPathPubAndIssue(path);
			pub=pubAndIssue[0];
			issue=pubAndIssue[1];
		}
		private String[]readPathPubAndIssue(String path){
			String[]core=path.replaceAll(".*/([^.]+).*","$1").split("_");
			return new String[]{core[0],core.length==1?"":("_"+core[1])};
		}
		public int compareTo(Object o){
			IncIssue that=(IncIssue)o;
			return sortByPaths?path.compareTo(that.path)
					:pub.compareTo(that.pub);
		}
		public boolean equals(Object o){
			IncIssue that=(IncIssue)o;
			return pub.equals(that.pub);
		}
		public String toString(){
			return path;
		}
		String newOptionLine(){
			return false?pub:OPTION_TOP+path+OPTION_VALUE_CLOSE+
					title+OPTION_TAIL;
		}
	}
	private boolean sortByPaths;
	public static void main(String[]args)throws IOException{//-DpvAdmin
		TextLines.setDefaultEncoding(true); 
		new IncBuild().execute(PubPaths.Inc.getPaths(),new File(ROOT_PDFS)); 
	}
	public void execute(String[]paths,File pdfsRoot)throws IOException{
		final boolean writeIncs=false;
		if(paths.length==0){
			if(true)throw new RuntimeException("Not tested in "+Debug.info(this));
			else trace(": Executing Drawings");
			new DrawingsInc(pdfsRoot).writeInc(writeIncs);
			return;
		}
		File nowAll=new File(MASTER_DIR,ALLPUBS),
			thenAll=new File(nowAll.getParent(),"_"+ALLPUBS);
		if(!thenAll.exists())Util.copyFile(nowAll,thenAll);
		Map<String,IncIssue>thenIssues=new HashMap();
		for(String line:new TextLines(thenAll).readLines()){
			IncIssue issue=new IncIssue(pathAndTitle(line));
			if(thenIssues.get(issue.pub)==null)thenIssues.put(issue.pub,issue);
			else throw new IllegalStateException("Duplicate pub "+issue);
		}
		List<IncIssue>nowIssues=new ArrayList(),newIssues=new ArrayList();
		Map<IncIssue,IncIssue>duplicates=new HashMap();
		for(String path:paths){
			IncIssue issue=new IncIssue(path),issueThen=thenIssues.get(issue.pub);
			int thenAt=nowIssues.indexOf(issue);
			if(thenAt>=0)duplicates.put(issue,nowIssues.get(thenAt));
			else nowIssues.add(issue);
			if(issueThen==null||!issueThen.issue.equals(issue.issue)) 
				newIssues.add(issue);
		}
		if(duplicates.size()>0){
			File file=new File("duplicates.txt");
			List<Entry<IncIssue,IncIssue>>out=new ArrayList(duplicates.entrySet());
			sort(out,new Comparator<Entry<IncIssue,IncIssue>>(){
				public int compare(Entry<IncIssue,IncIssue>p,
						Entry<IncIssue,IncIssue>q){
					return p.getKey().compareTo(q.getKey());
				}
			});
			new TextLines(file).writeLines(
					Objects.toLines(out.toArray()).split("\n"));
			throw new IllegalStateException("Duplicate pubs: see file="+file.getAbsolutePath());
		}
		if(true){
			sort(nowIssues);
			sortByPaths=true;
			sort(newIssues);
		}
		trace(": New issues: "+Objects.toLines(newIssues.toArray()));
		if(writeIncs){
			TextLines allpubsNow=new TextLines(nowAll);
			for(IncIssue issue:nowIssues)
				allpubsNow.writeNextLine(issue.newOptionLine());
			allpubsNow.closeLineWriter();
		}
		if(true)for(String file:CHECKFILES){
			File webRoot=new File("R:/VTS Intranet/techpubs"),
				check=new File(false?pdfsRoot:webRoot,file);
			String[]lines=new TextLines(check).readLines();
			int lineAt=0;
			boolean linesChanged=false;
			for(String line:lines){
				for(IncIssue issue:newIssues){
					IncIssue thenIssue=thenIssues.get(issue.pub);
					if(thenIssue==null)continue;
					String thenPath=false?issue.path+(false?"+":"")
							:thenIssue.path;
					if(line.contains(thenPath)){
						linesChanged=true;
						lines[lineAt]=line.replace(thenPath,issue.path);
						trace(": updated " +file+":\n"+line+"\n"+lines[lineAt]);
					}
				}
				lineAt++;
			}
			trace(": file=" +file+" linesChanged=",linesChanged);
			if(linesChanged){
				new Util.FileBackup(check).doBackup();
				if(writeIncs)new TextLines(check).writeLines(lines);
			}
		}
	}
	private String[]pathAndTitle(String line){
		return false?Regex.replaceAll(line,OPTION_TOP,"",OPTION_TAIL,"","\\\\","/"
				).split(OPTION_VALUE_CLOSE)
			:new String[]{line,
				Regex.replaceAll(line.split("/")[1],"\\.pdf","")
					};
	}
}
