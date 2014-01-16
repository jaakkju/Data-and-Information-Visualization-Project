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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

/**
 * Panel that is used to display a {@link chart.ParallelCoordinatesChart}.
 */
public class ParallelCoordinatesChartPanel extends ChartPanel implements MouseMotionListener, MouseListener, MouseWheelListener {
	static Logger logger = Logger.getLogger(ParallelCoordinatesChartPanel.class);

	/** The version tracking unique identifier for Serialization. */
	static final long serialVersionUID = 0003;

	/** The chart . */
	private ParallelCoordinatesChart parallelCoordinatesChart;

	/** The buffered image that is used to make redrawing the chart more efficient. */
	private BufferedImage bufferedImage;

	/** Reference to a filter that is currently being dragged by the user. */
	private Filter draggedFilter;

	/** When the user is dragging a filter, the initial x position is stored in this field. */
	private int dragStartX;

	/** When the user is dragging a filter, the initial y position is stored in this field. */
	private int dragStartY;

	/** Stores how far left or right the mouse was dragged for further use. */
	private int dragCurrentX;

	/** Stores how far up or down the mouse was dragged for further use. */
	private int dragOffsetY;

	/** Reference to an axis that is currently being dragged by the user. */
	private Axis draggedAxis;

	/**
	 * Instantiates a new parallel coordinates chart panel.
	 * 
	 * @param mainWindow the main Window
	 * @param chart the chart
	 */
	public ParallelCoordinatesChartPanel(ParallelCoordinatesChart parallelCoordinatesChart, DataSheet dataSheet) {
		super(dataSheet, parallelCoordinatesChart);
		this.parallelCoordinatesChart = parallelCoordinatesChart;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		paint(getGraphics());
		this.addMouseWheelListener(this);
	}

