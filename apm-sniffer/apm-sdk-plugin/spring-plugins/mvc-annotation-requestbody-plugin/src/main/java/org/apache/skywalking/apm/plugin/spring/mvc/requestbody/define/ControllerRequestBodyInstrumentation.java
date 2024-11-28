package org.apache.skywalking.apm.plugin.spring.mvc.requestbody.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassAnnotationMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.MethodInheritanceAnnotationMatcher.byMethodInheritanceAnnotationMatcher;

public class ControllerRequestBodyInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    public static final String ENHANCE_ANNOTATION_CONTROLLER = "org.springframework.stereotype.Controller";
    public static final String ENHANCE_ANNOTATION_REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";

    @Override
    protected ClassMatch enhanceClass() {
        return ClassAnnotationMatch.byClassAnnotationMatch(
                ENHANCE_ANNOTATION_CONTROLLER,
                ENHANCE_ANNOTATION_REST_CONTROLLER
        );
    }

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
                new InstanceMethodsInterceptPoint() {

                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {

                        return byMethodInheritanceAnnotationMatcher(named("org.springframework.web.bind.annotation.RequestMapping"))
                                .or(byMethodInheritanceAnnotationMatcher(named("org.springframework.web.bind.annotation.PostMapping")));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return "org.apache.skywalking.apm.plugin.spring.mvc.requestbody.ControllerRequestInterceptor";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }
}
