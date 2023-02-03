/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.libninepatch;

import java.lang.reflect.Array;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A nine-patch renderer.
 *
 * <p>New {@code NinePatch} instances are creating using {@link NinePatch.Builder}.
 *
 * @param <T> the texture type
 */
public final class NinePatch<T> {
    private final T[][] ninePatchTextures;
    private final int cornerWidth, cornerHeight;
    private final @Nullable Integer tileWidth;
    private final @Nullable Integer tileHeight;
    private final Mode mode;

    NinePatch(Builder<T> builder) {
        this.ninePatchTextures = builder.textures;
        this.cornerWidth = builder.cornerWidth;
        this.cornerHeight = builder.cornerHeight;
        this.tileWidth = builder.tileWidth;
        this.tileHeight = builder.tileHeight;
        this.mode = builder.mode;

        if (mode == Mode.TILING && (tileWidth == null || tileHeight == null)) {
            throw new IllegalArgumentException("Tile size must be specified when for Tiling-Mode.");
        }
    }

    /**
     * Draws a nine-patch region using a contextual texture renderer.
     *
     * @param renderer the renderer for drawing the texture
     * @param context  the context used for drawing the texture
     * @param width    the width of the target region
     * @param height   the height of the target region
     * @param <C>      the type of context that is needed to draw a texture
     */
    public <C> void draw(ContextualTextureRenderer<? super T, C> renderer, C context, int width, int height) {
        if (mode == Mode.TILING) {
            drawTiling(renderer, context, width, height);
        } else {
            drawStretching(renderer, context, width, height);
        }

        drawCorners(renderer, context, width, height);
    }

    /**
     * Draws a nine-patch region using a non-contextual texture renderer.
     *
     * @param renderer the renderer for drawing the texture
     * @param width    the width of the target region
     * @param height   the height of the target region
     */
    public void draw(TextureRenderer<? super T> renderer, int width, int height) {
        draw(renderer, null, width, height);
    }

    private boolean hasCorners() {
        return cornerWidth > 0 && cornerHeight > 0;
    }

    private <C> void drawCorners(ContextualTextureRenderer<? super T, C> renderer, C context, int width, int height) {
        if (!hasCorners()) return;

        renderer.draw(ninePatchTextures[0][0], context, 0, 0, cornerWidth, cornerHeight, 0, 0, 1, 1);
        renderer.draw(ninePatchTextures[0][2], context, width - cornerWidth, 0, cornerWidth, cornerHeight, 0, 0, 1, 1);
        renderer.draw(ninePatchTextures[2][0], context, 0, height - cornerHeight, cornerWidth, cornerHeight, 0, 0, 1, 1);
        renderer.draw(ninePatchTextures[2][2], context, width - cornerWidth, height - cornerHeight, cornerWidth, cornerHeight, 0, 0, 1, 1);
    }

    private <C> void drawTiling(ContextualTextureRenderer<? super T, C> renderer, C context, int width, int height) {
        assert this.tileWidth != null;
        assert this.tileHeight != null;

        int centerWidth = width - (2 * cornerWidth);
        int centerHeight = height - (2 * cornerHeight);

        renderer.drawTiled(ninePatchTextures[1][1], context, cornerWidth, cornerHeight, centerWidth, centerHeight, tileWidth, tileHeight);

        if (hasCorners()) {
            // horizontal
            renderer.drawTiled(ninePatchTextures[0][1], context, cornerWidth, 0, centerWidth, cornerHeight, tileWidth, cornerHeight);
            renderer.drawTiled(ninePatchTextures[2][1], context, cornerWidth, height - cornerHeight, centerWidth, cornerHeight, tileWidth, cornerHeight);

            // vertical
            renderer.drawTiled(ninePatchTextures[1][0], context, 0, cornerHeight, cornerWidth, centerHeight, cornerWidth, tileHeight);
            renderer.drawTiled(ninePatchTextures[1][2], context, width - cornerWidth, cornerHeight, cornerWidth, centerHeight, cornerWidth, tileHeight);
        }
    }

    private <C> void drawStretching(ContextualTextureRenderer<? super T, C> renderer, C context, int width, int height) {
        int w = width - 2 * cornerWidth;
        int h = height - 2 * cornerHeight;

        if (hasCorners()) {
            /* top   */
            renderer.draw(ninePatchTextures[0][1], context, cornerWidth, 0, w, cornerHeight, 0, 0, 1, 1);
            /* left  */
            renderer.draw(ninePatchTextures[1][0], context, 0, cornerHeight, cornerWidth, h, 0, 0, 1, 1);
            /* down  */
            renderer.draw(ninePatchTextures[2][1], context, cornerWidth, height - cornerHeight, w, cornerHeight, 0, 0, 1, 1);
            /* right */
            renderer.draw(ninePatchTextures[1][2], context, width - cornerWidth, cornerHeight, cornerWidth, h, 0, 0, 1, 1);
        }

        renderer.draw(ninePatchTextures[1][1], context, cornerWidth, cornerHeight, w, h, 0, 0, 1, 1);
    }

