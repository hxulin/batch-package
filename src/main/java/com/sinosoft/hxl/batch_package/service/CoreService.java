package com.sinosoft.hxl.batch_package.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sinosoft.hxl.batch_package.bean.ConfigItem;
import com.sinosoft.hxl.batch_package.bean.MainConfig;
import com.sinosoft.hxl.batch_package.bean.Strategy;
import com.sinosoft.hxl.batch_package.utils.Const;
import com.sinosoft.hxl.batch_package.utils.TxtUtils;
import com.sinosoft.hxl.batch_package.utils.WarUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 功能描述: 核心服务类
 *
 * @author hxulin
 */
public enum CoreService {

    INSTANCE;

    private static final Logger logger = Logger.getLogger(CoreService.class);

    /**
     * 根据strategy配置筛选有效的配置项
     *
     * @param mainConfig 核心配置对象
     * @return 有效的配置项
     */
    private List<ConfigItem> getValidConfigItems(MainConfig mainConfig) {

        Strategy strategy = mainConfig.getStrategy();
        String activeStrategy = mainConfig.getStrategy().getActive();
        List<ConfigItem> configItems = mainConfig.getConfiguration();

        if ("include".equals(activeStrategy)) {
            logger.info("当前使用的生成策略：include");
            String includes = "|" + strategy.getInclude() + "|";
            Iterator<ConfigItem> iterator = configItems.iterator();
            while (iterator.hasNext()) {
                ConfigItem configItem = iterator.next();
                String folder = configItem.getGenerateFolder();
                if (!includes.contains("|" + folder + "|")) {
                    logger.info("排除配置节点：" + folder);
                    iterator.remove();
                }
            }
            return configItems;
        }

        if ("exclude".equals(activeStrategy)) {
            logger.info("当前使用的生成策略：exclude");
            String excludes = "|" + strategy.getExclude() + "|";
            Iterator<ConfigItem> iterator = configItems.iterator();
            while (iterator.hasNext()) {
                ConfigItem configItem = iterator.next();
                String folder = configItem.getGenerateFolder();
                if (excludes.contains("|" + folder + "|")) {
                    logger.info("排除配置节点：" + folder);
                    iterator.remove();
                }
            }
            return configItems;
        }

        if (!"default".equals(activeStrategy)) {
            logger.warn("核心配置 strategy -> active 应为 default、include或exclude，当前配置为：" + activeStrategy + "，配置无效，已使用default配置");
        }
        logger.info("当前使用的生成策略：default");
        return configItems;
    }

    /**
     * 备份需要修改的文件
     *
     * @param configItems 配置项条目
     * @return 需要修改的待处理文件的源和备份信息，key表示
     * @throws Exception #
     */
    private Map<Path, Path> backup(List<ConfigItem> configItems) throws Exception {
        logger.info("开始扫描待处理文件，请稍候...");
        // 存储待处理文件的源和目标信息，键：待处理文件的源Path，值：备份待处理文件的Path
        Map<Path, Path> confFileInfo = new HashMap<>();
        for (ConfigItem configItem : configItems) {
            for (JSONObject item : configItem.getItems()) {
                String configFileName = item.getString("configFileName");
                if (!configFileName.startsWith("/") && !configFileName.startsWith("\\")) {
                    configFileName = File.separator + configFileName;
                }
                String relativeFileName = Const.TMP_DIRECTORY + configFileName;
                URL confFileUrl = ClassLoader.getSystemResource(relativeFileName);
                if (confFileUrl == null) {
                    logger.warn("指定的待处理文件不存在：" + configFileName);
                } else {
                    logger.info("发现待处理文件：" + configFileName);
                    URI configFileUri = confFileUrl.toURI();
                    Path confFilePath = Paths.get(configFileUri);
                    File targetFile = new File(Const.WORK_DIRECTORY + Const.BAK_DIRECTORY + configFileName);
                    confFileInfo.put(confFilePath, Paths.get(targetFile.toURI()));
                }
            }
        }
        if (confFileInfo.isEmpty()) {
            logger.info("根据你的配置，二次打包时，没有需要修改的待处理待处理文件");
        } else {
            logger.info("根据你的配置，共发现 " + confFileInfo.size() + " 份待处理文件");
            logger.info("开始备份待处理文件...");
            for (Map.Entry<Path, Path> entry : confFileInfo.entrySet()) {
                Path targetDirectory = entry.getValue().getParent();
                if (!Files.exists(targetDirectory)) {
                    Files.createDirectories(targetDirectory);
                }
                logger.info("从源：" + entry.getKey());
                logger.info("复制到：" + entry.getValue());
                Files.copy(entry.getKey(), entry.getValue());
            }
        }
        return confFileInfo;
    }

