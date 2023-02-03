/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.libninepatch;

/**
 * Texture renderers are used for drawing platform-specific textures.
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
     * Draws a tiled rectangular region of a texture.
     *
     * @param texture            the texture
     * @param context            the context needed to draw the texture
     * @param x                  the leftmost X coordinate of the target drawing region
     * @param y                  the topmost Y coordinate of the target drawing region
     * @param width              the width of the target region
     * @param height             the height of the target region
     * @param tileWidth          the width of each tile
     * @param tileHeight         the height of each tile
     * @since 2.0.0
     */
    default void drawTiled(T texture, C context, int x, int y, int width, int height, int tileWidth, int tileHeight) {
        float numHorizontalTiles = width / ((float) tileWidth);
        float numVerticalTiles = height / ((float) tileHeight);
        int numFullHorizontalTiles = Math.round(numHorizontalTiles);
        int numFullVerticalTiles = Math.round(numVerticalTiles);

        for (int i = 0; i < numFullHorizontalTiles; i++) {
            for (int j = 0; j < numFullVerticalTiles; j++) {
                draw(texture, context, x + (tileWidth * i), y + (tileHeight * j), tileWidth, tileHeight, 0, 0, 1, 1);
            }
        }

        int tiledHorizontalSpace = tileWidth * numFullHorizontalTiles;
        int missingFractionalXTiling = width - tiledHorizontalSpace;
        if (missingFractionalXTiling > 0) {
            for (int j = 0; j < numFullVerticalTiles; j++) {
                draw(texture, context, x + tiledHorizontalSpace, y + (tileHeight * j), missingFractionalXTiling, tileHeight, 0, 0, 1, 1);
            }
        }

        int tiledVerticalSpace = tileHeight * numFullVerticalTiles;
        int missingFractionalYTiling = height - tiledVerticalSpace;
        if (missingFractionalYTiling > 0) {
            for(int i = 0; i < numFullHorizontalTiles; i++) {
                draw(texture, context, x + (tileWidth * i), y + tiledVerticalSpace, tileWidth, missingFractionalYTiling, 0, 0, 1, 1);
            }
        }

        if (missingFractionalXTiling > 0 && missingFractionalYTiling > 0) {
            draw(texture, context, x + tiledHorizontalSpace, y + tiledVerticalSpace, missingFractionalXTiling, missingFractionalYTiling, 0, 0, 1, 1);
        }
    }
}
