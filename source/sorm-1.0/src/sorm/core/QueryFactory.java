package sorm.core;
/**
 * 创建Query对象的工厂类  :
 * 		不允许创建工厂对象了，直接当做类工具
 * 		初始化，反射特定的Qeury对象。
 * 		当你要反复创建该对象时，采用原型模式。快速。
 * @author jx
 *
 */
public class QueryFactory {
	private static Query prototypeObj;  //原型对象
	
	static {
	
		try {
			//加载指定的query类
			Class c = Class.forName(DBManager.getConf().getQueryClass()); 
			
			prototypeObj = (Query) c.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}  
		
//		//加载po包下面所有的类，便于重用，提高效率！
//		TableContext.loadPOTables();
				
	}
	private QueryFactory(){  //私有构造器
	}
	public static Query createQuery(){
		try {
			return (Query) prototypeObj.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
