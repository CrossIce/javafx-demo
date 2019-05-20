package com.kh.javafx.utils;

import java.math.BigDecimal;

/**
 * NumberUtil
 *
 * @author crossice
 * @date 2019-05-19
 */
public class NumberUtil {


    /**
     * 方法名:         calcDoubleValue
     * 方法功能描述:   double类型的加减乘除，避免Java 直接计算错误
     *
     * @param: type:   加：add   减：  subtract  乘：multiply 除：divide
     */
    public static double calcDoubleValue(double t1, double t2, int type) {

        BigDecimal a = new BigDecimal(String.valueOf(t1));
        BigDecimal b = new BigDecimal(String.valueOf(t2));
        double retValue = 0f;
        switch (type) {

            case 1:
                retValue = a.add(b).doubleValue();
                break;
            case 2:
                retValue = a.subtract(b).doubleValue();
                break;
            case 3:
                retValue = a.multiply(b).doubleValue();
                break;
            case 4:
                retValue = a.divide(b).doubleValue();
                break;
        }

        return retValue;
    }


}
