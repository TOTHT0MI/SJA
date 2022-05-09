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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.tothtomi.songlink.enums.APIProvider;
import me.tothtomi.songlink.enums.Platform;
import me.tothtomi.songlink.track.PlatformTrack;
import me.tothtomi.songlink.track.Track;
import me.tothtomi.songlink.track.meta.Links;
import me.tothtomi.songlink.track.meta.Metadata;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Java wrapper for Songlink's API.
 * <br /><br />
 * Please note, that this is only a wrapper, and it is YOUR responsibility to Attribute Songlink's service and to read their Terms of Service.
 * You can find their documentation, with their attribution guidelines and their terms of service here: <a href="https://www.notion.so/API-d0ebe08a5e304a55928405eb682f6741" target="_blank">https://www.notion.so/API-d0ebe08a5e304a55928405eb682f6741</a>
 * <br /><br />
 * Currently the wrapper only supports searching for URLs. It is planned however to implement the other way (via platform, type & id).
 *
 * @author TOTHTOMI
 * @version 1.0.0
 */
public class Songlink {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final String countryCode;
    private final String apiKey;
    private final String userAgent;

    private final Cache<String, Track> cache;

    protected Songlink(String countryCode, String apiKey, long cacheMaxSize, Duration cacheExpire, String userAgent) {
        this.countryCode = countryCode;
        this.apiKey = apiKey;
        this.userAgent = userAgent;

        this.cache = Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(cacheMaxSize)
                .expireAfterAccess(cacheExpire)
                .build();
    }

    /**
     * Fetches Songlink API and parses the data into a {@link Track} object.
     * This is an async method, which calls {@link ExecutorService}.
     *
     * @param uri the URI to search for. (Will be encoded automatically)
     * @param onSuccess if the search was completed successfully. The consumer will contain the resulting {@link Track}
     * @param onError if something went bad. It will contain the error thrown.
     */
    public void search(String uri, Consumer<Track> onSuccess, Consumer<Exception> onError) {
        String url = URLEncoder.encode(uri, StandardCharsets.UTF_8);
        executorService.execute(() -> {
            try {
                Track result = search(url);
                onSuccess.accept(result);
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    /**
     * Fetches Songlink API and parses the data into a {@link Track} object.
     * WARNING! This is not an async function, meaning it will block the thread calling this method! You may wanna use {@link #search(String, Consumer, Consumer)} instead.
     *
     * @param uri the URI to search for. (Will be encoded automatically)
     * @return the resulting {@link Track} (will never be null)
     * @throws Exception if something goes bad
     */
    public Track search(String uri) throws Exception {
        String url = URLEncoder.encode(uri, StandardCharsets.UTF_8);
        return cache.get(url, u -> {
            JSONObject web;

            try {
                web = getResponseFromSonglink(u);
            } catch (IOException e) {
                // We are mapping it to a runtime exception, the cache will automatically throw this
                throw new RuntimeException(e);
            }

            String entityUniqueId = web.getString("entityUniqueId");
            String userCountry = web.getString("userCountry");
            String pageUrl = web.getString("pageUrl");

            return new Track(entityUniqueId, userCountry, pageUrl, getPlatforms(web));
        });
    }

    private Map<Platform, PlatformTrack> getPlatforms(JSONObject web) {
        final Map<Platform, PlatformTrack> tracks = new HashMap<>();
        final JSONObject entities = web.getJSONObject("entitiesByUniqueId");
        final JSONObject platforms = web.getJSONObject("linksByPlatform");

        for (Platform value : Platform.values()) {
            if (!platforms.has(value.getPlatformId())) continue;
            JSONObject platform = platforms.getJSONObject(value.getPlatformId());

            final Links links = Links.fromJsonObject(platform);

            final String uuid = platform.getString("entityUniqueId");
            final JSONObject metaObject = entities.getJSONObject(uuid);
            final Metadata metadata = Metadata.fromJson(metaObject, uuid, platform.getString("country"));

            final PlatformTrack platformTrack = new PlatformTrack(value, links, metadata,
                    APIProvider.fromId(metaObject.getString("apiProvider")), getPoweredPlatforms(metaObject));

            tracks.put(value, platformTrack);
        }

        return tracks;
    }

    private Platform[] getPoweredPlatforms(JSONObject metaObject) {
        JSONArray array = metaObject.getJSONArray("platforms");
        List<Platform> platformList = new ArrayList<>(1);

        for (Object o : array) {
            if (!(o instanceof String)) continue;
            platformList.add(Platform.fromId((String) o));
        }

        return platformList.toArray(Platform[]::new);
    }

    /**
     * Method responsible for actually calling the API.
     *
     * @param url the raw URL
     * @return the {@link JSONObject} parsed from the response of the API
     * @throws IOException if something goes bad
     */
    private JSONObject getResponseFromSonglink(String url) throws IOException {
        String apiUrl = String.format("https://api.song.link/v1-alpha.1/links?url=%s&userCountry=%s", url, countryCode);
        if (apiKey != null) {
            apiUrl += ("&key" + apiKey);
        }

        return new JSONObject(Utilities.readWebsite(apiUrl, this.userAgent));
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @return the builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Factory for the {@link Songlink} class.
     * Always use this, as this will provide default values if they are not specified!
     */
    public static final class Builder {

        private String countryCode = "US";
        private String apiKey = null;
        private String userAgent = Utilities.DEFAULT_USER_AGENT;

        private long cacheMaxSize = 500;
        private Duration cacheExpire = Duration.ofHours(2);

        /**
         * Two-letter country code. Specifies the country/location Songlink uses when searching streaming catalogs. Optional (default to US).
         *
         * @param countryCode the countryCode
         * @return the {@link Builder} for chaining
         */
        public Builder country(String countryCode) {
            this.countryCode = countryCode.toUpperCase(Locale.ROOT);
            return this;
        }

        /**
         * The API key to use when accessing Songlink's service.
         * <b>Without specifying an API key you will be rate limited to 10 requests/min.</b>
         *
         * @param apiKey the API key
         * @return the {@link Builder} for chaining
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * The duration after the last access to the expiration of data.
         * When the same URI has not been requested for this specified amount of time, the entry in the cache will expire.
         *
         * @param expiration the expiration duration
         * @return the {@link Builder} for chaining
         * @see Caffeine#expireAfterAccess(Duration)
         */
        public Builder cacheDuration(Duration expiration) {
            this.cacheExpire = expiration;
            return this;
        }

        /**
         * The user agent to use when accessing Songlink's service.
         * Default is: "Songlink Java API".
         *
         * @param userAgent the userAgent to use
         * @return the {@link Builder} for chaining
         */
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * The maximum cache size.
         *
         * @param size the size.
         * @return the {@link Builder} for chaining
         */
        public Builder cacheSize(long size) {
            this.cacheMaxSize = size;
            return this;
        }

        /**
         * Builds and returns the songlink instance.
         * It should be considered as a singleton, do not create more unless necessary (ex. other api keys)
         *
         * @return the {@link Songlink} instance
         */
        public Songlink build() {
            return new Songlink(countryCode, apiKey, cacheMaxSize, cacheExpire, userAgent);
        }
    }
}
