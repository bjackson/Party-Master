package com.partyrock.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.*;

import com.partyrock.LightMaster;
import com.partyrock.element.ElementController;

/**
 * Manages the custom rendering for elements in our LightWindow. This class has
 * an obscenely long name.
 * @author Matthew
 * 
 */
public class LightWindowElementsTableRenderer {
	private LightMaster master;
	private LightWindow lightWindow;
	private Color separatorColor;

	public LightWindowElementsTableRenderer(LightMaster master, LightWindow lightWindow) {
		this.master = master;
		this.lightWindow = lightWindow;

		separatorColor = new Color(lightWindow.getDisplay(), 87, 87, 87);
	}

	/**
	 * Add all of the listeners to the main elements table necessary for custom
	 * element and animation rendering
	 * @param table the table to add these listeners too
	 */
	public void addCustomListeners(final Table table) {

		// Set a custom height and width
		table.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {

				for (TableColumn c : table.getColumns()) {
					c.setWidth(75);
				}

				// Custom height, which is just some static predefined constant
				event.height = 30;
				if (event.index == 1) {
					// Set the width of the render column, which is proportional
					// to the total time of the show.
					// Magnification, if necessary, will need to be done here
					double songLength = master.getMusicManager().getSongLength();

					// This happens if there isn't a song
					if (songLength == -1) {
						// Subtract the width of the first column in the default
						// case, and just render the enter table width
						event.width = getAnimationColumnWidth(table);
					} else {
						// This is completely arbitrary
						event.width = (int) (10 * songLength);
					}

				}
			}
		});

		// The all important render code! Renders everything necessary for
		// animations
		table.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {

				// Get the item that's being rendered
				TableItem item = (TableItem) event.item;
				ElementController element = (ElementController) item.getData();

				// Don't let any silly selection stuff be rendered
				event.detail &= ~SWT.HOT;

				Display display = lightWindow.getDisplay();

				GC gc = event.gc;
				// Area of the entire visible table
				Rectangle area = table.getClientArea();

				// I don't remember why this clipping stuff was necessary but
				// I'll update this comment when I figure it out
				// Clipping
				int width = area.x + area.width - event.x + 1;

				if (event.index == 0) {
					System.out.println(area.width + " x " + area.height);
					System.out.println(event.x);
					System.out.println(event.width);
				}

				if (width < 0) {
					// fix for drag and drop, where this can become < 0
					width = 0;
				}
				Region region = new Region();
				gc.getClipping(region);
				region.add(event.x, event.y, width, event.height + 1);
				gc.setClipping(region);
				region.dispose();

				/*
				 * This is confusing. rect is the bounds of the event, so the Y
				 * value is correct, but the width is wrong, I think since we're
				 * increasing the width of the second table by some value. We
				 * fix the rect.width so it doesn't suck, and everything else
				 * (x, y, and height) should be right
				 */
				Rectangle rect = event.getBounds();
				// The width is correct for the first column, but not the one
				// with animations
				if (event.index == 1) {
					rect.width = getAnimationColumnWidth(table);
				}

				Color origBackground = gc.getBackground();

				// Start Rendering

				// Select a background color based on if the channel is selected
				// or not
				Color backgroundColor = ((event.detail & SWT.SELECTED) != 0) ? display.getSystemColor(SWT.COLOR_BLUE)
						: display.getSystemColor(SWT.COLOR_BLACK);
				gc.setBackground(backgroundColor);
				gc.fillRectangle(rect.x, rect.y, rect.width, rect.height);

				// Fill the background based on selection
				if (event.index == 0) {
					// First column, where the name should go

					// Tell the ElementRenderer to render the name
					element.getRenderer().renderName(gc, rect);
				}

				// Set the color of the separator color
				gc.setForeground(separatorColor);
				gc.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width, rect.y + rect.height - 1);

				// End rendering

				// Resets the original background color
				gc.setBackground(origBackground);

				// We've handled selection here
				event.detail &= ~SWT.SELECTED;
			}
		});
	}

	public int getAnimationColumnWidth(final Table table) {
		return table.getSize().x - table.getColumn(0).getWidth();
	}
}
