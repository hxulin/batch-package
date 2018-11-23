package com.sinosoft.hxl.batch_package.utils;

import com.alibaba.fastjson.JSON;
import com.sinosoft.hxl.batch_package.bean.MainConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * 功能描述: 处理配置文件
 *
 * @author hxulin
 */
public final class Config {

    /**
     * 配置文件名
     */
    private static final String CONFIG_FILE_NAME = "config.json";

    private Config() {

    }

    /**
     * 加载核心配置文件
     *
     * @return 全局配置对象
     */
    public static MainConfig loadMainConfig() throws IOException {
        InputStream in = ClassLoader.getSystemResourceAsStream(CONFIG_FILE_NAME);
        return JSON.parseObject(in, MainConfig.class);
    }

}
