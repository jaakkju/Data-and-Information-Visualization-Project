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

import java.awt.Color;
import java.awt.Dimension;
import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * A serializable representation of all relevant settings for a chart which is displayed on
 * a ChartFrame.
 * This class should be extended to model actual charts.
 * @see chart.ParallelCoordinatesChart
 */
public abstract class Chart implements Serializable {
	static Logger logger = Logger.getLogger(Chart.class);

	/** The version tracking unique identifier for Serialization. */
	static final long serialVersionUID = 1;

	/** The size of this Chart. */
	private Dimension dimensions;

	/**
	 * The data sheet that is displayed in this Chart.
	 * @see data.DataSheet
	 */
	private DataSheet dataSheet;

	/**
	 * Instantiates a new chart.
	 * @param dataSheet the data sheet
	 */
	public Chart(DataSheet dataSheet, Dimension dimensions) {
		this.dataSheet = dataSheet;
		this.dimensions = dimensions;
	}

	/**
	 * Gets the title.
	 * @return the title
	 */
	public abstract String getTitle();

	/**
	 * Determines the width of this Chart.
	 * @return the width of this Chart
	 */
	public abstract int getWidth();

	/**
	 * Determines the height of this Chart.
	 * @return the height of this Chart
	 */
	public abstract int getHeight();

	/**
	 * Gets the data sheet.
	 * @return the data sheet
	 */
	public DataSheet getDataSheet() {
		return dataSheet;
	}

	/**
	 * Sets the data sheet.
	 * @param dataSheet the new data sheet
	 */
	public void setDataSheet(DataSheet dataSheet) {
		this.dataSheet = dataSheet;
	}

	/**
	 * Gets the back ground color.
	 * @return the back ground color
	 */
	public abstract Color getBackGroundColor();

	/**
	 * Sets the back ground color.
	 * @param backGroundColor the new back ground color
	 */
	public abstract void setBackGroundColor(Color backGroundColor);

	/**
	 * Reset display settings to default.
	 */
	public abstract void resetDisplaySettingsToDefault();

	public Dimension getDimensions() {
		return dimensions;
	}

	public void setDimensions(Dimension dimensions) {
		this.dimensions = dimensions;
	}
}
