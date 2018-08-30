package facets.facet.kit.swing;
import static java.awt.event.KeyEvent.*;
import static javax.swing.KeyStroke.*;
import facets.core.app.TextView;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.Util;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
final class TextSmarts extends Tracer{
	final private JTextComponent field;
	final private UndoManager undo;
	protected void traceOutput(String msg){
		if(false)Util.printOut(Debug.info(field)+msg);
		else super.traceOutput(msg);
	};
	TextSmarts(JTextComponent c){
		field=c;
		field.setDragEnabled(true);
		final boolean debug=field.getText().equals(TextView.DEBUG_TEXT);
		if(debug)field.setFont(field.getFont().deriveFont(20f));
		undo=new UndoManager(){
			private int charsThen;
			private String editingThen="";
			private CompoundEdit edit;
			{
				AbstractDocument doc=(AbstractDocument)field.getDocument();
				for(UndoableEditListener l:doc.getUndoableEditListeners())
					doc.removeUndoableEditListener(l);
				doc.addUndoableEditListener(this);
				InputMap inputs=field.getInputMap();
				ActionMap actions=field.getActionMap();
				int keyMask=Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
				inputs.put(getKeyStroke(VK_Z,keyMask),"Undo");
				inputs.put(getKeyStroke(VK_Y,keyMask),"Redo");
				actions.put("Undo",new AbstractAction(){public void actionPerformed(ActionEvent e){
					if(canUndo())undo();
				}});
				actions.put("Redo",new AbstractAction(){public void actionPerformed(ActionEvent e){
					if(canRedo())redo();
				}});
				charsThen=field.getText().length();
			}
			@Override
			public synchronized boolean addEdit(UndoableEdit e){
				int charsNow=field.getText().length(),changed=Math.abs(charsNow-charsThen);
				String editingNow=e.getPresentationName();
				boolean added;
				if(edit==null)edit=newCompoundEdit();
				added=edit.addEdit(e);
				if(false&&changed>1)trace(".addEdit: "+" editingNow="+editingNow+" editingThen="+editingThen+" changed="+changed+
						" added="+added);
				if(!editingNow.equals(editingThen)||changed!=1)added=endEdit();
				editingThen=editingNow;
				charsThen=charsNow;
				return added;
			}
			private boolean endEdit(){
				boolean added;
				editingThen="";
				edit.end();
				added=super.addEdit(edit);
				if(false)traceDebug(".endEdit: edits="+edits.size()+" ",this);
				edit=null;
				return added;
			}
			@Override
			public synchronized boolean canUndo(){
				if(edit!=null&&edit.isInProgress())endEdit();
				return super.canUndo();
			}
			@Override
			public synchronized void undo()throws CannotUndoException{
				super.undo();
				charsThen=field.getText().length();
			}
			@Override
			public synchronized void redo()throws CannotRedoException{
				super.redo();
				if(false)traceDebug(".undo: ",editToBeRedone());
			}
			private CompoundEdit newCompoundEdit(){
				return new CompoundEdit(){
					@Override
					public boolean addEdit(UndoableEdit anEdit){
						boolean added=super.addEdit(anEdit);
						if(false)traceDebug(".addEdit: edits="+edits.size()+" ",this);
						return added;
					}
				};
			}
		};
		mouse=new MouseAdapter(){
			private Boolean words;
			private int dotAt=-1,checkAt=-1;
			private boolean draggable;
			private MouseMotionListener[]motionListeners;
			@Override
			public void mousePressed(MouseEvent e){
				checkAt=getAt(e);
			}
			@Override
			public void mouseReleased(MouseEvent e){
				words=null;
				if(motionListeners!=null&&field.getMouseMotionListeners().length==1)
					for(MouseMotionListener l:motionListeners)
						if(l!=this)field.addMouseMotionListener(l);
				dotAt=-1;
				SwingUtilities.invokeLater(new Runnable(){public void run(){
					draggable=isSelection();
				}});
			}
			@Override
			public void mouseDragged(MouseEvent e){
				if(draggable)return;
				int nowAt=getAt(e);
				String text=field.getText();
				if(words==null){
					words=isSelection();
					motionListeners=field.getMouseMotionListeners();
					if(words)for(MouseMotionListener l:motionListeners)
						if(l!=this)field.removeMouseMotionListener(l);
					dotAt=nowAt;
				}
				if(!words)return;
				boolean lookRight=nowAt>=dotAt;
				final int left=findStopLeft(text,lookRight?dotAt:nowAt),
					right=findStopRight(text,lookRight?nowAt:dotAt);
				field.select(left,right);
				checkAt=nowAt;
				if(nowAt!=checkAt&&lookRight)Util.printOut("TS: words="+words
						+" nowAt="+nowAt+" text="+text.substring(left,right));
			}
			private boolean isSelection(){
				String text=field.getSelectedText();
				return text!=null&&text.length()>0;
			}
			private int findStopRight(String text,final int start){
				int stop=start;
				for(;stop<text.length();stop++){
					String select=text.substring(start,stop);
					if(select.matches("\\w* "))return stop;
					else if(!select.matches("\\w*"))return stop-1;
				}
				return stop;
			}
			private int findStopLeft(String text,final int start){
				int stop=start;
				for(;stop>=0;stop--){
					String select=text.substring(stop,start);
					if(select.matches(" \\w*"))return stop+1;
					else if(!select.matches("\\w*"))return stop;
				}
				return stop;
			}
			private int getAt(MouseEvent e){
				return field.viewToModel(e.getPoint());
			}
		};
		field.addMouseMotionListener(mouse);
		field.addMouseListener(mouse);
	}
	final MouseAdapter mouse;
	void resetUndo(){
		undo.discardAllEdits();
	}
}