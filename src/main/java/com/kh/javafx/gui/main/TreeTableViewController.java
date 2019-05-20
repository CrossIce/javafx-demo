package com.kh.javafx.gui.main;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jfoenix.controls.*;
import com.jfoenix.controls.cells.editors.DoubleTextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.kh.javafx.utils.NumberUtil;
import io.datafx.controller.ViewController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import org.apache.commons.io.FileUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@ViewController(value = "/fxml/TreeTableView.fxml", title = "Material Design Example")
public class TreeTableViewController {

    private static final String PREFIX = "( ";
    private static final String POSTFIX = " )";

    private static final ObservableList<OptionFx> optionFxes = FXCollections.observableArrayList();


    @FXML
    private JFXTreeTableView<Offer> offerTableView;
    @FXML
    private JFXTreeTableColumn<Offer, String> nameColumn;
    @FXML
    private JFXTreeTableColumn<Offer, String> remarkColumn;
    @FXML
    private JFXTreeTableColumn<Offer, Double> priceColumn;
    @FXML
    private JFXTreeTableColumn<Offer, Double> totalPriceColumn;
    @FXML
    private JFXTreeTableColumn<Offer, Double> countColumn;


    @FXML
    private JFXButton treeTableViewAdd;
    @FXML
    private JFXButton treeTableViewRemove;
    @FXML
    private Label offerTableViewCount;
    @FXML
    private JFXTextField searchField;
    @FXML
    private Label allTotalPrice;

    @FXML
    private JFXComboBox<String> nameField;
    @FXML
    private JFXTextField singlePrice;
    @FXML
    private JFXTextField countNum;
    @FXML
    private JFXTextArea remarkField;


    @FXML
    private JFXTreeTableView<OptionFx> materialTableView;
    @FXML
    private JFXTreeTableColumn<OptionFx, String> materialName;
    @FXML
    private JFXTreeTableColumn<OptionFx, Double> materialPrice;

    @FXML
    private JFXButton materialTableViewRemove;
    @FXML
    private Label materialTableViewCount;
    @FXML
    private JFXTextField searchField1;

    /**
     * init fxml when loaded.
     */
    @PostConstruct
    public void init() {
        setupEditableTableView();
        setupMaterialTableView();
    }

