本笔记为java笔记的下部分。



# 10.IO

## 10.1 IO介绍

```
一切以程序为中心，进程序为输入一块和服务器，一块和客户（文件上传下载，更重要）

流：程序与源交流
	一切以程序为中心，进程序为输入
	分类：
		流向 ：输入：两个，输出：两个类
		直接 ：节点流和处理流（包装下节点，提高效率）
		对象 ：字节流和字符（底层还是弄成字节，找字符集）	

源：文件。内存、网络连接，数据库，设备

#输入直接连输出就是拷贝
java.io  核心五类三接口。
	#java--操作系统--文件：通过接口实现通知，自己无权
```

![](Java2.assets/10-1.jpg)



## 10.2 File

### 1）API

```
File 代表文件或者文件夹，不一定存在，就是路径名抽象表示，如在8 所展示的

#1，读API：权限>default才显示 
1）介绍和继承体系：文件和目录路径名的抽象表示（io.File --lang.Object）
2）常量：
pathSeparator 路径常量（PATH路径分割的 Win; Lin:）
separator  名称分割 ：linux /  windos \ (反斜杠别忘转义)
3）构造器：
有就   直接new
没有	  a.工具类（Math）不需要
	   b.静态方法返回对象(RunTime)
4）方法： 方法名，参数，描述，返回

```

<img src="Java2.assets/10-1-1.png" style="zoom: 80%;" />

<img src="Java2.assets/10-1-2.png" style="zoom: 67%;" />

```
#2，具体：
1）路径： / 或 \\ \  或 常量拼接 （用第一个就行）

2）File构造对象：四种（还有个uri）

3）名称或路径：
有盘符绝对路径（getAbsolutePath()）。
没有盘符相对路径（直接上来就是，不要写工程名，默认在usr.dir 也就是工程下）

getPath（给啥返回啥）,getA（绝对） 
getP (父，写啥返回前一层，如果没了返回null), getP File  返回个父对象。

4）文件状态：
	e---存在前提下，然后文件，文件夹
	src = new File("xxx");
		if(null == src || !src.exists()) {
			System.out.println("没有");
		}else {
			if(src.isFile()) {
				System.out.println("是文件");
			}else {
				System.out.println("是文件夹");
			}
		}
#不能命名文件操作系统关键字（例如con），，可以不管后不后缀创建
5）其他信息：
	long--返回文件长度，文件夹返回长度或者东西不存在是0，（要文件夹，递归sum）
	创文件-- createNF:  boolea(丢异常，可能失败，不存在才创建)
    	 --	delete
		
    文件夹--  没s  boolea     s:上面没有也行。
	
	下一级---（数组）  名称String[] list      对象 File[]  listFiles()  ,
	所有盘符对象  	 File[]  listRoots

   	
   	#子孙：递归（好处简单）头，体。笔记上的8
          也可以统计大小。oop思想。弄成个类，然后方法也在构造里面。得到结果get属性就行。


```

### 2）文件编码

```
#1，编码与解码
字符---->>字节：编码(encode)
字节---->>字符：解码(decode)  

编码数组： byte[] b = c.getBytes() (默认工程的)													  			   或" ”或者特定的
解码字符串   String  c = new (b);
				 	   new (b,offset,length,charSetNmae);//少了个数可能乱码

#2,字符集 
GBK（中文2，英文1）, UTF-8（1个中文3个，英文啥的1个）
#右边低地址，[X,X,X] ，大端表示右边大值（低地址是一开始，直接大，就是大端）
```

![](Java2.assets/10-2-4.jpg)

```
#3，乱码
#txt写的时候默认GBK

1) 字节数不够  2）字符集不同
```



面向接口编程（父类）：多态

文字：字符

一切，表格和音频：字节



C：提醒释放系统资源（不是内存）（关了，也能刷下）

F：刷新下



iS–:C

int read()

int read(byte[] b) :返回读到了几个，，配合解码,用第二种，将b解码

void close()



OS–C和F  

void write(int)

void write(b,0，len)



void flush()

close



r: C  被R（可读）

int read()

close

w：C,F,Appendable(写)

f,c

void write(String s)

#字符串是字符数组





步骤：

​	源，流，操作，释放



流：文件字节输入流 FileInputStream

操作：一个一个

实例：源代码 Study2 test2

操作：分段  3

缓冲容器（数组b）

#1024字节=1K



流：文件字节输出流 FileOutputStream

操作： 

​	#没有就创建           

​			os构造方法，无参数（File或者地址str），有参数（，append 默认false）

​			字符串解码得数组b

​			os.write(b,0，len)—

​			flush

实例：源代码 Study2 test4

操作：分段  3

缓冲容器（数组b）

#1024字节=1K





字节流

字符流

CommonsIO

