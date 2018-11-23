package com.sinosoft.hxl.batch_package.bean;

import java.util.List;

/**
 * 功能描述: 映射配置文件的主实体类
 *
 * @author hxulin
 */
public class MainConfig {

    private String warFileName;

    private Strategy strategy;

    private List<ConfigItem> configuration;

    public String getWarFileName() {
        return warFileName;
    }

    public void setWarFileName(String warFileName) {
        this.warFileName = warFileName;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public List<ConfigItem> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(List<ConfigItem> configuration) {
        this.configuration = configuration;
    }
}
