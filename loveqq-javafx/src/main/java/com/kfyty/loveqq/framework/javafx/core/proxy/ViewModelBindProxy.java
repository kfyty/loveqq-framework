package com.kfyty.loveqq.framework.javafx.core.proxy;

import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;
import com.kfyty.loveqq.framework.core.utils.PackageUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.javafx.core.AbstractViewModelBindCapableController;
import com.kfyty.loveqq.framework.javafx.core.LifeCycleController;
import com.kfyty.loveqq.framework.javafx.core.ViewModelBindAware;
import com.kfyty.loveqq.framework.javafx.core.binder.ViewPropertyBinder;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * 描述: 模型绑定代理
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class ViewModelBindProxy implements MethodInterceptorChainPoint {
    /**
     * 控制器
     */
    private final AbstractViewModelBindCapableController controller;

    /**
     * 视图模型绑定列表
     */
    private final List<Pair<String, ObservableValue<?>>> bindViews;

    /**
     * 属性绑定器
     */
    private static volatile List<ViewPropertyBinder> viewPropertyBinders;

    public void addBindView(String bindPath, ObservableValue<?> view) {
        this.bindViews.add(new Pair<>(bindPath, view));
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        final int hashCode = methodProxy.getTarget().hashCode();
        final Object proceed = chain.proceed(methodProxy);
        final int proceedHashCode = methodProxy.getTarget().hashCode();
        if (hashCode != proceedHashCode) {
            this.viewBind(methodProxy);
        } else if (proceed instanceof ViewModelBindAware viewModelBindAware && viewModelBindAware.isMarkBind()) {
            this.viewBind(methodProxy);
            viewModelBindAware.unmarkBind();
        }
        return proceed;
    }

    public void viewBind(MethodProxy methodProxy) {
        // 事件过来的不处理
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement.getClassName().equals(AbstractViewModelBindCapableController.ViewBindEventHandler.class.getName())) {
                return;
            }
        }

        // 更新到视图
        for (Pair<String, ObservableValue<?>> bindView : this.bindViews) {
            Object viewValue = bindView.getValue().getValue();
            Object modelValue = ReflectUtil.resolveValue(bindView.getKey(), this.controller);
            if (!Objects.equals(viewValue, modelValue)) {
                try {
                    this.viewBind(bindView.getValue(), modelValue);
                } catch (Throwable e) {
                    if (this.controller instanceof LifeCycleController lifeCycleController) {
                        lifeCycleController.onViewBindCause(bindView.getValue(), modelValue, e);
                        return;
                    }
                    throw e;
                }
            }
        }
    }

    public void viewBind(ObservableValue<?> view, Object value) {
        this.obtainViewPropertyBinder();
        for (ViewPropertyBinder binder : viewPropertyBinders) {
            if (view instanceof WritableValue<?> writableValue) {
                if (binder.support(writableValue, view.getClass())) {
                    binder.bind(writableValue, value);
                    break;
                }
            }
        }
    }

    protected void obtainViewPropertyBinder() {
        if (viewPropertyBinders == null) {
            synchronized (ViewModelBindProxy.class) {
                if (viewPropertyBinders == null) {
                    viewPropertyBinders = PackageUtil.scanInstance(ViewPropertyBinder.class);
                    viewPropertyBinders.addAll(IOC.getApplicationContext().getBeanOfType(ViewPropertyBinder.class).values());
                }
            }
        }
    }

    /**
     * 触发一次控制器中模型到视图的绑定
     *
     * @param controller 控制器
     */
    public static void triggerViewBind(Object controller) {
        for (Field value : ReflectUtil.getFields(controller.getClass())) {
            if (value.getDeclaringClass() == Object.class) {
                continue;
            }
            Object fieldValue = ReflectUtil.getFieldValue(controller, value, false);
            if (fieldValue != null && AopUtil.isProxy(fieldValue)) {
                AopUtil.getProxyInterceptorChain(fieldValue)
                        .getChainPoints()
                        .stream()
                        .filter(e -> e instanceof ViewModelBindProxy)
                        .map(e -> (ViewModelBindProxy) e)
                        .findAny()
                        .ifPresent(proxy -> proxy.viewBind(null));
            }
        }
    }
}
