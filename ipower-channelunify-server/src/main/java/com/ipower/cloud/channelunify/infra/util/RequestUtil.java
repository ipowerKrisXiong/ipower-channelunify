package com.ipower.cloud.channelunify.infra.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author hk
 * @date 2022/7/22
 */
@Slf4j
public class RequestUtil {
    /**
     * 获取请求体
     *
     * @param request
     * @return
     */
    public static String getBody(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            br = request.getReader();
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            log.error("获取请求体失败", e);
//            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        }
    }
}
