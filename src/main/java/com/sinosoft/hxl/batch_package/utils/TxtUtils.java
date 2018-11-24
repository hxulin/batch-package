package com.sinosoft.hxl.batch_package.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述: 文本文件处理工具类
 *
 * @author hxulin
 */
public final class TxtUtils {

    private static final String CHARACTER_ENCODING = "utf-8";

    private TxtUtils() {

    }

    /**
     * 将文本文件按行读取，然后将每一行的值封装到ArrayList集合中
     *
     * @param txtFile 需要读取的文本文件
     * @return 封装在集合中的数据
     * @throws IOException 文件读取失败，抛出IO异常
     */
    public static List<String> readTxt(File txtFile) throws IOException {
        List<String> linesData = new ArrayList<>();
        if (txtFile.exists()) {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(txtFile), CHARACTER_ENCODING);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                linesData.add(line);
            }
            br.close();
            // 判断最后一行是否为空行，最后一个空行需要单独处理
            RandomAccessFile raf = new RandomAccessFile(txtFile, "r");
            raf.seek(raf.length() - 1);
            line = raf.readLine();
            raf.close();
            if (line.length() == 0) {
                linesData.add("");
            }
        }
        return linesData;
    }

    /**
     * 将List集合中的数据分行写到文本文件中
     *
     * @param list    需要写入文件的集合
     * @param txtFile 指定需要写入的文件
     * @throws IOException 若写文件失败，则抛IO异常
     */
    public static void writeTxt(List<String> list, File txtFile) throws IOException {
        if (list == null) {
            return;
        }
        Path newFilePath = Paths.get(txtFile.toURI());
        Files.deleteIfExists(newFilePath);
        Files.createFile(newFilePath);
        StringBuilder txtBuilder = new StringBuilder(80);
        for (String str : list) {
            txtBuilder.append(str).append("\r\n");
        }
        txtBuilder.delete(txtBuilder.length() - 2, txtBuilder.length());
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), CHARACTER_ENCODING));
        out.write(txtBuilder.toString());
        out.flush();
        out.close();
    }
}
