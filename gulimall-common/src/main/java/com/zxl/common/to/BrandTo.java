package com.zxl.common.to;

import com.baomidou.mybatisplus.annotation.TableId;
import com.zxl.common.valid.AddGroup;
import com.zxl.common.valid.ListValue;
import com.zxl.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * @author ：zxl
 * @Description:
 * @ClassName: BrandTo
 * @date ：2024/11/21 21:08
 */
@Data
public class BrandTo {

    /**
     * 品牌id
     */
    private Long brandId;
    /**
     * 品牌名
     */
    private String name;
    /**
     * 品牌logo地址
     */
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    private Integer showStatus;
    /**
     * 检索首字母
     */
    private String firstLetter;
    /**
     * 排序
     */

    private Integer sort;
}
