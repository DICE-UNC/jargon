package org.irods.jargon.idrop.desktop.systraygui.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to create a rotating color palate for the series panels
 * @author Mike Conway - DICE (www.irods.org)
 */
public class ColorHelper {

    private List<Color> colorWheel = new ArrayList<Color>();
    private int colorIndex = 0;

    public ColorHelper() {
        initColorWheel();
    }

    public synchronized Color getNextColor()
     {
        if (colorIndex++ == colorWheel.size() -1) {
            colorIndex = 0;
        }
        return colorWheel.get(colorIndex);
    }

    private synchronized void initColorWheel() {
        colorWheel.add(Color.LIGHT_GRAY);
        colorWheel.add(new Color(198, 229, 235));
        /*colorWheel.add(new Color(198, 229, 235));
         *         colorWheel.add(new Color(198, 235, 217));

        colorWheel.add(new Color(235, 204, 198));
        colorWheel.add(new Color(198, 205, 235));
        colorWheel.add(new Color(212, 206, 204));*/
    }
}
