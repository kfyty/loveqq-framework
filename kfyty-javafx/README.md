# kfyty-javafx
    javafx mvvm 框架，实现视图和模型的双向绑定。

## 快速开始
引入pom（暂未发布到中央仓库，需源码安装）
```xml
    <dependencies>
        <dependency>
            <groupId>com.kfyty</groupId>
            <artifactId>kfyty-boot</artifactId>
            <version>1.0.0</version>
        </dependency>

        <dependency>
            <groupId>com.kfyty</groupId>
            <artifactId>kfyty-javafx</artifactId>
            <version>1.0.0</version>
        </dependency>

        <dependency>
            <groupId>jakarta.annotation</groupId>
            <artifactId>jakarta.annotation-api</artifactId>
        </dependency>
    </dependencies>
```

编写 hello-view.fxml
```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Label?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.layout.FlowPane?>
<?import javafx.scene.control.TextField?>
<FlowPane fx:id="root" alignment="CENTER" minWidth="200" vgap="20" prefWrapLength="150" xmlns:fx="http://javafx.com/fxml" fx:controller="com.kfyty.javafx.demo.HelloController">
    <Label text="姓名: " /> <TextField fx:id="name" />
    <Label text="部门: " /> <TextField fx:id="deptName" editable="false" onMouseClicked="#showSelectedWindow"/>

    <Button text="获取数据" onAction="#getFormData"/>
</FlowPane>
```

编写 HelloController
```java
package com.kfyty.javafx.demo;

import com.kfyty.core.autoconfig.annotation.EventListener;
import com.kfyty.core.event.GenericApplicationEvent;
import com.kfyty.core.lang.Lazy;
import com.kfyty.javafx.core.AbstractController;
import com.kfyty.javafx.core.annotation.FController;
import com.kfyty.javafx.core.annotation.FView;
import com.kfyty.javafx.demo.model.User;
import jakarta.annotation.Resource;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * {@link FController} 标识是一个 fxml 控制器，并且是一个单例控制器
 * <p>
 * 添加 {@link com.kfyty.javafx.core.annotation.FPrototypeScope} 注解可标识一个原型作用域，每次新开窗口都创建新的窗口
 */
@FController(value = "root", path = "/fxml/hello-view.fxml", title = "hello", main = true)
public class HelloController extends AbstractController<FlowPane> {
    /**
     * 文本框的 text 绑定到 user 对象的 name 属性
     */
    @FXML
    @FView("text:user.name")
    private TextField name;

    /**
     * 文本框的 text 绑定到 user 对象的 dept 属性中的 deptName 属性
     */
    @FXML
    @FView("text:user.dept.deptName")
    private TextField deptName;

    /**
     * 由于窗口的创建必须在 {@link javafx.application.Application#launch(String...)} 之后
     * 因此要使用自动注入，需要包装为 {@link Lazy} 进行懒注入
     * <p>
     * 新开窗口时，应调用 {@link Lazy#create()} 方法，具体创建新窗口还是返回原窗口，由 ioc 容器管理
     */
    @Resource
    private Lazy<Stage> newWindow;

    /**
     * 要绑定的模型
     */
    private User user;

    /**
     * 事件监听机制
     */
    @EventListener
    public void onCloseNewWindow(GenericApplicationEvent<WindowEvent, String> event) {
        System.out.println(STR."on close new winow event: \{event.getSource()}");
    }

    /**
     * 直接从模型中获取视图数据
     */
    @FXML
    protected void getFormData() {
        NewWindowController childController = this.getChildController(NewWindowController.class);
        System.out.println(STR."\{this.user}, child form: \{childController == null ? null : childController.getSelected().get()}");
    }

    /**
     * 打开新窗口并传值
     */
    @FXML
    protected void showSelectedWindow() {
        this.openWindow(NewWindowController.class, "deptNames.value[0]=dept1&deptNames.value[1]=dept2");
    }

    /**
     * 模型数据绑定异常回调
     */
    @Override
    public void onModelBindCause(ObservableValue<?> target, String bindPath, Object value, Throwable throwable) {
        System.out.println(STR."\{target}:\{throwable.getMessage()}");
    }

    /**
     * 子窗口关闭回调
     */
    @Override
    public void onChildClose(String name, Node child, AbstractController controller) {
        System.out.println(STR."on child close name=\{name}, window=\{controller.getWindow()}");

        /**
         * 设置子窗口选择的值
         */
        if (controller instanceof NewWindowController newWindowController) {
            this.user.getDept().setDeptName(newWindowController.getSelected().get());
        }
    }
}
```

