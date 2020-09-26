package io.github.pak3nuh.util.lang.sealed.processor

import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.Manifest
import javax.annotation.processing.Filer
import javax.tools.FileObject
import javax.tools.StandardLocation

class ManifestModifier(val filer: Filer) {

    private fun getResourceObject(filer: Filer): FileObject {
        val location = StandardLocation.SOURCE_OUTPUT
        val existingManifest: FileObject = filer.getResource(location, "", "META-INF/MANIFEST.MF")

        return if (Files.exists(Paths.get(existingManifest.toUri()))) {
            log("Manifest exists")
            existingManifest
        } else {
            log("Manifest doesn't exist")
            filer.createResource(location, "", "META-INF/MANIFEST.MF")
        }
    }

    private fun sealPackage(packageName: String, sealed: Boolean, manifest: Manifest) {
        val attributes = Attributes().apply {
            putValue("Sealed", sealed.toString())
        }
        manifest.entries[packageName.replace('.','/')] = attributes
    }

    fun sealPackages(roundData: RoundData) {
        val manifest = Manifest()
        roundData.packages.forEach {
            sealPackage(it.packageName, it.sealed, manifest)
        }
        val resourceObject = getResourceObject(filer)
        resourceObject.openOutputStream().use {
            manifest.write(it)
        }
    }
}

private fun log(msg: String) = println(msg)
