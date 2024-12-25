package org.apache.skywalking.apm.plugin.spring.mvc.yjj.request;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.spring.mvc.yjj.request.constant.TraceConstants;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

public class RequestHeaderInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Object[] allArgs, Class<?>[] classes, MethodInterceptResult methodInterceptResult) throws Throwable {
    }

    @Override
    public Object afterMethod(EnhancedInstance enhancedInstance, Method method, Object[] allArgs, Class<?>[] classes, Object ret) {
        if (ContextManager.getRuntimeContext().get(TraceConstants.YJJ_HEADER_TAG) == null) {
            HttpServletRequest request = (HttpServletRequest) allArgs[0];
            String type = request.getHeader(TraceConstants.YJJ_HEADER_CLIENT_TYPE_KEY);
            String token = request.getHeader(TraceConstants.YJJ_HEADER_TOKEN_KEY);
            if (Objects.nonNull(token) || Objects.nonNull(type)) {
                AbstractSpan span = ContextManager.createLocalSpan(TraceConstants.YJJ_REQUEST_SPAN);
                span.setComponent(ComponentsDefine.SPRING_MVC_ANNOTATION);
                span.setLayer(SpanLayer.HTTP);
                span.tag(TraceConstants.YJJ_HEADER_TAG,
                        TraceConstants.YJJ_HEADER_CLIENT_TYPE_KEY + ':' + type + ','
                        + TraceConstants.YJJ_HEADER_TOKEN_KEY + ':' + token);
                ContextManager.stopSpan();
            }
        } else {
            ContextManager.getRuntimeContext().remove(TraceConstants.YJJ_HEADER_TAG);
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes, Throwable t) {
        if (t != null) {
            AbstractSpan activeSpan = ContextManager.activeSpan();
            activeSpan.log(t);
            activeSpan.errorOccurred();
        }
    }
}
