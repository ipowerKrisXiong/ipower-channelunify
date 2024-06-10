package com.ipower.cloud.channelunify.jobs.timer.util;

import com.mars.commonutils.core.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务参数提取工具类
 * 全部传参类似uri
 * 比如 comId=xxx&clubId=xxx
 */
public class JobParamUtil {

    public static Map<String, String> getParm(String jobParamStr) {
        if (!StringUtils.isEmpty(jobParamStr)) {
            //有参数
            String[] params = jobParamStr.split("&");
            HashMap hashMap = new HashMap<>();
            for (String param : params) {
                String[] keyValue = param.split("=");
                hashMap.put(keyValue[0], keyValue[1]);
            }
            return hashMap;
        }
        return null;
    }

    public static <T> T getParm(String jobParamStr, Class<T> clazz) {
        if (!StringUtils.isEmpty(jobParamStr)) {
            //有参数
            String[] params = jobParamStr.split("&");
            if (params.length > 0) {
                HashMap hashMap = new HashMap<>();
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        hashMap.put(keyValue[0], keyValue[1]);
                    }
                }
                String a = JsonUtil.toJSONString(hashMap);
                return JsonUtil.parseObject(a, clazz);
            }
        }
        return null;
    }

//    public static void main(String[] args) {
//        String param = "comId=123";
//        System.out.println(getParm(param));
//        System.out.println(JobParamUtil.<ComAndClub>getParm(param, ComAndClub.class));
//    }

}