	/**
	 * Overridden to implement the painting of the chart.
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.draggedAxis == null) {
			this.drawDesigns(g);
			this.drawAxes(g);
		} else {
			g.drawImage(this.bufferedImage, 0, 0, this);
			g.setColor(new Color(100, 100, 100));
			int yPosition = parallelCoordinatesChart.getAxisTopPos();
			int xPosition = this.dragCurrentX;
			g.drawLine(xPosition - 1, yPosition, xPosition - 1, yPosition + (parallelCoordinatesChart.getAxisHeight()));
			g.drawLine(xPosition, yPosition, xPosition, yPosition + (parallelCoordinatesChart.getAxisHeight()));
			g.drawLine(xPosition + 1, yPosition, xPosition + 1, yPosition + (parallelCoordinatesChart.getAxisHeight()));
		}
	}

	/**
	 * Draws the lines representing the designs.
	 * 
	 * @param g the graphics object
	 */
	public void drawDesigns(Graphics g) {
		ParallelCoordinatesChart chart = ((ParallelCoordinatesChart) this.getChart());
		int axisTopPos = chart.getAxisTopPos();
		int designLabelFontSize = chart.getDesignLabelFontSize();
		int axisCount = chart.getAxisCount();
		double[] axisRanges = new double[axisCount];
		int[] axisHeights = new int[axisCount];
		int[] axisWidths = new int[axisCount];
		double[] axisMaxValues = new double[axisCount];
		double[] axisMinValues = new double[axisCount];
		int[] axisTicLabelFontsizes = new int[axisCount];
		boolean[] axisActiveFlags = new boolean[axisCount];
		boolean[] axisInversionFlags = new boolean[axisCount];
		for (int i = 0; i < axisCount; i++) // read all the display settings and put them into arrays to improve rendering speed of the chart
		{
			axisRanges[i] = chart.getAxis(i).getMax() - chart.getAxis(i).getMin();
			axisHeights[i] = chart.getAxisHeight();
			axisWidths[i] = chart.getAxis(i).getWidth();
			axisMaxValues[i] = chart.getAxis(i).getMax();
			axisMinValues[i] = chart.getAxis(i).getMin();
			axisTicLabelFontsizes[i] = chart.getAxis(i).getTicLabelFontSize();
			axisActiveFlags[i] = chart.getAxis(i).isActive();
			axisInversionFlags[i] = chart.getAxis(i).isAxisInverted();
		}

		for (int designID = 0; designID < this.getDataSheet().getDesignCount(); designID++) // draw all designs
		{
			Design currentDesign = this.getDataSheet().getDesign(designID);
			//			logger.info("drawDesigns: currentDesign.isInsideBounds(chart) = "+currentDesign.isInsideBounds(chart));
			if (!currentDesign.isInsideBounds(chart)) // do not draw design if it is not inside bounds of the chart
			{
				logger.info("design not inside bounds, continue");
				continue;
			}
			boolean firstAxisDrawn = false;
			boolean currentDesignClusterActive = true;
			if (currentDesign.getCluster() != null) // determine if design belongs to an active cluster
			{
				currentDesignClusterActive = currentDesign.getCluster().isActive();
			}

			boolean currentDesignActive = true;
			currentDesignActive = currentDesign.isActive(chart); // determine if current design is active

			boolean displayDesign; // flag that determines if design will be displayed

			if (chart.isShowOnlySelectedDesigns()) {
				displayDesign = currentDesign.isSelected() && (currentDesignActive || chart.isShowFilteredDesigns())
				      && (currentDesignClusterActive);
			} else {
				displayDesign = (currentDesignActive || chart.isShowFilteredDesigns()) && (currentDesignClusterActive);
			}

			if (displayDesign) // only draw design if the cluster is active and the design is active (or inactive design drawing is active)
			{
				int lineThickness;
				if (chart.isShowOnlySelectedDesigns() || !currentDesign.isSelected()) {
					g.setColor(chart.getDesignColor(currentDesign, currentDesignActive));
					lineThickness = chart.getDesignLineThickness(currentDesign);
				} else {
					g.setColor(chart.getSelectedDesignColor());
					lineThickness = chart.getSelectedDesignsLineThickness();
				}
				int xPositionCurrent = this.getMarginLeft();
				int yPositionCurrent = axisTopPos;
				int xPositionLast = xPositionCurrent;
				int yPositionLast;
				for (int i = 0; i < axisCount; i++) {
					int yPosition = axisTopPos;
					if (axisActiveFlags[i]) {
						double value = currentDesign.getDoubleValue(this.getDataSheet().getParameter(i));

						int yPositionRelToBottom;
						if (axisRanges[i] == 0) {
							yPositionRelToBottom = (int) (axisHeights[i] * 0.5);
						} else {
							double ratio;
							if (axisInversionFlags[i]) {
								ratio = (axisMaxValues[i] - value) / axisRanges[i];
							} else {
								ratio = (value - axisMinValues[i]) / axisRanges[i];
							}
							yPositionRelToBottom = (int) (axisHeights[i] * ratio);
						}

						yPositionLast = yPositionCurrent;
						yPositionCurrent = yPosition + (axisHeights[i]) - yPositionRelToBottom;

						if (firstAxisDrawn) {
							xPositionCurrent = xPositionCurrent + (int) (axisWidths[i] * 0.5);

							if (lineThickness == 0) {
								g.drawLine(xPositionLast - 3, yPositionLast, xPositionLast + 3, yPositionLast);
								g.drawLine(xPositionCurrent - 3, yPositionCurrent, xPositionCurrent + 3, yPositionCurrent);
							} else {
								for (int t = 1; t <= lineThickness; t++) {
									int deltaY = -((int) (t / 2)) * (2 * (t % 2) - 1);
									g.drawLine(xPositionLast, yPositionLast + deltaY, xPositionCurrent, yPositionCurrent + deltaY);
								}
							}

						} else {
							firstAxisDrawn = true;
							if (chart.isShowDesignIDs()) {
								FontMetrics fm = g.getFontMetrics();
								g.setFont(new Font("SansSerif", Font.PLAIN, designLabelFontSize));
								g.drawString(Integer.toString(currentDesign.getId()),
								      xPositionCurrent - 5 - fm.stringWidth(Integer.toString(currentDesign.getId())), yPositionCurrent
								            + (int) (0.5 * chart.getAxis(i).getTicLabelFontSize()));
							}
						}
						xPositionLast = xPositionCurrent;
						xPositionCurrent = xPositionCurrent + (int) (axisWidths[i] * 0.5);
					}
				}
			}
		}
	}

