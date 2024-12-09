package com.zxl.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: FareVo
 * @date ：2024/12/02 20:21
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;

}
