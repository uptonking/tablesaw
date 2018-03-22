package player.data.csv;

import org.apache.commons.beanutils.BeanUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV文件操作
 */
public class CSVUtil {

    /**
     * list生成为CSV文件
     *
     * @param dataList   源数据List
     * @param headerMap  csv文件的列表头map
     * @param outputPath 文件路径，以/结尾
     * @param fileName   文件名称，代码会自动添加.csv后缀
     * @return 创建的文件对象
     */
    @SuppressWarnings("rawtypes,unused")
    public static File createCSVFile(List dataList,
                                     LinkedHashMap headerMap,
                                     String outputPath,
                                     String fileName,
                                     boolean hasHeader) {

        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File csvFile = new File(outputPath + fileName + ".csv");
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
                //System.out.println("文件新建");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //System.out.println("文件已存在");
            //不用手动删除已存在的文件
            //csvFile.delete();
        }

        BufferedWriter csvFileWriter = null;

        try {

            // UTF-8使正确读取分隔符","
            csvFileWriter = new BufferedWriter(
                    //FileOutputStream(file,false)默认会覆盖原文件而不是追加
                    new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"),
                    1024);

            if (hasHeader) {
                // 写入文件头部
                for (Iterator it = headerMap.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = (Map.Entry) it.next();
                    csvFileWriter.write("\"" + entry.getValue() != null ? (String) entry.getValue() : "" + "\"");
                    if (it.hasNext()) {
                        csvFileWriter.write(",");
                    }
                }
                csvFileWriter.newLine();
            }

            // 写入文件内容
            for (Iterator it = dataList.iterator(); it.hasNext(); ) {

                Object row = it.next();

                for (Iterator it2 = headerMap.entrySet().iterator(); it2.hasNext(); ) {
                    Map.Entry entry = (Map.Entry) it2.next();
                    csvFileWriter.write(BeanUtils.getProperty(row, (String) entry.getKey()));
                    if (it2.hasNext()) {
                        csvFileWriter.write(",");
                    }
                }
                if (it.hasNext()) {
                    csvFileWriter.newLine();
                }
            }
            csvFileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (csvFileWriter != null) {
                    csvFileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return csvFile;
    }


    /**
     * 删除该目录filePath下的所有文件
     *
     * @param filePath 文件目录路径
     */
    public static void deleteFiles(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                System.out.println("目录下没有文件");

            } else {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        files[i].delete();
                    }
                }
            }

        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath 文件目录路径
     * @param fileName 文件名称
     */
    public static void deleteFile(String filePath, String fileName) {
        File file = new File(filePath);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    if (files[i].getName().equals(fileName)) {
                        files[i].delete();
                        return;
                    }
                }
            }
        }
    }


    /**
     * 测试数据
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) {

        List dataList = new ArrayList<Map>();

        Map row = new LinkedHashMap<String, String>();
        row.put("1", "110");
        row.put("2", "12");
        row.put("3", "13");
        row.put("4", "14");
        dataList.add(row);

        row = new LinkedHashMap<String, String>();
        row.put("1", "210");
        row.put("2", "22");
        row.put("3", "23");
        row.put("4", "24");
        dataList.add(row);

        LinkedHashMap map = new LinkedHashMap();
        map.put("1", "第一列");
        map.put("2", "第二列");
        map.put("3", "第三列");
        map.put("4", "第四列");

        String path = "/root/Downloads/";
        String fileName = "csv文件导出";

        createCSVFile(dataList, map, path, fileName, true);

    }

}