	/**
	 * Draws the axes.
	 * @param g the graphics object
	 */
	public void drawAxes(Graphics g) {
		ParallelCoordinatesChart chart = ((ParallelCoordinatesChart) this.getChart());
		int xPosition = this.getMarginLeft();
		int yPosition = chart.getAxisTopPos();
		FontMetrics fm = g.getFontMetrics();
		Axis lastAxis = null;
		Axis currentAxis;
		int drawnAxisCount = 0;
		for (int i = 0; i < chart.getAxisCount(); i++) {
			//			logger.info("drawing axis "+chart.getAxis(i).getName());
			if (chart.getAxis(i).isActive()) {
				//axes
				currentAxis = chart.getAxis(i);
				if (null != lastAxis) {
					xPosition = xPosition + (int) (lastAxis.getWidth() * 0.5) + (int) (currentAxis.getWidth() * 0.5);
				}

				String axisLabel = currentAxis.getName();
				int slenX = fm.stringWidth(axisLabel);
				g.setFont(new Font("SansSerif", Font.PLAIN, currentAxis.getAxisLabelFontSize()));

				int yLabelOffset = 0;
				if (chart.isVerticallyOffsetAxisLabels()) {
					yLabelOffset = ((drawnAxisCount++) % 2) * (chart.getMaxAxisLabelFontSize() + chart.getAxisLabelVerticalDistance());
				}

				g.setColor(currentAxis.getAxisLabelFontColor());
				g.drawString(axisLabel, xPosition - (int) (0.5 * slenX), chart.getMaxAxisLabelFontSize() + chart.getTopMargin() + yLabelOffset);

				g.setColor(currentAxis.getAxisColor());
				g.drawLine(xPosition, yPosition, xPosition, yPosition + (chart.getAxisHeight()));

				//Filters

				Filter uf = currentAxis.getUpperFilter();
				Filter lf = currentAxis.getLowerFilter();

				uf.setXPos(xPosition);
				lf.setXPos(xPosition);

				g.setColor(chart.getFilterColor());
				g.drawLine(uf.getXPos(), uf.getYPos(), uf.getXPos() - chart.getFilterWidth(), uf.getYPos() - chart.getFilterHeight());
				g.drawLine(uf.getXPos(), uf.getYPos(), uf.getXPos() + chart.getFilterWidth(), uf.getYPos() - chart.getFilterHeight());
				g.drawLine(uf.getXPos() - chart.getFilterWidth(), uf.getYPos() - chart.getFilterHeight(),
				      uf.getXPos() + chart.getFilterWidth(), uf.getYPos() - chart.getFilterHeight());

				g.drawLine(lf.getXPos(), lf.getYPos(), lf.getXPos() - chart.getFilterWidth(), lf.getYPos() + chart.getFilterHeight());
				g.drawLine(lf.getXPos(), lf.getYPos(), lf.getXPos() + chart.getFilterWidth(), lf.getYPos() + chart.getFilterHeight());
				g.drawLine(lf.getXPos() - chart.getFilterWidth(), lf.getYPos() + chart.getFilterHeight(),
				      lf.getXPos() + chart.getFilterWidth(), lf.getYPos() + chart.getFilterHeight());

				g.setFont(new Font("SansSerif", Font.PLAIN, currentAxis.getTicLabelFontSize()));
				//				logger.info("Font size: "+currentAxis.getTicLabelFontSize());
				if ((uf == this.draggedFilter || lf == this.draggedFilter) && currentAxis.getParameter().isNumeric()) {
					g.drawString(String.format(currentAxis.getTicLabelFormat(), this.draggedFilter.getValue()), this.draggedFilter.getXPos()
					      + chart.getFilterWidth() + 4, this.draggedFilter.getYPos() - chart.getFilterHeight());
				}

				if (null != lastAxis) {
					g.drawLine(lastAxis.getUpperFilter().getXPos(), lastAxis.getUpperFilter().getYPos(), uf.getXPos(), uf.getYPos());
					g.drawLine(lastAxis.getLowerFilter().getXPos(), lastAxis.getLowerFilter().getYPos(), lf.getXPos(), lf.getYPos());
				}

				//tics

				int ticSize = currentAxis.getTicLength();
				int ticCount = currentAxis.getTicCount();
				double ticSpacing; // must be double to avoid large round off errors
				if (ticCount > 1)
					ticSpacing = chart.getAxisHeight() / ((double) (ticCount - 1));
				else
					ticSpacing = 0;
				double axisRange = currentAxis.getRange();
				double ticValueDifference = axisRange / ((double) (ticCount - 1));
				for (int ticID = 0; ticID < ticCount; ticID++) {
					int currentTicYPos;
					if (currentAxis.isAxisInverted())
						currentTicYPos = yPosition + chart.getAxisHeight() - (int) (ticID * ticSpacing);
					else
						currentTicYPos = yPosition + (int) (ticID * ticSpacing);
					g.setColor(currentAxis.getAxisColor());
					if (ticCount > 1)
						g.drawLine(xPosition, currentTicYPos, xPosition + ticSize, currentTicYPos);
					else
						g.drawLine(xPosition, yPosition + (int) (chart.getAxisHeight() / 2), xPosition + ticSize,
						      yPosition + (int) (chart.getAxisHeight() / 2));

					g.setColor(currentAxis.getAxisTicLabelFontColor());

					String ticLabel;
					g.setFont(new Font("SansSerif", Font.PLAIN, currentAxis.getTicLabelFontSize()));
					if (currentAxis.getParameter().isNumeric()) {
						Double ticValue;
						if (ticCount > 1) {
							ticValue = currentAxis.getMax() - ticValueDifference * ticID;
							ticLabel = String.format(currentAxis.getTicLabelFormat(), ticValue);
							g.drawString(ticLabel, xPosition + ticSize + 7, currentTicYPos + (int) (0.5 * currentAxis.getTicLabelFontSize()));
						} else {
							ticValue = currentAxis.getMax();
							ticLabel = String.format(currentAxis.getTicLabelFormat(), ticValue);
							g.drawString(ticLabel, xPosition + 2 * ticSize, yPosition + ((int) (chart.getAxisHeight() / 2))
							      + (int) (0.5 * currentAxis.getTicLabelFontSize()));
						}

					} else {
						if (ticCount > 1) {
							ticLabel = currentAxis.getParameter().getStringValueOf(currentAxis.getMax() - ticValueDifference * ticID);
							g.drawString(ticLabel, xPosition + 2 * ticSize, currentTicYPos + (int) (0.5 * currentAxis.getTicLabelFontSize()));
						} else {
							ticLabel = currentAxis.getParameter().getStringValueOf(currentAxis.getMax());
							g.drawString(ticLabel, xPosition + 2 * ticSize, yPosition + ((int) (chart.getAxisHeight() / 2))
							      + (int) (0.5 * currentAxis.getTicLabelFontSize()));
						}
					}
				}

				lastAxis = currentAxis;
			}
		}
	}

