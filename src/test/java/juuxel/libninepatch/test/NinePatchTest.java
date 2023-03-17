package juuxel.libninepatch.test;

import juuxel.libninepatch.AwtTextureRenderer;
import juuxel.libninepatch.NinePatch;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import static org.assertj.swing.assertions.Assertions.assertThat;

public class NinePatchTest {
    private static BufferedImage load(String name) {
        try (InputStream in = Objects.requireNonNull(NinePatchTest.class.getResourceAsStream("/" + name + ".png"))) {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private NinePatch<BufferedImage> createNinePatch(BufferedImage image, NinePatch.Mode mode) {
        return NinePatch.builder(image)
            .cornerSize(4)
            .cornerUv(0.25f)
            .mode(mode)
            .build();
    }

    private static BufferedImage draw(NinePatch<BufferedImage> ninePatch, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        AwtTextureRenderer renderer = new AwtTextureRenderer(g);
        ninePatch.draw(renderer, width, height);
        g.dispose();
        return image;
    }

    @Test
    void testStretching() {
        NinePatch<BufferedImage> ninePatch = createNinePatch(load("9patch"), NinePatch.Mode.STRETCHING);
        BufferedImage found = draw(ninePatch, 32, 32);
        assertThat(found).isEqualTo(load("stretch"));
    }

    @Test
    void testTiling() {
        NinePatch<BufferedImage> ninePatch = createNinePatch(load("9patch"), NinePatch.Mode.TILING);
        BufferedImage found = draw(ninePatch, 32, 32);
        assertThat(found).isEqualTo(load("tile"));
    }
}
