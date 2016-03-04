/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fxmltableview;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;

/**
 *
 * @author stefankoch
 * from http://info.michael-simons.eu/2014/10/27/custom-editor-components-in-javafx-tablecells/
 * Enables Color Picker in Table functionality
 */
public class ColorTableCell<T> extends TableCell<T, Color> {    
    private final ColorPicker colorPicker;
 
    public ColorTableCell(TableColumn<T, Color> column) {
	this.colorPicker = new ColorPicker();
	this.colorPicker.editableProperty().bind(column.editableProperty());
	this.colorPicker.disableProperty().bind(column.editableProperty().not());
	this.colorPicker.setOnShowing(event -> {
	    final TableView<T> tableView = getTableView();
	    tableView.getSelectionModel().select(getTableRow().getIndex());
	    tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);	    
	});
	this.colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
	    if(isEditing()) {
		commitEdit(newValue);
	    }
	});		
	setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }
 
    @Override
    protected void updateItem(Color item, boolean empty) {
	super.updateItem(item, empty);	
 
	setText(null);	
	if(empty) {	    
	    setGraphic(null);
	} else {	    
	    this.colorPicker.setValue(item);
	    this.setGraphic(this.colorPicker);
	} 
    }
}
