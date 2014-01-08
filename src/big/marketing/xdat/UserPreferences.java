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
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * This class stores all user preference settings.
 * 
 * When the user makes changes to settings such as default background color of
 * charts or delimiting characters for data imports these settings are stored
 * using the java Preferences API.
 */
public class UserPreferences {
	static Logger logger = Logger.getLogger(UserPreferences.class);

	/** The version tracking unique identifier for Serialization. */
	static final long serialVersionUID = 0003;

	/** Preferences Object to store settings. */
	private Preferences prefs;

	/** Current Release Version to be able to store version-specific boolean value for the click-wrap license. **/
	private String versionString;

	//File Options
	/** Open file import browse dialog in the user's home directory by default. */
	public static final int IMPORT_FROM_HOMEDIR = 0;

	/** Open file import browse dialog in the last opened directory by default. */
	public static final int IMPORT_FROM_LASTDIR = 1;

	/** Open file import browse dialog in a userspecified directory by default. */
	public static final int IMPORT_FROM_USERDIR = 2;

	//Number Formats
	/** US Locale for number formatting. */
	public static final int LOCALE_US = 0;

	/** German Locale for number formatting. */
	public static final int LOCALE_DE = 1;

	/**
	 * Instantiates a new user preferences object.
	 */
	public UserPreferences(String versionString) {
		logger.info("Loading Xdat Preferences");
		this.prefs = Preferences.userNodeForPackage(getClass());
		this.versionString = versionString;
	}

	// License	

	/**
	 * Returns true, when the user has accepted the license
	 * @return whether the license was accepted by the user
	 */
	public boolean isLicenseAccepted() {
		return this.prefs.getBoolean("version" + this.versionString + "licenseAcceptedBy" + System.getProperty("user.name"), false);
	}

	/**
	 * Specifies whether the user has accepted the license
	 * @param licenseAccepted the flag that specifies whether the user has accepted the license
	 */
	public void setLicenseAccepted(boolean licenseAccepted) {
		this.prefs.putBoolean("version" + this.versionString + "licenseAcceptedBy" + System.getProperty("user.name"), licenseAccepted);
	}

	// Settings for Parallel Coordinates Charts	

	/**
	 * Gets the axis color for parallel coordinate charts
	 * @return the axis color
	 */
	public Color getParallelCoordinatesAxisColor() {
		int r = this.prefs.getInt("ParallelCoordinatesAxisColorRed", 0);
		int g = this.prefs.getInt("ParallelCoordinatesAxisColorGreen", 0);
		int b = this.prefs.getInt("ParallelCoordinatesAxisColorBlue", 0);
		return new Color(r, g, b);
	}

	/**
	 * Sets the axis color for parallel coordinate charts
	 * @param axisColor the new axis color
	 */
	public void setParallelCoordinatesAxisColor(Color axisColor) {
		this.prefs.putInt("ParallelCoordinatesAxisColorRed", axisColor.getRed());
		this.prefs.putInt("ParallelCoordinatesAxisColorGreen", axisColor.getGreen());
		this.prefs.putInt("ParallelCoordinatesAxisColorBlue", axisColor.getBlue());
	}

	/**
	 * Checks if axis labels on parallel coordinate charts should be vertically offset
	 * @return true, if axis labels should be vertically offset
	 */
	public boolean isParallelCoordinatesVerticallyOffsetAxisLabels() {
		return this.prefs.getBoolean("ParallelCoordinatesVerticallyOffsetAxisLabels", true);
	}

	/**
	 * Sets whether axis labels on parallel coordinate charts should be vertically offset
	 * @param verticallyOffsetAxisLabels flag whether axis labels should be vertically offset
	 */
	public void setParallelCoordinatesVerticallyOffsetAxisLabels(boolean verticallyOffsetAxisLabels) {
		this.prefs.putBoolean("ParallelCoordinatesVerticallyOffsetAxisLabels", verticallyOffsetAxisLabels);
	}

