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

    public Option(String key, Double value) {
        this.key = key;
        this.value = value;
    }

    public Option() {
    }

    public Option(TreeTableViewController.OptionFx optionFx) {
        this(optionFx.getName(), optionFx.getPrice());
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
