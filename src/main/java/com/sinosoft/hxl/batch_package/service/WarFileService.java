package com.sinosoft.hxl.batch_package.service;

import com.sinosoft.hxl.batch_package.utils.Const;
import com.sinosoft.hxl.batch_package.utils.WarUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * 功能描述: 提供对War文件进行相关操作的支持
 *
 * @author hxulin
 */
public enum WarFileService {

    INSTANCE;

    private final Logger logger = Logger.getLogger(WarFileService.class);

    /**
     * 检查War文件
     *
     * @param warFileName War文件名
     */
    public void checkWarFile(String warFileName) {
        logger.info("开始检查War文件是否存在...");
        File warFile = new File(Const.WORK_DIRECTORY, warFileName);
        if (!warFile.exists()) {
            logger.error("War文件未找到：" + warFile);
            throw new RuntimeException("War文件未找到：" + warFile);
        }
        logger.info("存在War文件：" + warFile);
    }

    /**
     * 解压待处理的War文件
     *
     * @param warFileName War文件的简单文件名，如：Root.war
     * @throws URISyntaxException #
     */
    public void unZip(String warFileName) throws URISyntaxException {
        URI unZipDirUri = ClassLoader.getSystemResource(Const.TMP_DIRECTORY).toURI();
        URI warFileUri = ClassLoader.getSystemResource(warFileName).toURI();
        WarUtils.unZipWar(Paths.get(warFileUri), Paths.get(unZipDirUri));
    }
}
