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
 * 负责针对Mysql数据库的查询
 * @author jx
 * 
 *
 */
public class MySqlQuery extends Query {
	
	//其他数据库基础操作，不分数据库
	@Override
	public Object queryPagenate(int pageNum, int size) {
		// TODO Auto-generated method stub
		return null;
	}

//	public static void testQueryRows(){
//		List<T_usr> list = new MySqlQuery().queryRows("select id,usrname,pwd from T_usr where id>?",
//				T_usr.class, new Object[]{9});
//		System.out.println(list);
//		for(T_usr e:list){
//			System.out.println(e.getUsrname());
//		}
//	}
//		
//
//	
//	public static void main(String[] args) {
//		testQueryRows();
//	}
}
