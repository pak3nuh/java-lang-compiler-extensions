package pt.pak3nuh.util.lang.enum_expression.processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import java.nio.file.Paths
import java.util.Objects
import java.util.function.Supplier
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class ExpressionWriter(val filer: Filer) {
    fun write(enumData: EnumData) {
        val className: ClassName = ClassName.bestGuess("${enumData.pkg}.${enumData.name}Expression")
        val variableName: TypeVariableName = TypeVariableName.get("T")
        val type = TypeSpec.interfaceBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(variableName)
                .addMethods(toInterfaceMethods(enumData, variableName))
                .addMethod(evaluator(enumData, className))
                .addMethod(lambdaExhaustive(enumData, className))
                .build()
        val file = JavaFile.builder(enumData.pkg, type).build()
        file.writeTo(filer)
    }

    private fun evaluator(enumData: EnumData, className: ClassName): MethodSpec {
        val variableName = TypeVariableName.get("W")
        val expressionType = ParameterizedTypeName.get(className, variableName)
        return MethodSpec.methodBuilder("eval")
                .addTypeVariable(variableName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(enumData.enumType, "value")
                .addParameter(ParameterSpec.builder(expressionType, "delegate").build())
                .addCode(evaluatorCode(enumData.symbols, enumData.enumType))
                .returns(variableName)
                .build();
    }

    private fun evaluatorCode(symbols: List<String>, enumType: ClassName): CodeBlock {
        val objectsClassName = ClassName.get(Objects::class.java)
        return CodeBlock.builder()
                .addStatement("\$T.requireNonNull(value)", objectsClassName)
                .apply {
                    symbols.forEach {
                        beginControlFlow("if (\$T.$it.equals(value))", enumType)
                        addStatement("return delegate.$it()")
                        endControlFlow()
                    }
                }
                .addStatement("throw new IllegalStateException(String.valueOf(value))")
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

    private fun lambdaExhaustive(enumData: EnumData, expressionName: ClassName): MethodSpec {
        val variableName = TypeVariableName.get("W")
        return MethodSpec.methodBuilder("eval")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(variableName)
                .addParameters(toParameters(enumData.symbols, variableName, enumData.enumType))
                .addCode(lambdaCode(enumData.symbols, expressionName, variableName))
                .returns(variableName)
                .build()
    }

    private fun lambdaCode(symbols: List<String>, expressionName: ClassName, variableName: TypeVariableName): CodeBlock {
        return CodeBlock.builder()
                .beginControlFlow("return eval(value, new \$T<\$T>() ", expressionName, variableName)
                .apply {
                    symbols.forEach {
                        addLine("@Override")
                        beginControlFlow("public \$T \$L()", variableName, it)
                                .addStatement("return \$L.get()", it)
                        endControlFlow()
                    }
                }
                .endControlFlow(")")
                .build()
    }

    private fun toParameters(symbols: List<String>, variableName: TypeVariableName, enumType: ClassName): Iterable<ParameterSpec> {
        val supplierType = ParameterizedTypeName.get(ClassName.get(Supplier::class.java), variableName)
        val enumParameter = ParameterSpec.builder(enumType, "value").build()
        return listOf(enumParameter) + symbols.map {
            ParameterSpec.builder(supplierType, it).build()
        }
    }
}

private fun CodeBlock.Builder.addLine(format: String, vararg args: Any): CodeBlock.Builder {
    return this.add("$format${System.lineSeparator()}", args)
}
