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

package big.marketing.xdat.chart;

import java.io.Serializable;

import org.apache.log4j.Logger;

import big.marketing.xdat.data.DataSheet;
import big.marketing.xdat.data.Parameter;

/**
 * Provides the possibility to filter the Designs on a {@link chart.ParallelCoordinatesChart}.
 * Filtered Designs are not displayed on he Chart anymore, or are displayed in a different color than the unfiltered Designs.
 * Each filter is a small triangular draggable symbol on an Axis. Each Axis has two filters, an upper Filter and a lower Filter. The upper
 * Filter is used to filter out all Designs that have a larger value than the Filter value for the Parameter represented by the Filter's
 * Axis. Accordingly, the lower Filter is used to filter out all Designs that have a lower value than the Filter value for the Parameter
 * represented by the Filter's Axis.
 * The behavior described above changes when
 * <ul>
 * <li>the Filter's Axis is inverted: In this case the lower Filter filters out large values and the upper Filter filters out small values.
 * <li>the Filter is inverted. In this case all designs with values between the two Filters become inactive.
 * </ul>
 * 
 * @see ParallelCoordinatesChart
 * @see Axis
 * @see data.Parameter
 * @see data.Design
 */
public class Filter implements Serializable {
	static Logger logger = Logger.getLogger(Filter.class);

	/** The version tracking unique identifier for Serialization. */
	static final long serialVersionUID = 0002;

	/** Constant that describes an upper filter. */
	public static final int UPPER_FILTER = 0;

	/** Constant that described a lower filter. */
	public static final int LOWER_FILTER = 1;

	/** Filter tolerance to compensate round off errors and prevent filtering designs that are not supposed to be filtered. */
	public static final double FILTER_TOLERANCE = 0.00001;

	/** The data sheet. */
	private DataSheet dataSheet;

	/** The filter type. Upper or Lower */
	private int filterType;

	/** The axis to which the Filter belongs. */
	private Axis axis;

	/** The x position of the Filter on the Chart. */
	private int xPos;

	/** The filter's value. */
	private double value;

	/**
	 * Instantiates a new filter.
	 * @param dataSheet the data sheet
	 * @param axis the axis to which the Filter belongs
	 * @param filterType the filter type: UPPER_FILTER, LOWER_FILTER
	 */
	public Filter(DataSheet dataSheet, Axis axis, int filterType) {
		this.dataSheet = dataSheet;
		this.axis = axis;
		this.filterType = filterType;

		this.reset();
	}

	/**
	 * Gets the current value of this Filter.
	 * @return the current value of this Filter.
	 */
	public double getValue() {
		return this.value;
	}

	/**
	 * Sets the current value of this Filter.
	 * Also calls applyToDesigns in order to make sure that the modified Filter positions is accounted for in the Filter states of all
	 * Designs.
	 * @param value the new current value of this Filter.
	 */
	public void setValue(double value) {
		this.value = value;
		apply();
	}

	/**
	 * Gets the x position of this Filter on the Chart.
	 * @return the x position of this Filter on the Chart.
	 */
	public int getXPos() {
		return xPos;
	}

	/**
	 * Sets the x position of this Filter on the Chart.
	 * @param pos the new x position of this Filter on the Chart.
	 */
	public void setXPos(int pos) {
		this.xPos = pos;
	}

	/**
	 * Gets the y position of this Filter on the Chart.
	 * @return the y position of this Filter on the Chart.
	 */
	public int getYPos() {
		if (this.axis.getTicCount() == 1)
			return this.getAxis().getChart().getAxisTopPos() + (int) (this.getAxis().getChart().getAxisHeight() * 0.5);
		else {
			double upperLimit = this.axis.getMax();
			double lowerLimit = this.axis.getMin();
			if (value > upperLimit)
				value = upperLimit;
			else if (value < lowerLimit)
				value = lowerLimit;
			double valueRange = upperLimit - lowerLimit;

			int topPos = this.axis.getChart().getAxisTopPos() - 1;
			int bottomPos = topPos + this.getAxis().getChart().getAxisHeight() + 1;
			double posRange = bottomPos - topPos;
			double ratio;
			int yPos;
			if (axis.isAxisInverted()) {
				ratio = (value - lowerLimit) / valueRange;
				yPos = topPos + (int) (posRange * ratio);
			} else {
				ratio = (value - lowerLimit) / valueRange;
				yPos = bottomPos - (int) (posRange * ratio);
			}
			return yPos;
		}
	}

	/**
	 * Sets the y position of this Filter on the Chart.
	 * Also calls applyToDesigns in order to make sure that the modified Filter positions is accounted for in the Filter states of all
	 * Designs.
	 * @param pos the new y position of this Filter on the Chart.
	 */
	public void setYPos(int pos) {
		double upperLimit = this.axis.getMax();
		double lowerLimit = this.axis.getMin();
		double valueRange = upperLimit - lowerLimit;
		int topPos = this.axis.getChart().getAxisTopPos() - 1;
		int bottomPos = topPos + this.getAxis().getChart().getAxisHeight() + 1;
		double posRange = bottomPos - topPos;
		double ratio;
		if (axis.isAxisInverted())
			ratio = (pos - topPos) / posRange;
		else
			ratio = (bottomPos - pos) / posRange;
		this.value = lowerLimit + valueRange * ratio;
		apply();
	}

	/**
	 * Gets the highest position that this Filter may reach.
	 * Used to make sure that a lower Filter is not dragged to a position above the upper Filter or outside the Axis range.
	 * @return the highest reachable position
	 */
	public int getHighestPos() {
		int pos;
		if (this.getFilterType() == Filter.UPPER_FILTER) {
			pos = this.getAxis().getChart().getAxisTopPos();
		} else if (this.getFilterType() == Filter.LOWER_FILTER) {
			pos = this.getAxis().getUpperFilter().getYPos();
		} else {
			pos = this.getAxis().getChart().getAxisTopPos();
		}
		return pos - 1;
	}

