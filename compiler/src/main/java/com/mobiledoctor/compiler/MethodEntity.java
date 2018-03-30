package com.mobiledoctor.compiler;

/**
 * Created by zhaojian on 2018/3/30.
 */

import com.mobiledoctor.annotation.METHOD;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Created by weilu on 2017/12/14.
 */

public class MethodEntity
{
    String annotationValue;
    String inputName;
    TypeName returnType;
    TypeName subReturnType;
    ClassName thirdReturnType;
    ReturnEntity returnEntity = new ReturnEntity();
    public MethodEntity(ExecutableElement executableElement)
    {
        init(executableElement);
    }

    public String getAnnotationValue()
    {
        return annotationValue;
    }

    public String getInputName()
    {
        return inputName;
    }

    public TypeName getReturnType()
    {
        return returnType;
    }

    public TypeName getSubReturnType()
    {
        return subReturnType;
    }

    public TypeName getThirdReturnType()
    {
        return thirdReturnType;
    }

    public ReturnEntity getReturnEntity()
    {
        return returnEntity;
    }

    private void init(ExecutableElement executableElement)
    {
        parseAnnotationValue(executableElement);
        TypeMirror typeMirror = executableElement.getReturnType();
        parseReturnEntity(typeMirror, returnEntity);
        parseInputName(executableElement);
        parseReturnType();
    }

    private void parseReturnType()
    {
        ClassName first = ClassName.get(returnEntity.getPackageName(), returnEntity.getSimpleName());
        List<ReturnEntity> subEntityList = returnEntity.getGenerics();
        if (subEntityList.size() != 1)
        {
            returnType = ParameterizedTypeName.get(first);
            return;
        }

        ReturnEntity subEntity = subEntityList.get(0);
        ClassName second = ClassName.get(subEntity.getPackageName(), subEntity.getSimpleName());
        List<ReturnEntity> subSubEntityList = subEntity.getGenerics();
        if (subSubEntityList.size() != 1)
        {
            returnType = ParameterizedTypeName.get(first, second);
            subReturnType = ParameterizedTypeName.get(second);
            return;
        }

        ReturnEntity subSubEntity = subSubEntityList.get(0);
        thirdReturnType = ClassName.get(subSubEntity.getPackageName(), subSubEntity.getSimpleName());
        subReturnType = ParameterizedTypeName.get(first, thirdReturnType);
        returnType = ParameterizedTypeName.get(first, subReturnType);

    }

    private void parseInputName(ExecutableElement executableElement)
    {
        List<? extends VariableElement> variableElements = executableElement.getParameters();
        if (variableElements.size() != 1)
        {
            throw new IllegalArgumentException("illegal input count");
        }
        inputName = variableElements.get(0).toString();
    }

    private void parseReturnEntity(TypeMirror typeMirror, ReturnEntity returnEntity)
    {
        String typeStr = typeMirror.toString();
        if (typeStr == null || typeStr.length() == 0)
        {
            return;
        }
        if (typeStr.contains("<"))
        {
            int index = typeStr.indexOf("<");
            typeStr = typeStr.substring(0, index);
        }
        int index = typeStr.lastIndexOf(".");
        returnEntity.setPackageName(typeStr.substring(0, index));
        returnEntity.setSimpleName(typeStr.substring(index+1, typeStr.length()));
        System.out.println("packageName--" + returnEntity.getPackageName());
        System.out.println("simpleName--" + returnEntity.getSimpleName());
        if (typeMirror instanceof DeclaredType)
        {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            List<? extends TypeMirror> arguments = declaredType.getTypeArguments();
            if (arguments != null && arguments.size() > 0)
            {
                for (TypeMirror argument : arguments)
                {
                    ReturnEntity subReturnEntity = new ReturnEntity();
                    returnEntity.addReturnEnetity(subReturnEntity);
                    parseReturnEntity(argument, subReturnEntity);
                }
            }
        }
    }

    private void parseAnnotationValue(ExecutableElement executableElement)
    {
        METHOD method = executableElement.getAnnotation(METHOD.class);
        annotationValue = method.value();
        if (annotationValue.length() == 0)
        {
            throw new IllegalArgumentException(
                    String.format("value() in %s for field %s is not valid !", METHOD.class.getSimpleName(),
                            executableElement.getSimpleName()));
        }
    }

    @Override
    public String toString()
    {
        return "annotationValue:" + annotationValue + "|inputName:" + inputName + "|returnEntity:" + returnEntity.toString();
    }
}