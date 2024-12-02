package org.apache.skywalking.apm.plugin.spring.mvc.yjj.request;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.spring.mvc.yjj.request.constant.TraceConstants;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;

public class RequestHeaderInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Object[] allArgs, Class<?>[] classes, MethodInterceptResult methodInterceptResult) throws Throwable {
    }

    @Override
    public Object afterMethod(EnhancedInstance enhancedInstance, Method method, Object[] allArgs, Class<?>[] classes, Object ret) {
        if (ContextManager.getRuntimeContext().get(TraceConstants.YJJ_HEADER_TAG) == null) {
            AbstractSpan span = ContextManager.createLocalSpan(TraceConstants.YJJ_REQUEST_SPAN);
            span.setComponent(ComponentsDefine.SPRING_MVC_ANNOTATION);
            span.setLayer(SpanLayer.HTTP);
            WebRequest webRequest = (WebRequest) allArgs[0];
            StringBuilder headers = new StringBuilder("[");
            webRequest.getHeaderNames().forEachRemaining(name -> headers.append("\"").append(name).append("\":\"").append(webRequest.getHeader(name)).append("\","));
            headers.deleteCharAt(headers.length() - 1).append("]");
            span.tag(new StringTag(TraceConstants.YJJ_HEADER_TAG), headers.toString());
            ContextManager.stopSpan();
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes, Throwable throwable) {
        AbstractSpan activeSpan = ContextManager.activeSpan();
        activeSpan.log(throwable);
        activeSpan.errorOccurred();
    }
}
