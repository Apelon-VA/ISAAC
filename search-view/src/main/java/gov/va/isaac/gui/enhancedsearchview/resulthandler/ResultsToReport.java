package gov.va.isaac.gui.enhancedsearchview.resulthandler;

import gov.va.isaac.gui.enhancedsearchview.model.SearchModel;
import gov.va.isaac.search.CompositeSearchResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultsToReport {
	private static final Logger LOG = LoggerFactory.getLogger(ResultsToReport.class);
	private static SearchModel searchModel = new SearchModel();
	private static Window windowForTableViewExportDialog;

	interface ColumnValueExtractor {
		String extract(TableColumn<CompositeSearchResult, ?> col);
	}

	public static void resultsToReport() {
		FileChooser fileChooser = new FileChooser();
		final String delimiter = ",";
		final String newLine = "\n";

		//Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
		fileChooser.getExtensionFilters().add(extFilter);

		//Show save file dialog
		File file = fileChooser.showSaveDialog(windowForTableViewExportDialog);

		//String tempDir = System.getenv("TEMP");
		//File file = new File(tempDir + File.separator + "EnhanceSearchViewControllerTableViewData.csv");

		if (file == null) {
			LOG.warn("FileChooser returned null export file.  Cancel possibly requested.");
		} else { // if (file != null)
			LOG.debug("Writing TableView data to file \"" + file.getAbsolutePath() + "\"...");

			Writer writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file));
				String headerRow = getTableViewRow(searchModel.getSearchResultsTable().getResults(), delimiter, newLine, (col) -> col.getText());

				LOG.trace(headerRow);
				writer.write(headerRow);

				for (int rowIndex = 0; rowIndex < searchModel.getSearchResultsTable().getResults().getItems().size(); ++rowIndex) {
					final int finalRowIndex = rowIndex;
					String dataRow = getTableViewRow(searchModel.getSearchResultsTable().getResults(), delimiter, newLine, (col) -> col.getCellObservableValue(finalRowIndex).getValue().toString());
					LOG.trace(dataRow);
					writer.write(dataRow);
				}

				LOG.debug("Wrote " + searchModel.getSearchResultsTable().getResults().getItems().size() + " rows of TableView data to file \"" + file.getAbsolutePath() + "\".");
			} catch (IOException e) {
				LOG.error("FAILED writing TableView data to file \"" + file.getAbsolutePath() + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
				e.printStackTrace();
			}
			finally {
				try {
					writer.flush();
				} catch (IOException e) {
					LOG.error("FAILED flushing TableView data file \"" + file.getAbsolutePath() + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
					e.printStackTrace();
				}
				try {
					writer.close();
				} catch (IOException e) {
					LOG.error("FAILED closing TableView data file \"" + file.getAbsolutePath() + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		}
	}

	protected void windowForTableViewExportDialog(Window window) {
		ResultsToReport.windowForTableViewExportDialog = window;
	}

	private static String getTableViewRow(TableView<CompositeSearchResult> table, String delimiter, String lineTerminator, ColumnValueExtractor extractor) {
		ObservableList<TableColumn<CompositeSearchResult, ?>> columns = table.getColumns();
		StringBuilder row = new StringBuilder();

		for (int colIndex = 0; colIndex < columns.size(); ++colIndex) {
			TableColumn<CompositeSearchResult, ?> col = columns.get(colIndex);
			if (! col.isVisible()) {
				// Ensure that newline is written even if column is not
				if (colIndex == (columns.size() - 1) && lineTerminator != null) {
					// Append newline to row
					row.append(lineTerminator);
				}

				continue;
			}
			// Extract text or data from column and append to row
			row.append(extractor.extract(col));
			if (colIndex < (columns.size() - 1)) {
				if (delimiter != null) {
					// Ensure that delimiter is written only if there are remaining visible columns to be written
					boolean hasMoreVisibleCols = false;
					for (int remainingColsIndex = colIndex + 1; remainingColsIndex < columns.size(); ++remainingColsIndex) {
						if (columns.get(remainingColsIndex).isVisible()) {
							hasMoreVisibleCols = true;
							break;
						}
					}
					if (hasMoreVisibleCols) {
						// Append delimiter to row
						row.append(delimiter);
					}
				}
			} else if (colIndex == (columns.size() - 1) && lineTerminator != null) {
				// Append newline to row
				row.append(lineTerminator);
			}
		}

		return row.toString();
	}

}
