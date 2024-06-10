package com.ipower.cloud.channelunify.infra.util;

import java.math.BigDecimal;

/**
 * 价格计算通用工具
 */
public class PriceUtil {

    final static BigDecimal HUNDRED = BigDecimal.valueOf(100L);

    /**
     * 传入分价格，返回一个展示用元.角分单位价格,精确到分
     * 保留2位，后面全部舍弃
     * @param priceFen
     * @return
     */
    public static String getDisplayYuanPrice(Long priceFen){
        return BigDecimal.valueOf(priceFen).divide(HUNDRED,2,BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * 元转分
     * @param yuan
     * @return
     */
    public static long yuanToFen(BigDecimal yuan){
        return yuan.multiply(HUNDRED).longValue();
    }

    /**
     * 分转元带小数
     * @param fenLong
     * @return
     */
    public static BigDecimal fenToYuan(Long fenLong){
        return BigDecimal.valueOf(fenLong).divide(HUNDRED);
    }

    /**
     * 分转元带小数
     * @param fenDecimal
     * @return
     */
    public static BigDecimal fenToYuan(BigDecimal fenDecimal){
        return fenDecimal.divide(HUNDRED);
    }

    /**
     * 用于显示的百分比
     * 除数/被除数 固定精度百分比，保留1位 如果是20.22%,最终返回20.2
     * @param sourceDecimal
     * @param toDivideDecimal
     * @return
     */
    public static BigDecimal divideRatioForDisplay(BigDecimal sourceDecimal, BigDecimal toDivideDecimal){
        return sourceDecimal.divide(toDivideDecimal,4, BigDecimal.ROUND_HALF_UP).multiply(HUNDRED).setScale(1,BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 用于计算的百分比
     * 除数/被除数 固定精度百分比，保留1位 如果是20.26%,最终返回0.203
     * @param sourceDecimal
     * @param toDivideDecimal
     * @return
     */
    public static BigDecimal divideRatioForCompute(BigDecimal sourceDecimal, BigDecimal toDivideDecimal){
        return sourceDecimal.divide(toDivideDecimal,3, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 浮点转整数
     * @return
     */
    public static BigDecimal floatToIntegerRoundHalfUp(BigDecimal sourceDecimal){
        return sourceDecimal.setScale(0,BigDecimal.ROUND_HALF_UP);
    }

    public static void main(String[] args) {
        System.out.println(PriceUtil.divideRatioForDisplay(new BigDecimal("1.21"),new BigDecimal("101"))); //1.20
        System.out.println(PriceUtil.divideRatioForDisplay(new BigDecimal("3"),new BigDecimal("101"))); //2.97
        System.out.println(PriceUtil.floatToIntegerRoundHalfUp(BigDecimal.valueOf(1L).multiply(PriceUtil.divideRatioForCompute(BigDecimal.valueOf(300),BigDecimal.valueOf(10100)))));
    }

}
