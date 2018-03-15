package tech.tablesaw.bdp;

import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.ShortColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvReader;
import tech.tablesaw.io.csv.CsvWriter;
import tech.tablesaw.util.DictionaryMap;

import java.io.IOException;
import java.util.Set;

import static tech.tablesaw.api.ColumnType.CATEGORY;
import static tech.tablesaw.api.ColumnType.FLOAT;
import static tech.tablesaw.api.ColumnType.INTEGER;
import static tech.tablesaw.api.ColumnType.LOCAL_DATE;
import static tech.tablesaw.api.ColumnType.SHORT_INT;

/**
 * 销售业绩表 统计计算
 * <p>
 * Created by yaoo on 3/14/18
 */
public class SalesPerformanceStats {


    private static Table t;

    public static Table loadCSV(String path) {

        ColumnType[] columnTypes = {
                LOCAL_DATE, // 0     单据日期
                CATEGORY,   // 1     地区名称
                CATEGORY,   // 2     业务员名称
                CATEGORY,   // 3     客户分类
                CATEGORY,   // 4     存货编码
                CATEGORY,   // 5     客户名称
                SHORT_INT,  // 6     业务员编码
                CATEGORY,   // 7     存货名称
                SHORT_INT,  // 8     订单号
                CATEGORY,   // 9     客户编码
                CATEGORY,   // 10    部门名称
                SHORT_INT,  // 11    部门编码
                CATEGORY,   // 12    存货分类
                FLOAT,      // 13    税费
                FLOAT,      // 14    不含税金额
                FLOAT,      // 15    订单金额
                FLOAT,      // 16    利润
                FLOAT,      // 17    单价
                INTEGER,    // 18    订单明细号
                SHORT_INT,  // 19    数量
        };


        try {

            //System.out.println(CsvReader.printColumnTypes(csvPath, true, ','));

            t = Table.read().csv(CsvReadOptions.builder(path).columnTypes(columnTypes));

        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(t.structure().print());
//        System.out.println(t.shape());

        return t;
    }

    public static Table salesVolumeByCategory() {

        Table tSalesVolume;

//        CategoryColumn productCat = t.categoryColumn("存货分类");
//        String[] pCatArr = productCat.dictionaryMap().categoryArray();
//        CategoryColumn salesman = t.categoryColumn("业务员名称");

        tSalesVolume = t.sum("数量").by("存货分类");

//        System.out.println(tSalesVolume.print());

//        tSalesVolume.save("/root/Downloads");

        try {
            CsvWriter.write(tSalesVolume, "/root/Downloads/sales-volumn.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tSalesVolume;
    }

    public static Table salesAmountByCategory() {

        return t.sum("订单金额").by("存货分类");
    }


    public static Table salesAmountPerDay() {

        return t.sum("订单金额").by("单据日期");
    }

    public static Table topSeller() {

        return t.sum("订单金额").by("业务员名称");
    }

    public static void main(String[] args) {
        String csvPath = "/root/Documents/repo/todofrontend/d3-3.5.17/hellores/bdp/sales-performance.csv";

        Table tResult;

        loadCSV(csvPath);

//        tResult = salesVolumeByCategory();
//        tResult = salesAmountByCategory();
//        tResult = salesAmountPerDay();
        tResult = topSeller();

        try {
            CsvWriter.write(tResult, "/root/Downloads/sales-amount-by-salesman.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
