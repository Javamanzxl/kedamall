package com.zxl.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: FareVo
 * @date ：2024/12/03 14:51
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
