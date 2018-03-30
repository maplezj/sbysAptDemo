package com.mobiledoctor.compiler;

import com.mobiledoctor.annotation.METHOD;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({ // 标注注解处理器支持的注解类型
        "com.example.annotation.METHOD",
})
public class HttpProcessor extends AbstractProcessor
{
    private Filer mFiler;       // File util, write class file into disk.
    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment)
    {
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        try
        {

            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(METHOD.class);
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder("RequestProxy")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
/*            for (TypeElement typeElement : set)
            {
                parseTypeElement(typeElement);
            }*/
            for (Element element : elements)
            {
                MethodSpec methodSpec = parseMethod(element);
                classBuilder.addMethod(methodSpec);
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

/*    private void parseTypeElement(TypeElement typeElement)
    {
        System.out.println("begin TypeElement---------------------->");
        System.out.println("element:" + typeElement.getSimpleName());
        System.out.println("simple name:" + typeElement.getSimpleName());
        System.out.println("typeParameters:" + typeElement.getTypeParameters().toArray());
        System.out.println("end TypeElement---------------------->");
    }*/

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment)
    {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();                  // Generate class.

    }

    private MethodSpec parseMethod(Element element)
    {
        if (element.getKind() != ElementKind.METHOD)
        {
            throw new IllegalArgumentException(String.format("Only method can be annotated with @%s",
                    METHOD.class.getSimpleName()));
        }

        System.out.println("begin Element---------------------->");
        ExecutableElement executableElement = (ExecutableElement) element;

        MethodEntity methodEntity = new MethodEntity(executableElement);
        System.out.println("methodEntity:" + methodEntity.toString());

        ClassName inputType = ClassName.get("java.util", "Map");
        ClassName requestParam = ClassName.get("com.wadata.mobiledoctor.http.data", "RequestParams");
        ClassName apiManger = ClassName.get("com.wadata.mobiledoctor.http", "APIManager");
        ClassName requestAPI = ClassName.get("com.wadata.mobiledoctor.http", "RequestAPI");
        ClassName httpError = ClassName.get("com.wadata.mobiledoctor.http", "HttpError");
        ClassName preProcessFlatMap = ClassName.get("com.wadata.mobiledoctor.http", "PreProcessFlatMap");
        TypeName httpErrorType = ParameterizedTypeName.get(httpError, methodEntity.getThirdReturnType());
        TypeName preProcessFlatMapType = ParameterizedTypeName.get(preProcessFlatMap, methodEntity.getThirdReturnType());

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(element.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(methodEntity.getSubReturnType())
                .addStatement("$T requestParams = new $T()", requestParam, requestParam)
                .addStatement("requestParams.paramMap = paramMap")
                .addStatement("requestParams.method = $S", methodEntity.getAnnotationValue())
                .addStatement("return $T.getAPI($T.class).$L(requestParams)" +
                                ".onErrorReturn(new $T())" +
                                ".flatMap(new $T())",
                        apiManger, requestAPI, executableElement.getSimpleName(), httpErrorType, preProcessFlatMapType)
                .addParameter(inputType, "paramMap");
        return methodBuilder.build();
    }

}
