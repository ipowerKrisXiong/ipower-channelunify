package com.ipower.cloud.channelunify.infra.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.service.core.model.PageInfoRsp;

/**
 * @author: hechenggang
 * @date: 2022/8/16 11:21
 * @description:
 */
public class PageInfoRspFactory {

    public static PageInfoRsp create(Page page) {
        return PageInfoRsp.builder().pageNum((int) page.getCurrent())
                .pageSize((int) page.getSize())
                .total((int) page.getTotal())
                //组装转换成application层 DTO传出
                .datalist(page.getRecords())
                .build();
    }
}
