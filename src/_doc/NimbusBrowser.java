package _doc;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * NimbusBrowser
 *
 * @author Created by Jasper Potts (Aug 27, 2008)
 * @version 1.0
 */

@SuppressWarnings("deprecation")
public class NimbusBrowser {

    private static Set<String> NIMBUS_PRIMARY_COLORS = new HashSet<String>(Arrays.asList(
            "text", "control", "nimbusBase", "nimbusOrange", "nimbusGreen", "nimbusRed", "nimbusInfoBlue",
            "nimbusAlertYellow", "nimbusFocus", "nimbusSelectedText", "nimbusSelectionBackground",
            "nimbusDisabledText", "nimbusLightBackground", "info"));
    private static Set<String> NIMBUS_SECONDARY_COLORS = new HashSet<String>(Arrays.asList(
            "textForeground", "textBackground", "background",
            "nimbusBlueGrey", "nimbusBorder", "nimbusSelection", "infoText", "menuText", "menu", "scrollbar",
            "controlText", "controlHighlight", "controlLHighlight", "controlShadow", "controlDkShadow", "textHighlight",
            "textHighlightText", "textInactiveText", "desktop", "activeCaption", "inactiveCaption"));
    private static String[] NIMBUS_COMPONENTS = new String[]{
            "ArrowButton", "Button", "ToggleButton", "RadioButton", "CheckBox", "ColorChooser", "ComboBox",
            "\"ComboBox.scrollPane\"", "FileChooser", "InternalFrameTitlePane", "InternalFrame", "DesktopIcon",
            "DesktopPane", "Label", "List", "MenuBar", "MenuItem", "RadioButtonMenuItem", "CheckBoxMenuItem", "Menu",
            "PopupMenu", "PopupMenuSeparator", "OptionPane", "Panel", "ProgressBar", "Separator", "ScrollBar",
            "ScrollPane", "Viewport", "Slider", "Spinner", "SplitPane", "TabbedPane", "Table", "TableHeader",
            "\"Table.editor\"", "\"Tree.cellEditor\"", "TextField", "FormattedTextField", "PasswordField", "TextArea",
            "TextPane", "EditorPane", "ToolBar", "ToolBarSeparator", "ToolTip", "Tree", "RootPane"};


    private static String DEFAULT_FONT = "defaultFont";

