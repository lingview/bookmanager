package xyz.lingview.astra.util;

/**
 * @Author: lingview
 * @Date: 2026/01/02 00:15:34
 * @Description: url处理工具
 * @Version: 1.0
 */
public class UrlUtil {

    public static String getFullUrl(String domain, String path) {
        if (path == null || path.isEmpty()) {
            return domain + "/favicon.ico";
        }

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        if (path.startsWith("/")) {
            return domain + path;
        } else {
            return domain + "/" + path;
        }
    }

    public static String sanitizeInternalRedirect(String target) {
        if (target == null || target.isEmpty()) {
            return "/";
        }
        if (!target.startsWith("/") || target.startsWith("//") || target.contains("\\")) {
            return "/";
        }
        if (target.indexOf('\r') >= 0 || target.indexOf('\n') >= 0) {
            return "/";
        }
        return target;
    }
}