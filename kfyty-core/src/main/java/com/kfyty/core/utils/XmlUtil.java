package com.kfyty.core.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 描述: xml 工具
 *
 * @author kfyty725
 * @date 2021/7/15 16:51
 * @email kfyty725@hotmail.com
 */
public abstract class XmlUtil {
    /**
     * DeferredTextImpl 实现类
     */
    private static final String DEFERRED_TEXT_IMPL_CLASS = "com.sun.org.apache.xerces.internal.dom.DeferredTextImpl";

    /**
     * 获取一个 DocumentBuilderFactory
     *
     * @return DocumentBuilderFactory
     */
    public static DocumentBuilderFactory factory() {
        return DocumentBuilderFactoryHolder.INSTANCE;
    }

    /**
     * 从工厂返回一个 DocumentBuilder
     *
     * @return DocumentBuilder
     */
    public static DocumentBuilder builder() {
        try {
            return factory().newDocumentBuilder();
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 从文件创建一个 xml 文档
     *
     * @param file 文件
     * @return xml 文档
     */
    public static Document create(File file) {
        return create(IOUtil.newInputStream(file));
    }

    /**
     * 从 URL 创建一个 xml 文档
     *
     * @param url 文件
     * @return xml 文档
     */
    public static Document create(URL url) {
        try {
            return create(url.openStream());
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 从输入流创建一个 xml 文档
     *
     * @param inputStream 输入流
     * @return xml 文档
     */
    public static Document create(InputStream inputStream) {
        try {
            return builder().parse(inputStream);
        } catch (SAXException | IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 获取元素的属性值
     *
     * @param element 元素
     * @param name    属性名称
     * @return 属性值
     */
    public static String resolveAttribute(Element element, String name) {
        return resolveAttribute(element, name, null);
    }

    /**
     * 获取元素的属性值
     *
     * @param element        元素
     * @param name           属性名称
     * @param emptyException 属性值存在但是为空时抛出的异常
     * @return 属性值
     */
    public static String resolveAttribute(Element element, String name, Supplier<RuntimeException> emptyException) {
        String attribute = element.getAttribute(name);
        if (emptyException != null && CommonUtil.empty(attribute)) {
            throw emptyException.get();
        }
        return attribute;
    }

    /**
     * 根据断言搜索第一个符合的子节点
     *
     * @param element       元素
     * @param nodePredicate 子节点断言
     * @return 子元素
     */
    public static Element resolveChild(Element element, Predicate<Element> nodePredicate) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element && nodePredicate.test((Element) node)) {
                return (Element) node;
            }
        }
        return null;
    }

    /**
     * 根据断言搜索第一个符合的子节点，并获取其属性值
     *
     * @param element          元素
     * @param nodePredicate    子节点断言
     * @param attributeMapping 提取属性值函数
     * @return 子节点属性值
     */
    public static String resolveChildAttribute(Element element, Predicate<Element> nodePredicate, Function<Element, String> attributeMapping) {
        Element node = resolveChild(element, nodePredicate);
        return node == null ? null : attributeMapping.apply(node);
    }

    /**
     * 获取全部子节点的某个属性值
     *
     * @param element          元素
     * @param attributeMapping 提取属性值函数
     * @return 子节点属性值集合
     */
    public static List<String> resolveChildrenAttribute(Element element, Function<Element, String> attributeMapping) {
        return resolveChildrenAttribute(element, (i, e) -> true, attributeMapping);
    }

    /**
     * 获取指定索引的子节点的属性值
     *
     * @param element          元素
     * @param index            子节点索引
     * @param attributeMapping 提取属性值函数
     * @return 子节点属性值集合
     */
    public static List<String> resolveChildrenAttribute(Element element, List<Integer> index, Function<Element, String> attributeMapping) {
        return resolveChildrenAttribute(element, (i, e) -> i < index.size() && index.get(i).equals(i), attributeMapping);
    }

    /**
     * 根据断言获取子节点的属性值
     *
     * @param element          元素
     * @param nodePredicate    子节点断言
     * @param attributeMapping 提取属性值函数
     * @return 子节点属性值集合
     */
    public static List<String> resolveChildrenAttribute(Element element, BiPredicate<Integer, Element> nodePredicate, Function<Element, String> attributeMapping) {
        NodeList childNodes = element.getChildNodes();
        List<String> attribute = new ArrayList<>(childNodes.getLength());
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element && nodePredicate.test(i, (Element) node)) {
                attribute.add(attributeMapping.apply((Element) node));
            }
        }
        return attribute;
    }

    /**
     * 根据路径查询子元素
     *
     * @param document 文档
     * @param path     路径 eg: ./a/../b/c[0]/d
     * @return 子元素
     */
    public static Element resolveElement(Document document, String path) {
        return resolveElement(document.getDocumentElement(), path);
    }

    /**
     * 根据路径查询子元素
     *
     * @param element 元素
     * @param path    路径 eg: ./a/../b/c[0]/d
     * @return 子元素
     */
    public static Element resolveElement(Element element, String path) {
        return resolveElement(element, path.split("/"), 0);
    }

    /**
     * 根据路径查询子元素
     *
     * @param element 元素
     * @param paths   路径 eg: [".", "a", "..", "b", "c[0]", "d"]
     * @param index   当前路径索引
     * @return 子元素
     */
    public static Element resolveElement(Element element, String[] paths, int index) {
        if (index >= paths.length) {
            return element;
        }
        String current = paths[index];
        Integer elementIndex = current.contains("[") && current.contains("]") ? Integer.parseInt(current.substring(current.indexOf('[') + 1, current.length() - 1)) : null;
        current = elementIndex == null ? current : current.substring(0, current.lastIndexOf('['));
        if (current.equals(".")) {
            return resolveElement(element, paths, index + 1);
        }
        if (current.equals("..")) {
            return resolveElement((Element) element.getParentNode(), paths, index + 1);
        }
        List<Node> childNodes = availableChildren(element);
        for (int i = 0; i < childNodes.size(); i++) {
            Node node = childNodes.get(i);
            if (node.getNodeName().equals(current)) {
                if (elementIndex == null) {
                    return resolveElement((Element) node, paths, index + 1);
                }
                return resolveElement((Element) childNodes.get(i + elementIndex), paths, index + 1);
            }
        }
        throw new IllegalArgumentException("the path does not exist in the xml document: " + String.join("/", paths));
    }

    /**
     * 返回可用的子节点，过滤掉了 DeferredTextImpl 实现
     *
     * @param element 元素
     * @return 子节点
     */
    public static List<Node> availableChildren(Element element) {
        return filterChildren(element, node -> !node.getClass().getName().equals(DEFERRED_TEXT_IMPL_CLASS));
    }

    /**
     * 获取过滤的子节点
     *
     * @param element       元素
     * @param nodePredicate 过滤器
     * @return 子节点
     */
    public static List<Node> filterChildren(Element element, Predicate<Node> nodePredicate) {
        NodeList childNodes = element.getChildNodes();
        List<Node> children = new ArrayList<>(childNodes.getLength());
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (nodePredicate.test(node)) {
                children.add(node);
            }
        }
        return children;
    }

    /**
     * DocumentBuilderFactory 单例工厂
     */
    private static final class DocumentBuilderFactoryHolder {
        static final DocumentBuilderFactory INSTANCE = DocumentBuilderFactory.newInstance();
    }
}
