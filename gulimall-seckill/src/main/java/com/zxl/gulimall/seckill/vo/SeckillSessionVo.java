package com.zxl.gulimall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: SeckillSessionTo
 * @date ：2024/12/08 14:56
 */
@Data
public class SeckillSessionVo {
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    private List<SeckillSkuRelationVo> relationSkus;
}
