package sever;


import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
/**
 * 2，!!!  分发器： 完成请求，响应，
 *响应附加内容和状态处理  404 505 及首页
 *
 *把请求的 通过工具类，响应成 服务类。
 */
public class Dispatcher implements Runnable {
	private Socket client;
	private Request request;
	private Response response ;
	public Dispatcher(Socket client) {
		this.client = client;
		try {
			//获取请求协议
			//获取响应协议
			request =new Request(client);
			response =new Response(client);
		} catch (IOException e) {
			e.printStackTrace();
			this.release();
		}
	}
	@Override
	public void run() {	
		
		try {
			if(null== request.getUrl() || request.getUrl().equals("")) {				
				response.print( new String( Files.readAllBytes(Paths.get("index.html") ) ));
				response.pushToBrowser(200);
				
				//InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream("index.html");
				//response.print((new String(is.readAllBytes())));  jdk8 没有
				//is.close();
				
				return ;
			}
			Servlet servlet= WebApp.getServletFromUrl(request.getUrl());
			if(null!=servlet) {
				servlet.service(request, response);
				//关注了状态码
				response.pushToBrowser(200);
			}else {

				response.print( new String( Files.readAllBytes(Paths.get("error.html") ) ));
				response.pushToBrowser(404);
			}		
		}catch(Exception e) {
			try {
				response.println("你好我不好，我会马上好");
				response.pushToBrowser(500);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}finally{
			release();
		}
	}
	//释放资源
	private void release() {
		try {
			client.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
