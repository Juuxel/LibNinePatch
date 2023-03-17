/**
 * A simple 9-patch rectangle renderer for Java that works on any graphics API.
 *
 * <p>AWT support is bundled out of the box as {@link juuxel.libninepatch.AwtTextureRenderer AwtTextureRenderer}.
 * Other renderers are easy to implement: they only need
 * {@linkplain juuxel.libninepatch.ContextualTextureRenderer#draw(Object, Object, int, int, int, int, float, float, float, float)
 * one method} to render a platform-specific texture type, such as
 * {@link java.awt.image.BufferedImage BufferedImage} in AWT's case.
 */
module io.github.juuxel.libninepatch {
    requires java.desktop;
    requires static org.jetbrains.annotations;

    exports juuxel.libninepatch;
}
