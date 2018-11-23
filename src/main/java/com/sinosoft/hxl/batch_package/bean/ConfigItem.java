package com.sinosoft.hxl.batch_package.bean;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 功能描述: 映射配置项的实体类
 *
 * @author hxulin
 */
public class ConfigItem {

    private String generateFolder;

    private List<JSONObject> items;

    public String getGenerateFolder() {
        return generateFolder;
    }

    public void setGenerateFolder(String generateFolder) {
        this.generateFolder = generateFolder;
    }

    public List<JSONObject> getItems() {
        return items;
    }

    public void setItems(List<JSONObject> items) {
        this.items = items;
    }
}
