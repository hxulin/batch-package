package com.sinosoft.hxl.batch_package.bean;

/**
 * 功能描述: 配置策略
 *
 * @author hxulin
 */
public class Strategy {

    private String include;

    private String exclude;

    private String active;

    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
