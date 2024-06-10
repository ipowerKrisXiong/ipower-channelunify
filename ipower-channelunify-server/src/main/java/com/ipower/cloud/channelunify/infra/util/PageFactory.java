package com.ipower.cloud.channelunify.infra.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.service.core.model.PageInfoReq;

/**
 * @Author ranze
 * @create 2022/7/13 18:46
 */
public class PageFactory {

    public static <E> Page create(PageInfoReq query) {
        return new Page<E>(query.getPageNum(), query.getPageSize());
    }
}
