package io.github.pak3nuh.util.lang.enum_expression.processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import io.github.pak3nuh.util.processor.ExpressionBuilder
import java.util.*
import java.util.function.Supplier
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class ExpressionWriter(private val filer: Filer) {
    fun write(enumData: EnumData) {
        val exprInterfaceName: ClassName = ClassName.bestGuess("${enumData.pkg}.${enumData.expressionName}")
        val variableName: TypeVariableName = TypeVariableName.get("T")
        val builder = TypeSpec.interfaceBuilder(exprInterfaceName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(variableName)
                .addMethods(toInterfaceMethods(enumData, variableName))
                .addMethod(evaluatorMethod(enumData, exprInterfaceName))
                .addMethod(lambdaExhaustive(enumData))

        if (enumData.defaultInterface) {
            builder.addType(defaultInterface(enumData, variableName, exprInterfaceName))
        }

        if (enumData.expressionBuilder) {
            val expressionBuilder = getExpressionBuilder(enumData)
            builder.addTypes(expressionBuilder.buildInterfaces())
            builder.addType(expressionBuilder.buildBuilder(exprInterfaceName, "evalLambda"))
        }

        val file = JavaFile.builder(enumData.pkg, builder.build()).build()
        file.writeTo(filer)
    }

    private fun getExpressionBuilder(enumData: EnumData): ExpressionBuilder {
        val builder = ExpressionBuilder(enumData.enumType, enumData.pkg)
        val inputType = ParameterizedTypeName.get(ClassName.get(Supplier::class.java), TypeVariableName.get("T"))
        enumData.symbols.map {
            builder.addBranch("on$it", inputType)
        }
        return builder
    }

    private fun defaultInterface(enumData: EnumData, variableName: TypeVariableName, exprInterfaceName: ClassName): TypeSpec {
        return TypeSpec.interfaceBuilder("WithDefault")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(ParameterizedTypeName.get(exprInterfaceName, variableName))
                .addTypeVariable(variableName)
                .addMethod(
                        MethodSpec.methodBuilder("defaultValue")
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .returns(variableName).build()
                )
                .addMethods(toDefaultInterfaceMethods(enumData, variableName))
                .build()
    }

    private fun evaluatorMethod(enumData: EnumData, exprInterfaceName: ClassName): MethodSpec {
        val variableName = TypeVariableName.get("W")
        val expressionType = ParameterizedTypeName.get(exprInterfaceName, variableName)
        return MethodSpec.methodBuilder("eval")
                .addTypeVariable(variableName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(enumData.enumType, "value")
                .addParameter(ParameterSpec.builder(expressionType, "delegate").build())
                .addCode(evaluatorCode(enumData.symbols){ "delegate.$it()" })
                .returns(variableName)
                .build()
    }

    private fun evaluatorCode(symbols: Set<String>, symbolFn: (String) -> String): CodeBlock {
        val objectsClassName = ClassName.get(Objects::class.java)
        return CodeBlock.builder()
                .beginControlFlow("switch(\$T.requireNonNull(value))", objectsClassName)
                .apply {
                    symbols.forEach {
                        addStatement("case $it: return ${symbolFn(it)}")
                    }
                }
                .addStatement("default: throw new IllegalStateException(String.valueOf(value))")
                .endControlFlow()
                .build()
    }

    private fun toInterfaceMethods(enumData: EnumData, variableName: TypeVariableName): Iterable<MethodSpec> {
        return enumData.symbols.map {
            MethodSpec.methodBuilder(it)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(variableName)
                    .build()
        }
    }

    private fun toDefaultInterfaceMethods(enumData: EnumData, variableName: TypeVariableName): Iterable<MethodSpec> {
        return enumData.symbols.map {
            MethodSpec.methodBuilder(it)
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                    .addCode("return defaultValue();")
                    .returns(variableName)
                    .build()
        }
    }

    private fun lambdaExhaustive(enumData: EnumData): MethodSpec {
        val variableName = TypeVariableName.get("W")
        return MethodSpec.methodBuilder("evalLambda")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(variableName)
                .addParameters(toParameters(enumData.symbols, variableName, enumData.enumType))
                .addCode(evaluatorCode(enumData.symbols){ "$it.get()" })
                .returns(variableName)
                .build()
    }

    private fun toParameters(symbols: Set<String>, variableName: TypeVariableName, enumType: ClassName): Iterable<ParameterSpec> {
        val supplierType = ParameterizedTypeName.get(ClassName.get(Supplier::class.java), variableName)
        val enumParameter = ParameterSpec.builder(enumType, "value").build()
        return listOf(enumParameter) + symbols.map {
            ParameterSpec.builder(supplierType, it).build()
        }
    }
}
