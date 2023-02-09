/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.libninepatch.demo;

import juuxel.libninepatch.AwtTextureRenderer;
import juuxel.libninepatch.NinePatch;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Consumer;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::setupAndShowGui);
    }

    private static void setupAndShowGui() {
        JPanel panel = new JPanel(new GridLayout(1, 0));
        BufferedImage texture = loadImage("9patch");
        NinePatchComponent stretch = new NinePatchComponent(createNinePatch(texture, builder -> builder.mode(NinePatch.Mode.STRETCHING)));
        stretch.setBorder(BorderFactory.createTitledBorder("Stretching"));
        NinePatchComponent tile = new NinePatchComponent(createNinePatch(texture, builder -> builder.mode(NinePatch.Mode.TILING)));
        tile.setBorder(BorderFactory.createTitledBorder("Tiling"));
        panel.add(stretch);
        panel.add(tile);

        JFrame frame = new JFrame("LibNinePatch Demo");
        frame.setContentPane(panel);
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static BufferedImage loadImage(String name) {
        try (InputStream in = Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".png"))) {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> NinePatch<T> createNinePatch(T texture, Consumer<NinePatch.Builder<T>> configurator) {
        NinePatch.Builder<T> builder = NinePatch.builder(texture)
            .cornerSize(4)
            .cornerUv(0.25f);
        configurator.accept(builder);
        return builder.build();
    }

    private static class NinePatchComponent extends JComponent {
        private static final double SCALE = 4.0;
        private final NinePatch<BufferedImage> ninePatch;

        NinePatchComponent(NinePatch<BufferedImage> ninePatch) {
            this.ninePatch = ninePatch;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D h = (Graphics2D) g.create();
            int width = getWidth() - getInsets().left - getInsets().right;
            int height = getHeight() - getInsets().top - getInsets().bottom;
            h.translate(getInsets().left, getInsets().top);
            h.scale(SCALE, SCALE);
            ninePatch.draw(new AwtTextureRenderer(h), (int) (width / SCALE), (int) (height / SCALE));
            h.dispose();
        }
    }
}
