package io.github.pak3nuh.util.processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import javax.lang.model.element.Modifier

class ExpressionBuilder(
        private val inputType: TypeName,
        private val packageElement: String
) {

    private val outputType: TypeVariableName = TypeVariableName.get("T")
    private val map = mutableMapOf<String, TypeName>()
    private var definitionList: List<Definition>? = null

    /**
     * Unique branch name because we cannot implement the same method name on the builder with an erased
     * function type.
     */
    fun addBranch(branchName: String, inputType: TypeName) = apply {
        map.compute(branchName) { key, value ->
            check(value == null) { "Value for $key already defined $value" }
            inputType
        }
    }

    val builderName = childClassName("ExpressionBuilder")

    private fun getDefinitionsList(): List<Definition> = definitionList
            ?: createDefinitionList().also {
                definitionList = it
            }

    private fun createDefinitionList(): List<Definition> {
        val sortedDefinitions = map.entries.sortedBy { it.key }
                .mapIndexed { idx, entry ->
                    val interfaceName = childClassName("Builder$idx")
                    Definition(interfaceName, entry.key, entry.value)
                }
                .plus(Definition(ClassName.get(packageElement, "Evaluator"), "evaluator", inputType))

        sortedDefinitions.reduce { acc, definition ->
            acc.nextDefinition = definition
            definition
        }

        return sortedDefinitions
    }

    fun buildInterfaces(): List<TypeSpec> {
        return getDefinitionsList().map { it.buildInterface(outputType) }
    }

    fun buildBuilder(expressionTypeName: TypeName, evalMethodName: String): TypeSpec {
        val builder = TypeSpec.classBuilder(builderName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(outputType)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .addMethod(buildCreateMethod())

        getDefinitionsList().onEach {
            builder.addSuperinterface(ParameterizedTypeName.get(it.interfaceTypeName.simpleClassName(), outputType))
            builder.addMethod(it.buildMethodImpl(outputType))
            builder.addField(it.buildField())
        }

        builder.addMethod(buildEvalMethod(expressionTypeName, evalMethodName))
        return builder.build()
    }

    private fun buildCreateMethod(): MethodSpec? {
        val methodVariable = TypeVariableName.get("Y")
        val firstInterfaceName = getDefinitionsList().first().interfaceTypeName.simpleClassName()
        val returnType = ParameterizedTypeName.get(firstInterfaceName, methodVariable)
        return MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.STATIC)
                .returns(returnType)
                .addTypeVariable(methodVariable)
                .addStatement("return new \$L()", builderName.simpleName())
                .build()
    }

    private fun buildEvalMethod(expressionTypeName: TypeName, methodName: String): MethodSpec {
        val inputDefinition = getDefinitionsList().last()
        val arguments = getDefinitionsList().minus(inputDefinition).map { it.branchName }.joinToString()

        return MethodSpec.methodBuilder("evaluate")
                .addModifiers(Modifier.PUBLIC)
                .returns(outputType)
                .addStatement("return \$T.\$L(\$L, \$L)", expressionTypeName, methodName, inputDefinition.branchName, arguments)
                .build()
    }

    private fun childClassName(simpleName: String) = ClassName.get(packageElement, simpleName)

    private class Definition(val interfaceTypeName: ClassName, val branchName: String, val methodInput: TypeName) {
        var nextDefinition: Definition? = null

        fun nextReturnType(outputType: TypeName): TypeName {
            return nextDefinition
                    ?.let {
                        ParameterizedTypeName.get(it.interfaceTypeName.simpleClassName(), outputType)
                    }
                    ?: outputType
        }

        fun buildInterface(outputType: TypeName): TypeSpec {
            val outputVariable = TypeVariableName.get("T")
            return TypeSpec.interfaceBuilder(interfaceTypeName.simpleName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addTypeVariable(outputVariable)
                    .addMethod(buildAbstractMethod(outputType))
                    .build()
        }

        fun buildAbstractMethod(outputType: TypeName): MethodSpec {
            return MethodSpec.methodBuilder(branchName)
                    .addParameter(methodInput, branchName)
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(nextReturnType(outputType))
                    .build()
        }

        fun buildMethodImpl(outputType: TypeName): MethodSpec {
            return if (nextDefinition == null) {
                buildMethodImplFinal(outputType)
            } else {
                buildMethodImplIntermediate(outputType)
            }
        }

        private fun buildMethodImplIntermediate(outputType: TypeName): MethodSpec {
            return MethodSpec.methodBuilder(branchName)
                    .addParameter(methodInput, branchName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(nextReturnType(outputType))
                    .addAnnotation(Override::class.java)
                    .addStatement("this.\$L = \$L", branchName, branchName)
                    .addStatement("return this")
                    .build()
        }

        private fun buildMethodImplFinal(outputType: TypeName): MethodSpec {
            return MethodSpec.methodBuilder(branchName)
                    .addParameter(methodInput, branchName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(nextReturnType(outputType))
                    .addAnnotation(Override::class.java)
                    .addStatement("this.\$L = \$L", branchName, branchName)
                    .addStatement("return evaluate()")
                    .build()
        }

        fun buildField(): FieldSpec {
            return FieldSpec.builder(methodInput, branchName).build()
        }
    }

}

private fun ClassName.simpleClassName() = ClassName.get("", this.simpleName())