	/**
	 * Gets the axis label font color for parallel coordinate charts
	 * @return the axis label font color
	 */
	public Color getParallelCoordinatesAxisLabelFontColor() {
		int r = this.prefs.getInt("ParallelCoordinatesAxisLabelFontColorRed", 0);
		int g = this.prefs.getInt("ParallelCoordinatesAxisLabelFontColorGreen", 0);
		int b = this.prefs.getInt("ParallelCoordinatesAxisLabelFontColorBlue", 0);
		return new Color(r, g, b);
	}

	/**
	 * Sets the axis label font color for parallel coordinate charts
	 * @param axisLabelFontColor the new axis label font color
	 */
	public void setParallelCoordinatesAxisLabelFontColor(Color axisLabelFontColor) {
		this.prefs.putInt("ParallelCoordinatesAxisLabelFontColorRed", axisLabelFontColor.getRed());
		this.prefs.putInt("ParallelCoordinatesAxisLabelFontColorGreen", axisLabelFontColor.getGreen());
		this.prefs.putInt("ParallelCoordinatesAxisLabelFontColorBlue", axisLabelFontColor.getBlue());
	}

	/**
	 * Gets the axis label font size for parallel coordinate charts.
	 * @return the axis label font size
	 */
	public int getParallelCoordinatesAxisLabelFontSize() {
		return this.prefs.getInt("ParallelCoordinatesAxisLabelFontSize", 20);
	}

	/**
	 * Sets the axis label font size for parallel coordinate charts
	 * @param axisLabelFontSize the new axis label font size
	 */
	public void setParallelCoordinatesAxisLabelFontSize(int axisLabelFontSize) {
		this.prefs.putInt("ParallelCoordinatesAxisLabelFontSize", axisLabelFontSize);
	}

	/**
	 * Gets the axis tick count for parallel coordinate charts
	 * @return the axis tick count
	 */
	public int getParallelCoordinatesAxisTicCount() {
		return this.prefs.getInt("ParallelCoordinatesAxisTicCount", 11);
	}

	/**
	 * Sets the axis tick count for parallel coordinate charts
	 * @param axisTicCount the new axis tick count
	 */
	public void setParallelCoordinatesAxisTicCount(int axisTicCount) {
		this.prefs.putInt("ParallelCoordinatesAxisTicCount", axisTicCount);
	}

	/**
	 * Gets the axis tick label font color for parallel coordinate charts
	 * @return the axis tick label font color
	 */
	public Color getParallelCoordinatesAxisTicLabelFontColor() {
		int r = this.prefs.getInt("ParallelCoordinatesAxisTicLabelFontColorRed", 0);
		int g = this.prefs.getInt("ParallelCoordinatesAxisTicLabelFontColorGreen", 0);
		int b = this.prefs.getInt("ParallelCoordinatesAxisTicLabelFontColorBlue", 0);
		return new Color(r, g, b);
	}

	/**
	 * Sets the axis tick label font color for parallel coordinate charts
	 * @param axisTicLabelFontColor the new axis tick label font color
	 */
	public void setParallelCoordinatesAxisTicLabelFontColor(Color axisTicLabelFontColor) {
		this.prefs.putInt("ParallelCoordinatesAxisTicLabelFontColorRed", axisTicLabelFontColor.getRed());
		this.prefs.putInt("ParallelCoordinatesAxisTicLabelFontColorGreen", axisTicLabelFontColor.getGreen());
		this.prefs.putInt("ParallelCoordinatesAxisTicLabelFontColorBlue", axisTicLabelFontColor.getBlue());
	}

	/**
	 * Gets the axis tick label format for parallel coordinate charts
	 * @return the axis tick label format
	 */
	public String getParallelCoordinatesAxisTicLabelFormat() {
		return this.prefs.get("ParallelCoordinatesAxisTicLabelFormat", "%4.3f");
	}

	/**
	 * Sets the axis tick label format for parallel coordinate charts
	 * @param axisTicLabelFormat the new axis tick label format
	 */
	public void setParallelCoordinatesAxisTicLabelFormat(String axisTicLabelFormat) {
		this.prefs.put("ParallelCoordinatesAxisTicLabelFormat", axisTicLabelFormat);
	}

