object Versions {
    val kotlin = "1.4.10"
}

object Dependencies {
    val kotlinPlatform = "org.jetbrains.kotlin:kotlin-bom:${Versions.kotlin}"
    val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val javaPoet = "com.squareup:javapoet:1.13.0"
}

object Projects {
    const val baseGroupId = "io.github.pak3nuh.util.lang.compiler"
}
