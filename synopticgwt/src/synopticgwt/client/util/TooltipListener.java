/*
Tooltip component for GWT
Copyright (C) 2006 Alexei Sokolov http://gwt.components.googlepages.com/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA

 */
package synopticgwt.client.util;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

public class TooltipListener implements MouseOverHandler, MouseOutHandler {
	private static final String DEFAULT_TOOLTIP_STYLE = "TooltipPopup";
	private static final int DEFAULT_OFFSET_X = 10;
	private static final int DEFAULT_OFFSET_Y = 35;
	
	private Tooltip tooltip;
	private String text;
	private String styleName;
	private int delay;
	private int offsetX = DEFAULT_OFFSET_X;
	private int offsetY = DEFAULT_OFFSET_Y;

	public TooltipListener(String text, int delay) {
		this(text, delay, DEFAULT_TOOLTIP_STYLE);
	}

	public TooltipListener(String text, int delay, String styleName) {
		this.text = text;
		this.delay = delay;
		this.styleName = styleName;
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		if (tooltip != null)
			tooltip.hide();
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		Widget sender = (Widget) event.getSource();
		if (tooltip != null)
			tooltip.hide();
		tooltip = new Tooltip(sender, offsetX, offsetY, text, delay, styleName);
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

		public Tooltip(Widget sender, int offsetX, int offsetY, 
				final String text, final int delay, final String styleName) {
			super(true);

			this.delay = delay;

			HTML contents = new HTML(text);
			add(contents);

			int left = sender.getAbsoluteLeft() + offsetX;
			int top = sender.getAbsoluteTop() + offsetY;

			setPopupPosition(left, top);
			setStyleName(styleName);
		}

		public void show() {
			super.show();

			Timer t = new Timer() {

				public void run() {
					Tooltip.this.hide();
				}

			};
			t.schedule(delay);
		}
	}

} 