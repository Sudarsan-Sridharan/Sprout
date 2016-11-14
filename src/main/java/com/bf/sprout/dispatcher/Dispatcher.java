/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bf.sprout.dispatcher;

import com.bf.sprout.annotations.HttpComponent;
import com.bf.sprout.annotations.HttpSocket;
import com.bf.sprout.annotations.RestMethod;
import com.bf.sprout.annotations.Restrict;
import com.bf.sprout.annotations.Restrict.RestrictLevel;
import com.bf.sprout.annotations.WebMethod;
import com.bf.sprout.permissions.SiteUser;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.atteo.classindex.ClassIndex;

/**
 *
 * @author Brandon Alexander Ragland
 */
public class Dispatcher extends HttpServlet{

	private Map args = new HashMap();

	public Dispatcher(){
		args.put(JsonWriter.WRITE_LONGS_AS_STRINGS, true);
	}

	protected void processRequest(HttpServletRequest request, HttpServletResponse response, boolean post)
			throws ServletException, IOException{

		try{
			String path = request.getPathInfo();
			if(path == null)return;

			String[] paths = path.split("\\/");

			if(paths != null && paths.length > 2){
				Class<?> klass = getSocket(paths[1]);

				if(klass != null){
					Method method = getMethod(klass, paths[2], post);

					if(method != null){
						if(method.isAnnotationPresent(Restrict.class)){
							if(isRestricted(request, method)){
								response.sendError(403);
								return;
							}
						}

						Object[] parameters = makeParams(request, response, method);

						Object socket = klass.newInstance();

						Object result = method.invoke(socket, parameters);

						if(method.isAnnotationPresent(RestMethod.class)){
							response.setContentType("application/json;charset=UTF-8");

							try(PrintWriter out = response.getWriter()){
								out.print(JsonWriter.objectToJson(result, args));
								out.flush();
							}catch(Exception e){
								System.err.println("Could not marshal response into JSON for RestMethod!");
								System.err.println(e);
								throw e;
							}
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		processRequest(request, response, false);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		processRequest(request, response, true);
	}

	@Override
	public String getServletInfo(){
		return "Short description";
	}// </editor-fold>

	private Class<?> getSocket(String path){
		for(Class<?> klass : ClassIndex.getAnnotated(HttpSocket.class)){
			HttpSocket annotation = klass.getAnnotation(HttpSocket.class);

			if(annotation.path() != null && !annotation.path().isEmpty() && annotation.path().equalsIgnoreCase(path)){
				return klass;
			}else if(klass.getSimpleName().equalsIgnoreCase(path)){
				return klass;
			}
		}
		return null;
	}

	private Object[] makeParams(HttpServletRequest request, HttpServletResponse response, Method method){
		List<Object> params = new ArrayList<>();
		
		if(method == null){
			return null;
		}

		for(Parameter p : method.getParameters()){

			if(method.isAnnotationPresent(WebMethod.class)){
				//Add request if necessary
				if(p.getType().isAssignableFrom(HttpServletRequest.class)){
					params.add(request);
					continue;
				}

				//Add response if necessary
				if(p.getType().isAssignableFrom(HttpServletResponse.class)){
					params.add(response);
					continue;
				}
			}

			for(String field : request.getParameterMap().keySet()){
				String value = request.getParameter(field);

				if(p.getType().isAssignableFrom(Long.class)){
					params.add(Long.valueOf(value));

				}else if(p.getType().isAssignableFrom(Integer.class)){
					params.add(Integer.valueOf(value));

				}else if(p.getType().isAssignableFrom(Short.class)){
					params.add(Short.valueOf(value));

				}else if(p.getType().isAssignableFrom(Double.class)){
					params.add(Double.valueOf(value));

				}else if(p.getType().isAssignableFrom(Float.class)){
					params.add(Float.valueOf(value));

				}else if(p.getType().isAssignableFrom(String.class)){
					params.add(value);

				}else if(p.getType().isAssignableFrom(Boolean.class)){
					params.add(Boolean.valueOf(value));

				}else{
					try{
						Object obj = JsonReader.jsonToJava(value);
						//Make sure it's a registered component!
						if(obj.getClass().isAnnotationPresent(HttpComponent.class)){
							params.add(obj);
						}
					}catch(Exception e){
						System.err.println("Could not determine type of parameter from HTTP request!");
						System.err.println(e);
						throw e;
					}
				}
			}
		}

		return params.toArray();
	}

	private Method getMethod(Class<?> klass, String path, boolean post){
		if(klass != Object.class){
			for(final Method method : klass.getMethods()){
				if(method != null && method.isAnnotationPresent(RestMethod.class)){
					RestMethod annotation = method.getAnnotation(RestMethod.class);

					if((annotation.path() != null && annotation.path().equalsIgnoreCase(path)) || method.getName().equalsIgnoreCase(path)){
						if(annotation.requestType() == RequestType.BOTH){
							return method;
						}else if(annotation.requestType() == RequestType.POST && post){
							return method;
						}else if(annotation.requestType() == RequestType.GET && !post){
							return method;
						}else{
							return null;
						}
					}
				}else if(method != null && method.isAnnotationPresent(WebMethod.class)){
					WebMethod annotation = method.getAnnotation(WebMethod.class);

					if((annotation.path() != null && annotation.path().equalsIgnoreCase(path)) || method.getName().equalsIgnoreCase(path)){
						if(annotation.requestType() == RequestType.BOTH){
							return method;
						}else if(annotation.requestType() == RequestType.POST && post){
							return method;
						}else if(annotation.requestType() == RequestType.GET && !post){
							return method;
						}else{
							return null;
						}
					}
				}
			}
		}

		//return null as default
		return null;
	}
	
	//return true to BLOCK the access
	private boolean isRestricted(HttpServletRequest request, Method method){
		HttpSession session = request.getSession(false);
		
		if(session != null && method.isAnnotationPresent(Restrict.class)){
			SiteUser user = (SiteUser) session.getAttribute("sprout.siteuser");
			
			if(user == null){
				return true;
			}
			
			Restrict restriction = method.getAnnotation(Restrict.class);
			String permission = restriction.value();
			
			if(permission == null || permission.isEmpty()){
				return false;
			}
			
			if(restriction.level() == RestrictLevel.PERMISSION){
				if(user.getPermissions().hasPermission(permission) || user.getRole().getPermissions().hasPermission(permission)){
					return false;
				}
			}else if(restriction.level() == RestrictLevel.ROLE){
				if(user.getRole().getName().equalsIgnoreCase(permission)){
					return false;
				}
			}else if(restriction.level() == RestrictLevel.USER){
				if(user.getName().equalsIgnoreCase(permission)){
					return false;
				}
			}
		}
		//return true to block access
		return true;
	}
}
