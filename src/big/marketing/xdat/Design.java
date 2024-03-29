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

import java.io.Serializable;
import java.util.Hashtable;

import org.apache.log4j.Logger;

/**
 * A Design represents a row in the {@link data.DataSheet}. It stores the values for each {@link data.Parameter} of the dataSheet.
 */
public class Design implements Serializable {
	static Logger logger = Logger.getLogger(Design.class);

	/** The version tracking unique identifier for Serialization. */
	static final long serialVersionUID = 0004;

	/** The String parameter values. */
	private Hashtable<Parameter, String> stringParameterValues = new Hashtable<Parameter, String>(0, 1);

	/** The numerical parameter values. */
	private Hashtable<Parameter, Float> numericalParameterValues = new Hashtable<Parameter, Float>(0, 1);

	/** The design id. */
	private int id;

	/**
	 * is used to store the information is the design is filtered. Information is kept for
	 * each {@link chart.Filter} individually. Each Filter is responsible for updating this
	 * Hashtable himself. This makes the code a little less secure but yields significant
	 * benefits in terms of performance because this way the design must only be checked with
	 * respect to a Filter that is currently being modified.
	 * .
	 */
	private Hashtable<Filter, Boolean> activationMap = new Hashtable<Filter, Boolean>(0, 1);

	/**
	 * is used to store the information whether the design is selected in the data sheet.
	 * This information could be evaluated every time it is needed, but storing it and only
	 * updating it when selection changes is more convenient.
	 */
	private boolean selected = false;

	/**
	 * /**
	 * Instantiates a new design.
	 * 
	 * @param id the design id
	 */
	public Design(int id) {
		this.id = id;
	}

	public void setStringValue(Parameter param, String value) {
		stringParameterValues.put(param, value);
	}

	public void setNumValue(Parameter param, float value) {
		this.numericalParameterValues.put(param, value);
	}

	/**
	 * Gets the numeric (double) representation of a value for a given parameter.
	 * @param param the parameter for which the value should be returned.
	 * @return the parameter value
	 * @throws IllegalArgumentException if the parameter is unknown to the design.
	 */
	public double getDoubleValue(Parameter param) {

		if (stringParameterValues.containsKey(param)) {
			return param.getDoubleValueOf(stringParameterValues.get(param));
		} else if (numericalParameterValues.containsKey(param) && param.isNumeric()) {
			return (numericalParameterValues.get(param));
		} else if (numericalParameterValues.containsKey(param)) {
			return param.getDoubleValueOf(Float.toString(numericalParameterValues.get(param)));
		} else {
			throw new IllegalArgumentException("Unknown parameter " + param.getName());
		}
	}

	/**
	 * Gets the String representation of a value for a given parameter.
	 * @param param the parameter for which the value should be returned.
	 * @return the string value for the given parameter
	 * @throws IllegalArgumentException if the parameter is unknown to the design.
	 */
	public String getStringValue(Parameter param) {
		if (stringParameterValues.containsKey(param)) {
			return (stringParameterValues.get(param));
		} else if (numericalParameterValues.containsKey(param)) {
			return Float.toString(numericalParameterValues.get(param));
		} else {
			throw new IllegalArgumentException("Unknown parameter " + param.getName());
		}
	}

	/**
	 * Removes a parameter from the design
	 * @param param the parameter to be removed.
	 * @throws IllegalArgumentException if the parameter is unknown to the design.
	 */
	public void removeParameter(Parameter param) {
		if (stringParameterValues.containsKey(param)) {
			stringParameterValues.remove(param);
		} else if (numericalParameterValues.containsKey(param)) {
			numericalParameterValues.remove(param);
		} else {
			throw new IllegalArgumentException("Unknown parameter " + param.getName());
		}
	}

	/**
	 * Checks whether the design is active.
	 * This check is carried out by looking up each filter in the {@link Design#activationMap}
	 * @param chart the chart
	 * @return true, if the design is active
	 */
	public boolean isActive(ParallelCoordinatesChart chart) {
		for (int i = 0; i < chart.getAxisCount(); i++) {
			Filter uf = chart.getAxis(i).getUpperFilter();
			Filter lf = chart.getAxis(i).getLowerFilter();
			if (!this.activationMap.containsKey(uf)) {
				this.activationMap.put(uf, true);
			}
			if (!this.activationMap.containsKey(lf)) {
				this.activationMap.put(lf, true);
			}
			if (chart.getAxis(i).isFilterInverted()) {
				if (!(this.activationMap.get(uf) || this.activationMap.get(lf)))
					return false;
			} else {
				if (!this.activationMap.get(uf))
					return false;
				if (!this.activationMap.get(lf))
					return false;
			}
		}
		return true;
	}

	/**
	 * Specifies whether the design is still active after applying a given {@link chart.Filter}.
	 * @param filter the filter
	 * @param active the active
	 */
	public void setActive(Filter filter, boolean active) {
		this.activationMap.put(filter, active);
	}

	/**
	 * Checks if the design is inside the filters for a given Axis.
	 * @param axis the axis
	 * @return true, if is inside filters
	 */
	private boolean isInsideBounds(Axis axis) {
		double lower = axis.getLowerFilter().getValue();
		double upper = axis.getUpperFilter().getValue();
		double value = this.getDoubleValue(axis.getParameter());

		if (lower <= value && value <= upper) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the value of the boolean field insideBounds.
	 * @param chart the chart
	 * @return true, if the design is inside all axis bounds on the given chart.
	 */
	public boolean isInsideBounds(ParallelCoordinatesChart chart) {
		for (int i = 0; i < chart.getAxisCount(); i++) {
			if (!isInsideBounds(chart.getAxis(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the value of the boolean field selected.
	 * @return true, if the design is selected.
	 */
	public boolean isSelected() {
		return this.selected;
	}

	/**
	 * Sets the value of the boolean field selected.
	 * @param selected the selected value.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Gets the id of the design.
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of the design.
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}
}
