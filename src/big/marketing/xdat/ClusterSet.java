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
import java.io.Serializable;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

/**
 * A collection of several {@link Cluster}s.
 * <p>
 * 
 */
public class ClusterSet implements Serializable, TableModel {
	static Logger logger = Logger.getLogger(ClusterSet.class);

	/** The version tracking unique identifier for Serialization. */
	static final long serialVersionUID = 0001;

	/** The data sheet. */
	private DataSheet dataSheet;

	/** The clusters. */
	private Vector<Cluster> clusters = new Vector<Cluster>(0, 1);

	/**
	 * A buffer of clusters that is used while the user is editing this ClusterSet.
	 * While the user edits the ClusterSet all changes are only applied to this buffer.
	 * The changes are only applied when the user confirms his actions. The method {@link #applyChanges()} is used for this purpose.
	 */
	private Vector<Cluster> clustersBuffer = new Vector<Cluster>(0, 1);

	/** Used for automatic update of the GUI when table contents change. */
	private transient Vector<TableModelListener> listeners = new Vector<TableModelListener>();

	/**
	 * Counter which allows to attribute a unique identifier to each Cluster.
	 * This is needed to keep the correct references between the clusters Vector and the editing buffer.
	 */
	private int uniqueIdentificationNumberCounter = 0;

