package org.participate.processing;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 1. 扩展 {@link AbstractProcessor} 抽象类
 * 2. 指定需要处理的注解类名集合 {@link SupportedAnnotationTypes}
 * 3. 指定值得 Java 源代码版本 {@link SupportedSourceVersion} {@link SourceVersion}
 * 4. 指定支持的 {@code Repository} 参数 Options 可选
 *
 * @author: zhangxinzhong
 * @since: 1.0.0
 * @version: JDK8
 * @create: 2021-01-25 10:48
 **/
@SupportedAnnotationTypes(RepositoryAnnotationProcess.REPOSITORY_ANNOTATION_CLASS_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RepositoryAnnotationProcess extends AbstractProcessor {

    public static final String REPOSITORY_ANNOTATION_CLASS_NAME = "com.participate.web.annotation.Repository";

    public static final String CRUD_REPOSITORY_ANNOTATION_CLASS_NAME = "com.participate.web.repository.CrudRepository";


    /**
     * 存储{@code CrudRepository} 的实现及泛型参数
     * key 实现 {@code CrudRepository} 的类
     * value 泛型参数
     */
    private Map<String, String> crudRepositoryParameterizedTypesMapping = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 第一个阶段:处理阶段
        System.out.println("org.participate.processing.RepositoryAnnotationProcess.process ------------------------->");

        // 获取所有的编译类
        roundEnv.getRootElements()
                .stream()
                // 过滤标注 @Repository 注解的元素
                .filter(this::isRepositoryAnnotationPresent)
                // 处理标注 @Repository 注解的元素
                .forEach(this::processRepositoryAnnotatedElement);

        // 第二个阶段：完成阶段
        if (roundEnv.processingOver()) {
            try {
                generateCrudRepositoryParameterizedTypesMetaData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private void generateCrudRepositoryParameterizedTypesMetaData() throws IOException {
        // 找到classpath
        Filer filer = processingEnv.getFiler();
        String resourceName = "META-INF-CUSTOM/curd-repos-mappings.properties";
        FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        try (Writer writer = fileObject.openWriter()) {
            Properties properties = new Properties();
            properties.putAll(crudRepositoryParameterizedTypesMapping);
            properties.store(writer, "Generated by RepositoryAnnotationProcess ");

        }
    }

    /**
     * 处理实现了{@code CrudRepository} 和标注了{@code Repository} 的元素
     *
     * @param element
     */
    private void processRepositoryAnnotatedElement(Element element) {
        if (!isConcreteClass(element) || !isCrudRepositoryType(element)) {
            return;
        }

        System.out.println("CrudRepository 实现类为：" + element.toString());

        // CrudRepository 接口类型
        TypeMirror crudRepositoryGenericInterfaceType = getGenericInterfaceType(element, CRUD_REPOSITORY_ANNOTATION_CLASS_NAME);

        System.out.println("CrudRepository 实现泛型接口定义为：" + crudRepositoryGenericInterfaceType);

        // CrudRepository 是接口类型，强制转化为 DeclaredType
        DeclaredType declaredType = DeclaredType.class.cast(crudRepositoryGenericInterfaceType);

        // 获取泛型参数类型列表
        List<? extends TypeMirror> parameterizedType = declaredType.getTypeArguments();

        TypeMirror firstParameterizedType = parameterizedType.get(0);

        System.out.println("CrudRepository 实现泛型接口的首个参数类型为：" + firstParameterizedType);

        crudRepositoryParameterizedTypesMapping.put(crudRepositoryGenericInterfaceType.toString(), firstParameterizedType.toString());

    }

    /**
     * 是否是{@code CrudRepository}类型
     *
     * @param element
     * @return
     */
    private boolean isCrudRepositoryType(Element element) {
        return getGenericInterfaceType(element, CRUD_REPOSITORY_ANNOTATION_CLASS_NAME) != null;
    }

    /**
     * 获取指定的{@code interfaceTypeName}的类型
     *
     * @param element
     * @param interfaceTypeName
     * @return
     */
    private TypeMirror getGenericInterfaceType(Element element, String interfaceTypeName) {
        ElementKind elementKind = element.getKind();
        if (elementKind.isClass() && element instanceof TypeElement) {
            TypeElement typeElement = TypeElement.class.cast(element);
            //获取当前element（Class文件）实现的所有接口
            return typeElement.getInterfaces()
                    .stream()
                    // 过滤只有interfaceTypeName 的接口
                    .filter(interfaceType -> typeEquals(interfaceType, interfaceTypeName))
                    .findFirst()
                    .orElse(null);

        }
        return null;
    }

    private boolean typeEquals(TypeMirror type, String interfaceTypeName) {
        // 工具类
        Types types = processingEnv.getTypeUtils();
        // 擦写泛型参数
        TypeMirror erasedType = types.erasure(type);
        return Objects.equals(interfaceTypeName, erasedType.toString());

    }

    /**
     * 是不是具体类
     *
     * @param element
     * @return
     */
    private boolean isConcreteClass(Element element) {
        return !element.getModifiers().contains(Modifier.ABSTRACT);
    }


    /**
     * 是否标注了{@code REPOSITORY_ANNOTATION_CLASS_NAME}
     *
     * @param element
     * @return
     */
    private boolean isRepositoryAnnotationPresent(Element element) {
        return isAnnotationPresent(element, REPOSITORY_ANNOTATION_CLASS_NAME);
    }

    /**
     * 判断当前类是否存在某个注解 类似于 Class.isAnnotationPresent()
     *
     * @param element
     * @param annotationClassName
     * @return
     */
    private boolean isAnnotationPresent(Element element, String annotationClassName) {
        // 返回当前元素的注解集合
        return element.getAnnotationMirrors()
                .stream()
                .filter(annotation -> Objects.equals(annotationClassName, annotation.getAnnotationType().toString()))
                .count() > 0;
    }
}
