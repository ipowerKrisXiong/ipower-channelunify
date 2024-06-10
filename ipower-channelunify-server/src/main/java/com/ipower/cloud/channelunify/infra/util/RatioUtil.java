package com.ipower.cloud.channelunify.infra.util;

import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : ranze
 * @create 2023/3/2
 * description : 百分比计算的工具类
 */
public class RatioUtil {

    private static final BigDecimal HUNDRED = new BigDecimal(100);

    /**
     *  计算有序列表对应的每一项在这个列表中总和的比率
     * @param dataList 要计算的有序数据列表
     * @param precision 精度，保留几位小数
     * @return 数据列表对应的比率
     */
    public static List<BigDecimal> getPercentValue(List<BigDecimal> dataList, int precision){
        if (CollectionUtils.isEmpty(dataList)){
            return Collections.emptyList();
        }
        BigDecimal sumNum = BigDecimal.ZERO;
        for (BigDecimal data : dataList) {
            sumNum = sumNum.add(data);
        }
        if (sumNum.compareTo(BigDecimal.ZERO) <= 0){
            List<BigDecimal> zeroList = new ArrayList<>(dataList.size());
            for (int i = 0; i < dataList.size(); i++) {
                zeroList.add(BigDecimal.ZERO);
            }
            return zeroList;
        }

        //10的 precision 次幂，用于计算精度。
        BigDecimal digits = BigDecimal.TEN.pow(precision);

        //在精度的基础上扩大比例100
        List<BigDecimal> votesPerQuota = new ArrayList<>(dataList.size());
        for(int i = 0; i < dataList.size(); i++){
            // precision + 3 这里为什么是加3， 加2的话是正好以10为底数需要保留的精度长度的位数，多加1则正好需要保留的位数位上不会存在四舍五入问题，主要是让计算的结果更加精准
            BigDecimal val = dataList.get(i).divide(sumNum, precision + 3, RoundingMode.HALF_UP).multiply(digits).multiply(HUNDRED);
            votesPerQuota.add(val);
        }

        //总数,扩大比例意味的总数要扩大
        BigDecimal targetSeats = digits.multiply(HUNDRED);

        //再向下取值，组成数组
        List<BigDecimal> seats = new ArrayList<>(dataList.size());
        for(int i = 0; i < votesPerQuota.size(); i++){
            seats.add(votesPerQuota.get(i).setScale(0, RoundingMode.DOWN));
        }

        //再新计算合计，用于判断与总数量是否相同,相同则占比会100%
        BigDecimal currentSum = BigDecimal.ZERO;
        for (int i = 0; i < seats.size(); i++) {
            currentSum = currentSum.add(seats.get(i));
        }
        //余数部分的数组:原先数组减去向下取值的数组,得到余数部分的数组
        List<BigDecimal> remainder = new ArrayList<>(dataList.size());

        for(int i = 0; i < seats.size(); i++){
            remainder.add(votesPerQuota.get(i).subtract(seats.get(i)));
        }
        while(currentSum.compareTo(targetSeats) < 0){
            BigDecimal max = BigDecimal.ZERO;
            int maxId = 0;
            for(int i = 0;i < remainder.size();++i){
                if(remainder.get(i).compareTo(max) > 0){
                    max = remainder.get(i);
                    maxId = i;
                }
            }
            //对最大项余额加1
            seats.set(maxId, seats.get(maxId).add(BigDecimal.ONE));
            //已经增加最大余数加1,则下次判断就可以不需要再判断这个余额数。
            remainder.set(maxId, BigDecimal.ZERO);
            //总的也要加1,为了判断是否总数是否相同,跳出循环。
            currentSum = currentSum.add(BigDecimal.ONE);
        }

        for (int i = 0; i < seats.size(); i++) {
            seats.set(i, seats.get(i).divide(digits, precision, RoundingMode.HALF_UP));
        }
        return seats;
    }


    public static void main(String[] args) {
        List<BigDecimal> dataList = new ArrayList<>();
//        dataList.add(BigDecimal.valueOf(0));
//        dataList.add(BigDecimal.valueOf(5));
//        dataList.add(BigDecimal.valueOf(5));
//        dataList.add(BigDecimal.valueOf(1));
//        dataList.add(BigDecimal.valueOf(3));
//        dataList.add(BigDecimal.valueOf(1));
//        dataList.add(BigDecimal.valueOf(2));
//        dataList.add(BigDecimal.valueOf(15));
        dataList.add(BigDecimal.valueOf(6));
        dataList.add(BigDecimal.valueOf(1));
        dataList.add(BigDecimal.valueOf(0));

        List<BigDecimal> percentValue = getPercentValue(dataList, 2);
        for (BigDecimal bigDecimal : percentValue) {
            System.out.println(bigDecimal.doubleValue());
        }
        System.out.println(percentValue);
        System.out.println("============================");

    }


}
