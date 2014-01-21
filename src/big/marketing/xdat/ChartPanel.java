/*
 *  Copyright 2013, Enguerrand de Rochefort modified by Juhani Jaakkola
 * 
 * This file is part of xdat.
 *
 * xdat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * xdat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with xdat.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package big.marketing.xdat;

import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Panel that is used to display a {@link chart.Chart}.
 * <p>
 * This class should be extended to model specific chart panels.
 */
public abstract class ChartPanel extends JPanel {
	/** The version tracking unique identifier for Serialization. */
	static final long serialVersionUID = 0001;

	/** The chart. */
	private Chart chart;

	/** The data sheet. */
	private DataSheet dataSheet;

	/**
	 * Instantiates a new chart panel.
	 * @param dataSheet the data sheet
	 * @param chart the chart
	 */
	public ChartPanel(DataSheet dataSheet, Chart chart) {
		this.dataSheet = dataSheet;
		this.chart = chart;
	}

	/**
	 * Overridden to implement the painting of the chart.
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(chart.getBackGroundColor());
		this.drawPlotFieldBackground(g);
	}

	/**
	 * Draws the plot field background.
	 * @param g the graphics object
	 */
	public void drawPlotFieldBackground(Graphics g) {
		g.setColor(this.chart.getBackGroundColor());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

	/**
	 * Gets the panel's chart.
	 * @return the chart
	 */
	public Chart getChart() {
		return this.chart;
	}

	/**
	 * Gets the data sheet.
	 * @return the data sheet
	 */
	public DataSheet getDataSheet() {
		return dataSheet;
	}
}
