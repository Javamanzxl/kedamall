package com.zxl.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: StockeLockedTo
 * @date ：2024/12/05 19:52
 */
@Data
public class StockLockedTo {
    private Long taskId; //库存工作单id
    private StockDetailTo taskDetail; //库存工作单详情id
}
