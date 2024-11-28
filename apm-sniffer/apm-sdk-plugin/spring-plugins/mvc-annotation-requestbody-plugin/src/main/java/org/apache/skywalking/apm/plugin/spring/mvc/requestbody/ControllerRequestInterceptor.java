package org.apache.skywalking.apm.plugin.spring.mvc.requestbody;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.util.MethodUtil;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

import java.lang.reflect.Method;

public class ControllerRequestInterceptor implements InstanceMethodsAroundInterceptor {

    public static final String RUNTIME_REQUEST_START_TIME_KEY = "RUNTIME_REQUEST_START_TIME_KEY";

    public static final Long SLOW_REQUEST_THRESHOLD_MS = 1000L;

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        AbstractSpan span = ContextManager.createLocalSpan(MethodUtil.generateOperationName(method));
        span.tag(new StringTag(9999999, "http.requestBody"), allArguments[0].toString());

        span.setComponent(ComponentsDefine.SPRING_MVC_ANNOTATION);
        span.setLayer(SpanLayer.CACHE);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        AbstractSpan span = ContextManager.activeSpan();
        Tags.HTTP_RESPONSE_STATUS_CODE.set(span, 50000);
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        AbstractSpan activeSpan = ContextManager.activeSpan();
        // 记录日志
        activeSpan.log(t);
        activeSpan.errorOccurred();
    }
}
