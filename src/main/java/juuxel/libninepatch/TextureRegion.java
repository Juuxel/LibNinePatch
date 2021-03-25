/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.libninepatch;

import java.util.Objects;

/**
 * Texture regions represent a partial region of a larger texture image.
 *
 * @param <T> the texture type
 */
public final class TextureRegion<T> {
    /** The texture from which this region is cut. */
    public final T texture;
    /** The left edge of this region as a fraction from 0 to 1. */
    public final float u1;
    /** The top edge of this region as a fraction from 0 to 1. */
    public final float v1;
    /** The right edge of this region as a fraction from 0 to 1. */
    public final float u2;
    /** The bottom edge of this region as a fraction from 0 to 1. */
    public final float v2;

    /**
     * Constructs a texture region.
     *
     * @param texture the texture from which this region is cut
     * @param u1      The left edge of this region as a fraction from 0 to 1
     * @param v1      the top edge of this region as a fraction from 0 to 1
     * @param u2      the right edge of this region as a fraction from 0 to 1
     * @param v2      the bottom edge of this region as a fraction from 0 to 1
     */
    public TextureRegion(T texture, float u1, float v1, float u2, float v2) {
        this.texture = texture;
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextureRegion<?> that = (TextureRegion<?>) o;
        return Float.compare(that.u1, u1) == 0 && Float.compare(that.v1, v1) == 0 && Float.compare(that.u2, u2) == 0 && Float.compare(that.v2, v2) == 0 && Objects.equals(texture, that.texture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texture, u1, v1, u2, v2);
    }

    @Override
    public String toString() {
        return "TextureRegion{" +
            "texture=" + texture +
            ", u1=" + u1 +
            ", v1=" + v1 +
            ", u2=" + u2 +
            ", v2=" + v2 +
            '}';
    }
}