    /**
     * Linearly interpolates a value between two endpoints.
     *
     * @param delta the progress from {@code a} to {@code b}
     * @param a     the starting point
     * @param b     the ending point
     * @return the interpolated value
     */
    static float lerp(float delta, float a, float b) {
        return a + delta * (b - a);
    }

    /**
     * Creates a {@link Builder} using a full texture.
     *
     * @param <T> the texture type
     * @return a nine-patch renderer builder using the texture
     */
    public static <T> Builder<T> builder(Class<T> textureClass,
                                         T topLeft, T topCenter, T topRight,
                                         T centerLeft, T centerCenter, T centerRight,
                                         T bottomLeft, T bottomCenter, T bottomRight) {
        return new Builder<>(textureClass, topLeft, topCenter, topRight, centerLeft, centerCenter, centerRight, bottomLeft, bottomCenter, bottomRight);
    }

    /**
     * The rendering mode of a nine-patch renderer.
     */
    public enum Mode {
        /**
         * Regions of the texture are tiled to fill areas. The default value.
         */
        TILING,
        /**
         * Regions of the texture are stretched to fill areas.
         */
        STRETCHING
    }

    /**
     * A builder for {@link NinePatch}.
     *
     * @param <T> the texture type
     * @see NinePatch#builder(Class, Object, Object, Object, Object, Object, Object, Object, Object, Object)
     */
    public static final class Builder<T> {
        private final T[][] textures;
        private int cornerWidth = 0;
        private int cornerHeight = 0;
        private @Nullable Integer tileWidth = null;
        private @Nullable Integer tileHeight = null;
        private Mode mode = Mode.TILING;

        private Builder(Class<T> textureClass,
                        T topLeft, T topCenter, T topRight,
                        T centerLeft, T centerCenter, T centerRight,
                        T bottomLeft, T bottomCenter, T bottomRight) {
            //noinspection unchecked
            this.textures = (T[][]) Array.newInstance(textureClass, 3, 3);

            textures[0][0] = Objects.requireNonNull(topLeft, "topLeft");
            textures[0][1] = Objects.requireNonNull(topCenter, "topCenter");
            textures[0][2] = Objects.requireNonNull(topRight, "topRight");
            textures[1][0] = Objects.requireNonNull(centerLeft, "centerLeft");
            textures[1][1] = Objects.requireNonNull(centerCenter, "centerCenter");
            textures[1][2] = Objects.requireNonNull(centerRight, "centerRight");
            textures[2][0] = Objects.requireNonNull(bottomLeft, "bottomLeft");
            textures[2][1] = Objects.requireNonNull(bottomCenter, "bottomCenter");
            textures[2][2] = Objects.requireNonNull(bottomRight, "bottomRight");
        }

        /**
         * Sets the corner size of the rendered rectangle.
         *
         * @param width  the corner width
         * @param height the corner height
         * @return this builder
         */
        public Builder<T> cornerSize(int width, int height) {
            cornerWidth = width;
            cornerHeight = height;
            return this;
        }

        /**
         * Sets the corner size of the rendered rectangle.
         *
         * @param size the corner width and height
         * @return this builder
         */
        public Builder<T> cornerSize(int size) {
            return cornerSize(size, size);
        }

        /**
         * Sets the tile size of the rendered texture when the mode is {@link Mode#TILING}.
         *
         * @param width  the tile width
         * @param height the tile height
         * @return this builder
         */
        public Builder<T> tileSize(int width, int height) {
            tileWidth = width;
            tileHeight = height;
            return this;
        }

        /**
         * Sets the tile size of the rendered texture when the mode is {@link Mode#TILING}.
         *
         * @param size the tile width and height
         * @return this builder
         */
        public Builder<T> tileSize(int size) {
            return tileSize(size, size);
        }

        /**
         * Sets the rendering mode of this builder.
         *
         * @param mode the new rendering mode
         * @return this builder
         */
        public Builder<T> mode(Mode mode) {
            this.mode = Objects.requireNonNull(mode, "mode");
            return this;
        }

        /**
         * Creates a new {@link NinePatch} from this builder.
         *
         * @return the created nine-patch renderer
         */
        public NinePatch<T> build() {
            return new NinePatch<>(this);
        }
    }
}
