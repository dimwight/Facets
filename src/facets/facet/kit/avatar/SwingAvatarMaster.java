package facets.facet.kit.avatar;

import facets.core.app.SAreaTarget;
import facets.core.app.StatefulViewable;
import facets.core.app.ViewerTarget;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.superficial.Notifying.Impact;
import facets.facet.kit.avatar.AvatarCanvas.CanvasHost;
import facets.facet.kit.swing.ClipperSwing;
import facets.facet.kit.swing.KitSwing.PauseWaiter;
import facets.facet.kit.swing.ViewerMaster;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.shade.Shade;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;

import static facets.facet.FacetFactory.dragNotifyInterim;
import static facets.facet.FacetFactory.dragNotifyPause;
import static java.awt.event.MouseEvent.MOUSE_DRAGGED;
import static java.awt.event.MouseEvent.MOUSE_PRESSED;

abstract class SwingAvatarMaster extends ViewerMaster implements CanvasHost {
    @Override
    protected void disposeAvatarPane() {
        canvas = null;
        painters = null;
    }

    final class CanvasPane extends JPanel {
        private final PauseWaiter waiter = new PauseWaiter();
        private final ViewerMaster viewer;
        private Dimension sizeThen;
        MouseEvent lastEvent;
        private int lastX, lastY, lastMods;
        private boolean waitCursorSet;

        protected void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);
            lastEvent = e;
            int id = e.getID();
            switch (id) {
                case MOUSE_PRESSED:
                    requestFocus();
                    break;
                case MOUSE_DRAGGED:
                    dragging = true;
                    if (dragNotifyInterim) waiter.startWait(dragNotifyPause,
                            new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    canvas.consumeMouseEvent(AvatarCanvas.MOUSE_DRAG_NOTIFY,
                                            lastX, lastY, lastMods);
                                }
                            });
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    dragging = false;
                    if (waiter == null) break;
                    waiter.abandonWait();
                    break;
            }
            if (!canvas.consumeMouseEvent(id, lastX = e.getX(), lastY = e.getY(),
                    lastMods = true ? 0 : e.getModifiers() << 13))
                SwingAvatarMaster.this.unconsumedMouseEvent(e);
        }

        CanvasPane() {
            viewer = SwingAvatarMaster.this;
            enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK |
                    AWTEvent.KEY_EVENT_MASK);
            setFocusTraversalKeysEnabled(false);
            setTransferHandler(dndHandler);
        }

        public void doLayout() {
            super.doLayout();
            Dimension sizeNow = getSize();
            if (false) trace(".doLayout: sizeNow=" + sizeNow.width + " equal=" + (sizeNow.equals(sizeThen)));
            if (sizeNow.equals(sizeThen)) return;
            if (sizeNow.width < 0 || sizeNow.height < 0) sizeNow = new Dimension();
            sizeThen = sizeNow;
            canvas.paneSizeSet(sizeNow.width, sizeNow.height);
        }

        public void paintComponent(final Graphics g) {
            double scale = ((PlaneCanvas) canvas).scale;
            if (scale != scale) return;
            ViewerTarget target = viewerTarget();
            AvatarView view = (AvatarView) target.view();
            Shade background = (Shade) view.backgroundStyle();
            setBackground(new Color(background.rgb()));
            super.paintComponent(g);
            painters.doPainting((Graphics2D) g.create());
            SAreaTarget areaRoot = target.areaParent().areaParent();
            if (false && areaRoot != null && areaRoot.indexableTargets().length > 1
                    && target.isActive()) {
                Graphics hilite = g.create();
                Dimension size = getSize();
                hilite.setColor(Color.lightGray);
                for (int xy = 0, inset = 1; xy < 1; xy++, inset += 2)
                    hilite.drawRect(xy, xy, size.width - inset, size.height - inset);
            }
        }

        protected void processMouseMotionEvent(MouseEvent e) {
            processMouseEvent(e);
        }
    }

    protected AvatarCanvas canvas;
    private SwingCanvasPainters painters;
    private PainterSource painterSource;
    private boolean dragging;

    public SwingAvatarMaster(AvatarCanvas canvas) {
        (this.canvas = canvas).host = this;
        painters = new SwingCanvasPainters(this);
    }

    final public void setCanvasCursor(int cursor) {
        findCanvasPane().setCursor(Cursor.getPredefinedCursor(cursor));
    }

    public boolean isScrollable() {
        return false;
    }

    private String areaTitle() {
        return viewerTarget().areaParent().title();
    }

    final public void setAndPaintPainters(Painter backPainter, Painter[] viewPainters,
                                          Painter[] motionPainters) {
        painters.setPainters(backPainter, viewPainters, motionPainters);
        avatarPane().repaint();
    }

    public void refreshAvatars(Impact impact) {
        if (!dragging) canvas.refreshViewPainters();
    }

    protected CanvasPane findCanvasPane() {
        return (CanvasPane) avatarPane();
    }

    protected JComponent newAvatarPane() {
        return new CanvasPane();
    }

    protected void unconsumedMouseEvent(MouseEvent e) {
    }

    public void launchDnD() {
        dndHandler.exportAsDrag(avatarPane(), findCanvasPane().lastEvent,
                TransferHandler.MOVE);
    }

    private final static TransferHandler dndHandler = new TransferHandler() {
        private SwingAvatarMaster from, to;
        private DataFlavor toFlavor;

        public int getSourceActions(JComponent c) {
            from = getComponentViewer(c);
            if (from == null) throw new IllegalStateException("Null from in " + Debug.info(c));
            return COPY_OR_MOVE;
        }

        protected Transferable createTransferable(JComponent c) {
            if (getComponentViewer(c) != from)
                throw new IllegalArgumentException("Bad component " + Debug.info(c));
            return new ClipperSwing((StatefulViewable) from.viewerTarget().viewable, false).newClip();
        }

        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            to = getComponentViewer(c);
            if (to == null || from == null ||
                    to.viewerTarget().viewable ==
                            from.
                                    viewerTarget().
                                    viewable) return false;
            toFlavor = new ClipperSwing((StatefulViewable) to.viewerTarget().viewable, false).dataFlavor;
            for (int i = 0; i < flavors.length; i++)
                if (flavors[i].equals(toFlavor)) return true;
            return false;
        }

        public boolean importData(JComponent c, Transferable t) {
            if (!canImport(c, t.getTransferDataFlavors())) return false;
            try {
                StatefulViewable viewable = (StatefulViewable) to.viewerTarget().viewable;
                viewable.insertStatefuls(false, (Stateful[]) t.getTransferData(toFlavor));
                viewable.updateAfterEditAction();
                return true;
            } catch (UnsupportedFlavorException ufe) {
                throw new RuntimeException(ufe);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }

        protected void exportDone(JComponent c, Transferable data, int action) {
            if (getComponentViewer(c) != from) throw new IllegalArgumentException(
                    "Bad component " + Debug.info(c));
            StatefulViewable viewable = (StatefulViewable) from.viewerTarget().viewable;
            if (action == MOVE) {
                viewable.deleteSelection(false);
                viewable.updateAfterEditAction();
            }
            if (to != null) to.viewerTarget().ensureActive(Impact.ACTIVE);
            from.canvas.refreshViewPainters();
            to.canvas.refreshViewPainters();
            from = to = null;
        }

        private SwingAvatarMaster getComponentViewer(JComponent c) {
            return !(c instanceof CanvasPane) ? null
                    : (SwingAvatarMaster) ((CanvasPane) c).viewer;
        }
    };
}
