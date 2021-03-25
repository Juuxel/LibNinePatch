/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.libninepatch;

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
    private final TextureRegion<T> texture;
    private final int cornerWidth, cornerHeight;
    private final float cornerU, cornerV;
    private final @Nullable Integer tileWidth;
    private final @Nullable Integer tileHeight;
    private final Mode mode;

    NinePatch(Builder<T> builder) {
        this.texture = builder.texture;
        this.cornerWidth = builder.cornerWidth;
        this.cornerHeight = builder.cornerHeight;
        this.cornerU = builder.cornerU;
        this.cornerV = builder.cornerV;
        this.tileWidth = builder.tileWidth;
        this.tileHeight = builder.tileHeight;
        this.mode = builder.mode;

        if (mode == Mode.TILING && (cornerWidth == 0 || cornerHeight == 0) && (tileWidth == null || tileHeight == null)) {
            throw new IllegalArgumentException("Tile size must be specified when corner size is 0");
        }
    }

    /**
     * Draws a nine-patch region using the renderer.
     *
     * @param renderer the renderer for drawing the texture
     * @param width    the width of the target region
     * @param height   the height of the target region
     */
    public void draw(TextureRenderer<? super T> renderer, int width, int height) {
        if (mode == Mode.TILING) {
            drawTiling(renderer, width, height);
        } else {
            drawStretching(renderer, width, height);
        }

        drawCorners(renderer, width, height);
    }

    private boolean hasCorners() {
        return cornerWidth > 0 && cornerHeight > 0 && cornerU > 0 & cornerV > 0;
    }

    private void drawCorners(TextureRenderer<? super T> renderer, int width, int height) {
        if (!hasCorners()) return;

        renderer.draw(texture, 0, 0, cornerWidth, cornerHeight, 0, 0, cornerU, cornerV);
        renderer.draw(texture, width - cornerWidth, 0, cornerWidth, cornerHeight, 1 - cornerU, 0, 1, cornerV);
        renderer.draw(texture, 0, height - cornerHeight, cornerWidth, cornerHeight, 0, 1 - cornerV, cornerU, 1);
        renderer.draw(texture, width - cornerWidth, height - cornerHeight, cornerWidth, cornerHeight, 1 - cornerU, 1 - cornerV, 1, 1);
    }

    private void drawTiling(TextureRenderer<? super T> renderer, int width, int height) {
        float u1 = cornerU, v1 = cornerV;
        float u2 = 1 - cornerU, v2 = 1 - cornerV;
        int tileWidth = this.tileWidth == null ? (int) (cornerWidth / cornerU * (u2 - u1)) : this.tileWidth;
        int tileHeight = this.tileHeight == null ? (int) (cornerHeight / cornerV * (v2 - v1)) : this.tileHeight;

        // Middle
        {
            int widthRemaining = width - 2 * cornerWidth;
            int x = cornerWidth;

            while (widthRemaining > 0) {
                int tw = Math.min(widthRemaining, tileWidth);
                widthRemaining -= tw;
                float tu2 = tw == tileWidth ? u2 : lerp((float) tw / (float) tileWidth, u1, u2);

                int heightRemaining = height - 2 * cornerHeight;
                int y = cornerHeight;

                while (heightRemaining > 0) {
                    int th = Math.min(heightRemaining, tileHeight);
                    heightRemaining -= th;
                    float tv2 = th == tileHeight ? v2 : lerp((float) th / (float) tileHeight, v1, v2);

                    renderer.draw(texture, x, y, tw, th, u1, v1, tu2, tv2);

                    y += th;
                }

                x += tw;
            }
        }

        if (hasCorners()) {
            // draw edges

            // vertical
            {
                int heightRemaining = height - 2 * cornerHeight;
                int y = cornerHeight;

                while (heightRemaining > 0) {
                    int th = Math.min(heightRemaining, tileHeight);
                    heightRemaining -= th;
                    float tv2 = th == tileHeight ? v2 : lerp((float) th / (float) tileHeight, v1, v2);

                    renderer.draw(texture, 0, y, cornerWidth, th, 0, v1, cornerU, tv2);
                    renderer.draw(texture, width - cornerWidth, y, cornerWidth, th, 1 - cornerU, v1, 1, tv2);

                    y += th;
                }
            }

            // Horizontal
            {
                int widthRemaining = width - 2 * cornerWidth;
                int x = cornerWidth;

                while (widthRemaining > 0) {
                    int tw = Math.min(widthRemaining, tileWidth);
                    widthRemaining -= tw;
                    float tu2 = tw == tileWidth ? u2 : lerp((float) tw / (float) tileWidth, u1, u2);

                    renderer.draw(texture, x, 0, tw, cornerHeight, u1, 0, tu2, cornerV);
                    renderer.draw(texture, x, height - cornerHeight, tw, cornerHeight, u1, 1 - cornerV, tu2, 1);

                    x += tw;
                }
            }
        }
    }

    private void drawStretching(TextureRenderer<? super T> renderer, int width, int height) {
        int w = width - 2 * cornerWidth;
        int h = height - 2 * cornerHeight;
        float u = cornerU;
        float v = cornerV;

        if (hasCorners()) {
            /* top   */ renderer.draw(texture, cornerWidth, 0, w, cornerHeight, u, 0, 1 - u, v);
            /* left  */ renderer.draw(texture, 0, cornerHeight, cornerWidth, h, 0, v, u, 1 - v);
            /* down  */ renderer.draw(texture, cornerWidth, height - cornerHeight, w, cornerHeight, u, 1 - v, 1 - u, 1);
            /* right */ renderer.draw(texture, width - cornerWidth, cornerHeight, cornerWidth, h, 1 - u, v, 1, 1 - v);
        }

        renderer.draw(texture, cornerWidth, cornerHeight, w, h, u, v, 1 - u, 1 - v);
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
     * @param texture the texture
     * @param <T>     the texture type
     * @return a nine-patch renderer builder using the texture
     */
    public static <T> Builder<T> builder(T texture) {
        return builder(new TextureRegion<>(texture, 0, 0, 1, 1));
    }

    /**
     * Creates a {@link Builder} using a texture region.
     *
     * @param texture the texture region
     * @param <T>     the texture type
     * @return a nine-patch renderer builder using the texture region
     * @throws NullPointerException if the texture region is null
     */
    public static <T> Builder<T> builder(TextureRegion<T> texture) {
        return new Builder<>(texture);
    }

    /**
     * The rendering mode of a nine-patch renderer.
     */
    public enum Mode {
        /** Regions of the texture are tiled to fill areas. The default value. */
        TILING,
        /** Regions of the texture are stretched to fill areas. */
        STRETCHING
    }

    /**
     * A builder for {@link NinePatch}.
     *
     * @param <T> the texture type
     * @see NinePatch#builder(Object)
     * @see NinePatch#builder(TextureRegion)
     */
    public static final class Builder<T> {
        private final TextureRegion<T> texture;
        private int cornerWidth = 0;
        private int cornerHeight = 0;
        private float cornerU = 0f;
        private float cornerV = 0f;
        private @Nullable Integer tileWidth = null;
        private @Nullable Integer tileHeight = null;
        private Mode mode = Mode.TILING;

        private Builder(TextureRegion<T> texture) {
            this.texture = Objects.requireNonNull(texture, "texture");
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
         * Sets the corner UV sizes in the texture.
         *
         * @param u the width of the corner in the texture as a fraction from 0 to 1
         * @param v the height of the corner in the texture as a fraction from 0 to 1
         * @return this builder
         */
        public Builder<T> cornerUv(float u, float v) {
            cornerU = u;
            cornerV = v;
            return this;
        }

        /**
         * Sets the corner UV size in the texture.
         *
         * @param uv the width and height of the corner in the texture as a fraction from 0 to 1
         * @return this builder
         */
        public Builder<T> cornerUv(float uv) {
            return cornerUv(uv, uv);
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
