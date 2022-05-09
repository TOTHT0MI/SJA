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

package me.tothtomi.songlink.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Platform {

    SPOTIFY("spotify"),
    ITUNES("itunes"),
    APPLE_MUSIC("appleMusic"),
    YOUTUBE("youtube"),
    YOUTUBE_MUSIC("youtubeMusic"),
    GOOGLE("google"),
    GOOGLE_STORE("googleStore"),
    PANDORA("pandora"),
    DEEZER("deezer"),
    TIDAL("tidal"),
    AMAZON_STORE("amazonStore"),
    AMAZON_MUSIC("amazonMusic"),
    SOUNDCLOUD("soundcloud"),
    NAPSTER("napster"),
    YANDEX("yandex"),
    SPINRILLA("spinrilla"),
    AUDIUS("yandex"),
    AUDIOMACK("audiomack");

    private String platformId;

    public static Platform fromId(String id) {
        for (Platform value : values()) {
            if (value.platformId.equals(id)) return value;
        }

        throw new IllegalStateException("Invalid PlatformID for Platform.");
    }
}