	/**
	 * Instantiates a new cluster set.
	 * @param dataSheet the data sheet
	 */
	public ClusterSet(DataSheet dataSheet) {
		this.dataSheet = dataSheet;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case (0):
			return String.class;
		case (1):
			return Color.class;
		case (2):
			return Integer.class;
		case (3):
			return Boolean.class;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 4;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case (0):
			return "Cluster";
		case (1):
			return "Color";
		case (2):
			return "Active";
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return this.clustersBuffer.size();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case (0):
			return this.clustersBuffer.get(rowIndex).getName();
		case (1):
			return this.clustersBuffer.get(rowIndex).getActiveDesignColor();
		case (2):
			return this.clustersBuffer.get(rowIndex).getLineThickness();
		case (3):
			return this.clustersBuffer.get(rowIndex).isActive();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object arg0, int rowIndex, int columnIndex) {
		logger.info("setValueAt: argument is " + arg0.toString());
		switch (columnIndex) {
		case (0): {
			if (this.isNameUnique(arg0.toString(), rowIndex)) {
				this.clustersBuffer.get(rowIndex).setName(arg0.toString());
			} else
				logger.warn("This name is not unique. Please choose a different name.");
			//	TODO	JOptionPane.showMessageDialogger.info(null, "This name is not unique. Please choose a different name.", "Rename Cluster",
			//	JOptionPane.INFORMATION_MESSAGE);
			break;
		}
		case (1): {
			this.clustersBuffer.get(rowIndex).setActiveDesignColor((Color) arg0);
			break;
		}
		case (2): {
			try {
				int thickness = Integer.parseInt(arg0.toString());
				if (thickness < 0 || thickness > 10) {
					throw new NumberFormatException();
				}
				logger.info("setValueAt: setting line thickness of cluster " + this.clustersBuffer.get(rowIndex).getName() + " to "
				      + arg0.toString());
				this.clustersBuffer.get(rowIndex).setLineThickness(thickness);
				logger.info("setValueAt: line thickness of cluster " + this.clustersBuffer.get(rowIndex).getName() + " is now "
				      + this.clustersBuffer.get(rowIndex).getLineThickness());
			} catch (NumberFormatException e) {
				logger.warn("Invalid input. Values must be integers between 0 and 10.");
				//	TODO JOptionPane.showMessageDialogger.info(null, "Invalid input. Values must be integers between 0 and 10.", "Invalid Input",
				//	JOptionPane.ERROR_MESSAGE);
			}
			break;
		}
		case (3): {
			logger.info("setValueAt: setting active of cluster " + this.clustersBuffer.get(rowIndex).getName() + " to " + arg0.toString());
			this.clustersBuffer.get(rowIndex).setActive(Boolean.parseBoolean(arg0.toString()));
			logger.info("setValueAt: active of cluster " + this.clustersBuffer.get(rowIndex).getName() + " is now "
			      + this.clustersBuffer.get(rowIndex).isActive());
			break;
		}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void addTableModelListener(TableModelListener l) {
		if (this.listeners == null) // rebuild after deserialization
			this.listeners = new Vector<TableModelListener>();
		listeners.add(l);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	/**
	 * Called to update the display of the table
	 */
	public void fireTableChanged() {
		TableModelEvent e = new TableModelEvent(this);
		for (int i = 0, n = listeners.size(); i < n; i++) {
			((TableModelListener) listeners.get(i)).tableChanged(e);
		}
	}

	/**
	 * Adds a new cluster to the editing buffer.
	 */
	public void addClusterToBuffer() {
		logger.info("addClusterToBuffer called");
		String newClusterName = this.getUniqueClusterName();
		Cluster newCluster = new Cluster(newClusterName, uniqueIdentificationNumberCounter++);
		this.clustersBuffer.add(newCluster);
		this.fireTableChanged();
	}

	/**
	 * Removes a cluster from the editing buffer.
	 * @param i the index of the Cluster to be removed.
	 */
	public void removeClusterFromBuffer(int i) {
		this.clustersBuffer.remove(i);
		this.fireTableChanged();
	}

	/**
	 * Removes the specified Cluster from the editing buffer.
	 * @param cluster the Cluster to be removed
	 */
	public void removeClusterFromBuffer(Cluster cluster) {
		this.clustersBuffer.remove(cluster);
		this.fireTableChanged();
	}

	/**
	 * Gets a cluster by index i.
	 * @param i the index
	 * @return the cluster with index i
	 */
	public Cluster getCluster(int i) {
		return this.clusters.get(i);
	}

	/**
	 * Gets a cluster by name.
	 * @param clusterName the cluster name
	 * @return the cluster with name clusterName
	 */
	public Cluster getCluster(String clusterName) {
		for (int i = 0; i < this.clusters.size(); i++) {
			if (this.clusters.get(i).getName().equals(clusterName))
				return this.clusters.get(i);
		}
		throw new IllegalArgumentException("Could not find cluster " + clusterName);
	}

	/**
	 * Gets the cluster count.
	 * @return the cluster count
	 */
	public int getClusterCount() {
		return this.clusters.size();
	}

	/**
	 * Removes a cluster from buffer by name.
	 * @param clusterName the name of the Cluster to be removed.
	 */
	public void removeClusterFromBuffer(String clusterName) {
		for (int i = 0; i < this.clustersBuffer.size(); i++) {
			if (this.clustersBuffer.get(i).getName().equals(clusterName)) {
				this.clustersBuffer.remove(i);
				this.fireTableChanged();
			}
		}
		throw new IllegalArgumentException("Could not find cluster " + clusterName);
	}

	/**
	 * Creates a unique name for a new Cluster.
	 * @return the unique cluster name
	 */
	private String getUniqueClusterName() {
		String name = "Cluster 1";
		int id = 1;
		while (!isNameUnique(name))
			name = "Cluster " + (id++);
		return name;
	}

	/**
	 * Checks if a given name is a unique Cluster name.
	 * @param name the name to be checked
	 * @return true, if the name is a unique Cluster name
	 */
	private boolean isNameUnique(String name) {
		boolean unique = true;
		for (int i = 0; i < this.clustersBuffer.size(); i++) {
			if (name.equals(this.clustersBuffer.get(i).getName())) {
				unique = false;
				break;
			}
		}
		return unique;
	}

	/**
	 * Checks if a given name is a unique Cluster name but does not check against the name of
	 * Cluster with index exception.
	 * This is needed when the user edits the Cluster name. In this case, the entered name must
	 * be checked against the names of all other Clusters, but not against the current name of this
	 * Cluster. Otherwise reentering the same name as the Cluster had before would produce an error
	 * message.
	 * 
	 * @param name the name
	 * @param exception the exception
	 * @return true, if is name unique
	 */
	private boolean isNameUnique(String name, int exception) // clustersBuffer.get(exception) won't be evaluated
	{
		boolean unique = true;
		for (int i = 0; i < this.clustersBuffer.size(); i++) {
			if (name.equals(this.clustersBuffer.get(i).getName()) && i != exception) {
				unique = false;
				break;
			}
		}
		return unique;
	}

	/**
	 * All changes made to the editing buffer are now applied by copying the buffer to the persistent
	 * Vector clusters.
	 */
	public void applyChanges() {
		logger.info("applyChanges invoked");

		for (int i = this.clusters.size() - 1; i >= 0; i--) {
			boolean clusterRemoved = true;

			for (int j = this.clustersBuffer.size() - 1; j >= 0; j--) {
				if (this.clusters.get(i).getUniqueIdentificationNumber() == this.clustersBuffer.get(j).getUniqueIdentificationNumber()) {
					clusterRemoved = false;
					this.clustersBuffer.remove(j).copySettingsTo(this.clusters.get(i));
					break;
				}
			}
			if (clusterRemoved) {
				Cluster c = this.clusters.remove(i);
				for (int j = 0; j < this.dataSheet.getDesignCount(); j++) {
					if (c.equals(this.dataSheet.getDesign(j).getCluster())) {
						this.dataSheet.getDesign(j).setCluster(null);
					}
				}
			}
		}
		for (int i = 0; i < this.clustersBuffer.size(); i++) {
			this.clusters.add(this.clustersBuffer.get(i).duplicate());
		}
	}

	/**
	 * Creates a buffer by copying the clusters Vector.
	 * <p>This buffer is used to store modifications that the user makes until he choose to apply the changes made.
	 * 
	 */
	public void createBuffer() {
		logger.info("createBuffer invoked");
		clustersBuffer.removeAllElements();
		for (int i = 0; i < this.clusters.size(); i++) {
			this.clustersBuffer.add(this.clusters.get(i).duplicate());
		}
	}
}
