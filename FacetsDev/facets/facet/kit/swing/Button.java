package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import static facets.facet.kit.KButton.*;
import static facets.facet.kit.swing.KitSwing.*;
import facets.core.app.SurfaceStyle;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.facet.FacetFactory;
import facets.facet.kit.Decoration;
import facets.facet.kit.KButton;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.KitCore.FlashTextNotice;
import facets.facet.kit.swing.KitSwing.NimbusSize;
import facets.util.Debug;
import facets.util.Times;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicButtonUI;
final class Button extends Widget implements KButton{
	private final boolean forMenu;
	private boolean ignoreActionEvents;
	private final Type type;
	private ActionEvent lastEvent;
	private int mnemonic=-1;
	private Timer flasher;
	Button(final KitFacet facet,Type type,final int usage){
		super(facet,null);
		this.type=type;
		forMenu=usage==USAGE_MENU;
		AbstractButton swing;
		switch(type){
	  case FireDropdown:
			swing=new JButton();
			swing.setFont(swing.getFont().deriveFont(AffineTransform.getScaleInstance(0.5,0.5)));
			break;
	  case Fire:
			swing=usage==USAGE_ICON?new IconButton():
					forMenu?new JMenuItem():constructFireButton(usage);
			break;
	  case Check:
			swing=forMenu?new JCheckBoxMenuItem()
				:usage==USAGE_TOOLBAR?new JToggleButton(){
				public Insets getMargin(){
					return adjustedButtonMargin(usage,super.getMargin());
				}
			}
			:false?new TristateCheckBox():new JCheckBox();
			break;
	  case Radio:
			swing=forMenu?new JRadioButtonMenuItem()
				:true?new JRadioButton():new TristateRadioButton();
			break;
	  default:throw new IllegalStateException("Bad button type "+type);
	  }
		KitSwing.adjustComponents(false,swing);
		swing.addActionListener(this);
		setSwing(swing);
	}
	@Override
	public void redecorate(Decoration d){
		this.decoration=d;
		debug=d.hints.includeFlag(HINT_DEBUG);
		final AbstractButton button=(AbstractButton)swing;
		button.setFocusable(!(d.hints.includeFlag(HINT_NO_FOCUS)||button instanceof IconButton));
		button.setActionCommand(message());
		String caption=d.caption;
		boolean appletStyle=FacetFactory.surfaceStyle!=SurfaceStyle.DESKTOP,
			noMnemonics=appletStyle||d.hints.includeFlag(HINT_NO_MNEMONICS);
		boolean hintTooltips=d.hints.includeFlag(HINT_TOOLTIPS);
		if(!noMnemonics&&(mnemonic=d.mnemonic)>0)button.setMnemonic(mnemonic);
		STarget target=facet.target();
		if(!forMenu){
			if(d.icon!=null&&!d.hints.includeFlag(HINT_USAGE_PANEL)&&!(button instanceof JCheckBox)){
				button.setIcon((Icon)d.icon);
				button.setText(null);
				button.setDisabledIcon((Icon)d.disable);
				button.setMnemonic(mnemonic=0);
				if(!hintTooltips)button.setToolTipText(!d.rubric.equals("")?d.rubric:d.caption);
			}
			else{
				if(target instanceof STrigger)((JButton)button).setDefaultCapable(
						((STrigger)target).coupler.makeDefault((STrigger)target));
				button.setText(caption);
				if(!hintTooltips&&!d.rubric.equals(""))button.setToolTipText(d.rubric);
			}
			return;
		}
		final JMenuItem item=(JMenuItem)button;
		item.setText(caption);
		if(hintTooltips){
			if(!d.rubric.equals(""))button.setToolTipText(d.rubric);
			else if(d.titleSplits.length>1)item.setToolTipText(d.titleSplits[1]);
		}
		if(d.keyStroke!=KitCore.DECORATIONS_NO_KEY)item.setAccelerator((KeyStroke)d.keyStroke);
		final FlashTextNotice flash=new FlashTextNotice(item,target);
		if(false)item.addChangeListener(new ChangeListener(){
			private boolean armedThen;
			public void stateChanged(ChangeEvent e){
				if(button.getSize().height==0)return;
				boolean armed=item.isArmed();
				if(armed!=armedThen)
					flash.setTextAndNotify((armedThen=armed)?decoration.rubric:"");
			}
		});
	}
	@Override
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		AbstractButton button=(AbstractButton)swing;
		if(enabled&&mnemonic>=0)button.setMnemonic(mnemonic);
		else if(!enabled)button.setMnemonic(-1);
	}
	private AbstractButton constructFireButton(final int usage){
		if(false&&usage==USAGE_ICON){
			new JButton(){
				private final static boolean fireLater=false;
			  private ActionEvent later;
				private Dimension lockedSize;
			  protected void fireActionPerformed(ActionEvent e){
			  	if(debug)Times.printElapsed("Button.fireActionPerformed: "+facet().title());
					if(fireLater){later=e;return;}
					else super.fireActionPerformed(e);	  
			  }
				public void setUI(ButtonUI ui){
					if(true||usage!=USAGE_ICON){
						super.setUI(ui);
						return;
					}
					final ButtonModel model=getModel();
					addMouseListener(new MouseAdapter(){
						@Override
						public void mouseEntered(MouseEvent e){
							if(isEnabled())model.setRollover(true);
						}
						@Override
						public void mouseExited(MouseEvent e){
							if(isEnabled())model.setRollover(false);
						}
					});
			  	super.setUI(new BasicButtonUI(){
			  		@Override
			  		protected void paintIcon(Graphics g,JComponent c,Rectangle iconRect){
							if(model.isRollover()){
								g.setColor(Color.red);
								g.drawRect(0,0,30,30);
							}
			  			super.paintIcon(g,c,iconRect);
			  		}
			  	});
				}
				public Insets getMargin(){
					return adjustedButtonMargin(usage,super.getMargin());
				}
			  public Dimension getMinimumSize() {
			  	return lockedSize==null||!isShowing()?
			  			lockedSize=super.getMinimumSize():lockedSize;
			  }
			  public Dimension getMaximumSize() {
			  	return getMinimumSize();
			  }
			  public Dimension getPreferredSize() {
			  	return getMinimumSize();
			  }
			  public void paint(Graphics g){
					super.paint(g);
					if(debug)Times.printElapsed("Button.paint: "+facet().title());
					if(!fireLater||later==null)return;
					final ActionEvent send=later;later=null;
					SwingUtilities.invokeLater(new Runnable(){public void run(){
			        fireLater(send);
			    }});
			  }
			  protected void fireLater(ActionEvent send){
					super.fireActionPerformed(send);
				}
			}.putClientProperty(sizeKey,NimbusSize.mini.name());
			SwingUtilities.updateComponentTreeUI(new JButton(){
				private final static boolean fireLater=false;
			  private ActionEvent later;
				private Dimension lockedSize;
			  protected void fireActionPerformed(ActionEvent e){
			  	if(debug)Times.printElapsed("Button.fireActionPerformed: "+facet().title());
					if(fireLater){later=e;return;}
					else super.fireActionPerformed(e);	  
			  }
				public void setUI(ButtonUI ui){
					if(true||usage!=USAGE_ICON){
						super.setUI(ui);
						return;
					}
					final ButtonModel model=getModel();
					addMouseListener(new MouseAdapter(){
						@Override
						public void mouseEntered(MouseEvent e){
							if(isEnabled())model.setRollover(true);
						}
						@Override
						public void mouseExited(MouseEvent e){
							if(isEnabled())model.setRollover(false);
						}
					});
			  	super.setUI(new BasicButtonUI(){
			  		@Override
			  		protected void paintIcon(Graphics g,JComponent c,Rectangle iconRect){
							if(model.isRollover()){
								g.setColor(Color.red);
								g.drawRect(0,0,30,30);
							}
			  			super.paintIcon(g,c,iconRect);
			  		}
			  	});
				}
				public Insets getMargin(){
					return adjustedButtonMargin(usage,super.getMargin());
				}
			  public Dimension getMinimumSize() {
			  	return lockedSize==null||!isShowing()?
			  			lockedSize=super.getMinimumSize():lockedSize;
			  }
			  public Dimension getMaximumSize() {
			  	return getMinimumSize();
			  }
			  public Dimension getPreferredSize() {
			  	return getMinimumSize();
			  }
			  public void paint(Graphics g){
					super.paint(g);
					if(debug)Times.printElapsed("Button.paint: "+facet().title());
					if(!fireLater||later==null)return;
					final ActionEvent send=later;later=null;
					SwingUtilities.invokeLater(new Runnable(){public void run(){
			        fireLater(send);
			    }});
			  }
			  protected void fireLater(ActionEvent send){
					super.fireActionPerformed(send);
				}
			});
		}
		return new JButton(){
			private final static boolean fireLater=false;
		  private ActionEvent later;
			private Dimension lockedSize;
		  protected void fireActionPerformed(ActionEvent e){
		  	if(debug)Times.printElapsed("Button.fireActionPerformed: "+facet().title());
				if(fireLater){later=e;return;}
				else super.fireActionPerformed(e);	  
		  }
			public void setUI(ButtonUI ui){
				if(true||usage!=USAGE_ICON){
					super.setUI(ui);
					return;
				}
				final ButtonModel model=getModel();
				addMouseListener(new MouseAdapter(){
					@Override
					public void mouseEntered(MouseEvent e){
						if(isEnabled())model.setRollover(true);
					}
					@Override
					public void mouseExited(MouseEvent e){
						if(isEnabled())model.setRollover(false);
					}
				});
		  	super.setUI(new BasicButtonUI(){
		  		@Override
		  		protected void paintIcon(Graphics g,JComponent c,Rectangle iconRect){
						if(model.isRollover()){
							g.setColor(Color.red);
							g.drawRect(0,0,30,30);
						}
		  			super.paintIcon(g,c,iconRect);
		  		}
		  	});
			}
			public Insets getMargin(){
				return adjustedButtonMargin(usage,super.getMargin());
			}
		  public Dimension getMinimumSize() {
		  	return lockedSize==null||!isShowing()?
		  			lockedSize=super.getMinimumSize():lockedSize;
		  }
		  public Dimension getMaximumSize() {
		  	return getMinimumSize();
		  }
		  public Dimension getPreferredSize() {
		  	return getMinimumSize();
		  }
		  public void paint(Graphics g){
				super.paint(g);
				if(debug)Times.printElapsed("Button.paint: "+facet().title());
				if(!fireLater||later==null)return;
				final ActionEvent send=later;later=null;
				SwingUtilities.invokeLater(new Runnable(){public void run(){
	          fireLater(send);
	      }});
		  }
	    protected void fireLater(ActionEvent send){
				super.fireActionPerformed(send);
			}
		};
	}
	@Override
	public void setSelected(boolean selected){
		ignoreActionEvents=true;
		((AbstractButton)swing).setSelected(selected);
		ignoreActionEvents=false;
	}
	@Override
	public void setIndeterminate(boolean on){
		if(swing instanceof TristateCheckBox)
			((TristateCheckBox)swing).setIndeterminate();
		else if(swing instanceof TristateRadioButton)
			((TristateRadioButton)swing).setIndeterminate();
		else super.setIndeterminate(on);
	}
	@Override
	public void setMessage(String msg){
		redecorate(decoration=decoration.recreate(msg));
	}
	@Override
	public boolean isSelected(){
		return((AbstractButton)swing).isSelected();
	}
	@Override
	public String message(){
		return decoration.keyText;
	}
	@Override
	public void actionPerformed(ActionEvent e){
		if(ignoreActionEvents)return;
		if(swing instanceof TristateCheckBox||swing instanceof TristateRadioButton){
			super.actionPerformed(e);
			return;
		}
		if(e==lastEvent)return;
		lastEvent=e;
		super.actionPerformed(e);
	}
	private Insets adjustedButtonMargin(int usage,Insets sm){
		if(sm==null)return sm;
		if(KitSwing.laf==LaF.Nimbus)return usage==USAGE_TOOLBAR?new Insets(sm.top,0,sm.bottom,0)
			:sm;
		int px3=px(3),px5=px(5),px15=px(15);
		return usage==USAGE_TOOLBAR?new Insets(sm.top,px5,sm.bottom,px5)
			:new Insets(px3,px15,px3,px15);
	}
	private Timer newFlasher(){
		JButton swing=(JButton)this.swing;
		final JRootPane root=swing.getRootPane();
		root.setDefaultButton(swing);
		return new Timer(500,new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				root.setDefaultButton(null);
				flasher=null;
			}
		}){public boolean isRepeats(){return false;}
		};
	}
	void flash(){
		if(flasher==null)(flasher=newFlasher()).start();
	}
}
