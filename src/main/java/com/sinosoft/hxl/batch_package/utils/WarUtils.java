package com.sinosoft.hxl.batch_package.utils;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * 功能描述: War包工具类
 *
 * @author hxulin
 */
public final class WarUtils {

    private static final Logger logger = Logger.getLogger(WarUtils.class);

    private WarUtils() {

    }

    /**
     * 解压war文件
     *
     * @param warFile   War文件的Path
     * @param unZipPath 解压到的路径
     */
    public static void unZipWar(Path warFile, Path unZipPath) {
        logger.info("解压War文件：" + warFile);
        logger.info("解压到目标路径：" + unZipPath);
        logger.info("开始解压，请稍候...");
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(warFile.toFile()));
             ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(
                     ArchiveStreamFactory.JAR, bis)) {
            if (Files.exists(unZipPath)) {
                Files.delete(unZipPath);
            }
            Files.createDirectories(unZipPath);
            JarArchiveEntry entry;
            while ((entry = (JarArchiveEntry) ais.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    Path currentPath = Paths.get(unZipPath.toString(), entry.getName());
                    if (!Files.exists(currentPath.getParent())) {
                        Files.createDirectories(currentPath);
                    } else if (!Files.exists(currentPath)) {
                        Files.createDirectory(currentPath);
                    }
                } else {
                    Path currentPath = unZipPath.resolve(entry.getName());
                    if (!Files.exists(currentPath.getParent())) {
                        Files.createDirectories(currentPath.getParent());
                    }
                    try (OutputStream os = Files.newOutputStream(currentPath)) {
                        IOUtils.copy(ais, os);
                    }
                }
            }
            logger.info("成功解压到：" + unZipPath.toString());
        } catch (ArchiveException | IOException e) {
            logger.error("War文件解压失败");
            throw new RuntimeException("War文件解压失败。", e);
        }
    }

    /**
     * 创建War文件
     *
     * @param sourceDir 源文件所在目录
     * @param distFile  最终打包生成的文件
     */
    public static void zipWar(Path sourceDir, Path distFile) {
        sourceDir = sourceDir.toAbsolutePath();
        String rootDir = sourceDir.toString();
        logger.info("------------------------------------------------------------");
        logger.info("需要打包文件所在目录：" + rootDir);
        logger.info("需要创建的War文件：" + distFile);
        logger.info("开始创建，请稍候...");
        if (!rootDir.endsWith(File.separator)) {
            rootDir += File.separator;
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(distFile));
             ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.JAR, bos)) {
            Iterator<File> files = FileUtils.iterateFiles(sourceDir.toFile(), null, true);
            while (files.hasNext()) {
                File file = files.next();
                ZipArchiveEntry entry = new ZipArchiveEntry(file, StringUtils.remove(file.getAbsolutePath(), rootDir));
                aos.putArchiveEntry(entry);
                try (InputStream is = new FileInputStream(file)) {
                    IOUtils.copy(is, aos);
                }
                aos.closeArchiveEntry();
            }
            aos.flush();
            logger.info("成功创建War文件：" + distFile);
        } catch (IOException | ArchiveException e) {
            logger.error("War文件创建失败");
            throw new RuntimeException("War文件创建失败。", e);
        } finally {
            logger.info("------------------------------------------------------------");
        }
    }

}
