/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.libninepatch;

/**
 * Texture renderers are used for drawing platform-specific textures and {@linkplain TextureRegion texture regions}.
 *
 * @param <T> the texture type that this renderer can draw
 * @param <C> the type of context that is needed to draw a texture
 * @since 1.1.0
 * @see TextureRenderer texture renderers that don't need a context object
 */
@FunctionalInterface
public interface ContextualTextureRenderer<T, C> {
    /**
     * Draws rectangular region of a texture.
     *
     * @param texture the texture
     * @param context the context needed to draw the texture
     * @param x       the leftmost X coordinate of the target drawing region
     * @param y       the topmost Y coordinate of the target drawing region
     * @param width   the width of the target region
     * @param height  the height of the target region
     * @param u1      the left edge of the input region as a fraction from 0 to 1
     * @param v1      the top edge of the input region as a fraction from 0 to 1
     * @param u2      the right edge of the input region as a fraction from 0 to 1
     * @param v2      the bottom edge of the input region as a fraction from 0 to 1
     */
    void draw(T texture, C context, int x, int y, int width, int height, float u1, float v1, float u2, float v2);

    /**
     * Draws rectangular subregion of a texture region.
     *
     * @param region  the texture region
     * @param context the context needed to draw the texture
     * @param x       the leftmost X coordinate of the target drawing region
     * @param y       the topmost Y coordinate of the target drawing region
     * @param width   the width of the target region
     * @param height  the height of the target region
     * @param u1      the left edge of the input region as a fraction from 0 to 1
     * @param v1      the top edge of the input region as a fraction from 0 to 1
     * @param u2      the right edge of the input region as a fraction from 0 to 1
     * @param v2      the bottom edge of the input region as a fraction from 0 to 1
     */
    default void draw(TextureRegion<? extends T> region, C context, int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
        T texture = region.texture;
        u1 = NinePatch.lerp(u1, region.u1, region.u2);
        u2 = NinePatch.lerp(u2, region.u1, region.u2);
        v1 = NinePatch.lerp(v1, region.v1, region.v2);
        v2 = NinePatch.lerp(v2, region.v1, region.v2);
        draw(texture, context, x, y, width, height, u1, v1, u2, v2);
    }
}
