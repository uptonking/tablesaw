# ROADMAP

 > the ease of Pandas and the performance of C
 
## Target

- a platform for data science in Java
- learning source code of Tablesaw
- pr to issues
    
## Future

- Load data remotely from HDFS, S3, and off the Web using HTTP
- Interactive graphics
- Integrated machine learning
- More specialized column types and operations: (lat/lon, time interval, money)

## todo

- [ ] 合并easyexcel和jexcel
- [ ] 开发转换器 list2table map2table
- [ ] table.print()格式美化
- [*] 计算一列的不同值 
tablesaw已经实现 `table.sum("column").by("column2")`  
- [ ] 对两列的值，进行分组聚合 
- [ ] 重新设计读取器和写入器，以便支持自定义扩展 
- [ ] TopN过滤器 
- [ ] 二次排序   
- [ ] 重构缺失数据处理   
- [ ] Table join
