package io.github.pak3nuh.util.processor

import javax.annotation.processing.Filer

interface FileWriteable {
    fun writeTo(filer: Filer)
}