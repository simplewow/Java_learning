
1. 在src下建立db.properties
2. 每张表只能有一个主键。不能处理多个主键的情况。
3. po尽量使用包装类，不要使用基本数据类型。

4，
1）使用前，要先通过TableContext.updateJavaPOFile()，，加载类。
（每更新一次数据库，可以更新下类）
2）然后，调用TableContext.loadPOTables();  把类 和 表信息对应上。

