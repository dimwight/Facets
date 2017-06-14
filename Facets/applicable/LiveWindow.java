package applicable;
import facets.util.Debug;
import facets.util.Util;
import java.applet.Applet;
import netscape.javascript.JSObject;
/**
Connects to JavaScript in applet browser window.
 */
class LiveWindow{
  private JSObject window;
	public static final boolean debug = false;
	/**
	Modifies a Java escaped string for sending to JavaScript.
	<p>Applies regular expression replacements "\n"-&gt;"\\\\n",
		"'"-&gt;"\\\\'"
	 */
	final protected static String escapedString(String src){
		return src.replaceAll("\n", "\\\\n").replaceAll("'", "\\\\'");
	}
  /**
	 Attempts to connect to the applet browser window. 
	 <p>Fails silently; outcome can be tested by {@link #hasConnected()}.
	 */
	final public boolean connectToWindow(Applet applet){
		try{
			window=JSObject.getWindow(applet);
		}catch(Throwable e){
			Util.printOut("LiveWindow: No browser in ",Debug.info(this));
		}
		if(debug&&window!=null)
			Util.printOut("LiveWindow: Connected to browser window in ",Debug.info(this));
		return window!=null;
	}
  /**
	 Calls <code>alert</code> in the browser window. 
	 @param msg is passed via {@link #escapedString(String)}
	 */
	final public void alert(String msg) {
		window.eval("alert('" + escapedString(msg) + "')");
	}
  /**
	 Calls <code>status</code> in the browser window. 
	 @param msg is passed via {@link #escapedString(String)}
	 */
	final public void status(String msg) {
		if(window!=null)
			try {
				window.eval("window.status='" + escapedString(msg) + "'");
			} catch (Exception e) {}
		else if(debug)Util.printOut("LiveWindow: ",msg);
	}
	/**
	Has a browser connection been made?
	 */
	public boolean hasConnected(){return window!=null;}
	/**
	Convenience overload. 
	<p>Passes argument to {@link #call(String,String[])}.
	 */
	final protected Object call(String function){
		return call(function,new String[]{""});		
	}
	/**
	Convenience overload. 
	<p>Passes argument as string to {@link #call(String,String[])}.
	 */
  final protected Object call(String function,int arg){
    return call(function,new String[]{Integer.toString(arg)});
  }
	/**
	Convenience overload. 
	<p>Passes argument to {@link #call(String,String[])}.
	 */
	final protected Object call(String function,String arg){
    return call(function,new String[]{arg});
  }
  /**
  Calls Javascript function in the browser window. 
  @param function the function name
  @param args its arguments
  @return any return value
   */
  final protected Object call(String function,String[]args){
    return window.call(function,args);
  }
  /**
	Calls eval() in the browser window. 
	@param code is passed via {@link #escapedString(String)}
	@return any return value
	 */
	final protected Object eval(String code){
	  return window.eval(escapedString(code));
	}
}
