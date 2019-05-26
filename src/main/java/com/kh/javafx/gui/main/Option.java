package com.kh.javafx.gui.main;

import java.io.Serializable;

/**
 * Option
 *
 * @author crossice
 * @date 2019-05-19
 */
public class Option implements Serializable {

    private static final long serialVersionUID = 1L;
    private String key;
    private Double value;
    private String unit;

    public Option(String key, Double value, String unit) {
        this.key = key;
        this.value = value;
        this.unit = unit;
    }

    public Option() {
    }

    public Option(TreeTableViewController.OptionFx optionFx) {
        this(optionFx.getName(), optionFx.getPrice(), optionFx.getUnit());
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
