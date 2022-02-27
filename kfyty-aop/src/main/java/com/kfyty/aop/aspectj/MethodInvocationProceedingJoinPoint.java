package com.kfyty.aop.aspectj;

import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * 描述: JoinPoint 简单实现
 *
 * @author kfyty725
 * @date 2021/7/31 20:14
 * @email kfyty725@hotmail.com
 */
public class MethodInvocationProceedingJoinPoint implements ProceedingJoinPoint, JoinPoint.StaticPart {
    private Signature signature;
    private SourceLocation sourceLocation;

    private final MethodProxyWrapper methodProxyWrapper;
    private final MethodInterceptorChain interceptorChain;

    public MethodInvocationProceedingJoinPoint(MethodProxyWrapper methodProxyWrapper, MethodInterceptorChain interceptorChain) {
        this.methodProxyWrapper = methodProxyWrapper;
        this.interceptorChain = interceptorChain;
    }

    @Override
    public void set$AroundClosure(AroundClosure arc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object proceed() throws Throwable {
        return this.interceptorChain.proceed(this.methodProxyWrapper);
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getThis() {
        return this.methodProxyWrapper.getProxy();
    }

    @Override
    public Object getTarget() {
        return this.methodProxyWrapper.getTarget();
    }

    @Override
    public Object[] getArgs() {
        return this.methodProxyWrapper.getArguments();
    }

    @Override
    public String toShortString() {
        return "execution(" + getSignature().toShortString() + ")";
    }

    @Override
    public String toLongString() {
        return "execution(" + getSignature().toLongString() + ")";
    }

    @Override
    public String toString() {
        return "execution(" + getSignature().toString() + ")";
    }

    @Override
    public Signature getSignature() {
        if (this.signature == null) {
            this.signature = new MethodSignatureImpl();
        }
        return this.signature;
    }

    @Override
    public SourceLocation getSourceLocation() {
        if (this.sourceLocation == null) {
            this.sourceLocation = new SourceLocationImpl();
        }
        return this.sourceLocation;
    }

    @Override
    public StaticPart getStaticPart() {
        return this;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getKind() {
        return ProceedingJoinPoint.METHOD_EXECUTION;
    }

    private class MethodSignatureImpl implements MethodSignature {
        private volatile String[] parameterNames;

        @Override
        public String getName() {
            return this.getMethod().getName();
        }

        @Override
        public int getModifiers() {
            return this.getMethod().getModifiers();
        }

        @Override
        public Class<?> getDeclaringType() {
            return this.getMethod().getDeclaringClass();
        }

        @Override
        public String getDeclaringTypeName() {
            return this.getMethod().getDeclaringClass().getName();
        }

        @Override
        public Class<?> getReturnType() {
            return this.getMethod().getReturnType();
        }

        @Override
        public Method getMethod() {
            return methodProxyWrapper.getTargetMethod();
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return this.getMethod().getParameterTypes();
        }

        @Override
        public String[] getParameterNames() {
            if (this.parameterNames == null) {
                this.parameterNames = Arrays.stream(this.getMethod().getParameters()).map(Parameter::getName).toArray(String[]::new);
            }
            return this.parameterNames;
        }

        @Override
        public Class<?>[] getExceptionTypes() {
            return this.getMethod().getExceptionTypes();
        }

        @Override
        public String toShortString() {
            return toString(false, false, false, false);
        }

        @Override
        public String toLongString() {
            return toString(true, true, true, true);
        }

        @Override
        public String toString() {
            return toString(false, true, false, true);
        }

        private String toString(boolean includeModifier, boolean includeReturnTypeAndArgs, boolean useLongReturnAndArgumentTypeName, boolean useLongTypeName) {
            StringBuilder sb = new StringBuilder();
            if (includeModifier) {
                sb.append(Modifier.toString(getModifiers()));
                sb.append(" ");
            }
            if (includeReturnTypeAndArgs) {
                appendType(sb, getReturnType(), useLongReturnAndArgumentTypeName);
                sb.append(" ");
            }
            appendType(sb, getDeclaringType(), useLongTypeName);
            sb.append(".");
            sb.append(getMethod().getName());
            sb.append("(");
            Class<?>[] parametersTypes = getParameterTypes();
            appendTypes(sb, parametersTypes, includeReturnTypeAndArgs, useLongReturnAndArgumentTypeName);
            sb.append(")");
            return sb.toString();
        }

        private void appendTypes(StringBuilder sb, Class<?>[] types, boolean includeArgs, boolean useLongReturnAndArgumentTypeName) {
            if (includeArgs) {
                for (int size = types.length, i = 0; i < size; i++) {
                    appendType(sb, types[i], useLongReturnAndArgumentTypeName);
                    if (i < size - 1) {
                        sb.append(",");
                    }
                }
            } else {
                if (types.length != 0) {
                    sb.append("..");
                }
            }
        }

        private void appendType(StringBuilder sb, Class<?> type, boolean useLongTypeName) {
            if (type.isArray()) {
                appendType(sb, type.getComponentType(), useLongTypeName);
                sb.append("[]");
            } else {
                sb.append(useLongTypeName ? type.getName() : type.getSimpleName());
            }
        }
    }

    private class SourceLocationImpl implements SourceLocation {

        @Override
        public Class<?> getWithinType() {
            if (methodProxyWrapper.getTarget() == null) {
                throw new UnsupportedOperationException("No source location joinpoint available: target is null");
            }
            return methodProxyWrapper.getTarget().getClass();
        }

        @Override
        public String getFileName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLine() {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        public int getColumn() {
            throw new UnsupportedOperationException();
        }
    }
}
