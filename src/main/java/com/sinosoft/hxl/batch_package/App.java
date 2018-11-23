package com.sinosoft.hxl.batch_package;


import com.sinosoft.hxl.batch_package.bean.MainConfig;
import com.sinosoft.hxl.batch_package.service.CoreService;
import com.sinosoft.hxl.batch_package.service.Environment;
import com.sinosoft.hxl.batch_package.service.WarFileService;
import com.sinosoft.hxl.batch_package.utils.Config;

/**
 * 功能描述: 应用入口
 *
 * @author hxulin
 */
public class App {

    public static void main(String[] args) throws Exception {

        // 加载配置文件
        MainConfig mainConfig = Config.loadMainConfig();

        // 检查War文件是否存在
        String warFileName = mainConfig.getWarFileName();
        WarFileService.INSTANCE.checkWarFile(warFileName);

        // 初始化相关目录
        Environment.INSTANCE.initDirectory();

        // 解压待处理的War文件
        WarFileService.INSTANCE.unZip(warFileName);

        // 开始定制生成War文件
        CoreService.INSTANCE.work(mainConfig);

    }
}