    private static File IMAGES_DIR;
    private static int IMAGE_COUNT = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(laf.getName())) {
                        try {
                            UIManager.setLookAndFeel(laf.getClassName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                // get defaults
                UIDefaults defaults = UIManager.getLookAndFeelDefaults();
                // split of components
                Map<String, Map<String, Object>> componentDefaults = new HashMap<String, Map<String, Object>>();
                Map<String, Object> others = new HashMap<String, Object>();
                for (Object keyObj : defaults.keySet()) {
                    String key = (String) keyObj;
                    boolean matchesComponent = false;
                    componentloop:
                    for (String componentName : NIMBUS_COMPONENTS) {
                        if (key.startsWith(componentName + ".") || key.startsWith(componentName + ":") ||
                                key.startsWith(componentName + "[")) {
                            Map<String, Object> keys = componentDefaults.get(componentName);
                            if (keys == null) {
                                keys = new HashMap<String, Object>();
                                componentDefaults.put(componentName, keys);
                            }
                            keys.put(key, defaults.get(key));
                            matchesComponent = true;
                            break componentloop;
                        }
                    }
                    if (!matchesComponent) others.put(key, defaults.get(key));
                }
                // split out primary, secondary colors
                Map<String, Object> primaryColors = new HashMap<String, Object>();
                Map<String, Object> secondaryColors = new HashMap<String, Object>();
                for (Map.Entry<String, Object> entry : others.entrySet()) {
                    if (NIMBUS_PRIMARY_COLORS.contains(entry.getKey())) {
                        primaryColors.put(entry.getKey(), entry.getValue());
                    }
                    if (NIMBUS_SECONDARY_COLORS.contains(entry.getKey())) {
                        secondaryColors.put(entry.getKey(), entry.getValue());
                    }
                }
                for (String key : NIMBUS_PRIMARY_COLORS) {
                    others.remove(key);
                }
                for (String key : NIMBUS_SECONDARY_COLORS) {
                    others.remove(key);
                }
                // split out UIs
                Map<String, Object> uiClasses = new HashMap<String, Object>();
                for (Map.Entry<String, Object> entry : others.entrySet()) {
                    if (entry.getKey().endsWith("UI")) {
                        uiClasses.put(entry.getKey(), entry.getValue());
                    }
                }
                for (String key : uiClasses.keySet()) {
                    others.remove(key);
                }
                // write html file
                try {
                    File dir = new File("output");
                    dir.mkdir();
                    IMAGES_DIR = new File(dir,"images");
                    IMAGES_DIR.mkdir();
                    Util.printOut("Outputing to " + dir.getAbsolutePath());
                    File htmlFile = new File(dir, "nimbus.html");
                    PrintWriter html = new PrintWriter(htmlFile);

                    html.printOut("<html>");
                    html.printOut("<head>");
                    html.printOut("<link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" />");
                    html.printOut("</head>");
                    html.printOut("<body>");
                    html.printOut("<h1>Primary Colors</h1>");
                    printTable(html, primaryColors);

                    html.printOut("<h1>Secondary Colors</h1>");
                    printTable(html, secondaryColors);

                    html.printOut("<h1>Components</h1>");
                    for (Map.Entry<String, Map<String, Object>> entry : componentDefaults.entrySet()) {
                        html.printOut("<h2>" + entry.getKey() + "</h2>");
                        printTable(html, entry.getValue());
                    }

                    html.printOut("<h1>Others</h1>");
                    printTable(html, others);

                    html.printOut("<h1>UI Classes</h1>");
                    printTable(html, uiClasses);

                    html.printOut("</body>");
                    html.printOut("</html>");

                    html.flush();
                    html.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void printTable(PrintWriter html, Map<String, Object> map) {
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);
        html.printOut("<table>");
        html.printOut("<tr><th>Key</th><th>Value</th><th>Preview</th></tr>");
        for (String key : keys) {
            printRow(html, key, map.get(key));
        }
        html.printOut("</table>");
    }

    static void printRow(PrintWriter html, String key, Object value) {
        html.printOut("<tr>");
        // key
        html.printOut("<td width=\"250\"><code>" + key + "</code></td>");
        if (value instanceof Color) {
            printColor(html, (Color) value);
        } else if (value instanceof Font) {
            printFont(html, (Font) value);
        } else if (value instanceof Dimension) {
            printDimension(html, (Dimension) value);
        } else if (value instanceof Insets) {
            printInsets(html, (Insets) value);
        } else if (value instanceof Border) {
            printBorder(html, (Border) value);
        } else if (value instanceof Painter) {
            printPainter(html, (Painter) value);
        } else if (value instanceof InputMap) {
            // ignore, not intresting
            html.printOut("<td>&nbsp;</td>");
            html.printOut("<td>&nbsp;</td>");
        } else if (value instanceof Icon) {
            printIcon(html, (Icon) value);
        } else {
            html.printOut("<td>" + value.toString() + "</td>");
            html.printOut("<td>&nbsp;</td>");
        }
        html.printOut("</tr>");
    }

    static void printColor(PrintWriter html, Color color) {
        html.printOut("<td><pre>#" + getWebColor(color) + " (" + color.getRed() + "," + color.getGreen() + "," +
                color.getBlue() + ")</pre></td>");
        html.printOut("<td width=\"100\" bgcolor=\"#" + getWebColor(color) + "\">&nbsp;</td>");
    }

    static void printPainter(PrintWriter html, Painter painter) {
        html.printOut("<td>Painter</td>");
        int w=25,h=25;
        try {
            BufferedImage img = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0,0,w,h);
            g2.setComposite(old);
            painter.paint(g2, null,w,h);
            g2.dispose();
            html.printOut("<td>"+saveImage(img)+"</td>");
        } catch (Exception e) {
            e.printStackTrace();
            html.printOut("<td>&nbsp;</td>");
        }
    }

    static void printFont(PrintWriter html, Font font) {
        String style = "";
        if (font.isBold() && font.isItalic()){
            style = "Bold & Italic";
        } else if (font.isBold()){
            style = "Bold";
        } else if (font.isItalic()){
            style = "Italic";
        }
        html.printOut("<td>Font \"" + font.getFamily() + " " + font.getSize() + " " + style + "</td>");
        int w=300,h=30;
        BufferedImage img = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0,0,w,h);
        g2.setComposite(old);
        g2.setColor(Color.BLACK);
        g2.setFont(font);
        g2.drawString("the quick brown fox jumps over the crazy dog",5,20);
        g2.dispose();
        html.printOut("<td>"+saveImage(img)+"</td>");
    }

    static void printInsets(PrintWriter html, Insets insets) {
        html.printOut("<td>Insets (" + insets.top + "," + insets.left + "," + insets.bottom + "," +
                insets.right + ")</pre></td>");
        int w = 50+insets.left+insets.right;
        int h = 20+insets.top+insets.bottom;
        BufferedImage img = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0,0,w,h);
        g2.setComposite(old);
        g2.setColor(Color.BLACK);
        g2.drawRect(insets.left, insets.top, 49,19);
        g2.setColor(Color.RED);
        g2.drawRect(0,0,w-1,h-1);
        g2.dispose();
        html.printOut("<td>"+saveImage(img)+"</td>");
    }


    static void printBorder(PrintWriter html, Border border) {
        Insets insets = border.getBorderInsets(null);
        html.printOut("<td>Border Insets(" + insets.top + "," + insets.left + "," + insets.bottom + "," +
                insets.right + ")</pre></td>");
        int w = 50+insets.left+insets.right;
        int h = 20+insets.top+insets.bottom;
        try {
            BufferedImage img = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0,0,w,h);
            g2.setComposite(old);
            g2.setColor(Color.RED);
            g2.fillRect(insets.left, insets.top, 49,19);
            border.paintBorder(null,g2, 0,0,w,h);
            g2.dispose();
            html.printOut("<td>"+saveImage(img)+"</td>");
        } catch (Exception e) {
            e.printStackTrace();
            html.printOut("<td>&nbsp;</td>");
        }
    }

    static void printDimension(PrintWriter html, Dimension dim) {
        html.printOut("<td>Dimension (" + dim.width + "," + dim.height + ")</pre></td>");
        int w = dim.width;
        int h = dim.height;
        if (w==0 || h==0){
            html.printOut("<td>&nbsp;</td>");
        } else {
            BufferedImage img = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0,0,w,h);
            g2.setComposite(old);
            g2.setColor(Color.RED);
            g2.drawRect(0,0,w-1,h-1);
            g2.dispose();
            html.printOut("<td>"+saveImage(img)+"</td>");
        }
    }

    static void printIcon(PrintWriter html, Icon icon) {
        html.printOut("<td>Icon " + icon.getIconWidth() + " x " + icon.getIconHeight() + "</pre></td>");
        BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0,0,icon.getIconWidth(), icon.getIconHeight());
        g2.setComposite(old);
        icon.paintIcon(null,g2, 0, 0);
        g2.dispose();
        html.printOut("<td>"+saveImage(img)+"</td>");
    }



    static String saveImage(BufferedImage img){
        File imgFile = new File(IMAGES_DIR,"img_"+(IMAGE_COUNT++)+".png");
        try {
            ImageIO.write(img,"png",imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "<img src=\"images/"+imgFile.getName()+"\">";
    }

    static String getWebColor(Color color) {
        String result = "";
        String num;
        num = Integer.toHexString(color.getRed());
        if (num.length() == 1) num = "0" + num;
        result += num;
        num = Integer.toHexString(color.getGreen());
        if (num.length() == 1) num = "0" + num;
        result += num;
        num = Integer.toHexString(color.getBlue());
        if (num.length() == 1) num = "0" + num;
        result += num;
        return result;
    }

}