	/**
	 * Finds the axis at a given location in the chart.
	 * @param x the location
	 * @return the found axis
	 */
	private Axis getAxisAtLocation(int x) throws NoAxisFoundException {
		for (int i = 0; i < this.parallelCoordinatesChart.getAxisCount(); i++) {
			Filter uf = this.parallelCoordinatesChart.getAxis(i).getUpperFilter();
			if // check if this axis was meant by the click
			(this.parallelCoordinatesChart.getAxis(i).isActive()
			      && x >= uf.getXPos() - 0.5 * this.parallelCoordinatesChart.getAxis(i).getWidth()
			      && x < uf.getXPos() + 0.5 * this.parallelCoordinatesChart.getAxis(i).getWidth()) {
				return this.parallelCoordinatesChart.getAxis(i);
			}
		}
		throw new NoAxisFoundException(x);
	}

	// TODO MOVE AXIS ACTION
	//	/**
	//	 * Finds the new index when dragging an axis to a given x location.
	//	 * 
	//	 * @param x the location
	//	 * @return the found index
	//	 */
	//	private int getNewAxisIndexAtLocation(int x) throws NoAxisFoundException {
	//		for (int i = 0; i < this.chart.getAxisCount(); i++) {
	//			Filter uf = this.chart.getAxis(i).getUpperFilter();
	//			if // check if this axis was meant by the click
	//			(this.chart.getAxis(i).isActive() && x < uf.getXPos()) {
	//				logger.info("getNewAxisIndexAtLocation: returning index " + i);
	//				return i;
	//			}
	//		}
	//		logger.info("getNewAxisIndexAtLocation: returning index " + this.chart.getAxisCount());
	//		return this.chart.getAxisCount();
	//	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == 3) {
			logger.info("mouseClicked: button " + e.getButton());
			int x = e.getX();
			int y = e.getY();

