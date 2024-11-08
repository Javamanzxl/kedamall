package com.zxl.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: MemberPrice
 * @date ：2024/11/04 17:18
 */
@Data
public class MemberPrice {
    private Long id;
    private String name;
    private BigDecimal price;
}
