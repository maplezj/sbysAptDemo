package com.example.compiler;

import com.example.annotation.METHOD;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({ // 标注注解处理器支持的注解类型
        "com.example.annotation.METHOD",
})
public class HttpProcessor extends AbstractProcessor
{
    private Filer mFiler;       // File util, write class file into disk.


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment)
    {
        try
        {

            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(METHOD.class);
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder("RequestProxy")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            for (TypeElement typeElement : set)
            {
                parseTypeElement(typeElement);
            }
            for (Element element : elements)
            {
                parseMethod(element);
            }

            JavaFile javaFile = JavaFile.builder("com.example.helloworld", classBuilder.build())
                    .build();

            javaFile.writeTo(mFiler);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void parseTypeElement(TypeElement typeElement)
    {
        System.out.println("begin TypeElement---------------------->");
        System.out.println("element:" + typeElement.getSimpleName());
        System.out.println("simple name:" + typeElement.getSimpleName());
        System.out.println("typeParameters:" + typeElement.getTypeParameters().toArray());
        System.out.println("end TypeElement---------------------->");
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment)
    {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();                  // Generate class.

    }

    private void parseMethod(Element element)
    {
        if (element instanceof ExecutableElement)
        {
            ExecutableElement executableElement = (ExecutableElement) element;
            /*方法入参名称*/
            List<? extends VariableElement> variableElements = executableElement.getParameters();
            for (VariableElement variableElement : variableElements)
            {
                System.out.println("vari---" + variableElement);
            }
            /*返回值类型*/
            TypeMirror typeMirror = executableElement.getReturnType();
            System.out.println("return--" + typeMirror.toString());

            DeclaredType declaredType = (DeclaredType) typeMirror;
            System.out.println("Declared--" + declaredType.getTypeArguments());


            System.out.println(executableElement);

        }
        System.out.println("begin Element---------------------->");
        System.out.println("simple name:" + element.getSimpleName());
        System.out.println("element:" + element);
        System.out.println("element:" + element.toString());
        System.out.println("end Element---------------------->");

        VariableElement mVariableElement;
        if (element.getKind() != ElementKind.METHOD) {
            throw new IllegalArgumentException(String.format("Only fields can be annotated with @%s",
                    METHOD.class.getSimpleName()));
        }
        mVariableElement = (VariableElement) element;

        METHOD method = mVariableElement.getAnnotation(METHOD.class);
        String requestMethod = method.value();
        if (requestMethod.length() == 1)
        {
            throw new IllegalArgumentException(
                    String.format("value() in %s for field %s is not valid !", METHOD.class.getSimpleName(),
                            mVariableElement.getSimpleName()));
        }

        ClassName hoverboard = ClassName.get("io.reactivex", "Observable");
        ClassName list = ClassName.get("java.util", "List");
        ClassName arrayList = ClassName.get("java.util", "ArrayList");
        TypeName listOfHoverboards = ParameterizedTypeName.get(list, hoverboard);

        /*MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(element.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(ClassName.get(typeElement.asType()), "activity");*/
    }
}
