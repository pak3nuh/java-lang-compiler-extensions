package io.github.pak3nuh.util.processor

import com.squareup.javapoet.*
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror

class TypeNameReifier(private val element: TypeName, private val parameterTypes: Map<TypeName, TypeName>) {

    constructor(element: TypeMirror, parameterTypes: List<Pair<TypeParameterElement, TypeMirror>>) :
            this(TypeName.get(element), parameterTypes
                    .associate { Pair(TypeName.get(it.first.asType()), TypeName.get(it.second)) })

    fun reify(): TypeName {
        return reifyType(element)
    }

    private fun reifyType(type: TypeName): TypeName {
        return when (type) {
            is ArrayTypeName -> reifyArray(type)
            is WildcardTypeName -> reifyWildcard(type)
            is ParameterizedTypeName -> reifyParameterized(type)
            is TypeVariableName -> parameterTypes[type] ?: type
            is TypeName, is ClassName -> type
            else -> error("Unsupported type")
        }
    }

    private fun reifyParameterized(elementAsType: ParameterizedTypeName): ParameterizedTypeName {
        return ParameterizedTypeName.get(elementAsType.rawType, *elementAsType.typeArguments.map { reifyType(it) }.toTypedArray())
    }

    private fun reifyWildcard(elementAsType: WildcardTypeName): WildcardTypeName {
        // javapoet doesn't implement union types, so it is assumed that bound can only have one type
        return if(elementAsType.lowerBounds.isNotEmpty()) {
            WildcardTypeName.supertypeOf(reifyType(elementAsType.lowerBounds[0]))
        } else {
            WildcardTypeName.subtypeOf(reifyType(elementAsType.upperBounds[0]))
        }
    }

    private fun reifyArray(elementAsType: ArrayTypeName): ArrayTypeName {
        val componentType: TypeName = elementAsType.componentType
        return ArrayTypeName.of(reifyType(componentType))
    }

}