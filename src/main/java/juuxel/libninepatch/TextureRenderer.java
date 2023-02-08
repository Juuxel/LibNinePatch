/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.libninepatch;

import org.jetbrains.annotations.Nullable;

/**
 * Texture renderers are used for drawing platform-specific textures and {@linkplain TextureRegion texture regions}.
 *
 * <p>{@code TextureRenderer} is a special variant of {@link ContextualTextureRenderer} that does not need
 * a context object. You should pass {@code null} as the context object.
 *
 * @param <T> the texture type that this renderer can draw
 */
@FunctionalInterface
public interface TextureRenderer<T> extends ContextualTextureRenderer<T, @Nullable Void> {
    /**
     * A debugging texture renderer that prints out the data to {@link System#out}.
     */
    TextureRenderer<Object> STDOUT_DEBUG = (texture, x, y, width, height, u1, v1, u2, v2) -> {
        System.out.printf("Drawing %s at (%d, %d), sized (%d, %d), UVs from (%f, %f) to (%f, %f)%n", texture, x, y, width, height, u1, v1, u2, v2);
    };

    /**
     * Composes multiple texture renderers into one.
     *
     * @param renderers the renderers
     * @param <T> the texture type that the resulting renderer will accept
     * @return the combined renderer
     */
    @SafeVarargs
    static <T> TextureRenderer<T> compose(TextureRenderer<? super T>... renderers) {
        return (texture, x, y, width, height, u1, v1, u2, v2) -> {
            for (TextureRenderer<? super T> renderer : renderers) {
                renderer.draw(texture, x, y, width, height, u1, v1, u2, v2);
            }
        };
    }

    @Override
    default void draw(T texture, @Nullable Void context, int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
        draw(texture, x, y, width, height, u1, v1, u2, v2);
    }

    /**
     * Draws rectangular region of a texture.
     *
     * @param texture the texture
     * @param x       the leftmost X coordinate of the target drawing region
     * @param y       the topmost Y coordinate of the target drawing region
     * @param width   the width of the target region
     * @param height  the height of the target region
     * @param u1      the left edge of the input region as a fraction from 0 to 1
     * @param v1      the top edge of the input region as a fraction from 0 to 1
     * @param u2      the right edge of the input region as a fraction from 0 to 1
     * @param v2      the bottom edge of the input region as a fraction from 0 to 1
     */
    void draw(T texture, int x, int y, int width, int height, float u1, float v1, float u2, float v2);

    /**
     * Draws rectangular subregion of a texture region.
     *
     * @param region  the texture region
     * @param x       the leftmost X coordinate of the target drawing region
     * @param y       the topmost Y coordinate of the target drawing region
     * @param width   the width of the target region
     * @param height  the height of the target region
     * @param u1      the left edge of the input region as a fraction from 0 to 1
     * @param v1      the top edge of the input region as a fraction from 0 to 1
     * @param u2      the right edge of the input region as a fraction from 0 to 1
     * @param v2      the bottom edge of the input region as a fraction from 0 to 1
     */
    default void draw(TextureRegion<? extends T> region, int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
        T texture = region.texture;
        u1 = NinePatch.lerp(u1, region.u1, region.u2);
        u2 = NinePatch.lerp(u2, region.u1, region.u2);
        v1 = NinePatch.lerp(v1, region.v1, region.v2);
        v2 = NinePatch.lerp(v2, region.v1, region.v2);
        draw(texture, x, y, width, height, u1, v1, u2, v2);
    }
}
