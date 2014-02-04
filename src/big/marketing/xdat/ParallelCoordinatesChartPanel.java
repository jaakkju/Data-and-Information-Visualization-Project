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
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import big.marketing.controller.DataController;
import big.marketing.data.Node;

/**
 * Panel that is used to display a {@link chart.ParallelCoordinatesChart}.
 */
public class ParallelCoordinatesChartPanel extends ChartPanel implements MouseMotionListener, MouseListener {
	static Logger logger = Logger.getLogger(ParallelCoordinatesChartPanel.class);

	/** The version tracking unique identifier for Serialization. */
	static final long serialVersionUID = 0003;

	private static final int TICK_LIMIT = 40;

	/** The chart . */
	private ParallelCoordinatesChart chart;

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

	private DataController controller;

	/**
	 * Instantiates a new parallel coordinates chart panel.
	 * @param mainWindow the main Window
	 * @param chart the chart
	 * @param controller
	 */
	public ParallelCoordinatesChartPanel(ParallelCoordinatesChart chart, DataSheet dataSheet, DataController controller) {
		super(dataSheet, chart);
		this.chart = chart;
		this.controller = controller;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		paint(getGraphics());
	}

	/**
	 * Updates selected nodes based on current filter selection and passes selection to controller
	 * 
	 * Just a remark, this code is freaking ugly
	 */
	private void updateFilterSelection() {
		TreeSet<Node> selectedNodes = new TreeSet<>();

		for (int i = 0; i < getDataSheet().getDesignCount(); i++) {
			Design design = getDataSheet().getDesign(i);
			if (design.isInsideBounds(chart)) {

				for (Iterator<Node> iterator = controller.getNetwork().iterator(); iterator.hasNext();) {
					Node node = (Node) iterator.next();

					if (node.getHostName().toLowerCase().equals(design.getStringValue(getDataSheet().getParameter(0)))) {
						selectedNodes.add(node);
					}
				}

			}
		}
		controller.setSelectedNodes((Node[]) selectedNodes.toArray(new Node[selectedNodes.size()]));
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
			int yPosition = chart.getAxisTopPos();
			int xPosition = this.dragCurrentX;
			g.drawLine(xPosition - 1, yPosition, xPosition - 1, yPosition + (chart.getAxisHeight()));
			g.drawLine(xPosition, yPosition, xPosition, yPosition + (chart.getAxisHeight()));
			g.drawLine(xPosition + 1, yPosition, xPosition + 1, yPosition + (chart.getAxisHeight()));
		}
	}

	/**
	 * Draws the lines representing the designs.
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
			boolean firstAxisDrawn = false;
			boolean currentDesignClusterActive = true;
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
				g.setColor(chart.getSelectedDesignColor());
				lineThickness = chart.getSelectedDesignsLineThickness();

				int xPositionCurrent = this.chart.getMarginLeft();
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
		int xPosition = this.chart.getMarginLeft();
		int yPosition = chart.getAxisTopPos();
		FontMetrics fm = g.getFontMetrics();
		Axis lastAxis = null;
		Axis currentAxis;
		int drawnAxisCount = 0;
		for (int i = 0; i < chart.getAxisCount(); i++) {
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
					if (ticCount < TICK_LIMIT) {

						int currentTicYPos;

						if (currentAxis.isAxisInverted()) {
							currentTicYPos = yPosition + chart.getAxisHeight() - (int) (ticID * ticSpacing);
						} else {
							currentTicYPos = yPosition + (int) (ticID * ticSpacing);
							g.setColor(currentAxis.getAxisColor());
						}

						if (ticCount > 1) {
							g.drawLine(xPosition, currentTicYPos, xPosition + ticSize, currentTicYPos);
						} else {
							g.drawLine(xPosition, yPosition + (int) (chart.getAxisHeight() / 2), xPosition + ticSize,
							      yPosition + (int) (chart.getAxisHeight() / 2));

						}

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
				}
				lastAxis = currentAxis;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
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
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		if (this.draggedFilter != null) {
			this.draggedFilter = null;
			repaint();
			updateFilterSelection();
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
