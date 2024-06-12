package com.ipower.cloud.channelunify.domain.types;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 收货地址
 */
@Data
@Getter
@Setter
public class ShipmentAddress {

    //邮编
    String zipcode;

    //地址详情
    String address;

    public ShipmentAddress(String zipcode, String address) {
        this.zipcode = zipcode;
        this.address = address;
    }

    public ShipmentAddress() {
    }
}