	/**
	 * Gets the axis tick length for parallel coordinate charts
	 * @return the axis tick length
	 */
	public int getParallelCoordinatesAxisTicLength() {
		return this.prefs.getInt("ParallelCoordinatesAxisTicLength", 4);
	}

	/**
	 * Sets the axis tick length for parallel coordinate charts
	 * @param axisTicLength the new axis tick length
	 */
	public void setParallelCoordinatesAxisTicLength(int axisTicLength) {
		this.prefs.putInt("ParallelCoordinatesAxisTicLength", axisTicLength);
	}

	/**
	 * Gets the width in pixels that is used by one axis on parallel coordinate charts
	 * This setting is used to define the axis spacing. The distance of two axes is defined by the sum of their respective widths, divided by
	 * two
	 * 
	 * @return the axis width
	 */
	public int getParallelCoordinatesAxisWidth() {
		return this.prefs.getInt("ParallelCoordinatesAxisWidth", 200);
	}

	/**
	 * Sets the axis width for parallel coordinate charts
	 * @param axisWidth the new axis width
	 */
	public void setParallelCoordinatesAxisWidth(int axisWidth) {
		this.prefs.putInt("ParallelCoordinatesAxisWidth", axisWidth);
	}

	/**
	 * Gets the filter default color for parallel coordinate charts
	 * @return the filter default color
	 */
	public Color getParallelCoordinatesFilterDefaultColor() {
		int r = this.prefs.getInt("ParallelCoordinatesFilterColorRed", 255);
		int g = this.prefs.getInt("ParallelCoordinatesFilterColorGreen", 0);
		int b = this.prefs.getInt("ParallelCoordinatesFilterColorBlue", 0);
		return new Color(r, g, b);
	}

	/**
	 * Sets the filter color for parallel coordinate charts
	 * @param filterColor the new filter color
	 */
	public void setParallelCoordinatesFilterColor(Color filterColor) {
		this.prefs.putInt("ParallelCoordinatesFilterColorRed", filterColor.getRed());
		this.prefs.putInt("ParallelCoordinatesFilterColorGreen", filterColor.getGreen());
		this.prefs.putInt("ParallelCoordinatesFilterColorBlue", filterColor.getBlue());
	}

	/**
	 * Gets the filter height for parallel coordinate charts
	 * @return the filter height
	 */
	public int getParallelCoordinatesFilterHeight() {
		return this.prefs.getInt("ParallelCoordinatesFilterHeight", 10);
	}

	/**
	 * Sets the filter height for parallel coordinate charts
	 * @param filterHeight the new filter height
	 */
	public void setParallelCoordinatesFilterHeight(int filterHeight) {
		this.prefs.putInt("ParallelCoordinatesFilterHeight", filterHeight);
	}

	/**
	 * Gets the width of one half triangle that represents a filter in pixels. In other words,
	 * the filter triangle will be twice as large as the value entered here
	 * @return the filter width
	 */
	public int getParallelCoordinatesFilterWidth() {
		return this.prefs.getInt("ParallelCoordinatesFilterWidth", 7);
	}

	/**
	 * Sets the filter width for parallel coordinate charts
	 * @param filterWidth the new filter width
	 */
	public void setParallelCoordinatesFilterWidth(int filterWidth) {
		this.prefs.putInt("ParallelCoordinatesFilterWidth", filterWidth);
	}

	/**
	 * Gets the axis tick label font size for parallel coordinate charts
	 * @return the axis tick label font size
	 */
	public int getParallelCoordinatesAxisTicLabelFontSize() {
		return this.prefs.getInt("ticLabelFontSize", 10);
	}

	/**
	 * Sets the axis tick label font size for parallel coordinate charts
	 * @param ticLabelFontSize the new axis tick label font size
	 */
	public void setParallelCoordinatesAxisTicLabelFontSize(int ticLabelFontSize) {
		this.prefs.putInt("ticLabelFontSize", ticLabelFontSize);
	}