			logger.info("mouseClicked: x " + x);
			logger.info("mouseClicked: y " + y);
			try {
				Axis axis = this.getAxisAtLocation(x);
				logger.info("Clicked on axis " + axis.getName());
				(new ParallelCoordinatesContextMenu(axis, this)).show(this, x, y);
			} catch (NoAxisFoundException e1) {
				e1.printStackTrace();
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		ParallelCoordinatesChart chart = ((ParallelCoordinatesChart) this.getChart());
		dragStartX = e.getX();
		dragStartY = e.getY();
		for (int i = 0; i < chart.getAxisCount(); i++) {
			Filter uf = chart.getAxis(i).getUpperFilter();
			Filter lf = chart.getAxis(i).getLowerFilter();
			if // check whether the drag operation started on the upper filter
			(chart.getAxis(i).isActive() && dragStartY >= uf.getYPos() - chart.getFilterHeight() && dragStartY <= uf.getYPos()
			      && dragStartX >= uf.getXPos() - chart.getFilterWidth() && dragStartX <= uf.getXPos() + chart.getFilterWidth()) {
				this.draggedFilter = uf;
				this.dragOffsetY = uf.getYPos() - dragStartY;
			} else if // check whether the drag operation started on the lower filter
			(chart.getAxis(i).isActive() && dragStartY >= lf.getYPos() && dragStartY <= lf.getYPos() + chart.getFilterHeight()
			      && dragStartX >= lf.getXPos() - chart.getFilterWidth() && dragStartX <= lf.getXPos() + chart.getFilterWidth()) {
				this.draggedFilter = lf;
				this.dragOffsetY = lf.getYPos() - dragStartY;
			}
		}
		// TODO DRAG AXIS	ACTION
		//		if (this.draggedFilter == null && e.getButton() == 1) {
		//			try {
		//				this.storeBufferedImage();
		//				this.draggedAxis = this.getAxisAtLocation(dragStartX);
		//				logger.info("mousePressed: Drag started, dragged axis : " + this.draggedAxis.getName());
		//			} catch (NoAxisFoundException e1) {
		//				e1.printStackTrace();
		//			}
		//		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		boolean repaintRequired = false;
		if (this.draggedFilter != null) {
			repaintRequired = true;
			this.draggedFilter = null;
		}
		//		TODO MOVE AXIS ACTION
		//		if (this.draggedAxis != null) {
		//			repaintRequired = true;
		//			try {
		//				int newIndex = this.getNewAxisIndexAtLocation(e.getX() - 1); // column index starts at one, param index at 0
		//				DataSheetTableColumnModel cm = (DataSheetTableColumnModel) this.mainWindow.getDataSheetTablePanel().getDataTable()
		//				      .getColumnModel();
		//				int currentIndex = this.mainWindow.getDataSheet().getParameterIndex(this.draggedAxis.getName()) + 1;
		//				logger.info("mouseReleased: dragged axis " + this.draggedAxis.getName() + " had index " + currentIndex);
		//				if (newIndex > currentIndex)
		//					cm.moveColumn(currentIndex, newIndex);
		//				else if (newIndex < currentIndex)
		//					cm.moveColumn(currentIndex, newIndex + 1);
		//			} catch (NoAxisFoundException e1) {
		//				e1.printStackTrace();
		//			}
		//			this.draggedAxis = null;
		//		}
		if (repaintRequired) {
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		if (this.draggedFilter != null) {

			// try to make the filter follow the drag operation, but always keep it within axis boundaries and opposite filter
			this.draggedFilter.setYPos(Math.max(Math.min(e.getY() + this.dragOffsetY, this.draggedFilter.getLowestPos()),
			      this.draggedFilter.getHighestPos()));
			repaint();
		}
		// TODO AXIS DRAGGED ACTION
		//		else if (this.draggedAxis != null) {
		//			this.dragCurrentX = e.getX();
		//			this.repaint();
		//		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int modifier = e.getModifiers();
		if (modifier == 0) {
			this.parallelCoordinatesChart.incrementAxisWidth(-e.getUnitsToScroll());
		} else if (modifier == 2) {
			int x = e.getX();
			try {
				Axis axis = this.getAxisAtLocation(x);
				logger.info("Wheeled on axis " + axis.getName());
				axis.setWidth(Math.max(0, axis.getWidth() - e.getUnitsToScroll()));
			} catch (NoAxisFoundException e1) {
			}
		}

		else if (modifier == 8) {
			int x = e.getX();
			try {
				Axis axis = this.getAxisAtLocation(x);
				logger.info("wheeled on axis " + axis.getName());
				axis.setTicCount(Math.max(2, axis.getTicCount() - e.getWheelRotation()));
			} catch (NoAxisFoundException e1) {
			}
		}
		this.repaint();
	}

	/**
	 * Writes the current chart state to the temporary buffered image.
	 */
	public void storeBufferedImage() {
		this.bufferedImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = this.bufferedImage.createGraphics();
		this.paintComponent(g);
		g.dispose();
	}
}