	/**
	 * Gets the lowest position that this Filter may reach.
	 * Used to make sure that a upper Filter is not dragged to a position below the lower Filter or outside the Axis range.
	 * @return the lowest reachable position
	 */
	public int getLowestPos() {
		int pos;
		if (this.getFilterType() == Filter.UPPER_FILTER) {
			pos = this.getAxis().getLowerFilter().getYPos();
		} else if (this.getFilterType() == Filter.LOWER_FILTER) {
			pos = this.getAxis().getChart().getAxisTopPos() + this.getAxis().getChart().getAxisHeight();
		} else {
			pos = this.getAxis().getChart().getAxisTopPos() + this.getAxis().getChart().getAxisHeight();
		}
		return pos + 1;
	}

	/**
	 * Gets the filter type. Upper or lower.
	 * @return the filter type
	 */
	public int getFilterType() {
		return filterType;
	}

	/**
	 * Gets the axis to which the Filter belongs.
	 * @return the axis to which the Filter belongs.
	 */
	public Axis getAxis() {
		return axis;
	}

	/**
	 * Applies the Filter to all designs.
	 * This method has to be called every time this Filter value is modified. This allows to check the Filtering state of a Design with
	 * respect to a Filter only when there is actually a reason for checking rather then upon every repaint.
	 * This implementation was chosen because the check if a design is filtered would become to CPU expensive otherwise because each Filter
	 * would have to be checked for each design every time the Chart is repainted.
	 */
	public void apply() {
		Parameter param = this.axis.getParameter();
		double tolerance = this.getAxis().getRange() * FILTER_TOLERANCE;
		if (tolerance <= 0) {
			tolerance = FILTER_TOLERANCE;
		}
		double value = this.getValue();
		if (this.filterType == UPPER_FILTER && axis.isFilterInverted() && axis.isAxisInverted()) {
			for (int i = 0; i < this.dataSheet.getDesignCount(); i++) {
				if (this.dataSheet.getDesign(i).getDoubleValue(param) - tolerance > value)
					this.dataSheet.getDesign(i).setActive(this, false);
				else
					this.dataSheet.getDesign(i).setActive(this, true);
			}
		} else if (this.filterType == LOWER_FILTER && axis.isFilterInverted() && axis.isAxisInverted()) {
			for (int i = 0; i < this.dataSheet.getDesignCount(); i++) {
				if (this.dataSheet.getDesign(i).getDoubleValue(param) + tolerance < value)
					this.dataSheet.getDesign(i).setActive(this, false);
				else
					this.dataSheet.getDesign(i).setActive(this, true);
			}
		}

		else if (this.filterType == UPPER_FILTER && axis.isAxisInverted()) {
			for (int i = 0; i < this.dataSheet.getDesignCount(); i++) {
				if (this.dataSheet.getDesign(i).getDoubleValue(param) + tolerance < value)
					this.dataSheet.getDesign(i).setActive(this, false);
				else
					this.dataSheet.getDesign(i).setActive(this, true);
			}
		} else if (this.filterType == LOWER_FILTER && axis.isAxisInverted()) {
			for (int i = 0; i < this.dataSheet.getDesignCount(); i++) {
				if (this.dataSheet.getDesign(i).getDoubleValue(param) - tolerance > value)
					this.dataSheet.getDesign(i).setActive(this, false);
				else
					this.dataSheet.getDesign(i).setActive(this, true);
			}
		}

		else if (this.filterType == UPPER_FILTER && axis.isFilterInverted()) {
			for (int i = 0; i < this.dataSheet.getDesignCount(); i++) {
				if (this.dataSheet.getDesign(i).getDoubleValue(param) + tolerance < value)
					this.dataSheet.getDesign(i).setActive(this, false);
				else
					this.dataSheet.getDesign(i).setActive(this, true);
			}
		} else if (this.filterType == LOWER_FILTER && axis.isFilterInverted()) {
			for (int i = 0; i < this.dataSheet.getDesignCount(); i++) {
				if (this.dataSheet.getDesign(i).getDoubleValue(param) - tolerance > value)
					this.dataSheet.getDesign(i).setActive(this, false);
				else
					this.dataSheet.getDesign(i).setActive(this, true);
			}
		} else if (this.filterType == UPPER_FILTER) {
			for (int i = 0; i < this.dataSheet.getDesignCount(); i++) {
				if (this.dataSheet.getDesign(i).getDoubleValue(param) - tolerance > value)
					this.dataSheet.getDesign(i).setActive(this, false);
				else
					this.dataSheet.getDesign(i).setActive(this, true);
			}
		} else if (this.filterType == LOWER_FILTER) {
			for (int i = 0; i < this.dataSheet.getDesignCount(); i++) {
				if (this.dataSheet.getDesign(i).getDoubleValue(param) + tolerance < value)
					this.dataSheet.getDesign(i).setActive(this, false);
				else
					this.dataSheet.getDesign(i).setActive(this, true);
			}
		}
	}

	/**
	 * Resets the filter.
	 */
	public void reset() {
		if (this.filterType == UPPER_FILTER && this.axis.isAxisInverted()) {
			this.setValue(this.axis.getMax());
		} else if (this.filterType == UPPER_FILTER) {
			double value = this.axis.getMax();
			logger.info("reset: maxValue = " + value);
			this.setValue(value);
		} else if (this.filterType == LOWER_FILTER && this.axis.isAxisInverted()) {
			this.setValue(this.axis.getMax());
		} else {
			this.setValue(this.axis.getMin());
		}
	}
}