	/**
	 * Gets the design label font size for parallel coordinate charts
	 * @return the design label font size
	 */
	public int getParallelCoordinatesDesignLabelFontSize() {
		return this.prefs.getInt("designLabelFontSize", 10);
	}

	/**
	 * Sets the design label font size for parallel coordinate charts
	 * @param designLabelFontSize the new design label font size
	 */
	public void setParallelCoordinatesDesignLabelFontSize(int designLabelFontSize) {
		this.prefs.putInt("designLabelFontSize", designLabelFontSize);
	}

	/**
	 * Gets the line thickness for parallel coordinate charts
	 * @return the line thickness
	 */
	public int getParallelCoordinatesLineThickness() {
		return this.prefs.getInt("lineThickness", 1);
	}

	/**
	 * Sets the line thickness for parallel coordinate charts
	 * @param lineThickness the new line thickness
	 */
	public void setParallelCoordinatesLineThickness(int lineThickness) {
		this.prefs.putInt("lineThickness", lineThickness);
	}

	/**
	 * Gets the line thickness for selected designs in parallel coordinate charts
	 * @return the line thickness
	 */
	public int getParallelCoordinatesSelectedDesignLineThickness() {
		return this.prefs.getInt("selectedDesignLineThickness", 2);
	}

	/**
	 * Sets the line thickness for selected designs in parallel coordinate charts
	 * @param lineThickness the new line thickness
	 */
	public void setParallelCoordinatesSelectedDesignLineThickness(int lineThickness) {
		this.prefs.putInt("selectedDesignLineThickness", lineThickness);
	}

	/**
	 * Checks if filtered designs should be shown on parallel coordinate charts
	 * @return true, if filtered designs are shown
	 */
	public boolean isParallelCoordinatesShowFilteredDesigns() {
		return this.prefs.getBoolean("showFilteredDesigns", false);
	}

	/**
	 * Sets whether filtered designs should be shown on parallel coordinate charts
	 * @param showFilteredDesigns flag to set whether filtered designs should be shown.
	 */
	public void setParallelCoordinatesShowFilteredDesigns(boolean showFilteredDesigns) {
		this.prefs.putBoolean("showFilteredDesigns", showFilteredDesigns);
	}

	/**
	 * Checks if only selected designs are shown on parallel coordinate charts
	 * @return true, if only selected designs are shown
	 */
	public boolean isParallelCoordinatesShowOnlySelectedDesigns() {
		return this.prefs.getBoolean("parallelCoordinatesShowOnlySelectedDesigns", false);
	}

	/**
	 * Sets whether only selected designs should be shown on parallel coordinate charts
	 * @param showOnlySelectedDesigns flag to set whether only selected designs should be shown.
	 */
	public void setParallelCoordinatesShowOnlySelectedDesigns(boolean showOnlySelectedDesigns) {
		this.prefs.putBoolean("parallelCoordinatesShowOnlySelectedDesigns", showOnlySelectedDesigns);
	}

	/**
	 * Gets the active design default color for parallel coordinate charts
	 * @return the active design default color
	 */
	public Color getParallelCoordinatesActiveDesignDefaultColor() {
		int r = this.prefs.getInt("activeDesignDefaultColorRed", 0);
		int g = this.prefs.getInt("activeDesignDefaultColorGreen", 150);
		int b = this.prefs.getInt("activeDesignDefaultColorBlue", 0);
		return new Color(r, g, b);
	}

	/**
	 * Sets the selected design default color for parallel coordinate charts
	 * @param selectedDesignDefaultColor the new selected design default color
	 */
	public void setParallelCoordinatesSelectedDesignDefaultColor(Color selectedDesignDefaultColor) {
		this.prefs.putInt("selectedDesignDefaultColorRed", selectedDesignDefaultColor.getRed());
		this.prefs.putInt("selectedDesignDefaultColorGreen", selectedDesignDefaultColor.getGreen());
		this.prefs.putInt("selectedDesignDefaultColorBlue", selectedDesignDefaultColor.getBlue());
	}

