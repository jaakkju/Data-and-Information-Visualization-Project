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

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import big.marketing.data.HealthMessage;

/**
 * A representation of the data imported from a text file.
 */
public class DataSheet {
	static Logger logger = Logger.getLogger(DataSheet.class);

	/** The Vector containing all Designs. */
	private Vector<Design> data = new Vector<Design>(0, 1);

	/** The parameters. */
	private Vector<Parameter> parameters = new Vector<Parameter>(0, 1);

	public DataSheet(List<HealthMessage> healthMessages) {

		// TODO It is probably possible to make more general solution for this
		this.parameters.add(new Parameter(HealthMessage.FIELDS[0], this));
		this.parameters.add(new Parameter(HealthMessage.FIELDS[2], this));
		this.parameters.add(new Parameter(HealthMessage.FIELDS[3], this));
		this.parameters.add(new Parameter(HealthMessage.FIELDS[4], this));
		this.parameters.add(new Parameter(HealthMessage.FIELDS[5], this));
		this.parameters.add(new Parameter(HealthMessage.FIELDS[6], this));
		this.parameters.add(new Parameter(HealthMessage.FIELDS[1], this));
		this.parameters.add(new Parameter(HealthMessage.FIELDS[7], this));

		int idCounter = 0;
		for (HealthMessage healthMessage : healthMessages) {
			Design design = new Design(idCounter++);

			design.setStringValue(parameters.get(0), healthMessage.getHostname());
			design.setNumValue(parameters.get(1), healthMessage.getDiskUsage());
			design.setNumValue(parameters.get(2), healthMessage.getPageFileUsage());
			design.setNumValue(parameters.get(3), healthMessage.getNumProcs());
			design.setNumValue(parameters.get(4), healthMessage.getLoadAverage());
			design.setNumValue(parameters.get(5), healthMessage.getPhysicalMemoryUsage());
			design.setStringValue(parameters.get(6), healthMessage.statusValToString());
			design.setStringValue(parameters.get(7), healthMessage.connMadeToString());

			data.add(design);
		}
	}

	/**
	 * Gets the Design with index i.
	 * @param i the index
	 * @return the Design
	 */
	public Design getDesign(int i) {
		return this.data.get(i);
	}

	/**
	 * Adds a Design to the DataSheet.
	 * @param design the Design
	 */
	public void addDesign(Design design) {
		this.data.add(design);
	}

	/**
	 * Gets the Parameter count.
	 * @return the Parameter count
	 */
	public int getParameterCount() {
		return this.parameters.size();
	}

	/**
	 * Gets the Parameter name of the Parameter with index index.
	 * @param index the index
	 * @return the parameter name
	 */
	public String getParameterName(int index) {
		if (index >= this.parameters.size() || index < 0)
			throw new IllegalArgumentException("Invalid Index " + index);
		return this.parameters.get(index).getName();
	}

	/**
	 * Gets the Parameter with the index index.
	 * @param index the Parameter index
	 * @return the Parameter
	 */
	public Parameter getParameter(int index) {
		if (index >= this.parameters.size() || index < 0)
			throw new IllegalArgumentException("Invalid Index " + index);
		return this.parameters.get(index);
	}

	/**
	 * Gets the Parameter with the name parameterName.
	 * @param parameterName the Parameter name
	 * @return the Parameter
	 */
	public Parameter getParameter(String parameterName) {
		for (int i = 0; i < this.parameters.size(); i++) {
			if (parameterName.equals(this.parameters.get(i).getName())) {
				return this.parameters.get(i);
			}
		}
		throw new IllegalArgumentException("Parameter " + parameterName + " not found");
	}

	/**
	 * Gets the index of the Parameter with the name parameterName.
	 * @param parameterName the Parameter name
	 * @return the index
	 */
	public int getParameterIndex(String parameterName) {
		for (int i = 0; i < this.parameters.size(); i++) {
			if (parameterName.equals(this.parameters.get(i).getName())) {
				return i;
			}
		}
		throw new IllegalArgumentException("Parameter " + parameterName + " not found");
	}

	/**
	 * Checks if the given Parameter exists.
	 * @param param the Parameter
	 * @return true, if the parameter exists
	 */
	public boolean parameterExists(Parameter param) {
		return this.parameters.contains(param);
	}

	/**
	 * Gets the maximum value of a given Parameter in the DataSheet.
	 * @param param the Parameter
	 * @return the maximum value of the given Parameter.
	 */
	public double getMaxValueOf(Parameter param) {
		if (param.isNumeric()) {
			double max = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < this.data.size(); i++) {
				if (max < this.data.get(i).getDoubleValue(param)) {
					max = this.data.get(i).getDoubleValue(param);
				}
			}
			return max;
		} else {
			return param.getDiscreteLevelCount() - 1;
		}
	}

	/**
	 * Gets the minimum value of a given Parameter.
	 * @param param the parameter
	 * @return the minimum value of the given Parameter.
	 */
	public double getMinValueOf(Parameter param) {
		if (param.isNumeric()) {
			double min = Double.POSITIVE_INFINITY;
			for (int i = 0; i < this.data.size(); i++) {
				if (min > this.data.get(i).getDoubleValue(param)) {
					min = this.data.get(i).getDoubleValue(param);
				}
			}
			return min;
		} else {
			return 0.0;
		}
	}

	/**
	 * Gets the design count.
	 * @return the design count
	 */
	public int getDesignCount() {
		return this.data.size();
	}

	/**
	 * Evaluate each Design to check whether it is within all axis bounds.
	 * @param chart the chart
	 * @see Design
	 */
	public void evaluateBoundsForAllDesigns(ParallelCoordinatesChart chart) {
		for (int i = 0; i < this.getDesignCount(); i++) {
			this.data.get(i).evaluateBounds(chart);
		}
	}

	/**
	 * Return parameter name at certain index
	 * @param index number
	 * @return parameter name
	 */
	public Object getElementAt(int index) {
		return this.parameters.get(index).getName();
	}

	/**
	 * Returns the size of the parameter list
	 * @return
	 */
	public int getSize() {
		return this.parameters.size();
	}
}
