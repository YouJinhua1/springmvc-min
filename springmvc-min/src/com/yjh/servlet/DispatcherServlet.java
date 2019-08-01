package com.yjh.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpRetryException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.yjh.annotation.Controller;
import com.yjh.annotation.RequestMapping;
import com.yjh.view.ViewResolver;

public class DispatcherServlet extends HttpServlet{

	//ģ��IOC����������Controllerʵ������
    private Map<String,Object> iocContainer = new HashMap<String,Object>();
    //����handlerӳ��
    private Map<String,Method> handlerMapping = new HashMap<String,Method>();
    //�Զ���ͼ������
    private ViewResolver viewResolver;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //ɨ��Controller������ʵ�����󣬲�����iocContainer
        scanController(config);
        //��ʼ��handlerӳ��
        initHandlerMapping();
        //������ͼ������
        loadViewResolver(config);
    }

    /**
     * ɨ��Controller
     * @param config
     */
    public void scanController(ServletConfig config){
        SAXReader reader = new SAXReader();
        try {
            //����springmvc.xml
            String path = config.getServletContext().getRealPath("")+"\\WEB-INF\\classes\\"+config.getInitParameter("contextConfigLocation");   
            Document document = reader.read(path);
            Element root = document.getRootElement();
            Iterator iter = root.elementIterator();
            while(iter.hasNext()){
                Element ele = (Element) iter.next();
                if(ele.getName().equals("component-scan")){
                    String packageName = ele.attributeValue("base-package");
                    //��ȡbase-package���µ���������
                    List<String> list = getClassNames(packageName);
                    for(String str:list){
                        Class clazz = Class.forName(str);
                        //�ж��Ƿ���MyControllerע��
                        if(clazz.isAnnotationPresent(Controller.class)){
                            //��ȡController��MyRequestMappingע���value
                            RequestMapping annotation = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
                            String value = annotation.value().substring(1);
                            //Controllerʵ���������iocContainer
                            iocContainer.put(value, clazz.newInstance());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ȡ���µ���������
     * @param packageName
     * @return
     */
    public List<String> getClassNames(String packageName){
        List<String> classNameList = new ArrayList<String>();
        String packagePath = packageName.replace(".", "/");  
        ClassLoader loader = Thread.currentThread().getContextClassLoader();  
        URL url = loader.getResource(packagePath);  
        if(url != null){
            File file = new File(url.getPath());  
            File[] childFiles = file.listFiles();
            for(File childFile : childFiles){
                String className = packageName+"."+childFile.getName().replace(".class", "");
                classNameList.add(className);
            }
        }
        return classNameList;
    }

    /**
     * ��ʼ��handlerӳ��
     */
    public void initHandlerMapping(){
        for(String str:iocContainer.keySet()){
            Class clazz = iocContainer.get(str).getClass();
            Method[] methods = clazz.getMethods();
               for (Method method : methods) {
                 //�жϷ�ʽ�Ƿ����MyRequestMappingע��
                 if(method.isAnnotationPresent(RequestMapping.class)){
                     //��ȡMethod��MyRequestMappingע���value
                     RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                     String value = annotation.value().substring(1);
                     //method����methodMapping
                     handlerMapping.put(value, method);
                 }
             }
        }
    }

    /**
     * �����Զ�����ͼ������
     * @param config
     */
    public void loadViewResolver(ServletConfig config){
        SAXReader reader = new SAXReader();
        try {
            //����springmvc.xml
            String path = config.getServletContext().getRealPath("")+"\\WEB-INF\\classes\\"+config.getInitParameter("contextConfigLocation");   
            Document document = reader.read(path);
            Element root = document.getRootElement();
            List<Element> els=root.elements();
            for(Element el:els){
                if(el.getName().equals("bean")){
                    String className = el.attributeValue("class");
                    Class clazz = Class.forName(className);
                    Object obj = clazz.newInstance();
                    //��ȡsetter����
                    Method prefixMethod = clazz.getMethod("setPrefix", String.class);
                    Method suffixMethod = clazz.getMethod("setSuffix", String.class);
                    Iterator beanIter = el.elementIterator();
                    //��ȡpropertyֵ
                    Map<String,String> propertyMap = new HashMap<String,String>();
                    while(beanIter.hasNext()){
                        Element beanEle = (Element) beanIter.next();
                        String name = beanEle.attributeValue("name");
                        String value = beanEle.attributeValue("value");
                        propertyMap.put(name, value);
                    }
                    for(String str:propertyMap.keySet()){
                        //������Ƶ���setter��������ɸ�ֵ��
                        if(str.equals("prefix")){
                            prefixMethod.invoke(obj, propertyMap.get(str));
                        }
                        if(str.equals("suffix")){
                            suffixMethod.invoke(obj, propertyMap.get(str));
                        }
                    }
                    viewResolver = (ViewResolver) obj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	System.out.println("servletPath:"+req.getServletPath());
    	
    	System.out.println("requestURL:"+req.getRequestURL());
    	
    	System.out.println("requestURI:"+req.getRequestURI());
    	
    	System.out.println("contextPath:"+req.getContextPath());
    	
    	String projectName=req.getContextPath().substring(1);
    	//��ȡ����
    	String requestURI=req.getRequestURI();
    	//�ָ������·��
    	String[] str=requestURI.split("/");
    	
    	//�õ�controller��requestMapping���Q
        String handlerUri = str[str.length-2];
        //�õ�����controller��requestMapping�ķ������Q
        String methodUri = str[str.length-1];
        if(methodUri.equals(projectName)){
        	return;
        }
        //��ȡControllerʵ��
        Object obj = iocContainer.get(handlerUri);
        //��ȡҵ�񷽷�
        Method method = handlerMapping.get(methodUri);
        try {
            //������Ƶ���ҵ�񷽷�
            String value = (String) method.invoke(obj);
            //��ͼ���������߼���ͼת��Ϊ������ͼ
            String result = viewResolver.jspMapping(value);
            //ҳ����ת
            req.getRequestDispatcher(req.getContextPath()+result).forward(req, resp);
           // req.r
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

}