	/**
	 * Gets the selected design default color for parallel coordinate charts
	 * @return the selected design default color
	 */
	public Color getParallelCoordinatesSelectedDesignDefaultColor() {
		int r = this.prefs.getInt("selectedDesignDefaultColorRed", 0);
		int g = this.prefs.getInt("selectedDesignDefaultColorGreen", 0);
		int b = this.prefs.getInt("selectedDesignDefaultColorBlue", 255);
		return new Color(r, g, b);
	}

	/**
	 * Sets the active design default color for parallel coordinate charts
	 * @param activeDesignDefaultColor the new active design default color
	 */
	public void setParallelCoordinatesActiveDesignDefaultColor(Color activeDesignDefaultColor) {
		this.prefs.putInt("activeDesignDefaultColorRed", activeDesignDefaultColor.getRed());
		this.prefs.putInt("activeDesignDefaultColorGreen", activeDesignDefaultColor.getGreen());
		this.prefs.putInt("activeDesignDefaultColorBlue", activeDesignDefaultColor.getBlue());
	}

	/**
	 * Gets the filtered design default color for parallel coordinate charts
	 * @return the filtered design default color
	 */
	public Color getParallelCoordinatesFilteredDesignDefaultColor() {
		int r = this.prefs.getInt("inActiveDesignDefaultColorRed", 100);
		int g = this.prefs.getInt("inActiveDesignDefaultColorGreen", 100);
		int b = this.prefs.getInt("inActiveDesignDefaultColorBlue", 100);
		return new Color(r, g, b);
	}

	/**
	 * Sets the in active design default color for parallel coordinate charts
	 * @param inActiveDesignDefaultColor the new in active design default color
	 */
	public void setParallelCoordinatesInactiveDesignDefaultColor(Color inActiveDesignDefaultColor) {
		this.prefs.putInt("inActiveDesignDefaultColorRed", inActiveDesignDefaultColor.getRed());
		this.prefs.putInt("inActiveDesignDefaultColorGreen", inActiveDesignDefaultColor.getGreen());
		this.prefs.putInt("inActiveDesignDefaultColorBlue", inActiveDesignDefaultColor.getBlue());
	}

	/**
	 * Checks if design id numbers are shown left to the left-most axis on parallel coordinate charts
	 * @return whether design id numbers are shown left to the left-most axis
	 */
	public boolean isParallelCoordinatesShowDesignIDs() {
		return this.prefs.getBoolean("showDesignIDs", true);
	}

	/**
	 * Specifies whether design id numbers are shown left to the left-most axis on parallel coordinate charts
	 * @param showDesignIDs specifies whether design id numbers are shown left to the left-most axis
	 */
	public void setParallelCoordinatesShowDesignIDs(boolean showDesignIDs) {
		this.prefs.putBoolean("showDesignIDs", showDesignIDs);
	}

	/**
	 * Gets the design id font size for parallel coordinate charts
	 * @return the design id font size
	 */
	public int getParallelCoordinatesDesignIDFontSize() {
		return this.prefs.getInt("designIDFontSize", 10);
	}

	/**
	 * Sets the design id font size for parallel coordinate charts
	 * @param designIDFontSize the new design id font size
	 */
	public void setParallelCoordinatesDesignIDFontSize(int designIDFontSize) {
		this.prefs.putInt("designIDFontSize", designIDFontSize);
	}

	/**
	 * Gets the default background color for parallel coordinate charts
	 * @return the default background color
	 */
	public Color getParallelCoordinatesDefaultBackgroundColor() {
		int r = this.prefs.getInt("backgroundColorRed", 255);
		int g = this.prefs.getInt("backgroundColorGreen", 255);
		int b = this.prefs.getInt("backgroundColorBlue", 255);
		return new Color(r, g, b);
	}

	/**
	 * Sets the default background color for parallel coordinate charts
	 * @param backgroundColor the new default background color
	 */
	public void setParallelCoordinatesDefaultBackgroundColor(Color backgroundColor) {
		this.prefs.putInt("backgroundColorRed", backgroundColor.getRed());
		this.prefs.putInt("backgroundColorGreen", backgroundColor.getGreen());
		this.prefs.putInt("backgroundColorBlue", backgroundColor.getBlue());
	}

