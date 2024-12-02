package org.apache.skywalking.apm.plugin.spring.mvc.yjj.request;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.dependencies.com.google.gson.Gson;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.spring.mvc.yjj.request.constant.TraceConstants;
import org.springframework.http.HttpInputMessage;

import java.lang.reflect.Method;
import java.util.Objects;

public class RequestBodyInterceptor implements InstanceMethodsAroundInterceptor {

    private Gson gson = new Gson();

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) {
        AbstractSpan span = ContextManager.createLocalSpan(TraceConstants.YJJ_REQUEST_SPAN);
        span.setComponent(ComponentsDefine.SPRING_MVC_ANNOTATION);
        span.setLayer(SpanLayer.HTTP);
        HttpInputMessage httpInputMessages = (HttpInputMessage) allArguments[0];
        span.tag(new StringTag(TraceConstants.YJJ_HEADER_TAG), httpInputMessages.getHeaders().toString());
        ContextManager.getRuntimeContext().put(TraceConstants.YJJ_HEADER_TAG, true);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        AbstractSpan span = ContextManager.activeSpan();
        if (Objects.nonNull(ret)) {
            span.tag(new StringTag(TraceConstants.YJJ_BODY_TAG), gson.toJson(ret));
        }
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        AbstractSpan activeSpan = ContextManager.activeSpan();
        activeSpan.log(t);
        activeSpan.errorOccurred();
    }
}
