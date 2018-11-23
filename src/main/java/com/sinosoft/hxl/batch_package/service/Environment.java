package com.sinosoft.hxl.batch_package.service;

import com.sinosoft.hxl.batch_package.utils.Const;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * 功能描述: 提供对工作空间环境的支持
 *
 * @author hxulin
 */
public enum Environment {

    INSTANCE;

    private static final Logger logger = Logger.getLogger(WarFileService.class);

    private void clearDirectory(String dirName) throws IOException {
        File dir = new File(Const.WORK_DIRECTORY, dirName);
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        if (!dir.mkdir()) {
            logger.error("创建目录失败：" + dir);
        }
    }

    /**
     * 初始化目录
     */
    public void initDirectory() throws IOException {

        // 初始化War文件的解压目录
        logger.info("初始化War文件的解压目录：" + Const.WORK_DIRECTORY + Const.TMP_DIRECTORY);
        clearDirectory(Const.TMP_DIRECTORY);

        // 初始化War文件的生成目录
        logger.info("初始化War文件的生成目录：" + Const.WORK_DIRECTORY + Const.GEN_DIRECTORY);
        clearDirectory(Const.GEN_DIRECTORY);

        // 初始化配置文件的备份目录
        logger.info("初始化配置文件的备份目录：" + Const.WORK_DIRECTORY + Const.BAK_DIRECTORY);
        clearDirectory(Const.BAK_DIRECTORY);
    }
}
