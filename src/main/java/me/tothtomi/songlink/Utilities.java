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

package me.tothtomi.songlink;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public final class Utilities {

    public static final String NORMAL_USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
    public static final String DEFAULT_USER_AGENT = "Songlink Java API";

    public static Color getAverageColor(BufferedImage bufferedImage) {
        float sumr = 0, sumg = 0, sumb = 0;

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                Color pixel = new Color(bufferedImage.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }

        float num = bufferedImage.getWidth() * bufferedImage.getHeight();


        return new Color(sumr / num / 255, sumg / num / 255, sumb / num / 255);
    }

    public static String readWebsite(String urlString, String userAgent) throws IOException {
        URL url = new URL(urlString);

        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", userAgent);

        try (InputStream inputStream = url.openStream()) {
            try (Scanner scanner = new Scanner(inputStream)) {
                StringBuilder stringBuilder = new StringBuilder();

                while (scanner.hasNext())
                    stringBuilder.append(scanner.next());

                return stringBuilder.toString();
            }
        }
    }
}