	/**
	 * True, when filters are inverted. This means that designs between the filters are shown on parallel coordinate charts, while designs
	 * above the top filter and below the bottom filter are not
	 * @return true, if filters are inverted.
	 */
	public boolean isFilterInverted() {
		return this.prefs.getBoolean("ParallelCoordinatesFilterInverted", false);
	}

	/**
	 * Specifies whether filters inverted on parallel coordinate charts
	 * @param filterInverted Specifies whether filters inverted.
	 */
	public void setFilterInverted(boolean filterInverted) {
		this.prefs.putBoolean("ParallelCoordinatesFilterInverted", filterInverted);
	}

	/**
	 * Checks whether axes are inverted on parallel coordinate charts
	 * @return true, if axes are inverted
	 */
	public boolean isParallelCoordinatesAxisInverted() {
		return this.prefs.getBoolean("ParallelCoordinatesAxisInverted", false);
	}

	/**
	 * Specifies whether axes are inverted on parallel coordinate charts
	 * @param axisInverted Specifies whether axes are inverted.
	 */
	public void setParallelCoordinatesAxisInverted(boolean axisInverted) {
		this.prefs.putBoolean("ParallelCoordinatesAxisInverted", axisInverted);
	}

	/**
	 * Checks if axes are autofitted on parallel coordinate charts
	 * @return true, if axes are autofitted.
	 */
	public boolean isParallelCoordinatesAutoFitAxis() {
		return this.prefs.getBoolean("ParallelCoordinatesAutoFitAxis", true);
	}

	/**
	 * Specifies whether axes should be autofitted on parallel coordinate charts
	 * @param autoFitAxis Specifies whether axes should be autofitted.
	 */
	public void setParallelCoordinatesAutoFitAxis(boolean autoFitAxis) {
		this.prefs.putBoolean("ParallelCoordinatesAutoFitAxis", autoFitAxis);
	}

	/**
	 * Gets the axis default minimum value for parallel coordinate charts
	 * @return the axis default minimum value
	 */
	public double getParallelCoordinatesAxisDefaultMin() {
		return this.prefs.getDouble("ParallelCoordinatesAxisDefaultMin", -10);
	}

	/**
	 * Sets the axis default minimum value for parallel coordinate charts.
	 * @param axisDefaultMin the new axis default minimum value
	 */
	public void setParallelCoordinatesAxisDefaultMin(double axisDefaultMin) {
		this.prefs.putDouble("ParallelCoordinatesAxisDefaultMin", axisDefaultMin);
	}

	/**
	 * Gets the axis default maximum value for parallel coordinate charts
	 * @return the axis default maximum value
	 */
	public double getParallelCoordinatesAxisDefaultMax() {
		return this.prefs.getDouble("ParallelCoordinatesAxisDefaultMax", 10.0);
	}

	/**
	 * Sets the axis default max for parallel coordinate charts.
	 * @param axisDefaultMax the new axis default max
	 */
	public void setParallelCoordinatesAxisDefaultMax(double axisDefaultMax) {
		this.prefs.putDouble("ParallelCoordinatesAxisDefaultMax", axisDefaultMax);
	}

	//  Data Import Settings

	/**
	 * Gets the dir to import from
	 * @return the dir to import from
	 */
	public int getDirToImportFrom() {
		return this.prefs.getInt("dirToImportFrom", IMPORT_FROM_LASTDIR);
	}

	/**
	 * Sets the dir to import from
	 * @param dirToImportFrom the new dir to import from
	 */
	public void setDirToImportFrom(int dirToImportFrom) {
		this.prefs.putInt("dirToImportFrom", dirToImportFrom);
	}

	/**
	 * Gets the last file
	 * @return the last file
	 */
	public String getLastFile() {
		return this.prefs.get("lastFileBrowsingDirectory", System.getProperty("user.home"));
	}

