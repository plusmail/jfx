/*
 * Copyright (c) 2014, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package a11y;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.When;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

public class HelloSimpleTableView extends Application {

    public class Item {
        public String item;
        public Item (String item) {
            this.item = item;
        }
        public String getItem() {
            return item;
        }
        public String getEmail() {
            return item+"@oracle.com";
        }
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle(getClass().getSimpleName());
        TableView<Item> tableView = new TableView<>();
        ObservableList<Item> list = FXCollections.observableArrayList();
        for (int i=0; i<128; i++) {
            list.add(new Item("Item " + i));
        }
        tableView.setItems(list);
        TableColumn<Item, String> column1 = new TableColumn<>("Name");
        column1.setPrefWidth(100);
        column1.setCellValueFactory(new PropertyValueFactory<Item, String>("item"));
        TableColumn<Item, String> column2 = new TableColumn<>("Email");
        column2.setPrefWidth(250);
        column2.setCellValueFactory(new PropertyValueFactory<Item, String>("email"));
        tableView.getColumns().addAll(column1, column2);
        tableView.getSelectionModel().selectedIndexProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
                System.out.println("SelectedIndex: " + tableView.getSelectionModel().getSelectedIndex());
            }
        });

        ToggleButton button1 = new ToggleButton("cell selection");
        tableView.getSelectionModel().cellSelectionEnabledProperty().bind(button1.selectedProperty());
        ToggleButton button2 = new ToggleButton("multi selection");
        tableView.getSelectionModel().selectionModeProperty().bind(new When(button2.selectedProperty()).then(SelectionMode.MULTIPLE).otherwise(SelectionMode.SINGLE));
        ToggleButton button3 = new ToggleButton("parented");
        ToggleButton button4 = new ToggleButton("empty");
        button4.setOnAction(t-> {
            if (list.size() == 0) {
                for (int i=0; i<128; i++) {
                    list.add(new Item("Item " + i));
                }
            } else {
                list.setAll();
            }
        });
        VBox group = new VBox(new HBox(button1, button2, button3, button4), tableView);
        button3.setOnAction(e -> {
            if (group.getChildren().contains(tableView)) {
                group.getChildren().remove(tableView);
            } else {
                group.getChildren().add(tableView);
            }
        });

        Scene scene = new Scene(group, 800, 600);
        scene.setOnKeyPressed(l -> {
            if (l.getCode() == KeyCode.DIGIT1) {
                button1.setSelected(!button1.isSelected());
            }
            if (l.getCode() == KeyCode.DIGIT2) {
                button2.setSelected(!button2.isSelected());
            }
        });
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
