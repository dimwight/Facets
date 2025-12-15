package facets.facet.kit.avatar;

import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PlaneView;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.app.ProvidingCache;
import facets.util.geom.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

final class SwingCanvasPainters_ extends Tracer {
    private static final boolean optimise = true, timing = false;
    private final ProvidingCache localCache =
            new ProvidingCache(true ? ProvidingCache.PASS_THROUGH : 20, null) {
                @Override
                protected boolean doTrace() {
                    return true;
                }
            };
    private final SwingAvatarMaster master;
    private Painter motionPainters[], viewPainters[], backPainter;
    private Dimension sizeThen;
    private Image immediate;
    private Object viewIdThen;

    SwingCanvasPainters_(SwingAvatarMaster master) {
        this.master = master;
    }

    void setPainters(Painter backPainter, Painter[] viewPainters, Painter[] motionPainters) {
        this.backPainter = backPainter;
        this.viewPainters = viewPainters;
        this.motionPainters = motionPainters;
        Times.times = timing;
    }

    void doPainting(Graphics2D g2) {
        if (viewPainters == null) return;
        if (timing) Times.printElapsed("SwingCanvasPainters.doPainting optimise=" + optimise);
        if (!optimise) {
            prepareAndPaint(g2, true, true, true);
        } else {
            doOptimisedPainting(g2, localCache);
        }
        if (timing) Times.printElapsed("SwingCanvasPainters.~doPainting");
    }

    private void doOptimisedPainting(Graphics2D g2, final ProvidingCache cache) {
        final JPanel pane = master.findCanvasPane();
        Dimension size = pane.getSize();
        final int width = size.width, height = size.height;
        final Object backId = backPainter.hashCode(),
                viewId = Arrays.hashCode(viewPainters),
                backValues[] = {backId, width, height},
                allValues[] = {backId, viewId, width, height};
        if (!viewId.equals(viewIdThen) ||
                motionPainters == null
                || motionPainters.length == 0
                || immediate == null) {
            if (true) trace(".doOptimisedPainting: viewId=" + viewId);
            Image back = newImager(cache, width, height, null)
                    .getImageForValues(backValues);
            immediate = newImager(cache, width, height, back)
                    .getImageForValues(allValues);
            viewIdThen = viewId;
        }
        g2.drawImage(immediate, 0, 0, null);
        if (motionPainters != null) {
            prepareAndPaint((Graphics2D) g2.create(), false, false, true);
        }
        sizeThen = size;
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
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        if (back) backPainter.paintInGraphics(g2.create());
        if (view) {
            if (false) trace(".prepareAndPaint: viewPainters=", viewPainters.length);
            for (Painter each : viewPainters)
                each.paintInGraphics(g2.create());
            ;
        }
        if (motion && motionPainters != null) {
//            System.out.println("motionPainters = " + motionPainters[0]);
            for (Painter each : motionPainters) each.paintInGraphics(g2.create());
        }
    }

    private ImageProviderAwt newImager(final ProvidingCache cache,
                                       final int width,
                                       final int height,
                                       final Image back) {
        Class<SwingCanvasPainters_> scp = SwingCanvasPainters_.class;
        return new ImageProviderAwt(cache, true ? scp : this,
                scp.getSimpleName() + ".newImages", width, height) {

            @Override
            protected BufferedImage newPaintedImage(int width, int height) {
                if (timing) Times.printElapsed("SwingCanvasPainters..newPaintedImage: back=" +
                        Debug.info(back));
                BufferedImage image = newPaintableImage(width, height,
                        master.findCanvasPane().getBackground());
                Graphics2D gi = (Graphics2D) image.getGraphics();
                final boolean backPainted = back != null;
                if (backPainted) gi.drawImage(back, 0, 0, null);
                prepareAndPaint(gi, !backPainted, backPainted, false);
                if (timing) Times.printElapsed("SwingCanvasPainters.~newPaintedImage: ");
                return image;
            }

            @Override
            protected long buildByteCount() {
                return (long) width * height * 4;
            }
        };
    }

    protected void traceOutput(String msg) {
        if (true) super.traceOutput(msg);
    }
}