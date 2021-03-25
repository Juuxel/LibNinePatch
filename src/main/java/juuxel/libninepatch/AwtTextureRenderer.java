/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.libninepatch;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Renders a {@link BufferedImage} using AWT's {@link Graphics}.
 */
public final class AwtTextureRenderer implements TextureRenderer<BufferedImage> {
    private final Graphics g;

    /**
     * Constructs an {@code AwtTextureRenderer} from a {@link Graphics} object.
     *
     * @param g the {@link Graphics} used for drawing
     */
    public AwtTextureRenderer(Graphics g) {
        this.g = g;
    }

    @Override
    public void draw(BufferedImage texture, int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
        int iw = texture.getWidth();
        int ih = texture.getHeight();
        g.drawImage(texture, x, y, x + width, y + height, (int) (iw * u1), (int) (ih * v1), (int) (iw * u2), (int) (ih * v2), null);
    }
}
