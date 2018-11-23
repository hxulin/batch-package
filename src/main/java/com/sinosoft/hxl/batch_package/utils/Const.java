package com.sinosoft.hxl.batch_package.utils;

/**
 * 功能描述: 系统常量
 *
 * @author hxulin
 */
public interface Const {

    /**
     * 应用的ClassPath所在目录
     */
    String WORK_DIRECTORY = ClassLoader.getSystemResource("").getPath();

    /**
     * 新生成War文件的存放目录
     */
    String GEN_DIRECTORY = "_gen";

    /**
     * 配置文件的备份目录
     */
    String BAK_DIRECTORY = "_bak";

    /**
     * 原War文件解压后的存放目录
     */
    String TMP_DIRECTORY = "_tmp";

}
