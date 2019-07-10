package com.adbest.smsmarketingfront.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class KeywordInfo implements Serializable {

    private BigDecimal price;

    private Integer freeNumm;
}
