# LibNinePatch

A simple 9-patch rectangle renderer for Java
that works on any graphics API.

AWT support is bundled out of the box as `AwtTextureRenderer`.
Other renderers are easy to implement: they only need one method to
render a platform-specific texture type, such as `BufferedImage` in AWT's case.

Get it on Maven Central:
```kotlin
dependencies {
    implementation("io.github.juuxel:libninepatch:1.2.0")
}
```
