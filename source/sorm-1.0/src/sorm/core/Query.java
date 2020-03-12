package sorm.core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import sorm.bean.ColumnInfo;
import sorm.bean.TableInfo;
import sorm.utils.JDBCUtils;
import sorm.utils.ReflectUtils;

/**
 * 负责查询（对外提供服务的核心类）
 * @author jx
 *
 */
@SuppressWarnings("all")
public abstract class Query implements Cloneable {

	//一，生成的SQL，和参数，给该方法执行DML
	/**
	 * 直接执行一个DML语句
	 * @param sql sql语句
	 * @param params 参数
	 * @return 执行sql语句后影响记录的行数
	 */
	public int executeDML(String sql, Object[] params) {
		Connection conn = DBManager.getConn();
		int count = 0; 
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			
			//给sql设参
			JDBCUtils.handleParams(ps, params);
			System.out.println(ps);
			count  = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			DBManager.close(ps, conn);
		}
		
		return count;
	}

	
	//1,Emp.class,2-->delete from emp where id=2
	/**
	 * 删除clazz表示类对应的表中的记录(指定主键值id的记录)
	 * @param clazz 跟表对应的类的Class对象
	 * @param id 主键的值
	 */
	public void delete(Class clazz, Object id) {
		//通过Class对象找TableInfo
		TableInfo tableInfo = TableContext.poClassTableMap.get(clazz);
		//获得主键
		ColumnInfo onlyPriKey = tableInfo.getOnlyPriKey();
		
		String sql = "delete from "+tableInfo.getTname()+" where "+onlyPriKey.getName()+"=? ";
		
		executeDML(sql, new Object[]{id});
	}

	/**
	 * 删除对象在数据库中对应的记录(对象所在的类对应到表，对象的主键的值对应到记录)
	 * @param obj
	 */
	public void delete(Object obj) {
		Class c = obj.getClass();
		TableInfo tableInfo = TableContext.poClassTableMap.get(c);
		ColumnInfo onlyPriKey = tableInfo.getOnlyPriKey();  //主键
		
		//通过反射机制，调用属性对应的get方法或set方法
		Object priKeyValue = ReflectUtils.invokeGet(onlyPriKey.getName(), obj);

		delete(c, priKeyValue);
	}
	
	//2,obj-->表中。             insert into 表名  (id,uname,pwd) values (?,?,?)
	/**
	 * 将一个对象存储到数据库中
	 * 把对象中不为null的属性往数据库中存储！如果数字为null则放0.
	 * @param obj 要存储的对象
	 */
	public void insert(Object obj) {
		Class c = obj.getClass();
		TableInfo tableInfo = TableContext.poClassTableMap.get(c);
		
		int countNotNullField = 0;   //计算不为null的属性值
		List<Object> params = new ArrayList<Object>();   //存储sql的参数对象
		StringBuilder sql  = new StringBuilder("insert into "+tableInfo.getTname()+" (");
		
		Field[] fs = c.getDeclaredFields();
		for(Field f:fs){
			String fieldName = f.getName();
			Object fieldValue = ReflectUtils.invokeGet(fieldName, obj);
			
			if(fieldValue!=null){
				countNotNullField++;
				sql.append(fieldName+",");
				params.add(fieldValue);
			}
		}	
		sql.setCharAt(sql.length()-1, ')');
		
		//  该 SQL 后部分
		sql.append(" values (");
		for(int i=0;i<countNotNullField;i++){
			sql.append("?,");
		}
		sql.setCharAt(sql.length()-1, ')');
		
		executeDML(sql.toString(), params.toArray());
	}
	
	//3,obj{"uanme","pwd"}-->update 表名  set uname=?,pwd=? where id=?
	/**
	 * 更新对象对应的记录，并且只更新指定的字段的值
	 * @param obj 所要更新的对象
	 * @param fieldNames 更新的属性列表
	 * @return 执行sql语句后影响记录的行数
	 */
	public int update(Object obj, String[] fieldNames) {
		Class c = obj.getClass();
		TableInfo tableInfo = TableContext.poClassTableMap.get(c);

		List<Object> params = new ArrayList<Object>();   //存储sql的参数对象
		ColumnInfo  priKey = tableInfo.getOnlyPriKey();   //获得唯一的主键
			
		StringBuilder sql  = new StringBuilder("update "+tableInfo.getTname()+" set ");	
		for(String fname:fieldNames){
			Object fvalue = ReflectUtils.invokeGet(fname,obj);
			params.add(fvalue);
			sql.append(fname+"=?,");
		}
		sql.setCharAt(sql.length()-1, ' ');
		sql.append(" where ");
		sql.append(priKey.getName()+"=? ");	
		params.add(ReflectUtils.invokeGet(priKey.getName(), obj));    //主键的值
		return executeDML(sql.toString(), params.toArray()); 
		}
	
	
	
	
	
	
	//二，查询:简单查询，直接用之前的类，复杂例如合并的，先写个在vo下，写个类封装
	
	/**
	 * 采用模板方法模式将JDBC查询操作封装成模板，便于重用
	 * @param sql sql语句
	 * @param params sql的参数
	 * @param clazz 记录要封装到的java类
	 * @param back CallBack的实现类，实现回调
	 * @return 
	 */
	public Object executeQueryTemplate(String sql,Object[] params,Class clazz,CallBack back){
		Connection conn = DBManager.getConn();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			//给sql设参
			JDBCUtils.handleParams(ps, params);
			System.out.println(ps);
			rs = ps.executeQuery();
			
			//这个钩子等匿名类下面实现。
			return  back.doExecute(conn, ps, rs);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			DBManager.close(rs,ps, conn);
		}
	}
		
	
	//1,多行 select username ,pwd,age from user where id>? and age>18
	
	/**
	 * 查询返回多行记录，并将每行记录封装到clazz指定的类的对象中
	 * @param sql 查询语句
	 * @param clazz 封装数据的javabean类的Class对象
	 * @param params sql的参数
	 * @return 查询到的结果
	 */
	public List queryRows(String sql, Class clazz, Object[] params) {

		return (List)executeQueryTemplate(sql, params, clazz, new CallBack() {
			
			@Override
			public Object doExecute(Connection conn, PreparedStatement ps, ResultSet rs) {
				List list = null;
				try {
					ResultSetMetaData metaData = rs.getMetaData();
					//多行
					while(rs.next()){
						if(list==null){
							list = new ArrayList();
						}
						
						Object rowObj = clazz.newInstance();   //调用javabean的无参构造器
						
						//多列       select username ,pwd,age from user where id>? and age>18
						for(int i=0;i<metaData.getColumnCount();i++){
							String columnName = metaData.getColumnLabel(i+1);  //username
							Object columnValue = rs.getObject(i+1);
							
							//调用rowObj对象的setUsername(String uname)方法，将columnValue的值设置进去
							ReflectUtils.invokeSet(rowObj, columnName, columnValue);
						}
						
						list.add(rowObj);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
				return list;
			}
		});
	}

	
	
	
	//2,一行，
	/**
	 * 查询返回一行记录，并将该记录封装到clazz指定的类的对象中
	 * @param sql 查询语句
	 * @param clazz 封装数据的javabean类的Class对象
	 * @param params sql的参数
	 * @return 查询到的结果
	 */
	public Object queryUniqueRow(String sql, Class clazz, Object[] params) {
		List list = queryRows(sql, clazz, params);
		return (list!=null && list.size()>0)?list.get(0):null; //不空且有都有东西,一定要注意逻辑，不然空指针
	}
	//2) 按照key来查
	public Object queryByKey(Class clazz , Object key){
		//通过Class对象找TableInfo
		TableInfo tableInfo = TableContext.poClassTableMap.get(clazz);
		//获得主键
		ColumnInfo onlyPriKey = tableInfo.getOnlyPriKey();
				
		String sql = "select * from "+tableInfo.getTname()+" where "+onlyPriKey.getName()+"=? ";
				
		return queryUniqueRow(sql, clazz, new Object[]{key});
	}
	
	
	//3,一个对象。  就是不要双重循环了，不要元信息了，直接返回Obj 或Num
	/**
	 * 查询返回一个值(一行一列)，并将该值返回
	 * @param sql 查询语句
	 * @param params sql的参数
	 * @return 查询到的结果
	 */
	public Object queryValue(String sql, Object[] params) {
		return executeQueryTemplate(sql, params, null, new CallBack() {
			
			@Override
			public Object doExecute(Connection conn, PreparedStatement ps, ResultSet rs) {
				Object value = null;    //存储查询结果的对象
				try {			
					while(rs.next()){
						value = rs.getObject(1);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return value;} }
			);
	}
		
	
	/**
	 * 查询返回一个数字(一行一列)，并将该值返回
	 * @param sql 查询语句
	 * @param params sql的参数
	 * @return 查询到的数字
	 */
	public Number queryNumber(String sql, Object[] params) {
		return (Number)queryValue(sql, params);
	}
	
	
	
	
	//三，不是基础操作，不能统一。
	/**
	 * 分页查询
	 * @param pageNum 第几页数据
	 * @param size 每页显示多少记录
	 * @return
	 */
	public abstract Object queryPagenate(int pageNum,int size);
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
