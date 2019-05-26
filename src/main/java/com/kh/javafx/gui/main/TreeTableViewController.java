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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.apache.commons.io.FileUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.Number;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ViewController(value = "/fxml/TreeTableView.fxml", title = "Material Design Example")
public class TreeTableViewController {

    private static final String PREFIX = "( ";
    private static final String POSTFIX = " )";

    private static final ObservableList<OptionFx> optionFxes = FXCollections.observableArrayList();

    private static final ObservableList<Object[]> partList = FXCollections.observableArrayList();

    private static final DoubleProperty total = new SimpleDoubleProperty(0.0);


    @FXML
    private JFXTreeTableView<Offer> offerTableView;
    @FXML
    private JFXTreeTableColumn<Offer, String> partColumn;
    @FXML
    private JFXTreeTableColumn<Offer, String> nameColumn;
    @FXML
    private JFXTreeTableColumn<Offer, String> unitColumn;
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
    private JFXButton excelExport;

    @FXML
    private JFXComboBox<String> nameField;
    @FXML
    private JFXTextField singlePrice;
    @FXML
    private JFXTextField countNum;
    @FXML
    private JFXTextArea remarkField;
    @FXML
    private JFXComboBox<String> partField;
    @FXML
    private JFXTextField unitField;


    @FXML
    private JFXTreeTableView<OptionFx> materialTableView;
    @FXML
    private JFXTreeTableColumn<OptionFx, String> materialName;
    @FXML
    private JFXTreeTableColumn<OptionFx, Double> materialPrice;
    @FXML
    private JFXTreeTableColumn<OptionFx, String> materialUnit;

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
        setupCellValueFactory(partColumn, Offer::partProperty);
        setupCellValueFactory(unitColumn, Offer::unitProperty);
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
            double oldTotalPrice = offer.getTotalPrice();
            double totalPrice = NumberUtil.calcDoubleValue(t.getNewValue(), offer.getNumber(), 3);
            offer.totalPrice.set(totalPrice);
            partList.forEach(part1 -> {
                if (offer.getPart().equals(part1[0])) {
                    Double oldValue = (Double) part1[1];
                    part1[1] = NumberUtil.calcDoubleValue(NumberUtil.calcDoubleValue(oldValue, oldTotalPrice, 2), offer.getTotalPrice(), 1);
                    total.setValue(NumberUtil.calcDoubleValue(NumberUtil.calcDoubleValue(total.doubleValue(), oldValue, 2), (Double) part1[1], 1));
                }
            });

        });

        totalPriceColumn.setCellFactory((TreeTableColumn<Offer, Double> param) -> {
            return new GenericEditableTreeTableCell<>(
                    new DoubleTextFieldEditorBuilder());
        });
        totalPriceColumn.setOnEditCommit((CellEditEvent<Offer, Double> t) -> {

            Offer offer = t.getTreeTableView()
                    .getTreeItem(t.getTreeTablePosition()
                            .getRow())
                    .getValue();
            offer.totalPrice.set(t.getNewValue());
            partList.forEach(part1 -> {
                if (offer.getPart().equals(part1[0])) {
                    Double oldValue = (Double) part1[1];
                    part1[1] = NumberUtil.calcDoubleValue(NumberUtil.calcDoubleValue(oldValue, t.getOldValue(), 2), offer.getTotalPrice(), 1);
                    total.setValue(NumberUtil.calcDoubleValue(NumberUtil.calcDoubleValue(total.doubleValue(), oldValue, 2), (Double) part1[1], 1));
                }
            });
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
            double oldTotalPrice = offer.getTotalPrice();
            offer.setTotalPrice(NumberUtil.calcDoubleValue(t.getNewValue(), offer.getPrice(), 3));
            partList.forEach(part1 -> {
                if (offer.getPart().equals(part1[0])) {
                    Double oldValue = (Double) part1[1];
                    part1[1] = NumberUtil.calcDoubleValue(NumberUtil.calcDoubleValue(oldValue, oldTotalPrice, 2), offer.getTotalPrice(), 1);
                    total.setValue(NumberUtil.calcDoubleValue(NumberUtil.calcDoubleValue(total.doubleValue(), oldValue, 2), (Double) part1[1], 1));

                }
            });
        });

        final ObservableList<Offer> dummyData = FXCollections.observableArrayList();
        offerTableView.setRoot(new RecursiveTreeItem<>(dummyData, RecursiveTreeObject::getChildren));
        offerTableView.setShowRoot(false);
        offerTableView.setEditable(true);
        offerTableViewCount.textProperty()
                .bind(Bindings.createStringBinding(() -> PREFIX + offerTableView.getCurrentItemsCount() + POSTFIX,
                        offerTableView.currentItemsCountProperty()));

        total.addListener((observable, oldValue, newValue) -> {
            allTotalPrice.textProperty().setValue(newValue.toString());
        });

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
                dummyData.add(offer);
                final IntegerProperty currCountProp = offerTableView.currentItemsCountProperty();
                currCountProp.set(currCountProp.get() + 1);

                nameField.getSelectionModel().clearSelection();
                singlePrice.textProperty().setValue("");
                countNum.textProperty().setValue("");
                remarkField.textProperty().setValue("");
                unitField.textProperty().setValue("");
            }
        });

        excelExport.setOnMouseClicked((e) -> {

            if (partList.size() > 0) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("导出报价单");
                fileChooser.setInitialFileName(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "报价单.xlsx");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLS Files", "*.xlsx"));
                File file = fileChooser.showSaveDialog(new Stage());
                if (file != null) {
                    exportExcel(file.getAbsolutePath());
                }
            }


        });

        nameField.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 0) {
                optionFxes.forEach(option -> {
                    if (option.getName().equals(newValue)) {
                        singlePrice.textProperty().setValue(String.valueOf(option.getPrice()));
                        unitField.textProperty().setValue(option.getUnit());
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
        setupCellValueFactory1(materialUnit, OptionFx::unitProperty);

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
        String part = partField.valueProperty().getValue();
        String unit = unitField.textProperty().getValue();
        if (name.length() > 0 && price.length() > 0 && count.length() > 0) {
            double priceDouble = Double.parseDouble(price);
            double countDouble = Double.parseDouble(count);
            double totalDouble = NumberUtil.calcDoubleValue(priceDouble, countDouble, 3);

            int size = optionFxes.size();
            int sizePart = partList.size();
            if (optionFxes.size() > 0) {
                List<OptionFx> list = optionFxes.stream().filter(option -> option.getName().equals(name)).collect(Collectors.toList());
                if (list.size() < 1) {
                    optionFxes.add(new OptionFx(name, priceDouble, unit));
                    materialTableView.currentItemsCountProperty().setValue(materialTableView.getCurrentItemsCount() + 1);
                }
            } else {
                optionFxes.add(new OptionFx(name, priceDouble, unit));
                materialTableView.currentItemsCountProperty().setValue(materialTableView.getCurrentItemsCount() + 1);
            }

            if (partList.size() > 0) {
                if (!partList.contains(part)) {
                    partList.add(new Object[]{part, totalDouble});
                    total.setValue(NumberUtil.calcDoubleValue(total.doubleValue(), totalDouble, 1));
                }
            } else {
                partList.add(new Object[]{part, totalDouble});
                total.setValue(NumberUtil.calcDoubleValue(total.doubleValue(), totalDouble, 1));
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

            if (sizePart < partList.size()) {
                partField.setItems(FXCollections.observableArrayList(partList.stream().map(part1 -> part1[0].toString()).collect(Collectors.toList())));
            }

            return new Offer(name, priceDouble, countDouble, totalDouble, remark, part, unit);
        }

        return null;
    }


    /**
     * 导出excel
     *
     * @param path
     */
    private void exportExcel(String path) {


        OutputStream out = null;
        try {
            out = new FileOutputStream(path);

            WritableWorkbook wwb = Workbook.createWorkbook(out);

            //标题
            WritableCellFormat wcfFC = new WritableCellFormat(
                    new jxl.write.WritableFont(WritableFont.createFont("宋体"), 14,
                            WritableFont.NO_BOLD, false));

            wcfFC.setAlignment(Alignment.CENTRE);
            wcfFC.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);


            //子标题
            WritableCellFormat cellFormat = new WritableCellFormat();

            cellFormat.setBackground(Colour.GRAY_25);
            cellFormat.setFont(new jxl.write.WritableFont(WritableFont.createFont("宋体"), 12,
                    WritableFont.BOLD, false));

            cellFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);

            //普通文本
            WritableCellFormat cellFormat1 = new WritableCellFormat(
                    new jxl.write.WritableFont(WritableFont.createFont("宋体"), 12,
                            WritableFont.NO_BOLD, false));
            cellFormat1.setWrap(true);
            cellFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            cellFormat1.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);


            WritableSheet ws = wwb.createSheet("报价单", 1);

            int[] cols = new int[]{10, 40, 20, 20, 20, 20, 60};

            for (int i = 0; i < cols.length; i++) {
                ws.setColumnView(i, cols[i]);
            }

            List<String> head = new ArrayList<String>() {{
                add("序号");
                add("工程或费用名称");
                add("单位");
                add("单价（元）");
                add("数量");
                add("合价（元）");
                add("材料结构与制作工艺标准");
            }};

            for (int i = 0; i < 7; i++) {
                jxl.write.Label labelC = new jxl.write.Label(i, 0, head.get(i), wcfFC);
                ws.addCell(labelC);
            }

            Map<String, List<Offer>> listMap = new HashMap<>();
            offerTableView.getRoot().getChildren().forEach(offerTreeItem -> {
                Offer offer = offerTreeItem.getValue();
                if (listMap.get(offer.getPart()) != null) {
                    listMap.get(offer.getPart()).add(offer);
                } else {
                    listMap.put(offer.getPart(), new ArrayList<Offer>() {{
                        add(offer);
                    }});
                }
            });


            final int[] rowIndex = {1};
            partList.forEach(partX -> {

                try {
                    for (int i = 0; i < 7; i++) {

                        if (i == 0) {
                            jxl.write.Label labelC = new jxl.write.Label(i, rowIndex[0], partX[0].toString(), cellFormat);
                            ws.addCell(labelC);

                        } else if (i == 5) {

                            jxl.write.Number labelC = new jxl.write.Number(i,
                                    rowIndex[0], ((Number) partX[1]).doubleValue(), cellFormat);

                            ws.addCell(labelC);
                        } else {
                            jxl.write.Label labelC = new jxl.write.Label(i, rowIndex[0], "", cellFormat);
                            ws.addCell(labelC);
                        }
                    }

                    ws.mergeCells(0, rowIndex[0], 4, rowIndex[0]);

                    rowIndex[0]++;


                    List<Offer> offers = listMap.get(partX[0]);

                    if (offers != null && offers.size() > 0) {
                        for (int i = 0; i < offers.size(); i++) {

                            Offer offer = offers.get(i);

                            jxl.write.Number label1 = new jxl.write.Number(0, rowIndex[0], i + 1, cellFormat1);
                            ws.addCell(label1);

                            jxl.write.Label label2 = new jxl.write.Label(1, rowIndex[0], offer.getName(), cellFormat1);
                            ws.addCell(label2);

                            jxl.write.Label label3 = new jxl.write.Label(2, rowIndex[0], offer.getUnit(), cellFormat1);
                            ws.addCell(label3);

                            jxl.write.Number label4 = new jxl.write.Number(3, rowIndex[0], offer.getPrice(), cellFormat1);
                            ws.addCell(label4);

                            jxl.write.Number label5 = new jxl.write.Number(4, rowIndex[0], offer.getNumber(), cellFormat1);
                            ws.addCell(label5);

                            jxl.write.Number label6 = new jxl.write.Number(5, rowIndex[0], offer.getTotalPrice(), cellFormat1);
                            ws.addCell(label6);

                            jxl.write.Label label7 = new jxl.write.Label(6, rowIndex[0], offer.getRemark(), cellFormat1);
                            ws.addCell(label7);

                            rowIndex[0]++;

                        }
                    }


                } catch (WriteException e) {
                    e.printStackTrace();
                }

            });

            for (int i = 0; i < 7; i++) {

                if (i == 0) {
                    jxl.write.Label labelC = new jxl.write.Label(i, rowIndex[0], "工程总价：", cellFormat);
                    ws.addCell(labelC);

                } else if (i == 5) {

                    jxl.write.Number labelC = new jxl.write.Number(i,
                            rowIndex[0], total.doubleValue(), cellFormat);

                    ws.addCell(labelC);
                } else {
                    jxl.write.Label labelC = new jxl.write.Label(i, rowIndex[0], "", cellFormat);
                    ws.addCell(labelC);
                }
            }
            ws.mergeCells(0, rowIndex[0], 4, rowIndex[0]);
            rowIndex[0]++;

            for (int i = 0; i < 7; i++) {

                if (i == 0) {
                    jxl.write.Label labelC = new jxl.write.Label(i, rowIndex[0], "税金：", cellFormat);
                    ws.addCell(labelC);

                } else if (i == 5) {

                    jxl.write.Number labelC = new jxl.write.Number(i,
                            rowIndex[0], 0.0, cellFormat);

                    ws.addCell(labelC);
                } else {
                    jxl.write.Label labelC = new jxl.write.Label(i, rowIndex[0], "", cellFormat);
                    ws.addCell(labelC);
                }
            }
            ws.mergeCells(0, rowIndex[0], 4, rowIndex[0]);
            rowIndex[0]++;

            for (int i = 0; i < 7; i++) {

                if (i == 0) {
                    jxl.write.Label labelC = new jxl.write.Label(i, rowIndex[0], "总计（大写）：", cellFormat);
                    ws.addCell(labelC);

                } else if (i == 5) {

                    jxl.write.Number labelC = new jxl.write.Number(i,
                            rowIndex[0], 0.0, cellFormat);

                    ws.addCell(labelC);
                } else {
                    jxl.write.Label labelC = new jxl.write.Label(i, rowIndex[0], "", cellFormat);
                    ws.addCell(labelC);
                }
            }
            ws.mergeCells(0, rowIndex[0], 4, rowIndex[0]);
            rowIndex[0]++;


            wwb.write();
            wwb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    static final class OptionFx extends RecursiveTreeObject<OptionFx> implements Serializable {

        private static final long serialVersionUID = 1L;
        StringProperty name;
        DoubleProperty price;
        StringProperty unit;

        OptionFx(String name, double price, String unit) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.unit = new SimpleStringProperty(unit);
        }

        OptionFx(Option option) {
            this.name = new SimpleStringProperty(option.getKey());
            this.price = new SimpleDoubleProperty(option.getValue());
            this.unit = new SimpleStringProperty(option.getUnit());
        }

        public String getUnit() {
            return unit.get();
        }

        public void setUnit(String unit) {
            this.unit.set(unit);
        }

        public StringProperty unitProperty() {
            return unit;
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

        final StringProperty part;
        final StringProperty name;
        final StringProperty unit;
        final DoubleProperty price;
        final DoubleProperty number;
        final DoubleProperty totalPrice;
        final StringProperty remark;

        public Offer(String name, double price, double number,
                     double totalPrice, String remark, String part, String unit) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.number = new SimpleDoubleProperty(number);
            this.totalPrice = new SimpleDoubleProperty(totalPrice);
            this.remark = new SimpleStringProperty(remark);
            this.part = new SimpleStringProperty(part);
            this.unit = new SimpleStringProperty(unit);
        }

        public String getPart() {
            return part.get();
        }

        public void setPart(String part) {
            this.part.set(part);
        }

        public StringProperty partProperty() {
            return part;
        }

        public String getUnit() {
            return unit.get();
        }

        public void setUnit(String unit) {
            this.unit.set(unit);
        }

        public StringProperty unitProperty() {
            return unit;
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
