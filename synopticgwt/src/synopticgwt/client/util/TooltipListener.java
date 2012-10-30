/*
 * Tooltip component for GWT Copyright (C) 2006 Alexei Sokolov
 * http://gwt.components.googlepages.com/
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package synopticgwt.client.util;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import synopticgwt.client.SynopticGWT;

public class TooltipListener implements MouseOverHandler, MouseOutHandler {
    // private static final String DEFAULT_TOOLTIP_STYLE = "TooltipPopup";
    private static final int DEFAULT_OFFSET_X = 10;
    private static final int DEFAULT_OFFSET_Y = 35;
    private static final int HIDE_DELAY = 1000;

    private Tooltip tooltip;
    private String text;
    private String urlLink;
    private String styleName;
    private int delay;
    private int offsetX = DEFAULT_OFFSET_X;
    private int offsetY = DEFAULT_OFFSET_Y;

    public TooltipListener(String text, String urlLink, int delay,
            String styleName) {
        this.text = text;
        this.urlLink = urlLink;
        this.delay = delay;
        this.styleName = styleName;
    }

    public static void setTooltip(Widget widget, String s, String urlLink) {
        // TODO: Use DEFAULT_TOOLTIP_STYLE or "tooltip" for style?
        if (!(widget instanceof HasAllMouseHandlers)) {
            return;
        }
        TooltipListener tooltip = new TooltipListener(s, urlLink, 5000,
                "tooltip");
        ((HasAllMouseHandlers) widget).addMouseOverHandler(tooltip);
        ((HasAllMouseHandlers) widget).addMouseOutHandler(tooltip);

    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        if (tooltip != null) {
            Timer t = new Timer() {

                public void run() {
                    tooltip.hide();
                }

            };
            t.schedule(HIDE_DELAY);
        }
    }

    @Override
    public void onMouseOver(MouseOverEvent event) {
        // Only show the tool-tip if tool-tips are enabled globally.
        if (!SynopticGWT.entryPoint.showHelpToolTips.getValue()) {
            return;
        }
        Widget sender = (Widget) event.getSource();
        if (tooltip != null)
            tooltip.hide();
        tooltip = new Tooltip(sender, offsetX, offsetY, text, urlLink, delay,
                styleName);
        tooltip.show();
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    private static class Tooltip extends PopupPanel {
        private int delay;
        static List<Tooltip> activeTips = new LinkedList<Tooltip>();

        public Tooltip(Widget sender, int offsetX, int offsetY,
                final String text, final String urlLink, final int delay,
                final String styleName) {
            super(true);

            this.delay = delay;

            HTML contents = new HTML(text);
            VerticalPanel vp = new VerticalPanel();
            vp.add(contents);

            if (urlLink != null) {
                Anchor link = new Anchor("More information", urlLink);
                vp.add(link);
            }

            add(vp);
            int left = sender.getAbsoluteLeft() + offsetX;
            int top = sender.getAbsoluteTop() + offsetY;

            setPopupPosition(left, top);
            setStyleName(styleName);
        }

        public void show() {
            // Hide any actively displayed tool tips.
            while (activeTips.size() != 0) {
                Tooltip activeTip = activeTips.get(0);
                activeTip.hide();
                activeTips.remove(activeTip);
            }

            super.show();
            activeTips.add(this);

            Timer t = new Timer() {

                public void run() {
                    Tooltip.this.hide();
                    activeTips.remove(Tooltip.this);
                }

            };
            t.schedule(delay);
        }
    }

}