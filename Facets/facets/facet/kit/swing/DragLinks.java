package facets.facet.kit.swing;
import static facets.facet.kit.swing.ViewerBase.*;
import static javax.swing.BorderFactory.*;
import facets.util.Debug;
import facets.util.Tracer;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
class DragLinks extends Tracer{
	private final static String property="minimumSize";
	private final int id=new Random().nextInt(),actions=TransferHandler.MOVE;
	private JComponent from;
	private boolean dragging;
	public DragLinks(){
		super("DragLinks");
	}
	final public void addComponent(final JComponent c){
		if(false)return;
		setShowLinkable(c,false);
		final TransferHandler handler=newHandler();
		c.setTransferHandler(handler);
		Dimension checkData=new Dimension(id,id);
		c.setMinimumSize(checkData);
		c.addMouseMotionListener(new MouseAdapter(){
			@Override
			public void mouseDragged(MouseEvent e){
				if(!dragging&&canLink(c))handler.exportAsDrag(from=c,e,actions);
			}
		});
	}
	protected void setShowLinkable(JComponent c,boolean on){
		c.setBorder(createLineBorder(on?FOCUS_COLOR:c.getBackground(),focusWidth()));
	}
	protected boolean canLink(JComponent c){
		return true;
	}
	protected void linkDefined(JComponent from,JComponent to){
		traceDebug(".linkDefined: from="+from+" to=",to);
	}
	private TransferHandler newHandler(){
		TransferHandler smart=new TransferHandler(property){
			Timer resetShowOver;
			Point offset;
			@Override
			public int getSourceActions(JComponent c){
				return actions;
			}
			@Override
			public void exportAsDrag(JComponent c,InputEvent e,int action){
				offset=((MouseEvent)e).getPoint();
				dragging=true;
				super.exportAsDrag(c,e,action);
			}
			@Override
			public boolean canImport(TransferSupport support){
				final JComponent c=(JComponent)support.getComponent();
				Transferable t=support.getTransferable();
				Dimension got;
				try{
					got=(Dimension)t.getTransferData(t.getTransferDataFlavors()[0]);
				}catch(Exception e){
					throw new RuntimeException(e);
				}
				if(false)trace(".canImport: got="+got.height+" id=",id);
				if(got.height!=id)return false;
				if(resetShowOver!=null)resetShowOver.stop();
				setShowLinkable(c,true);
				(resetShowOver=new Timer(100,new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e){
						setShowLinkable(c,false);
						resetShowOver=null;
					}
				}){
					@Override
					public boolean isRepeats(){return false;}
				}).start();
				return true;
			}
			@Override
			public boolean importData(TransferSupport support){
				final JComponent c=(JComponent)support.getComponent();
				if(c!=from)SwingUtilities.invokeLater(new Runnable(){public void run(){
					linkDefined(from,c);
					from=null;
				}});
				return true;
			}
			@Override
			protected void exportDone(JComponent source,Transferable data,int action){
				dragging=false;
				super.exportDone(source,data,action);
			}
			@Override
			public Image getDragImage(){
				int width=from.getWidth(),height=from.getHeight();
				BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
				setShowLinkable(from,true);
				Graphics2D g=image.createGraphics();
				g.setBackground(from.getBackground());
				g.clearRect(0,0,width,height);
				from.paint(g);
				setShowLinkable(from,false);
				return image;
			}
			@Override
			public Point getDragImageOffset(){
				return offset;
			}
		};
		return true?smart:new TransferHandler(property){
			@Override
			public void exportAsDrag(JComponent c,InputEvent e,int action){
				traceDebug(".exportAsDrag: c=",id(c));
				super.exportAsDrag(c,e,action);
			}
			@Override
			public int getSourceActions(JComponent c){
				int actions=super.getSourceActions(c);
				trace(".getSourceActions: c="+id(c)+" actions="+actions);
				return actions;
			}
			@Override
			protected Transferable createTransferable(JComponent c){
				trace(".createTransferable: c=",id(c));
				Transferable data=super.createTransferable(c);
				trace(".createTransferable: c=",id(c)+" data="+data);
				return data;
			}
			@Override
			public boolean canImport(TransferSupport support){
				boolean can=super.canImport(support);
				trace(".canImport: c=",id(support.getComponent())+" can="+can+
						" support="+Debug.info(support));
				return can;
			}
			@Override
			public boolean canImport(JComponent c,DataFlavor[]flavors){
				boolean can=super.canImport(c,flavors);
				trace(".canImport: c=",id(c)+" can="+can+
						" flavors="+flavors[0].getHumanPresentableName());
				return can;
			}
			@Override
			public boolean importData(TransferSupport support){
				trace(".importData: c=",id(support.getComponent())+
						" support="+Debug.info(support));
				return super.importData(support);
			}
			@Override
			public boolean importData(JComponent c,Transferable data){
				trace(".importData: c=",id(c)+" data="+data);
				return super.importData(c,data);
			}
			@Override
			protected void exportDone(JComponent c,Transferable data,int action){
				trace(".exportDone: c=",id(c)+" data="+data);
				super.exportDone(c,data,action);
			}
			@Override
			public Image getDragImage(){
				Image image=super.getDragImage();
				traceDebug(".getDragImage: image=",image);
				return image;
			}
			@Override
			public void setDragImage(Image img){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			public void setDragImageOffset(Point p){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			public Point getDragImageOffset(){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			public void exportToClipboard(JComponent comp,Clipboard clip,int action)
					throws IllegalStateException{
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			public Icon getVisualRepresentation(Transferable t){
				throw new RuntimeException("Not implemented in "+this);
			}
			private String id(Component c){
				return ((JLabel)c).getName();
			}
		};
	}
	public static void main(String[]args){
		final String aboves="first,second,third,",belows="fourth,fifth,sixth",
			titles[]=(aboves+belows).split(",");
		new JFrame(){
			DragLinks newLinks(){
				return new DragLinks(){
					protected void linkDefined(JComponent from,JComponent to){
						trace("linkDefined: "+from.getName()+" to "+to.getName());
					}
				};
			}
			@Override
			public Component add(Component comp){
				Container panel=(Container)comp;
				DragLinks above=newLinks(),below=newLinks();
				for(String title:titles){
					JComponent add=new JLabel(title,SwingConstants.CENTER);
					panel.add(add);
					(aboves.contains(title)?above:below).addComponent(add);
					add.setName(title);
				}
				super.add(panel);
				pack();
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				setLocation(600,400);
				setVisible(true);
				return panel;
			}}.add(new JPanel(new GridLayout(titles.length,1,10,10)));
	}
}