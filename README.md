# Tablesaw Overview

    > 为Java数据处理提供极致易用性的平台    

[README in English](https://github.com/uptonking/tablesaw/tree/master/README-en.md), 
[Quickstart](https://jtablesaw.wordpress.com/an-introduction/), 
[User Guide](https://jtablesaw.github.io/tablesaw/userguide/toc)  

## Features

- 丰富的数据处理方法
    - 支持的数据源包括csv，本地文件，rdbms，http文件
    - 文件合并
    - 添加或删除列
    - 过滤、排序、分组
    - map/reduce
    - 列式存储格式

- 常用统计方法
    - 计算常用的描述性统计量
        - 求和、求均、几何平均数、求积(product)
        - 最值、中位数、方差、百分位数
        - 偏度、峰度
    
- 机器学习方法
    - 分类：kNN、决策树、随机森林
    - 回归：最小二乘
    - 聚类：kMeans、gMeans
    - 关联分析：频繁项集
    - 特征工程：PCA

- 数据可视化
    - 饼、线、柱
    - 散点图
    - 直方图
    - 盒图
    - 帕累托图

## Tablesaw设计

- 为计算提供极致的易用性而设计，因此抛弃大多数情况下都用不到的分布式架构

- dataframe  
    dataframe是基于内存的类似表格的数据结构，每列数据类型相同，每行可以包含不同的类型

- 专用的列式存储格式.saw  
    tablesaw开发了一种基于压缩和劣势存储的自定义数据格式.saw，比csv、文本文件更省空间，更易存储、传输

- Table数据类型  
    - 使用最广泛的数据类型
    - Table的多数方法会返回另一个Table
    - 获取table元数据
        - table.name()
        - table.columnNames() 
        - table.structure()
        - table.shape() 
        - rowCount(), columnCount()
    - 列的索引从0开始
    - 对于需要反复使用的大规模数据Table，建议存储为.saw格式
    
- Column
    - 列支持的数据类型
        - Category (Strings from a finite set)
        - Integer (4 byte int)
        - LongInt (8 byte int)
        - ShortInt (2 byte int)
        - Float (4 byte floating point)
        - Double (8 byte floating point)
        - Boolean
        - Local Date
        - Local DateTime
        - Local Time
    - 所有的列都支持通用的方法
        - size()                           // returns the number of elements
        - isEmpty()                        // returns true if column has no data; false otherwise
        - first() and last()               // returns the first and last elements, respectively
        - first(n) and last(n)             // returns the first and last n elements
        - max() and min()                  // returns the largest and smallest elements
        - top(n) and bottom(n)             // returns the n largest and smallest elements
        - name()                           // returns the name of the column
        - type()                           // returns the ColumnType, e.g. LOCAL_DATE
        - print()                          // returns a String representation of the column
        - copy()
        - emptyCopy()
        - unique()
        - countUnique()
        - asSet()
        - summary()
        - sortAscending()
        - sortDescending()
        - append(Column)                         // Appends the data in other column to this one
    
- 导入
    - 导入数据时可以自动猜测数据类型CsvReader.printColumnTypes("data/a.csv", true, ','));
    - 可以将列数据类型设置为SKIP来跳过导入
    - 从http、rdbms加载远程数据的方法Table.read().csv(InputStream stream, String tableName)
    - 默认缺失值包括 `NaN`, `*`, `NA`, `null` and, of course, the empty string "".

- 过滤器
    - 主要使用方式：table.selectWhere(aFilter); table.rejectWhere(aFilter);
    - ColumnReference指向Table中的一列
    - 内置丰富的过滤器
    - 支持自定义过滤器public abstract RoaringBitmap apply(Table relation);

- 排序
    - 主要使用方式 table.sortOn("-recipe","col1");
    - 支持自定义排序

- Map函数
    - 应用到一列或多列，产生一个新列
    - Unary应用到1列 columnA.substring(startingPosition);
    - Binary应用到2列 columnB.add(columnC);
    - N-ary应用到N列
    - 生成的新列默认不添加到原表
    
- Reduce函数
    - 单分组  table.average("Injuries").by("Scale");
    - 多分组 table.sum("Fatalities").by("State", "Scale");


## 性能测试

  - 能够加载5亿行、4列的csv文件(35G)到10G的内存中进行计算
  - 如果使用tablesaw自定义的.saw文件格式，加载只需22秒

## Introducing Tablesaw
```
What I wanted for Tablesaw was the ease of Pandas and the performance of C. 
The biggest obstacle was memory. Primitives are far lighter than their equivalent objects, 
but they’re hard to use because many libraries auto-box them. 

Tablesaw avoids using non-primitives for data, 
and when that’s not possible (with Strings, or dates, for example), 
it uses type-specific encoding schemes to minimize the footprint. 
Even primitives use type-specific compression: 
boolean columns, for example, are compressed bitmaps that use 1/8th the storage of primitive booleans, 
or about 1/32 the storage of Boolean objects. 
We can do this, because the data is stored in columns, just as it is in advanced OLAP data-stores like Redshift.

```
