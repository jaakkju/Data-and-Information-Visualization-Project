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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ActionListener that is used for the context menu on the {@link ParallelCoordinatesChartPanel}.
 */
public class ParallelChartContextMenuActionListener implements ActionListener {

	/**
	 * The Axis currently being edited.
	 */
	private Axis axis;

	private ParallelCoordinatesChartPanel parallelCoordinatesChartPanel;

	/**
	 * Instantiates a new context menu for an axis on a parallel coordinates chart.
	 * @param axis the axis
	 */
	public ParallelChartContextMenuActionListener(Axis axis, ParallelCoordinatesChartPanel parallelCoordinatesChartPanel) {
		this.parallelCoordinatesChartPanel = parallelCoordinatesChartPanel;
		this.axis = axis;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals("setCurrentFilterAsNewRange")) {
			axis.setFilterAsNewRange();
			parallelCoordinatesChartPanel.repaint();
		} else if (actionCommand.equals("resetFilter")) {
			axis.resetFilters();
			parallelCoordinatesChartPanel.repaint();
		} else if (actionCommand.equals("autofit")) {
			axis.autofit();
			parallelCoordinatesChartPanel.repaint();
		}

		// TODO MOVE AXIS ACTION
		//			else if (actionCommand.equals("moveAxisLeft")) {
		//			DataSheetTableColumnModel cm = (DataSheetTableColumnModel) this.mainWindow.getDataSheetTablePanel().getDataTable()
		//			      .getColumnModel();
		//			int currentIndex = this.mainWindow.getDataSheet().getParameterIndex(axis.getName());
		//			boolean jumpedAxisWasInactive = true;
		//			while (currentIndex > 0 && jumpedAxisWasInactive) {
		//				jumpedAxisWasInactive = !((ParallelCoordinatesChart) parallelCoordinatesChartPanel.getChart()).getAxis(currentIndex - 1)
		//				      .isActive();
		//				//				log("jumpedAxisWasInactive "+jumpedAxisWasInactive+" name: "+((ParallelCoordinatesChart)parallelCoordinatesChartPanel.getChart()).getAxis(currentIndex-1).getName());
		//				cm.moveColumn(currentIndex + 1, currentIndex); // column index starts at one, param index at 0
		//				currentIndex--;
		//			}
		//		}
		//
		//		else if (actionCommand.equals("moveAxisRight")) {
		//			DataSheetTableColumnModel cm = (DataSheetTableColumnModel) this.mainWindow.getDataSheetTablePanel().getDataTable()
		//			      .getColumnModel();
		//			int currentIndex = this.mainWindow.getDataSheet().getParameterIndex(axis.getName());
		//			boolean jumpedAxisWasInactive = true;
		//			while (currentIndex + 2 < cm.getColumnCount() && jumpedAxisWasInactive) {
		//				jumpedAxisWasInactive = !((ParallelCoordinatesChart) parallelCoordinatesChartPanel.getChart()).getAxis(currentIndex + 1)
		//				      .isActive();
		//				//				log("jumpedAxisWasInactive "+jumpedAxisWasInactive+" name: "+((ParallelCoordinatesChart)parallelCoordinatesChartPanel.getChart()).getAxis(currentIndex-1).getName());
		//				cm.moveColumn(currentIndex + 1, currentIndex + 2); // column index starts at one, param index at 0
		//				currentIndex++;
		//			}
		//		}

		else if (actionCommand.equals("hideAxis")) {
			axis.setActive(false);
			parallelCoordinatesChartPanel.repaint();
		} else if (actionCommand.equals("addTic")) {
			axis.setTicCount(axis.getTicCount() + 1);
			parallelCoordinatesChartPanel.repaint();
		} else if (actionCommand.equals("removeTic")) {
			axis.setTicCount(Math.max(2, axis.getTicCount() - 1));
			parallelCoordinatesChartPanel.repaint();
		} else if (actionCommand.equals("reduceDistanceThisAxis")) {
			axis.setWidth(Math.max(0, axis.getWidth() - 10));
			parallelCoordinatesChartPanel.repaint();
		} else if (actionCommand.equals("increaseDistanceThisAxis")) {
			axis.setWidth(Math.max(0, axis.getWidth() + 10));
			parallelCoordinatesChartPanel.repaint();
		} else if (actionCommand.equals("resetAllFilters")) {
			ParallelCoordinatesChart chart = axis.getChart();
			for (int i = 0; i < chart.getAxisCount(); i++) {
				chart.getAxis(i).resetFilters();
			}
			parallelCoordinatesChartPanel.repaint();
		} else if (actionCommand.equals("reduceDistanceAllAxes")) {
			ParallelCoordinatesChart chart = axis.getChart();
			for (int i = 0; i < chart.getAxisCount(); i++) {
				chart.getAxis(i).setWidth(Math.max(0, chart.getAxis(i).getWidth() - 10));
			}
			parallelCoordinatesChartPanel.repaint();
		} else if (actionCommand.equals("increaseDistanceAllAxes")) {
			ParallelCoordinatesChart chart = axis.getChart();
			for (int i = 0; i < chart.getAxisCount(); i++) {
				chart.getAxis(i).setWidth(Math.max(0, chart.getAxis(i).getWidth() + 10));
			}
			parallelCoordinatesChartPanel.repaint();
		} else {
			System.out.println(e.getActionCommand());
		}
	}
}
