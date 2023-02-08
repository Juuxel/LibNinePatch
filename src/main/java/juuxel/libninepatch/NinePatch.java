/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.libninepatch;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
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
    private final @NotNull T textureCenter;
    private final @Nullable CornerInformation<T> cornerInformation;
    private final @Nullable TileInformation tileInformation;

    NinePatch(@NotNull T textureCenter, @Nullable CornerInformation<T> cornerInformation, @Nullable TileInformation tileInformation) {
        this.textureCenter = textureCenter;
        this.cornerInformation = cornerInformation;
        this.tileInformation = tileInformation;
    }

    /**
     * Draws a nine-patch region using a contextual texture renderer.
     *
     * @param renderer the renderer for drawing the texture
     * @param context  the context used for drawing the texture
     * @param width    the width of the target region
     * @param height   the height of the target region
     * @param <C> the type of context that is needed to draw a texture
     */
    public <C> void draw(ContextualTextureRenderer<? super T, C> renderer, C context, int width, int height) {
        if (tileInformation != null) {
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
        return cornerInformation != null;
    }

    private <C> void drawCorners(ContextualTextureRenderer<? super T, C> renderer, C context, int width, int height) {
        if (!hasCorners()) return;

        int cornerWidth = cornerInformation.cornerWidth;
        int cornerHeight = cornerInformation.cornerHeight;
        renderer.draw(cornerInformation.topLeft, context, 0, 0, cornerWidth, cornerHeight);
        renderer.draw(cornerInformation.topRight, context, width - cornerWidth, 0, cornerWidth, cornerHeight);
        renderer.draw(cornerInformation.bottomLeft, context, 0, height - cornerHeight, cornerWidth, cornerHeight);
        renderer.draw(cornerInformation.bottomRight, context, width - cornerWidth, height - cornerHeight, cornerWidth, cornerHeight);
    }

    private <C> void drawTiling(ContextualTextureRenderer<? super T, C> renderer, C context, int width, int height) {
        assert tileInformation != null;

        int cornerWidth = cornerInformation != null ? cornerInformation.cornerWidth : 0;
        int cornerHeight = cornerInformation != null ? cornerInformation.cornerHeight : 0;
        int centerWidth = width - (2 * cornerWidth);
        int centerHeight = height - (2 * cornerHeight);
        int tileWidth = tileInformation.tileWidth;
        int tileHeight = tileInformation.tileHeight;

        renderer.drawTiled(textureCenter, context, cornerWidth, cornerHeight, centerWidth, centerHeight, tileWidth, tileHeight);

        if (hasCorners()) {
            /* top   */ renderer.drawTiled(cornerInformation.topCenter, context, cornerWidth, 0, centerWidth, cornerHeight, tileWidth, cornerHeight);
            /* left  */ renderer.drawTiled(cornerInformation.centerLeft, context, 0, cornerHeight, cornerWidth, centerHeight, cornerWidth, tileHeight);
            /* down  */ renderer.drawTiled(cornerInformation.bottomCenter, context, cornerWidth, height - cornerHeight, centerWidth, cornerHeight, tileWidth, cornerHeight);
            /* right */ renderer.drawTiled(cornerInformation.centerRight, context, width - cornerWidth, cornerHeight, cornerWidth, centerHeight, cornerWidth, tileHeight);
        }
    }

    private <C> void drawStretching(ContextualTextureRenderer<? super T, C> renderer, C context, int width, int height) {
        int cornerWidth = cornerInformation != null ? cornerInformation.cornerWidth : 0;
        int cornerHeight = cornerInformation != null ? cornerInformation.cornerHeight : 0;
        int centerWidth = width - 2 * cornerWidth;
        int centerHeight = height - 2 * cornerHeight;

        renderer.draw(textureCenter, context, cornerWidth, cornerHeight, centerWidth, centerHeight);

        if (hasCorners()) {
            /* top   */ renderer.draw(cornerInformation.topCenter, context, cornerWidth, 0, centerWidth, cornerHeight);
            /* left  */ renderer.draw(cornerInformation.centerLeft, context, 0, cornerHeight, cornerWidth, centerHeight);
            /* down  */ renderer.draw(cornerInformation.bottomCenter, context, cornerWidth, height - cornerHeight, centerWidth, cornerHeight);
            /* right */ renderer.draw(cornerInformation.centerRight, context, width - cornerWidth, cornerHeight, cornerWidth, centerHeight);
        }
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
         * @param textureAllocator a function that creates a subsection of a texture.
         * @return the created nine-patch renderer
         */
        public <TextureType> NinePatch<TextureType> build(Function<TextureRegion<T>, TextureType> textureAllocator) {
            TextureType centerTexture = textureAllocator.apply(computeSubRegion(cornerU, cornerV, 1 - cornerU, 1 - cornerV));

            CornerInformation<TextureType> cornerInformation = null;
            if (cornerWidth != 0 && cornerHeight != 0) {
                cornerInformation = new CornerInformation<>(
                    textureAllocator.apply(computeSubRegion(0, 0, cornerU, cornerV)),
                    textureAllocator.apply(computeSubRegion(cornerU, 0, 1 - cornerU, cornerV)),
                    textureAllocator.apply(computeSubRegion(1 - cornerU, 0, 1, cornerV)),
                    textureAllocator.apply(computeSubRegion(0, cornerV, cornerU, 1 - cornerV)),
                    textureAllocator.apply(computeSubRegion(1 - cornerU, cornerV, 1, 1 - cornerV)),
                    textureAllocator.apply(computeSubRegion(0, 1 - cornerV, cornerU, 1)),
                    textureAllocator.apply(computeSubRegion(cornerU, 1 - cornerV, 1 - cornerU, 1)),
                    textureAllocator.apply(computeSubRegion(1 - cornerU, 1 - cornerV, 1, 1)),
                    cornerWidth,
                    cornerHeight
                );
            }

            TileInformation tileInformation = null;
            if (mode == Mode.TILING) {
                if (tileWidth == null && (cornerWidth == 0 || cornerU == 0)) {
                    throw new IllegalArgumentException("For Tiling-Mode, tileWidth must be specified if cornerWidth is 0");
                }
                if (tileHeight == null && (cornerHeight == 0 || cornerV == 0)) {
                    throw new IllegalArgumentException("For Tiling-Mode, tileHeight must be specified if cornerHeight is 0");
                }

                tileInformation = new TileInformation(
                    tileWidth != null ? tileWidth : (int) ((cornerWidth / cornerU) * (1f - (2 * cornerU))),
                    tileHeight != null ? tileHeight : (int) ((cornerHeight / cornerV) * (1f - (2 * cornerV)))
                );
            }

            return new NinePatch<>(centerTexture, cornerInformation, tileInformation);
        }

        private TextureRegion<T> computeSubRegion(float u1, float v1, float u2, float v2) {
            float baseUvWidth = this.texture.u2 - this.texture.u1;
            float baseUvHeight = this.texture.v2 - this.texture.v1;
            return new TextureRegion<>(
                this.texture.texture,
                this.texture.u1 + (baseUvWidth * u1),
                this.texture.v1 + (baseUvHeight * v1),
                this.texture.u1 + (baseUvWidth * u2),
                this.texture.v1 + (baseUvHeight * v2)
            );
        }
    }

    /**
     * NinePatch either has all corner/edge textures, or none.
     */
    private static class CornerInformation<TextureType> {
        public final @NotNull TextureType topLeft;
        public final @NotNull TextureType topCenter;
        public final @NotNull TextureType topRight;
        public final @NotNull TextureType centerLeft;
        public final @NotNull TextureType centerRight;
        public final @NotNull TextureType bottomLeft;
        public final @NotNull TextureType bottomCenter;
        public final @NotNull TextureType bottomRight;
        public final int cornerWidth;
        public final int cornerHeight;

        public CornerInformation(@NotNull TextureType topLeft, @NotNull TextureType topCenter, @NotNull TextureType topRight,
                                 @NotNull TextureType centerLeft, @NotNull TextureType centerRight,
                                 @NotNull TextureType bottomLeft, @NotNull TextureType bottomCenter, @NotNull TextureType bottomRight,
                                 int cornerWidth, int cornerHeight) {
            this.topLeft = topLeft;
            this.topCenter = topCenter;
            this.topRight = topRight;
            this.centerLeft = centerLeft;
            this.centerRight = centerRight;
            this.bottomLeft = bottomLeft;
            this.bottomCenter = bottomCenter;
            this.bottomRight = bottomRight;
            this.cornerWidth = cornerWidth;
            this.cornerHeight = cornerHeight;
        }
    }

    /**
     * NinePatch is either tiling and has tile information, or has neither.
     */
    private static class TileInformation {
        public final int tileWidth;
        public final int tileHeight;

        public TileInformation(int tileWidth, int tileHeight) {
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
        }
    }
}
