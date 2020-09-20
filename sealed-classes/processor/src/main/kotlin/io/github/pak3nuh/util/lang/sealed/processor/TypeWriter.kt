package io.github.pak3nuh.util.lang.sealed.processor

import javax.annotation.processing.Filer

class TypeWriter(val filer: Filer) {
    fun write(roundData: RoundData) {
        println(roundData)
    }
}
