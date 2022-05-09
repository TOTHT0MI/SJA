/*
 * MIT License
 *
 * Copyright (c) 2022 TOTHTOMI
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.tothtomi.songlink.track.meta;

import lombok.*;
import me.tothtomi.songlink.Utilities;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

@Data
@RequiredArgsConstructor
public class Thumbnail {

    private final String url;
    private final int width;
    private final int height;

    @Setter(AccessLevel.PRIVATE)
    private BufferedImage image = null;

    @SneakyThrows
    public BufferedImage getImage() {
        if (this.image != null) return this.image;
        this.setImage(ImageIO.read(new URL(url)));

        return image;
    }

    public Color getAverage() {
        return Utilities.getAverageColor(getImage());
    }

    public static Thumbnail fromJson(JSONObject jsonObject) {
        String url = jsonObject.getString("thumbnailUrl");
        int width = jsonObject.getInt("thumbnailWidth");
        int height = jsonObject.getInt("thumbnailHeight");

        return new Thumbnail(url, width, height);
    }
}
