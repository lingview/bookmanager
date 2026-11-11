package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import xyz.lingview.astra.dto.response.CoverCandidateDTO;
import xyz.lingview.astra.service.CoverSearchService;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CoverSearchServiceImpl implements CoverSearchService {

    private static final String BAIDU_HOME = "https://image.baidu.com/";
    private static final String BAIDU_SEARCH = "https://image.baidu.com/search/acjson";
    private static final String BING_SEARCH = "https://cn.bing.com/images/async";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    private static final String ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9,en;q=0.8";
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;
    private static final long COOKIE_TTL_MS = 10 * 60 * 1000L;
    private static final int CACHE_MAX_ENTRIES = 200;
    private static final Pattern BING_MURL = Pattern.compile("murl&quot;:&quot;(.*?)&quot;");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();

    private volatile String baiduCookie;
    private volatile long baiduCookieAt;

    @Value("${app.cover-search.source:auto}")
    private String source;

    @Override
    public List<CoverCandidateDTO> search(String keyword, int limit) {
        String normalized = normalize(keyword);
        if (normalized.isEmpty()) {
            return List.of();
        }
        int size = Math.max(1, Math.min(limit, 30));

        CachedResult cached = cache.get(normalized);
        if (cached != null && System.currentTimeMillis() - cached.at() < CACHE_TTL_MS) {
            return limitOf(cached.items(), size);
        }

        List<CoverCandidateDTO> result = List.of();
        if (useBaidu()) {
            result = searchBaidu(normalized);
        }
        if (result.isEmpty() && useBing()) {
            result = searchBing(normalized);
        }

        if (!result.isEmpty()) {
            if (cache.size() > CACHE_MAX_ENTRIES) {
                cache.clear();
            }
            cache.put(normalized, new CachedResult(System.currentTimeMillis(), result));
        }
        return limitOf(result, size);
    }

    List<CoverCandidateDTO> searchBaidu(String keyword) {
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String referer = "https://image.baidu.com/search/index?tn=baiduimage&word=" + encoded;
            URI uri = URI.create(BAIDU_SEARCH + "?tn=resultjson_com&ipn=rj&ct=201326592&fp=result"
                    + "&word=" + encoded + "&queryWord=" + encoded
                    + "&ie=utf-8&oe=utf-8&cl=2&lm=-1&st=-1&nc=1&pn=0&rn=30&gsm=1e");

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(12))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("Referer", referer)
                    .header("Cookie", baiduCookie())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("百度图片搜索返回状态码 {}，关键词 {}", response.statusCode(), keyword);
                return List.of();
            }
            return parseBaidu(response.body());
        } catch (Exception e) {
            log.warn("百度图片搜索失败，关键词 {}: {}", keyword, e.getMessage());
            return List.of();
        }
    }

    List<CoverCandidateDTO> parseBaidu(String json) {
        List<CoverCandidateDTO> list = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return list;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.path("data");
            if (!data.isArray()) {
                return list;
            }
            for (JsonNode node : data) {
                String middle = textOf(node, "middleURL");
                String thumb = textOf(node, "thumbURL");
                String origin = textOf(node, "objURL");
                String image = firstHttp(middle, origin, thumb);
                if (image == null) {
                    continue;
                }
                CoverCandidateDTO dto = new CoverCandidateDTO();
                dto.setImage_url(image);
                dto.setThumb_url(firstHttp(thumb, middle, image));
                dto.setSource("baidu");
                list.add(dto);
            }
        } catch (Exception e) {
            log.warn("解析百度图片结果失败: {}", e.getMessage());
        }
        return list;
    }

    List<CoverCandidateDTO> searchBing(String keyword) {
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            URI uri = URI.create(BING_SEARCH + "?q=" + encoded + "&first=1&count=35&mkt=zh-CN");
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(12))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("必应图片搜索返回状态码 {}，关键词 {}", response.statusCode(), keyword);
                return List.of();
            }
            return parseBing(response.body());
        } catch (Exception e) {
            log.warn("必应图片搜索失败，关键词 {}: {}", keyword, e.getMessage());
            return List.of();
        }
    }

    List<CoverCandidateDTO> parseBing(String html) {
        List<CoverCandidateDTO> list = new ArrayList<>();
        if (html == null || html.isBlank()) {
            return list;
        }
        Matcher matcher = BING_MURL.matcher(html);
        while (matcher.find()) {
            String url = unescape(matcher.group(1));
            if (!isHttpUrl(url)) {
                continue;
            }
            CoverCandidateDTO dto = new CoverCandidateDTO();
            dto.setImage_url(url);
            dto.setThumb_url(url);
            dto.setSource("bing");
            list.add(dto);
        }
        return list;
    }

    private String baiduCookie() {
        long now = System.currentTimeMillis();
        if (baiduCookie != null && now - baiduCookieAt < COOKIE_TTL_MS) {
            return baiduCookie;
        }
        synchronized (this) {
            if (baiduCookie != null && System.currentTimeMillis() - baiduCookieAt < COOKIE_TTL_MS) {
                return baiduCookie;
            }
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(BAIDU_HOME))
                        .timeout(Duration.ofSeconds(8))
                        .header("User-Agent", USER_AGENT)
                        .header("Accept-Language", ACCEPT_LANGUAGE)
                        .GET()
                        .build();
                HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                StringBuilder cookie = new StringBuilder();
                for (String value : response.headers().allValues("Set-Cookie")) {
                    int semicolon = value.indexOf(';');
                    String pair = semicolon > 0 ? value.substring(0, semicolon) : value;
                    if (pair.startsWith("BAIDUID")) {
                        if (cookie.length() > 0) {
                            cookie.append("; ");
                        }
                        cookie.append(pair);
                    }
                }
                baiduCookie = cookie.toString();
                baiduCookieAt = System.currentTimeMillis();
            } catch (Exception e) {
                log.warn("获取百度 Cookie 失败: {}", e.getMessage());
                baiduCookie = "";
                baiduCookieAt = System.currentTimeMillis();
            }
            return baiduCookie;
        }
    }

    private boolean useBaidu() {
        return source == null || source.isBlank() || "auto".equalsIgnoreCase(source)
                || "baidu".equalsIgnoreCase(source);
    }

    private boolean useBing() {
        return source == null || source.isBlank() || "auto".equalsIgnoreCase(source)
                || "bing".equalsIgnoreCase(source);
    }

    private String normalize(String keyword) {
        if (keyword == null) {
            return "";
        }
        String trimmed = keyword.trim().replaceAll("\\s+", " ");
        return trimmed.length() > 60 ? trimmed.substring(0, 60) : trimmed;
    }

    private List<CoverCandidateDTO> limitOf(List<CoverCandidateDTO> items, int size) {
        return items.size() <= size ? items : items.subList(0, size);
    }

    private String textOf(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String firstHttp(String... candidates) {
        for (String candidate : candidates) {
            if (isHttpUrl(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isHttpUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private String unescape(String value) {
        return value.replace("&quot;", "\"").replace("&amp;", "&").replace("\\/", "/");
    }

    private record CachedResult(long at, List<CoverCandidateDTO> items) {
    }
}