	/**
	 * Sets the last file
	 * @param lastFileBrowsingDirectory the new last file
	 */
	public void setLastFile(String lastFileBrowsingDirectory) {
		this.prefs.put("lastFileBrowsingDirectory", lastFileBrowsingDirectory);
	}

	/**
	 * Checks if the last file field has been initialised
	 * @return true, if is last file field has been initialised
	 */
	public boolean isLastFileInitialised() {
		return this.prefs.getBoolean("lastFileInitialised", false);
	}

	/**
	 * Gets the user home directory
	 * @return the user home directory
	 */
	public String getHomeDir() {
		return this.prefs.get("homeDir", System.getProperty("user.home"));
	}

	/**
	 * Sets the home directory
	 * @param homeDir the new home directory
	 */
	public void setHomeDir(String homeDir) {
		this.prefs.put("homeDir", homeDir);
	}

	/**
	 * Gets the user directory
	 * @return the user directory
	 */
	public String getUserDir() {
		return this.prefs.get("userDir", System.getProperty("user.home"));
	}

	/**
	 * Sets the user directory
	 * @param userDir the new user directory
	 */
	public void setUserDir(String userDir) {
		this.prefs.put("userDir", userDir);
	}

	/**
	 * Gets the directory for the file browsing dialog
	 * @return the current directory based on the user preferences.
	 */
	public String getCurrentDir() {
		switch (this.getDirToImportFrom()) {
		case (IMPORT_FROM_HOMEDIR): {
			return this.getHomeDir();
		}
		case (IMPORT_FROM_LASTDIR): {
			return this.getLastFile();
		}
		case (IMPORT_FROM_USERDIR): {
			return this.getUserDir();
		}
		default: {
			return this.getHomeDir();
		}
		}

	}

	/**
	 * Gets the delimiter for importing data
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return this.prefs.get("delimiter", "\\s");
	}

	/**
	 * Sets the delimiter for importing data
	 * @param delimiter the new delimiter
	 */
	public void setDelimiter(String delimiter) {
		this.prefs.put("delimiter", delimiter);
	}

	/**
	 * Checks if consecutive delimiters should be treated as one
	 * @return true, if consecutive delimiters should be treated as one
	 */
	public boolean isTreatConsecutiveAsOne() {
		return this.prefs.getBoolean("treatConsecutiveAsOne", true);
	}

	/**
	 * Sets whether consecutive delimiters should be treated as one
	 * @param treatConsecutiveAsOne flag for treating consecutive delimiters as one
	 */
	public void setTreatConsecutiveAsOne(boolean treatConsecutiveAsOne) {
		this.prefs.putBoolean("treatConsecutiveAsOne", treatConsecutiveAsOne);
	}

	/**
	 * Gets the character that is used as delimiter when importing data. Only used to store this information internally
	 * @return the user-defined delimiter
	 */
	public String getOtherDelimiter() {
		return this.prefs.get("otherDelimiter", "");
	}

	/**
	 * Sets the user-defined delimiter
	 * @param otherDelimiter the user-defined delimiter
	 */
	public void setOtherDelimiter(String otherDelimiter) {
		this.prefs.put("otherDelimiter", otherDelimiter);
	}

	/**
	 * Returns the number format locale to be used for parsing data
	 * @return the locale
	 */
	public Locale getLocale() {
		int locale = this.prefs.getInt("locale", LOCALE_US);
		switch (locale) {
		case (LOCALE_DE): {
			return Locale.GERMANY;
		}
		default: {
			return Locale.US;
		}
		}
	}

	/**
	 * Sets the number format string to be used for both parsing data
	 * @param locale the Number Format locale
	 */
	public void setLocale(int locale) {
		this.prefs.putInt("locale", locale);
	}

	/**
	 * Restores all default settings.
	 */
	public void resetToDefault() {
		try {
			this.prefs.clear();
		} catch (BackingStoreException e) {
			System.err.println(e.getMessage());
		}
		this.setLicenseAccepted(true);
	}
}
