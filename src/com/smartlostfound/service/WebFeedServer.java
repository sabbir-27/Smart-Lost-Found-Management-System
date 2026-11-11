package com.smartlostfound.service;

import com.smartlostfound.model.FoundItem;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.ItemStatus;
import com.smartlostfound.model.LostItem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP Server providing read-only Community Feed access to external web browsers.
 * Strict Security Policy:
 * - Only the Community Feed (GET / or GET /feed) and cached images (GET /images/*) are served.
 * - All other paths (/admin, /dashboard, /report, etc.) return HTTP 403 Forbidden.
 * - No item modifications, write operations, or user directories are exposed over HTTP.
 */
public class WebFeedServer {

    private static final int DEFAULT_PORT = 8080;
    private final LostFoundManager manager;
    private HttpServer server;
    private int boundPort = DEFAULT_PORT;

    public WebFeedServer(LostFoundManager manager) {
        this.manager = manager;
    }

    /**
     * Starts the HTTP server on port 8080 (or fallback if occupied).
     */
    public synchronized boolean start() {
        int[] portsToTry = {DEFAULT_PORT, 8081, 8082, 8888};
        for (int p : portsToTry) {
            try {
                server = HttpServer.create(new InetSocketAddress("0.0.0.0", p), 0);
                boundPort = p;

                server.createContext("/", new RootHandler());
                server.createContext("/images/", new ImageHandler());
                server.setExecutor(Executors.newFixedThreadPool(4, r -> {
                    Thread t = new Thread(r, "WebFeedServer-Worker");
                    t.setDaemon(true);
                    return t;
                }));

                server.start();
                System.out.println("==========================================================");
                System.out.printf("Web Feed Server started at http://localhost:%d/\n", boundPort);
                System.out.println("Access Policy: Community Feed Only (All other routes restricted 403)");
                System.out.println("==========================================================");
                return true;
            } catch (IOException ignored) {
            }
        }
        System.err.println("Warning: Could not bind WebFeedServer on default ports.");
        return false;
    }

    /**
     * Stops the HTTP server.
     */
    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public int getPort() {
        return boundPort;
    }

    public boolean isRunning() {
        return server != null;
    }

    // =========================================================================
    // Request Handlers
    // =========================================================================

    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Strict route filter: ONLY / or /feed is allowed
            if (!"/".equals(path) && !"/feed".equals(path) && !"/index.html".equals(path)) {
                sendForbiddenResponse(exchange, path);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String html = renderFeedHtml();
            byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    private class ImageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            // Path is /images/<filename>
            String filename = path.substring("/images/".length());

            // Prevent path traversal
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            File file = new File("data/images", filename);
            if (!file.exists() || !file.isFile()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            String contentType = "image/jpeg";
            String lower = filename.toLowerCase();
            if (lower.endsWith(".png")) {
                contentType = "image/png";
            } else if (lower.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (lower.endsWith(".gif")) {
                contentType = "image/gif";
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "public, max-age=86400");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private void sendForbiddenResponse(HttpExchange exchange, String requestedPath) throws IOException {
        String errorHtml = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>403 Access Restricted - Smart Lost & Found</title>" +
                "<style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0B0B0C; color: #FFF; margin: 0; display: flex; align-items: center; justify-content: center; min-height: 100vh; }" +
                ".card { background: #18181B; border: 1px solid #27272A; border-radius: 12px; padding: 36px; max-width: 480px; text-align: center; box-shadow: 0 20px 40px rgba(0,0,0,0.5); }" +
                ".badge { background: #E52E2D; color: #FFF; padding: 4px 10px; border-radius: 4px; font-weight: bold; font-size: 11px; display: inline-block; margin-bottom: 16px; }" +
                "h1 { margin: 0 0 10px 0; font-size: 24px; }" +
                "p { color: #A1A1AA; font-size: 14px; line-height: 1.6; margin: 0 0 24px 0; }" +
                "a { display: inline-block; background: #E52E2D; color: #FFF; text-decoration: none; padding: 10px 20px; border-radius: 6px; font-weight: 600; font-size: 13px; }" +
                "</style></head><body>" +
                "<div class='card'>" +
                "<div class='badge'>ACCESS RESTRICTED (HTTP 403)</div>" +
                "<h1>Web Browser Access is Limited</h1>" +
                "<p>From the web browser, only the <b>Community Feed</b> is accessible. Access to <code>" + escapeHtml(requestedPath) + "</code> is prohibited.<br><br>" +
                "To report lost property, manage inventory, or access administrative tools, please use the <b>Smart Lost & Found Desktop Application</b>.</p>" +
                "<a href='/'>← Return to Community Feed</a>" +
                "</div></body></html>";

        byte[] bytes = errorHtml.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(403, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // =========================================================================
    // HTML & CSS Template Rendering
    // =========================================================================

    private String renderFeedHtml() {
        List<Item> items = manager.getAllItems();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        sb.append("<title>Community Feed - Smart Lost &amp; Found</title>");
        sb.append("<style>");
        sb.append(":root { --primary: #E52E2D; --bg: #0B0B0C; --card-bg: #18181B; --border: #27272A; --text: #FFFFFF; --text-muted: #A1A1AA; }");
        sb.append("* { box-sizing: border-box; margin: 0; padding: 0; }");
        sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #F4F4F6; color: #0F0F11; line-height: 1.5; }");
        
        // Header
        sb.append("header { background: #0B0B0C; color: #FFF; padding: 16px 24px; position: sticky; top: 0; z-index: 100; border-bottom: 1px solid #27272A; box-shadow: 0 4px 16px rgba(0,0,0,0.3); }");
        sb.append(".header-inner { max-width: 820px; margin: 0 auto; display: flex; align-items: center; justify-content: space-between; }");
        sb.append(".brand { display: flex; align-items: center; gap: 10px; font-weight: 800; font-size: 16px; letter-spacing: -0.3px; }");
        sb.append(".brand-badge { background: #E52E2D; width: 22px; height: 22px; border-radius: 4px; display: inline-flex; align-items: center; justify-content: center; font-size: 12px; }");
        sb.append(".pill-banner { background: rgba(229, 46, 45, 0.15); border: 1px solid rgba(229, 46, 45, 0.35); color: #F87171; font-size: 11px; font-weight: 700; padding: 4px 10px; border-radius: 20px; }");
        
        // Banner info
        sb.append(".notice-bar { background: #FFFFFF; border-bottom: 1px solid #E4E4E7; padding: 12px 24px; text-align: center; font-size: 13px; color: #52525B; }");
        sb.append(".notice-bar b { color: #E52E2D; }");

        // Feed container
        sb.append(".feed-container { max-width: 740px; margin: 24px auto; padding: 0 16px 60px 16px; display: flex; flex-direction: column; gap: 20px; }");
        
        // Feed card (Facebook style)
        sb.append(".feed-card { background: #FFFFFF; border: 1px solid #E4E4E7; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.04); transition: transform 0.15s ease; }");
        sb.append(".card-header { padding: 16px 18px; display: flex; align-items: center; justify-content: space-between; }");
        sb.append(".user-meta { display: flex; align-items: center; gap: 12px; }");
        sb.append(".avatar { width: 38px; height: 38px; border-radius: 50%; background: #18181B; color: #FFF; font-weight: 700; font-size: 13px; display: flex; align-items: center; justify-content: center; }");
        sb.append(".user-title { font-weight: 700; font-size: 14px; color: #0F0F11; }");
        sb.append(".user-sub { font-size: 12px; color: #71717A; }");
        
        // Status badges
        sb.append(".status-badge { font-size: 11px; font-weight: 800; padding: 4px 10px; border-radius: 20px; text-transform: uppercase; letter-spacing: 0.5px; }");
        sb.append(".status-lost { background: #FEF2F2; color: #DC2626; border: 1px solid #FECACA; }");
        sb.append(".status-found { background: #EFF6FF; color: #2563EB; border: 1px solid #BFDBFE; }");
        sb.append(".status-resolved { background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0; }");

        // Body
        sb.append(".card-body { padding: 0 18px 14px 18px; }");
        sb.append(".item-name { font-size: 18px; font-weight: 800; margin-bottom: 6px; color: #0F0F11; }");
        sb.append(".item-desc { font-size: 14px; color: #3F3F46; line-height: 1.5; margin-bottom: 12px; }");
        sb.append(".meta-chips { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }");
        sb.append(".chip { background: #F4F4F5; color: #52525B; font-size: 12px; padding: 4px 10px; border-radius: 6px; font-weight: 500; }");
        
        // Image
        sb.append(".card-photo-wrapper { width: 100%; aspect-ratio: 16/9; background: #0B0B0C; overflow: hidden; display: flex; align-items: center; justify-content: center; }");
        sb.append(".card-photo-wrapper img { width: 100%; height: 100%; object-fit: cover; }");

        // Footer
        sb.append(".card-footer { padding: 12px 18px; background: #FAFAFA; border-top: 1px solid #F4F4F5; font-size: 12px; color: #71717A; display: flex; justify-content: space-between; }");
        sb.append("</style></head><body>");

        // Header
        sb.append("<header><div class='header-inner'>");
        sb.append("<div class='brand'><div class='brand-badge'>■</div> SMART LOST &amp; FOUND</div>");
        sb.append("<div class='pill-banner'>BROWSER MODE: FEED ONLY</div>");
        sb.append("</div></header>");

        // Notice Bar
        sb.append("<div class='notice-bar'>");
        sb.append("Public Community Feed • <b>Read-Only Mode</b>. Reporting, claim verification, and admin controls require the desktop application.");
        sb.append("</div>");

        // Feed Container
        sb.append("<div class='feed-container'>");

        if (items.isEmpty()) {
            sb.append("<div class='feed-card' style='padding: 40px; text-align: center; color: #71717A;'>");
            sb.append("<h3>No lost or found property reports logged yet.</h3>");
            sb.append("<p style='font-size: 13px; margin-top: 6px;'>Check back later for real-time community reports.</p>");
            sb.append("</div>");
        } else {
            for (Item item : items) {
                renderItemCard(sb, item);
            }
        }

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private void renderItemCard(StringBuilder sb, Item item) {
        boolean isLost = item instanceof LostItem;
        String statusClass = item.getStatus() == ItemStatus.LOST ? "status-lost" :
                (item.getStatus() == ItemStatus.FOUND ? "status-found" : "status-resolved");

        String reporterName = item.getReporter() != null && !item.getReporter().getName().isEmpty()
                ? item.getReporter().getName()
                : (isLost ? "Anonymous Owner" : "Community Finder");

        String initials = computeInitials(reporterName);

        sb.append("<div class='feed-card'>");

        // Header Row
        sb.append("<div class='card-header'>");
        sb.append("<div class='user-meta'>");
        sb.append("<div class='avatar'>").append(initials).append("</div>");
        sb.append("<div>");
        sb.append("<div class='user-title'>").append(escapeHtml(reporterName)).append("</div>");
        sb.append("<div class='user-sub'>Reported ").append(item.getFormattedDate()).append("</div>");
        sb.append("</div></div>");
        sb.append("<div class='status-badge ").append(statusClass).append("'>").append(item.getStatus().getDisplayName()).append("</div>");
        sb.append("</div>");

        // Body Content
        sb.append("<div class='card-body'>");
        sb.append("<div class='item-name'>").append(escapeHtml(item.getName())).append("</div>");

        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            sb.append("<div class='item-desc'>").append(escapeHtml(item.getDescription())).append("</div>");
        }

        // Meta chips
        sb.append("<div class='meta-chips'>");
        sb.append("<span class='chip'>📂 ").append(item.getCategory().getDisplayName()).append("</span>");
        sb.append("<span class='chip'>📍 ").append(escapeHtml(item.getSpecificLocation())).append("</span>");

        if (isLost) {
            LostItem lost = (LostItem) item;
            if (lost.getRewardAmount() > 0) {
                sb.append("<span class='chip' style='color:#DC2626; font-weight:700;'>💰 Reward: ").append(lost.getFormattedReward()).append("</span>");
            }
        } else if (item instanceof FoundItem) {
            FoundItem found = (FoundItem) item;
            if (found.getStorageLocation() != null && !found.getStorageLocation().isEmpty()) {
                sb.append("<span class='chip'>🏢 Custody: ").append(escapeHtml(found.getStorageLocation())).append("</span>");
            }
        }
        sb.append("</div></div>");

        // Photo if available
        String imagePath = item.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                sb.append("<div class='card-photo-wrapper'>");
                sb.append("<img src='/images/").append(escapeHtml(imgFile.getName())).append("' alt='Property photo' loading='lazy'>");
                sb.append("</div>");
            }
        }

        // Footer
        sb.append("<div class='card-footer'>");
        sb.append("<span>ID: <code>").append(item.getId()).append("</code></span>");
        sb.append("<span>DesignRush Public Feed</span>");
        sb.append("</div>");

        sb.append("</div>"); // .feed-card
    }

    private String computeInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "??";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
