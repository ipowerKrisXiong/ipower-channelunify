package com.ipower.cloud.channelunify.domain.exception;

import com.mars.service.core.exception.BusinessException;

/**
 * 库存不足异常
 */
public class InventoryNotEnoughException extends BusinessException {

    public InventoryNotEnoughException(){
        super(5000, "库存不足!");
    }

    public InventoryNotEnoughException(String msg){
        super(5000, msg);
    }

}
