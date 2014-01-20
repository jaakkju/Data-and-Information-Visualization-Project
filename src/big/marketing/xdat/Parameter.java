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
import java.text.ParseException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * A Parameter represents a parameter of a {@link data.DataSheet}.
 * It is used to store information about the type of data stored in a column of the DataSheet.
 * Two data types are supported: Numeric and Discrete.
 * Numeric parameters are used for columns that only contain numbers. Discrete parameters are used for all columns that contain at least one
 * non-numeric value. All values are stored in a TreeSet and sorted in alphabetical order. This makes it possible to also treat information
 * on parameters that are not quantifiable, such as different shapes of an object or similar.
 * 
 */
public class Parameter {
	static Logger logger = Logger.getLogger(Parameter.class);

	/** Datasheet to which the parameter belongs. */
	private DataSheet dataSheet;

	/** The parameter name. */
	private String name;

	/** Specifies whether the parameter is numeric. If it is not, it is discrete. */
	private boolean numeric = true;

	/** The discrete levels. Only applies for non-numeric parameters. */
	private TreeSet<String> discreteLevels = new TreeSet<String>(new ReverseStringComparator());

	/**
	 * Instantiates a new parameter.
	 * @param name the parameter name
	 */
	public Parameter(String name, DataSheet dataSheet) {
		this.name = name;
		this.dataSheet = dataSheet;
	}

	/**
	 * Gets the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Finds the maximum value for this parameter in the datasheet.
	 * @return the maximum value
	 */
	public double getMaxValue() {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < dataSheet.getDesignCount(); i++) {
			double value = dataSheet.getDesign(i).getDoubleValue(this);
			if (value > max)
				max = value;
		}
		return max;
	}

	/**
	 * Finds the minimum value for this parameter in the datasheet.
	 * @return the minimum value
	 */
	public double getMinValue() {
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < dataSheet.getDesignCount(); i++) {
			double value = dataSheet.getDesign(i).getDoubleValue(this);
			if (value < min)
				min = value;
		}
		return min;
	}

	/**
	 * Returns whether the parameter is numeric or discrete from the stored setting.
	 * @return true, if the parameter is numeric
	 */
	public boolean isNumeric() {
		return numeric;
	}

	/**
	 * Gets a numeric representation of a string value for this parameter.
	 * If the parameter is numeric, an attempt is made to parse the string as a Double. If this attempt leads to a ParseException, the
	 * parameter is not considered numeric anymore, but is transformed into a discrete parameter.
	 * If the parameter is not numeric, the string is looked up in the TreeSet discreteLevels that should contain all discrete values (that
	 * is Strings) that were found in the data sheet for this parameter. If the value is not found it is added as a new discrete level for
	 * this parameter. The treeSet is then searched again in order to get the correct index of the new discrete level.
	 * If this second search does not yield the result, something unexpected has gone wrong and a CorruptDataException is thrown.
	 * @param string the string
	 * @return the numeric representation of the given string
	 */
	public double getDoubleValueOf(String string) {
		if (this.numeric) {
			try {
				double value = NumberParser.parseNumber(string);
				return value;
			} catch (ParseException e1) {
				this.numeric = false;
			}
		}

		int index = 0;
		Iterator<String> it = discreteLevels.iterator();
		while (it.hasNext()) {
			if (string.equalsIgnoreCase(it.next())) {
				return (double) index;
			}
			index++;
		}

		// String not found, add it to discrete levels
		this.discreteLevels.add(string);
		index = 0;
		it = discreteLevels.iterator();
		while (it.hasNext()) {
			String next = it.next();
			if (string.equalsIgnoreCase(next)) {
				return (double) index;
			}
			index++;
		}
		throw new CorruptDataException(this);
	}

	/**
	 * Gets the string representation of a given double value for this parameter.
	 * If the parameter is numeric, the provided value is simply converted to a String and returned.
	 * If it is discrete, the double value is casted to an Integer value and this value is used as an index to look up the corresponding
	 * discrete value string in the TreeSet discreteLevels.
	 * If no value is found for the given index the data is assumed to be corrupt and a CorruptDataException is thrown.
	 * @param value the numeric value
	 * @return the string representation of the given double value for this parameter.
	 */
	public String getStringValueOf(double value) {
		if (this.numeric) {
			return Double.toString(value);

		} else {
			int index = (int) value;
			int currentIndex = 0;
			Iterator<String> it = discreteLevels.iterator();

			while (it.hasNext()) {
				String next = it.next();
				if (currentIndex == index) {
					return next;
				}
				currentIndex++;
			}
			throw new CorruptDataException(this);
		}
	}

	/**
	 * Gets the discrete level count in the TreeSet that stores all discrete levels.
	 * Only applies to non-numeric parameters.
	 * @return the discrete level count
	 */
	public int getDiscreteLevelCount() {
		if (this.isNumeric()) {
			throw new RuntimeException("Parameter " + this.name + " is numeric!");
		} else {
			return this.discreteLevels.size();
		}
	}

	/**
	 * Comparator for the discrete levels TreeSet to sort the data alphabetically.
	 */
	class ReverseStringComparator implements Comparator<String>, Serializable {

		/** The version tracking unique identifier for Serialization. */
		static final long serialVersionUID = 0000;

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(String s1, String s2) {
			return (s2.compareToIgnoreCase(s1));
		}
	}
}
