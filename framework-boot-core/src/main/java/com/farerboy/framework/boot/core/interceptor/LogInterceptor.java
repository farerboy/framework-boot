package com.farerboy.framework.boot.core.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.farerboy.framework.boot.core.log.Method;
import com.farerboy.framework.boot.core.log.pojo.ReqLog;
import com.farerboy.framework.boot.core.util.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * TODO description
 * @author farerboy
 * @date 2020/12/29 7:41 下午
 */
public class LogInterceptor implements HandlerInterceptor {

    private Logger logger=LoggerFactory.getLogger(LogInterceptor.class);

//    @Resource
//    private ClassLocator classLocator;

    @Value("${yiyu.log.req.notice:system.monitor.req.log.notice}")
    private String logReqNotice;

    @Value("${spring.application.name:yiyu-application}")
    private String application;

    @Value("${spring.application.name:yiyu-application}")
    private String sysCode;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 创建请求日志对象
        ReqLog reqLog=new ReqLog();
        reqLog.setSystemFlag(application);
        String reqLogId=UUID.randomUUID().toString().replaceAll("-", "");
        reqLog.setReqLogId(reqLogId);
        reqLog.setStartTime(new Timestamp(System.currentTimeMillis()));
        String requestMethod = request.getMethod();
        String requestUrl = request.getScheme() //当前链接使用的协议
                +"://" + request.getServerName()//服务器地址
                + ":" + request.getServerPort() //端口号
                + request.getContextPath() //应用名称，如果应用名称为
                + request.getServletPath(); //请求的相对url ;
        if(Method.GET.toString().equals(requestMethod)){
            if(!StringUtils.isEmpty(request.getQueryString())){
                requestUrl+= "?" + request.getQueryString(); //请求参数
            }
        }
        reqLog.setReqUrl(requestUrl);
        reqLog.setReqType(requestMethod);
        String remoteAddr = HttpUtil.getIPAddress(request);
        reqLog.setClientIp(remoteAddr);
        request.setAttribute("reqLog",reqLog);
        response.setHeader("reqLogId",reqLog.getReqLogId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ReqLog reqLog=(ReqLog)request.getAttribute("reqLog");
        long start=reqLog.getStartTime().getTime();
        response.setHeader("useTime",reqLog.getUseTime());
        reqLog.setSysCode(sysCode);
        long end=System.currentTimeMillis();
        reqLog.setEndTime(new Timestamp(end));
        reqLog.setUseTime(end-start+"ms");
        if(null != reqLog.getReqException()){
            reqLog.setReqFlag(0);
            logger.error(JSONObject.toJSONString(reqLog));
        }else {
            reqLog.setReqFlag(1);
            logger.debug(JSONObject.toJSONString(reqLog));
        }
        /*try {
            Object object = classLocator.getBean("activeProductManager");
            if(object != null){
                java.lang.reflect.Method method =object.getClass().getMethod("sendQueue",String.class,Object.class);
                method.invoke(object,logReqNotice,reqLog);
            }
        }catch (Exception e){

        }*/
    }
}