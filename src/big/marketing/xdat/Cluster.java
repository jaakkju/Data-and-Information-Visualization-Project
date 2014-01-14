/*
 *  Copyright 2013, Enguerrand de Rochefort
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

/**
 * A group of {@link data.Design}s that can be displayed in a userspecified different color or
 * removed from the display altogether, irrespective of the {@link chart.Filter} settings.
 * <p>
 * Clusters enable the user to regroup the designs in logical subsets. This achieved by storing a
 * reference to a Cluster in the Design instance. Whenever the Design is asked to which Cluster it
 * belongs, it returns this reference. The Cluster then provides the information whether it is active
 * (which determines whether the design should be displayed) and, if so, in which color the Design
 * is displayed.
 */
public class Cluster implements Serializable {

	/** The version tracking unique identifier for Serialization. */
	static final long serialVersionUID = 0001;

	/** The Cluster name. */
	private String name;

	/** The color in which Designs belonging to this Cluster are displayed. */
	private Color activeDesignColor;

	/** Specifies whether Designs belonging to this Cluster should be displayed. */
	private boolean active = true;

	/** Specifies the line thickness for designs belonging to the cluster. */
	private int lineThickness = 1;

	/** The unique identification number for tracking purposes in the {@link ClusterSet}. */
	private int uniqueIdentificationNumber;

	/**
	 * Instantiates a new cluster.
	 * @param name the Cluster name
	 * @param uniqueIdentificationNumber the unique identification number
	 */
	public Cluster(String name, int uniqueIdentificationNumber) {
		this.name = name;
		this.uniqueIdentificationNumber = uniqueIdentificationNumber;

		// TODO Implement User Preferences > Main.getUserPreferences().getParallelCoordinatesActiveDesignDefaultColor();
		this.activeDesignColor = new UserPreferences("eyeNet").getParallelCoordinatesActiveDesignDefaultColor();
	}

	/**
	 * Gets the color in which Designs belonging to this Cluster are displayed.
	 * @return the color in which Designs belonging to this Cluster are displayed.
	 */
	public Color getActiveDesignColor() {
		return activeDesignColor;
	}

	/**
	 * Sets the color in which Designs belonging to this Cluster are displayed.
	 * @param activeDesignColor the new color in which Designs belonging to this Cluster are displayed.
	 */
	public void setActiveDesignColor(Color activeDesignColor) {
		this.activeDesignColor = activeDesignColor;
	}

	/**
	 * Gets the line thickness.
	 * @return the line thickness
	 */
	public int getLineThickness() {
		return lineThickness;
	}

	/**
	 * Sets the line thickness.
	 * @param lineThickness the new line thickness
	 */
	public void setLineThickness(int lineThickness) {
		this.lineThickness = lineThickness;
	}

	/**
	 * Gets the Cluster name.
	 * @return the Cluster name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the Cluster name.
	 * @param name the new Cluster name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Checks whether Designs belonging to this Cluster should be displayed
	 * @return true, if Designs belonging to this Cluster should be displayed
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Specifies whether Designs belonging to this Cluster should be displayed
	 * @param active Specifies whether Designs belonging to this Cluster should be displayed
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Returns a duplicated instance of this Cluster for use in an editing Buffer.
	 * @return the cluster
	 * @see ClusterSet
	 */
	public Cluster duplicate() {
		Cluster duplication = new Cluster(this.name, this.uniqueIdentificationNumber);
		duplication.setActive(this.active);
		duplication.setActiveDesignColor(this.activeDesignColor);
		duplication.setLineThickness(this.lineThickness);
		return duplication;
	}

	/**
	 * Copies settings of this Cluster to a given Cluster.
	 * @param cluster the Cluster to which the settings of this Cluster should be copied.
	 */
	public void copySettingsTo(Cluster cluster) {
		cluster.setName(this.name);
		cluster.setActive(this.active);
		cluster.setActiveDesignColor(this.activeDesignColor);
		cluster.setLineThickness(this.lineThickness);
	}

	/**
	 * Gets the unique identification number.
	 * @return the unique identification number
	 */
	public int getUniqueIdentificationNumber() {
		return uniqueIdentificationNumber;
	}
}
