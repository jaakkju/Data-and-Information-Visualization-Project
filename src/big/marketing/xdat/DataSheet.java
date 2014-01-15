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

import javax.swing.ListModel;
import javax.swing.ProgressMonitor;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import big.marketing.data.HealthMessage;

/**
 * A representation of the data imported from a text file.
 * Everytime the user imports data from a text file the data is stored in a DataSheet. The data sheet is a kind of wrapper class for the
 * collection of all rows in the text file. Each row represents one {@link data.Design}.
 * In addition to storing the Designs and providing the possibility to display them in a JTable by implementing TableModel, the DataSheet
 * class also keeps track of the Parameters in the data set. Each column represents one {@link data.Parameter}.
 * The third main function of this class is to actually read the data from a text file. While doing this, the DataSheet also collects some
 * additional information, such as
 * <ul>
 * <li>the parameter types (numeric/discrete, see the Parameter class for further info)
 * <li>the Parameter names. These are obtained from a header line in the data or are given default names such as Parameter 1, Parameter 2
 * and so on.
 * </ul>
 * A ListModel is implemented for other functions of the program to be able to display parameters in a JList.
 * Finally, the DataSheet also keeps track of all {@link data.Cluster}s. However, it does not store the information to which Cluster each
 * Design belongs. This information is stored in the Designs themselves. It is important to understand this because it means that whenever
 * {@link DataSheet#updateData(String, boolean, ProgressMonitor) } is called this information is lost.
 */
@SuppressWarnings("rawtypes")
public class DataSheet implements ListModel {
	static Logger logger = Logger.getLogger(DataSheet.class);

	private String[] params = { "Hostname", "statusVal", "DiskUsage", "PageFileUsage", "NumProcs", "LoadAverage", "PhysicalMemoryUsage",
	      "ConnMade" };

	/** The cluster set. */
	private ClusterSet clusterSet;

	/** The Vector containing all Designs. */
	private Vector<Design> data = new Vector<Design>(0, 1);

	/** The parameters. */
	private Vector<Parameter> parameters = new Vector<Parameter>(0, 1);

	/** List Model Listeners to enable updating the GUI. */
	private transient Vector<ListDataListener> listDataListener = new Vector<ListDataListener>();

	public DataSheet(List<HealthMessage> healthMessages) {
		this.clusterSet = new ClusterSet(this);

		// TODO Dirty coding
		this.parameters.add(new Parameter(params[0], this, false));
		this.parameters.add(new Parameter(params[1], this, true));
		this.parameters.add(new Parameter(params[2], this, true));
		this.parameters.add(new Parameter(params[3], this, true));
		this.parameters.add(new Parameter(params[4], this, true));
		this.parameters.add(new Parameter(params[5], this, true));
		this.parameters.add(new Parameter(params[6], this, true));
		this.parameters.add(new Parameter(params[7], this, true));

		int idCounter = 0;
		for (HealthMessage healthMessage : healthMessages) {
			Design design = new Design(idCounter++);

			// { "Hostname", "statusVal", "DiskUsage", "PageFileUsage", "NumProcs", "LoadAverage", "PhysicalMemoryUsage", "ConnMade" };
			design.setStringValue(parameters.get(0), healthMessage.getHostname());
			design.setNumValue(parameters.get(1), healthMessage.getStatusVal());
			design.setNumValue(parameters.get(2), healthMessage.getDiskUsage());
			design.setNumValue(parameters.get(3), healthMessage.getPageFileUsage());
			design.setNumValue(parameters.get(4), healthMessage.getNumProcs());
			design.setNumValue(parameters.get(5), healthMessage.getLoadAverage());
			design.setNumValue(parameters.get(6), healthMessage.getPhysicalMemoryUsage());
			design.setNumValue(parameters.get(7), healthMessage.getConnMade());

			data.add(design);
		}

		//		logger.info("Loaded health messages to DataSheet " + idCounter + " values");
	}

	/**
	 * Gets the Design with index i.
	 * 
	 * @param i the index
	 * @return the Design
	 */
	public Design getDesign(int i) {
		return this.data.get(i);
	}

	/**
	 * Adds a Design to the DataSheet.
	 * 
	 * @param design the Design
	 */
	public void addDesign(Design design) {
		this.data.add(design);
	}

	/**
	 * Gets the Parameter count.
	 * 
	 * @return the Parameter count
	 */
	public int getParameterCount() {
		return this.parameters.size();
	}

	/**
	 * Gets the Parameter name of the Parameter with index index.
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @param param the Parameter
	 * @return true, if the parameter exists
	 */
	public boolean parameterExists(Parameter param) {
		return this.parameters.contains(param);
	}

	/**
	 * Gets the maximum value of a given Parameter in the DataSheet.
	 * 
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
	 * 
	 * @return the design count
	 */
	public int getDesignCount() {
		return this.data.size();
	}

	/**
	 * Gets the cluster set.
	 * 
	 * @return the cluster set
	 */
	public ClusterSet getClusterSet() {
		return clusterSet;
	}

	/**
	 * Sets the cluster set.
	 * 
	 * @param clusterSet the new cluster set
	 */
	public void setClusterSet(ClusterSet clusterSet) {
		this.clusterSet = clusterSet;
	}

	/**
	 * Evaluate each Design to check whether it is within all axis bounds.
	 * 
	 * @param chart the chart
	 * @see Design
	 */
	public void evaluateBoundsForAllDesigns(ParallelCoordinatesChart chart) {
		for (int i = 0; i < this.getDesignCount(); i++) {
			this.data.get(i).evaluateBounds(chart);
		}
	}

	/**
	 * Function to reorder the parameters in the datasheet
	 * 
	 * @param oldIndex the index of the parameter to be moved
	 * @param newIndex the target index for the parameter to be moved
	 */
	public void moveParameter(int oldIndex, int newIndex) {
		logger.info("moveParameter called with arguments " + oldIndex + " and " + newIndex);
		Parameter param = this.parameters.remove(oldIndex);
		this.parameters.insertElementAt(param, newIndex);
		logger.info("new order is:");
		for (int i = 0; i < this.parameters.size(); i++) {
			logger.info(i + " :  " + this.parameters.get(i).getName());
		}
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		if (listDataListener == null)
			listDataListener = new Vector<ListDataListener>();
		listDataListener.add(l);

	}

	@Override
	public Object getElementAt(int index) {
		return this.parameters.get(index).getName();
	}

	@Override
	public int getSize() {
		return this.parameters.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listDataListener.remove(l);

	}

}
