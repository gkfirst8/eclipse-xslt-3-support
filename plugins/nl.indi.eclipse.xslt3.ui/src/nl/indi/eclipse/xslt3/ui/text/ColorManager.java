package nl.indi.eclipse.xslt3.ui.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {

    private final Map<RGB, Color> colors = new HashMap<>();

    public void dispose() {
        for (Color color : colors.values()) {
            color.dispose();
        }
        colors.clear();
    }

    public Color getColor(RGB rgb) {
        return colors.computeIfAbsent(rgb, key -> new Color(Display.getDefault(), key));
    }
}
