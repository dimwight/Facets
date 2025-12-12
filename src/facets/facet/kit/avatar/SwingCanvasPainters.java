package facets.facet.kit.avatar;

import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PdfCanvas;
import facets.core.app.avatar.PlaneView;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.app.ProvidingCache;
import facets.util.geom.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

final class SwingCanvasPainters extends Tracer {
    private static final boolean optimise = false, timing = false;
    private final static PdfCanvas pdf = false ? null : new PdfCanvas();
    private final ProvidingCache localCache =
            System.getProperty("SwingCanvasPaintersLocalCache") == null ? null :
                    new ProvidingCache(false ? ProvidingCache.PASS_THROUGH : 20, null) {
                        @Override
                        protected boolean doTrace() {
                            return true;
                        }
                    };
    private final SwingAvatarMaster master;
    private Painter motionPainters[], viewPainters[], backPainter;
    private Dimension sizeThen;
    private Image immediate, codeBack;
    private Object viewIdThen;
    private Timer scaleWaiter_;

    SwingCanvasPainters(SwingAvatarMaster master) {
        this.master = master;
    }

    void setPainters(Painter backPainter, Painter[] viewPainters, Painter[] motionPainters) {
        this.backPainter = backPainter;
        this.viewPainters = viewPainters;
        this.motionPainters = motionPainters;
        Times.times=timing;
    }

    void doPainting(Graphics2D g2) {
        if (viewPainters == null) return;
        if (timing) Times.printElapsed("SwingCanvasPainters.doPainting optimise=" + optimise);
        if (!optimise) {
            prepareAndPaint(g2, true, true, true);
        } else {
            doOptimisedPainting(g2, localCache != null ? localCache : master.base().providingCache());
        }
        if (timing) Times.printElapsed("SwingCanvasPainters.~doPainting");
    }

    private void prepareAndPaint(Graphics2D g2, boolean back, boolean view, boolean motion) {
        PlaneCanvas canvas = (PlaneCanvas) master.canvas;
        double ySign = ((PlaneView) canvas.viewer().view()).ySign();
        AffineTransform plot = new AffineTransform();
        plot.scale(canvas.scale, canvas.scale * ySign);
        if (false) trace(".prepareAndPaint: plot=", plot.getScaleX());
        Point origin = canvas.origin;
        if (origin == null) throw new IllegalStateException("Null origin in " + Debug.info(this));
        else plot.translate(origin.x(), origin.y() * ySign);
        g2.transform(plot);
        if (true) g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        if (back) backPainter.paintInGraphics(g2.create());
        if (view) {
            if (false) trace(".prepareAndPaint: viewPainters=", viewPainters.length);
            for (Painter each : viewPainters) each.paintInGraphics(g2.create());
            pdf.tryRender(master.findCanvasPane().getSize(),
                    AvatarPolicies.joinPainters(new Painter[]{backPainter},
                            false ? new Painter[]{} : viewPainters));
        }
        if (motion && motionPainters != null) {
//            System.out.println("motionPainters = " + motionPainters[0]);
            for (Painter each : motionPainters) each.paintInGraphics(g2.create());
        }
    }
    protected void traceOutput(String msg) {
        if (true) super.traceOutput(msg);
    }

    private void doOptimisedPainting(Graphics2D g2, final ProvidingCache cache) {
        final JPanel pane = master.findCanvasPane();
        Dimension size = pane.getSize();
        final int width = size.width, height = size.height;
        boolean refIds = false;
        final Object backId = refIds ? backPainter : backPainter.hashCode(),
                viewId = refIds ? viewPainters : Arrays.hashCode(viewPainters),
                backValues[] = {backId, width, height},
                allValues[] = {backId, viewId, width, height};
        if (false && immediate != null && sizeThen != null && !sizeThen.equals(size)
                && (width > sizeThen.width || height > sizeThen.height)
                && !newImager(cache, 0, 0, null).hasForValues(allValues))
            scaleWithWait_(g2, pane, width, height);
        else if (!viewId.equals(viewIdThen) ||
                motionPainters == null || motionPainters.length == 0 || immediate == null) {
            if (false) traceDebug(".doOptimisedPainting: viewPainters=", viewPainters);
            else if (false) trace(".doOptimisedPainting: viewId=" + viewId);
            Image back = codeBack == null ? codeBack = newImager(cache, width, height, null
            ).getImageForValues(backValues) : codeBack;
            if (true) codeBack = null;
            final long maxInt = Integer.MAX_VALUE, ints = maxInt * 2, rowInts = ints / height;
            if (codeBack != null) for (Object v : viewPainters) {
                long code = v.hashCode() + maxInt,
                        x = (code % rowInts) * width / rowInts, y = code / rowInts;
                java.awt.Point at = new java.awt.Point((int) x, (int) y);
                Graphics gBack = codeBack.getGraphics();
                gBack.setColor(Color.gray);
                gBack.fillOval((int) x, (int) y, 5, 5);
            }
            immediate = newImager(cache, width, height, back).getImageForValues(allValues);
            viewIdThen = viewId;
        }
        g2.drawImage(immediate, 0, 0, null);
        if (motionPainters != null) prepareAndPaint((Graphics2D) g2.create(), false, false, true);
        sizeThen = size;
    }

    private ImageProviderAwt newImager(final ProvidingCache cache,
                                       final int width, final int height, final Image back) {
        Class<SwingCanvasPainters> scp = SwingCanvasPainters.class;
        return new ImageProviderAwt(cache, true ? scp : this,
                scp.getSimpleName() + ".newImages", width, height) {
            boolean backPainted = back != null;

            @Override
            protected BufferedImage newPaintedImage(int width, int height) {
                if (timing) Times.printElapsed("SwingCanvasPainters..newPaintedImage: back=" +
                        Debug.info(back));
                BufferedImage image = newPaintableImage(width, height,
                        master.findCanvasPane().getBackground());
                Graphics2D gi = (Graphics2D) image.getGraphics();
                if (backPainted) gi.drawImage(back, 0, 0, null);
                prepareAndPaint(gi, !backPainted, backPainted, false);
                if (timing) Times.printElapsed("SwingCanvasPainters.~newPaintedImage: ");
                return image;
            }

            @Override
            protected long buildByteCount() {
                return width * height * 4;
            }
        };
    }

    private void scaleWithWait_(Graphics2D g2, final JPanel pane, int width, int height) {
        if (scaleWaiter_ != null) scaleWaiter_.stop();
        g2.drawImage(true ? immediate
                : immediate.getScaledInstance(width, height, Image.SCALE_FAST), 0, 0, null);
        (scaleWaiter_ = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pane.repaint();
            }
        }) {
            public boolean isRepeats() {
                return false;
            }
        }).start();
    }
}