    /**
     * 核心解析器
     *
     * @param configItem 待解析的配置项
     * @return 是否解析正常
     */
    private boolean parser(ConfigItem configItem) throws IOException {
        String node = configItem.getGenerateFolder();
        List<JSONObject> items = configItem.getItems();
        for (JSONObject item : items) {

            String configFileName = item.getString("configFileName");
            if (!configFileName.startsWith("/") && !configFileName.startsWith("\\")) {
                configFileName = File.separator + configFileName;
            }
            File file = new File(Const.WORK_DIRECTORY + Const.TMP_DIRECTORY + configFileName);
            if (!file.exists()) {
                continue;
            }
            TreeMap<Integer, String> rowInfo = new TreeMap<>();
            for (String keyName : item.keySet()) {
                if (!"configFileName".equals(keyName)) {
                    String[] rows = keyName.split("-");
                    if (rows.length == 1) {
                        int row = Integer.valueOf(keyName.trim());
                        rowInfo.put(row, item.getString(keyName));
                    } else if (rows.length == 2) {
                        int startRow = Integer.valueOf(rows[0].trim());
                        int endRow = Integer.valueOf(rows[1].trim());
                        if (startRow <= 0 || endRow <= 0 || startRow > endRow) {
                            logger.warn("核心配置 configuration -> " + node + " 存在错误的键值：" + keyName);
                            return false;
                        }
                        Object obj = item.get(keyName);
                        if (obj instanceof String) {
                            if (startRow != endRow) {
                                logger.info("核心配置 configuration -> " + node + " 中存在配置项：" + keyName + "，多行元素将被改称同一个值：" + obj);
                            }
                            for (int index = startRow; index <= endRow; index++) {
                                rowInfo.put(index, item.getString(keyName));
                            }
                        } else if (obj instanceof JSONArray) {
                            JSONArray multiContent = (JSONArray) obj;
                            int statementRow = endRow - startRow + 1;
                            int realRow = multiContent.size();
                            if (statementRow <= realRow) {
                                if (statementRow < realRow) {
                                    logger.warn("核心配置 configuration -> " + node + " 中存在配置项：" + keyName + " 应该包含 " + statementRow + " 个子元素，实际配置了 " + realRow + " 个子元素，多余的子元素已经被忽略");
                                }
                                for (int index = startRow; index <= endRow; index++) {
                                    rowInfo.put(index, multiContent.getString(index - startRow));
                                }
                            } else {
                                logger.warn("核心配置 configuration -> " + node + " 中存在配置项：" + keyName + " 应该包含 " + statementRow + " 个子元素，实际配置了 " + realRow + " 个子元素，缺少的配置将会被忽略");
                                for (int index = startRow; index < startRow + realRow; index++) {
                                    rowInfo.put(index, multiContent.getString(index - startRow));
                                }
                            }
                        } else {
                            logger.error("核心配置 configuration -> " + node + " 存在错误的键值：" + keyName + "，该配置只能是String或JSONArray");
                            return false;
                        }
                    } else {
                        logger.error("核心配置 configuration -> " + node + " 存在错误的键值：" + keyName);
                        return false;
                    }
                }
            }
            List<String> txtRows = TxtUtils.readTxt(file);
            Integer startRow = rowInfo.firstKey();
            Integer endRow = rowInfo.lastKey();
            if (endRow <= txtRows.size()) {
                for (int index = startRow; index <= endRow; index++) {
                    if (rowInfo.keySet().contains(index)) {
                        txtRows.set(index - 1, rowInfo.get(index));
                    }
                }
            } else {
                logger.warn(node + " -> " + configFileName + " 文件需要修改的最大行数大于文本总行数，未指定的行将使用空行填充");
                if (startRow <= txtRows.size()) {
                    for (int index = startRow; index <= txtRows.size(); index++) {
                        if (rowInfo.keySet().contains(index)) {
                            txtRows.set(index - 1, rowInfo.get(index));
                        }
                    }
                }
                for (int index = txtRows.size(); index < endRow; index++) {
                    if (rowInfo.keySet().contains(index + 1)) {
                        txtRows.add(rowInfo.get(index + 1));
                    } else {
                        txtRows.add("");
                    }
                }
            }
            TxtUtils.writeTxt(txtRows, file);
        }
        return true;
    }

    /**
     * 定制生成War文件
     *
     * @param configItems 配置项
     * @param backupInfo  备份信息
     * @param warFileName 原War文件的简单名，如：Root.war
     * @throws Exception #
     */
    private void generate(List<ConfigItem> configItems, Map<Path, Path> backupInfo, String warFileName) throws Exception {
        for (ConfigItem configItem : configItems) {
            String generateFolder = configItem.getGenerateFolder();
            if (StringUtils.isBlank(generateFolder)) {
                logger.warn("核心配置 configuration -> generateFolder 必须配置，并且配置值不能为空格、空字符串等特殊字符，建议使用英文字母、数字");
                continue;
            }
            // 创建生成War文件存放的目录
            File generateDirectory = new File(Const.WORK_DIRECTORY + Const.GEN_DIRECTORY + File.separator + generateFolder);
            if (generateDirectory.exists()) {
                logger.warn("核心配置 configuration -> generateFolder 出现重复的值：" + generateFolder + "，已使用第一个节点的配置，其他重复配置将会被忽略");
                continue;
            }
            Files.createDirectories(Paths.get(generateDirectory.toURI()));

            // 动态解析和修改文件
            if (!parser(configItem)) {
                continue;
            }

            // 待打包文件存放的路径
            URI sourceUri = ClassLoader.getSystemResource(Const.TMP_DIRECTORY).toURI();
            // 新生成的War文件的URI
            URI generateUri = new File(generateDirectory, warFileName).toURI();
            // 生成War
            logger.info("开始生成War文件，使用配置节点：" + generateFolder);
            WarUtils.zipWar(Paths.get(sourceUri), Paths.get(generateUri));

            // 还原备份文件
            for (Map.Entry<Path, Path> entry : backupInfo.entrySet()) {
                Files.deleteIfExists(entry.getKey());
                Files.copy(entry.getValue(), entry.getKey());
            }
        }
    }

    /**
     * 核心方法入口
     *
     * @param mainConfig 核心配置对象
     */
    public void work(MainConfig mainConfig) throws Exception {

        // 解析配置策略，获取有效的配置项
        List<ConfigItem> configItems = getValidConfigItems(mainConfig);

        // 备份二次打包时需要修改的文件
        Map<Path, Path> backupInfo = backup(configItems);

        // 定制生成War文件
        generate(configItems, backupInfo, mainConfig.getWarFileName());

    }

}
