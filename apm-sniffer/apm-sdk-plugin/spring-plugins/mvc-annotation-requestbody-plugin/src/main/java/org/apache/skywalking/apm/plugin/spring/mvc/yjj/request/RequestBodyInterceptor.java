package org.apache.skywalking.apm.plugin.spring.mvc.yjj.request;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.dependencies.com.google.gson.Gson;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.spring.mvc.yjj.request.constant.TraceConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;

import java.lang.reflect.Method;

public class RequestBodyInterceptor implements InstanceMethodsAroundInterceptor {

    private static final Gson gson = new Gson();

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) {
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        HttpInputMessage httpInputMessages = (HttpInputMessage) allArguments[0];
        HttpHeaders headers = httpInputMessages.getHeaders();
        String type = headers.getFirst(TraceConstants.YJJ_HEADER_CLIENT_TYPE_KEY);
        String token = headers.getFirst(TraceConstants.YJJ_HEADER_TOKEN_KEY);

        String body = null;
        if (MediaType.APPLICATION_JSON.includes(headers.getContentType()) && ret != null) {
            body = gson.toJson(ret);
            if (body.length() > 10240) {
                body = null;
            }
        }
        if (type != null || token != null || body != null) {
            AbstractSpan span = ContextManager.createLocalSpan(TraceConstants.YJJ_REQUEST_SPAN);
            span.tag(TraceConstants.YJJ_HEADER_TAG,
                    TraceConstants.YJJ_HEADER_CLIENT_TYPE_KEY + ':' + type + ','
                            + TraceConstants.YJJ_HEADER_TOKEN_KEY + ':' + token);
            if (body != null) {
                span.tag(TraceConstants.YJJ_BODY_TAG, body);
            }
            span.setComponent(ComponentsDefine.SPRING_MVC_ANNOTATION);
            span.setLayer(SpanLayer.HTTP);
            ContextManager.getRuntimeContext().put(TraceConstants.YJJ_HEADER_TAG, true);
            ContextManager.stopSpan();
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        if (t != null) {
            AbstractSpan activeSpan = ContextManager.activeSpan();
            activeSpan.log(t);
            activeSpan.errorOccurred();
        }
    }
}