编写 new-window.fxml
```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Button?>
<?import javafx.scene.control.ListView?>
<?import javafx.scene.layout.AnchorPane?>
<AnchorPane fx:id="newWindow" prefHeight="400.0" prefWidth="600.0" xmlns:fx="http://javafx.com/fxml" fx:controller="com.kfyty.javafx.demo.NewWindowController">
    <children>
        <Button layoutX="400.0" layoutY="100.0" text="添加" onAction="#addDeptName" />
        <Button layoutX="400.0" layoutY="150.0" text="删除" onAction="#delChoose" />
        <Button layoutX="400.0" layoutY="200.0" text="选择" onAction="#closeWindow" />
        <ListView fx:id="deptList" layoutX="42.0" layoutY="51.0" prefHeight="275.0" prefWidth="334.0"/>
    </children>
</AnchorPane>
```

编写 NewWindowController
```java
package com.kfyty.javafx.demo;

import com.kfyty.core.event.GenericApplicationEvent;
import com.kfyty.core.lang.Value;
import com.kfyty.javafx.core.AbstractController;
import com.kfyty.javafx.core.BootstrapApplication;
import com.kfyty.javafx.core.annotation.FController;
import com.kfyty.javafx.core.annotation.FView;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.WindowEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 示例添加了 icon，可自动添加图片，或删除该属性
 */
@FController(value = "newWindow", path = "/fxml/new-window.fxml", show = true, icon = "/icon/icon.ico")
public class NewWindowController extends AbstractController<AnchorPane> {
    /**
     * 列表数据绑定到 deptNames 属性
     * 列表选择的数据绑定到 selected 属性
     */
    @FXML
    @FView("items:deptNames.value")
    @FView(value = "getSelectionModel.selectedItemProperty:selected.value", method = true)
    private ListView<String> deptList;

    /**
     * 绑定模型
     * 基本数据类型需要使用 {@link Value} 包装
     */
    @Getter
    private final Value<List<String>> deptNames = new Value<>(new ArrayList<>());

    /**
     * 绑定模型
     * 基本数据类型需要使用 {@link Value} 包装
     */
    @Getter
    private Value<String> selected;

    private int cnt = 0;

    @FXML
    public void addDeptName() {
        this.deptNames.get().add(STR."hello\{++cnt}");
    }

    @FXML
    protected void delChoose() {
        this.deptNames.get().remove(this.selected.get());
    }

    @FXML
    protected void closeWindow() {
        this.close();
    }

    /**
     * 窗口已显示回调
     */
    @Override
    public void onShown(WindowEvent event) {
        System.out.println("show new window");
    }

    /**
     * 本窗口关闭回调
     */
    @Override
    public void onClose(WindowEvent event) {
        System.out.println(STR."publish close new window event: \{this.window}");
        BootstrapApplication.publishEvent(new GenericApplicationEvent<>(event, STR."close window: \{this.window}"));
    }
}
```

编写数据模型
```java
package com.kfyty.javafx.demo.model;

import com.kfyty.core.autoconfig.annotation.NestedConfigurationProperty;
import lombok.Data;

@Data
@NestedConfigurationProperty
public class User {
    private String name;

    @NestedConfigurationProperty
    private Dept dept = new Dept();

    @Data
    public static class Dept {
        private String deptName;
    }
}
```

编写一个启动类
```java
package com.kfyty.javafx.demo;

import com.kfyty.boot.K;
import com.kfyty.core.autoconfig.annotation.BootApplication;

@BootApplication
public class Main {

    public static void main(String[] args) {
        K.run(Main.class, args);
    }
}
```

运行 Main.main 方法即可.