    private <T> void setupCellValueFactory(JFXTreeTableColumn<Offer, T> column, Function<Offer, ObservableValue<T>> mapper) {
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<Offer, T> param) -> {
            if (column.validateValue(param)) {
                return mapper.apply(param.getValue().getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
    }

    private <T> void setupCellValueFactory1(JFXTreeTableColumn<OptionFx, T> column, Function<OptionFx, ObservableValue<T>> mapper) {
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<OptionFx, T> param) -> {
            if (column.validateValue(param)) {
                return mapper.apply(param.getValue().getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
    }

    private void setupEditableTableView() {
        setupCellValueFactory(nameColumn, Offer::nameProperty);
        setupCellValueFactory(remarkColumn, Offer::remarkProperty);
        setupCellValueFactory(priceColumn, p -> p.price.asObject());
        setupCellValueFactory(totalPriceColumn, p -> p.totalPrice.asObject());
        setupCellValueFactory(countColumn, p -> p.number.asObject());

        // add editors
        nameColumn.setCellFactory((TreeTableColumn<Offer, String> param) -> {
            return new GenericEditableTreeTableCell<>(
                    new TextFieldEditorBuilder());
        });
        nameColumn.setOnEditCommit((CellEditEvent<Offer, String> t) -> {
            t.getTreeTableView()
                    .getTreeItem(t.getTreeTablePosition()
                            .getRow())
                    .getValue().name.set(t.getNewValue());
        });

        remarkColumn.setCellFactory((TreeTableColumn<Offer, String> param) -> {
            return new GenericEditableTreeTableCell<>(
                    new TextFieldEditorBuilder());
        });
        remarkColumn.setOnEditCommit((CellEditEvent<Offer, String> t) -> {
            t.getTreeTableView()
                    .getTreeItem(t.getTreeTablePosition()
                            .getRow())
                    .getValue().remark.set(t.getNewValue());
        });
        priceColumn.setCellFactory((TreeTableColumn<Offer, Double> param) -> {
            return new GenericEditableTreeTableCell<>(
                    new DoubleTextFieldEditorBuilder());
        });
        priceColumn.setOnEditCommit((CellEditEvent<Offer, Double> t) -> {

            Offer offer = t.getTreeTableView()
                    .getTreeItem(t.getTreeTablePosition()
                            .getRow())
                    .getValue();
            offer.price.set(t.getNewValue());
            double totalPrice = NumberUtil.calcDoubleValue(t.getNewValue(), offer.getNumber(), 3);
            offer.totalPrice.set(totalPrice);

        });

        totalPriceColumn.setCellFactory((TreeTableColumn<Offer, Double> param) -> {
            return new GenericEditableTreeTableCell<>(
                    new DoubleTextFieldEditorBuilder());
        });
        totalPriceColumn.setOnEditCommit((CellEditEvent<Offer, Double> t) -> {
            t.getTreeTableView()
                    .getTreeItem(t.getTreeTablePosition()
                            .getRow())
                    .getValue().totalPrice.set(t.getNewValue());
        });

        countColumn.setCellFactory((TreeTableColumn<Offer, Double> param) -> {
            return new GenericEditableTreeTableCell<>(
                    new DoubleTextFieldEditorBuilder());
        });
        countColumn.setOnEditCommit((CellEditEvent<Offer, Double> t) -> {
            Offer offer = t.getTreeTableView()
                    .getTreeItem(t.getTreeTablePosition()
                            .getRow())
                    .getValue();


            offer.number.set(t.getNewValue());
            offer.setTotalPrice(NumberUtil.calcDoubleValue(t.getNewValue(), offer.getPrice(), 3));
        });

        final ObservableList<Offer> dummyData = FXCollections.observableArrayList();
        offerTableView.setRoot(new RecursiveTreeItem<>(dummyData, RecursiveTreeObject::getChildren));
        offerTableView.setShowRoot(false);
        offerTableView.setEditable(true);
        offerTableViewCount.textProperty()
                .bind(Bindings.createStringBinding(() -> PREFIX + offerTableView.getCurrentItemsCount() + POSTFIX,
                        offerTableView.currentItemsCountProperty()));
        allTotalPrice.textProperty().bind(Bindings.createStringBinding(() -> {
            final Double[] allTotalPrice = {0.0};
            offerTableView.getRoot().getChildren().forEach(offerTreeItem -> {
                Offer offer = offerTreeItem.getValue();

                allTotalPrice[0] = NumberUtil.calcDoubleValue(allTotalPrice[0], offer.getTotalPrice(), 1);
            });
            return allTotalPrice[0].toString();
        }, offerTableView.getRoot().getChildren()));

        searchField.textProperty()
                .addListener(setupSearchField(offerTableView));

        treeTableViewRemove.disableProperty()
                .bind(Bindings.equal(-1, offerTableView.getSelectionModel().selectedIndexProperty()));

        treeTableViewRemove.setOnMouseClicked((e) -> {
            dummyData.remove(offerTableView.getSelectionModel().selectedItemProperty().get().getValue());
            final IntegerProperty currCountProp = offerTableView.currentItemsCountProperty();
            if (currCountProp.get() > 0) {
                currCountProp.set(currCountProp.get() - 1);
            }
        });


        treeTableViewAdd.setOnMouseClicked((e) -> {

            Offer offer = addOffer();
            if (offer != null) {
                dummyData.add(addOffer());
                final IntegerProperty currCountProp = offerTableView.currentItemsCountProperty();
                currCountProp.set(currCountProp.get() + 1);

                nameField.getSelectionModel().clearSelection();
                singlePrice.textProperty().setValue("");
                countNum.textProperty().setValue("");
                remarkField.textProperty().setValue("");
            }
        });

        nameField.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 0) {
                optionFxes.forEach(option -> {
                    if (option.getName().equals(newValue)) {
                        singlePrice.textProperty().setValue(String.valueOf(option.getPrice()));
                    }
                });
            }
        }));

        File json = new File(TreeTableViewController.class.getResource("/options.json").getPath());
        try {
            String input = FileUtils.readFileToString(json, "UTF-8");
            if (input != null && input.length() > 0) {
                List<Option> tableData = JSONArray.parseArray(input, Option.class);
                if (tableData != null) {
                    optionFxes.addAll(tableData.stream().map(OptionFx::new).collect(Collectors.toList()));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (optionFxes.size() > 0) {
            nameField.setItems(FXCollections.observableArrayList(optionFxes.stream().map(OptionFx::getName).collect(Collectors.toList())));
        }

    }

    private void setupMaterialTableView() {
        setupCellValueFactory1(materialName, OptionFx::nameProperty);
        setupCellValueFactory1(materialPrice, p -> p.price.asObject());

        materialTableView.setRoot(new RecursiveTreeItem<>(optionFxes, RecursiveTreeObject::getChildren));
        materialTableView.setShowRoot(false);

        materialTableViewCount.textProperty()
                .bind(Bindings.createStringBinding(() -> PREFIX + materialTableView.getCurrentItemsCount() + POSTFIX,
                        materialTableView.currentItemsCountProperty()));
        searchField1.textProperty()
                .addListener(setupSearchField1(materialTableView));

        materialTableViewRemove.disableProperty()
                .bind(Bindings.equal(-1, materialTableView.getSelectionModel().selectedIndexProperty()));

        materialTableViewRemove.setOnMouseClicked((e) -> {

            if (optionFxes.remove(materialTableView.getSelectionModel().selectedItemProperty().get().getValue())) {
                nameField.setItems(FXCollections.observableArrayList(optionFxes.stream().map(OptionFx::getName).collect(Collectors.toList())));
                String json = JSON.toJSONString(optionFxes.stream().map(Option::new).collect(Collectors.toList()));

                try {
                    FileUtils.writeStringToFile(new File(TreeTableViewController.class.getResource("/options.json").getPath()), json, "utf-8", false);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            final IntegerProperty currCountProp = materialTableView.currentItemsCountProperty();
            if (currCountProp.get() > 0) {
                currCountProp.set(currCountProp.get() - 1);
            }
        });


    }

    private ChangeListener<String> setupSearchField(final JFXTreeTableView<Offer> tableView) {
        return (o, oldVal, newVal) ->
                tableView.setPredicate(offerTreeItem -> {
                    final Offer offer = offerTreeItem.getValue();
                    return offer.name.get().contains(newVal)
                            || Double.toString(offer.totalPrice.get()).contains(newVal)
                            || offer.remark.get().contains(newVal)
                            || Double.toString(offer.price.get()).contains(newVal);
                });
    }

    private ChangeListener<String> setupSearchField1(final JFXTreeTableView<OptionFx> tableView) {
        return (o, oldVal, newVal) ->
                tableView.setPredicate(optionFxTreeItem -> {
                    final OptionFx optionFx = optionFxTreeItem.getValue();
                    return optionFx.name.get().contains(newVal)
                            || Double.toString(optionFx.price.get()).contains(newVal);
                });
    }

    private Offer addOffer() {

        String name = nameField.valueProperty().getValue();
        String price = singlePrice.textProperty().getValue();
        String count = countNum.textProperty().getValue();
        String remark = remarkField.textProperty().getValue();
        if (name.length() > 0 && price.length() > 0 && count.length() > 0) {
            double priceDouble = Double.parseDouble(price);
            double countDouble = Double.parseDouble(count);
            double totalDouble = NumberUtil.calcDoubleValue(priceDouble, countDouble, 3);

            int size = optionFxes.size();
            if (optionFxes.size() > 0) {
                List<OptionFx> list = optionFxes.stream().filter(option -> option.getName().equals(name)).collect(Collectors.toList());
                if (list.size() < 1) {
                    optionFxes.add(new OptionFx(name, priceDouble));
                    materialTableView.currentItemsCountProperty().setValue(materialTableView.getCurrentItemsCount() + 1);
                }
            } else {
                optionFxes.add(new OptionFx(name, priceDouble));
                materialTableView.currentItemsCountProperty().setValue(materialTableView.getCurrentItemsCount() + 1);
            }

            if (size < optionFxes.size()) {
                nameField.setItems(FXCollections.observableArrayList(optionFxes.stream().map(OptionFx::getName).collect(Collectors.toList())));
                String json = JSON.toJSONString(optionFxes.stream().map(Option::new).collect(Collectors.toList()));

                try {
                    FileUtils.writeStringToFile(new File(TreeTableViewController.class.getResource("/options.json").getPath()), json, "utf-8", false);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


            return new Offer(name, priceDouble, countDouble, totalDouble, remark);
        }

        return null;
    }

    static final class OptionFx extends RecursiveTreeObject<OptionFx> implements Serializable {

        private static final long serialVersionUID = 1L;
        StringProperty name;
        DoubleProperty price;

        OptionFx(String name, double price) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
        }

        OptionFx(Option option) {
            this.name = new SimpleStringProperty(option.getKey());
            this.price = new SimpleDoubleProperty(option.getValue());
        }


        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public double getPrice() {
            return price.get();
        }

        public void setPrice(double price) {
            this.price.set(price);
        }

        public DoubleProperty priceProperty() {
            return price;
        }
    }


    /**
     * 报价单实体
     */
    static final class Offer extends RecursiveTreeObject<Offer> {
        final StringProperty name;
        final DoubleProperty price;
        final DoubleProperty number;
        final DoubleProperty totalPrice;
        final StringProperty remark;

        public Offer(String name, double price, double number,
                     double totalPrice, String remark) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.number = new SimpleDoubleProperty(number);
            this.totalPrice = new SimpleDoubleProperty(totalPrice);
            this.remark = new SimpleStringProperty(remark);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public double getPrice() {
            return price.get();
        }

        public void setPrice(double price) {
            this.price.set(price);
        }

        public DoubleProperty priceProperty() {
            return price;
        }

        public double getNumber() {
            return number.get();
        }

        public void setNumber(double number) {
            this.number.set(number);
        }

        public DoubleProperty numberProperty() {
            return number;
        }

        public double getTotalPrice() {
            return totalPrice.get();
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice.set(totalPrice);
        }

        public DoubleProperty totalPriceProperty() {
            return totalPrice;
        }

        public String getRemark() {
            return remark.get();
        }

        public void setRemark(String remark) {
            this.remark.set(remark);
        }

        public StringProperty remarkProperty() {
            return remark;
        }
    }
}
