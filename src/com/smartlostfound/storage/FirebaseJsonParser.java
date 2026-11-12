package com.smartlostfound.storage;

import com.smartlostfound.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Lightweight, high-performance JSON Serializer/Parser for Domain Models.
 * Requires zero external dependencies, handling Firebase Realtime Database payloads.
 */
public class FirebaseJsonParser {

    // =========================================================================
    // Item Serialization & Parsing
    // =========================================================================

    public static String serializeItems(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Item item : items) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(item.getId())).append("\":");
            sb.append(serializeItem(item));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    public static String serializeItem(Item item) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":\"").append(escapeJson(item.getId())).append("\",");
        sb.append("\"type\":\"").append(escapeJson(item.getItemType())).append("\",");
        sb.append("\"name\":\"").append(escapeJson(item.getName())).append("\",");
        sb.append("\"category\":\"").append(item.getCategory() != null ? item.getCategory().name() : "OTHER").append("\",");
        sb.append("\"date\":\"").append(item.getDate() != null ? item.getDate().toString() : LocalDate.now().toString()).append("\",");
        sb.append("\"description\":\"").append(escapeJson(item.getDescription())).append("\",");
        sb.append("\"status\":\"").append(item.getStatus() != null ? item.getStatus().name() : "LOST").append("\",");
        sb.append("\"specificLocation\":\"").append(escapeJson(item.getSpecificLocation())).append("\",");

        User rep = item.getReporter();
        sb.append("\"reporterName\":\"").append(rep != null ? escapeJson(rep.getName()) : "").append("\",");
        sb.append("\"reporterEmail\":\"").append(rep != null ? escapeJson(rep.getEmail()) : "").append("\",");
        sb.append("\"reporterPhone\":\"").append(rep != null ? escapeJson(rep.getPhone()) : "").append("\",");
        sb.append("\"resolutionNotes\":\"").append(escapeJson(item.getResolutionNotes())).append("\",");
        sb.append("\"imagePath\":\"").append(escapeJson(item.getImagePath())).append("\"");

        if (item instanceof LostItem) {
            LostItem lost = (LostItem) item;
            sb.append(",\"rewardAmount\":").append(lost.getRewardAmount());
        } else if (item instanceof FoundItem) {
            FoundItem found = (FoundItem) item;
            sb.append(",\"storageLocation\":\"").append(escapeJson(found.getStorageLocation())).append("\",");
            sb.append("\"finderContactInfo\":\"").append(escapeJson(found.getFinderContactInfo())).append("\"");
        }

        sb.append("}");
        return sb.toString();
    }

    public static List<Item> parseItems(String json) {
        List<Item> items = new ArrayList<>();
        if (json == null || json.trim().isEmpty() || "null".equals(json.trim()) || "{}".equals(json.trim())) {
            return items;
        }

        List<Map<String, String>> records = parseJsonObjects(json);
        for (Map<String, String> map : records) {
            try {
                String id = map.getOrDefault("id", "ITEM-" + UUID.randomUUID().toString().substring(0, 8));
                String type = map.getOrDefault("type", id.startsWith("FND") ? "FOUND" : "LOST");
                String name = map.getOrDefault("name", "Untitled Item");
                ItemCategory category = ItemCategory.fromString(map.getOrDefault("category", "OTHER"));
                
                LocalDate date = LocalDate.now();
                String dateStr = map.get("date");
                if (dateStr != null && !dateStr.isEmpty()) {
                    try { date = LocalDate.parse(dateStr); } catch (Exception ignored) {}
                }

                String description = map.getOrDefault("description", "");
                ItemStatus status = ItemStatus.fromString(map.getOrDefault("status", "LOST"));
                String location = map.getOrDefault("specificLocation", "");

                User reporter = new User(
                        map.getOrDefault("reporterName", ""),
                        map.getOrDefault("reporterEmail", ""),
                        map.getOrDefault("reporterPhone", "")
                );

                String resolution = map.getOrDefault("resolutionNotes", "");
                String imagePath = map.getOrDefault("imagePath", "");

                if ("FOUND".equalsIgnoreCase(type)) {
                    String storage = map.getOrDefault("storageLocation", "");
                    String finderContact = map.getOrDefault("finderContactInfo", "");
                    FoundItem f = new FoundItem(id, name, category, date, description, status,
                            reporter, location, storage, finderContact);
                    f.setResolutionNotes(resolution);
                    f.setImagePath(imagePath);
                    items.add(f);
                } else {
                    double reward = 0.0;
                    try { reward = Double.parseDouble(map.getOrDefault("rewardAmount", "0.0")); } catch (Exception ignored) {}
                    LostItem l = new LostItem(id, name, category, date, description, status,
                            reporter, location, reward);
                    l.setResolutionNotes(resolution);
                    l.setImagePath(imagePath);
                    items.add(l);
                }
            } catch (Exception ex) {
                System.err.println("Warning: Skipping malformed item record in Firebase response: " + ex.getMessage());
            }
        }
        return items;
    }

    // =========================================================================
    // User Serialization & Parsing
    // =========================================================================

    public static String serializeUsers(List<UserAccount> users) {
        if (users == null || users.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (UserAccount user : users) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(user.getUsername())).append("\":");
            sb.append(serializeUser(user));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    public static String serializeUser(UserAccount user) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"username\":\"").append(escapeJson(user.getUsername())).append("\",");
        sb.append("\"passwordHash\":\"").append(escapeJson(user.getPasswordHash())).append("\",");
        sb.append("\"role\":\"").append(user.getRole() != null ? user.getRole().name() : "USER").append("\",");
        sb.append("\"name\":\"").append(escapeJson(user.getName())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(user.getEmail())).append("\",");
        sb.append("\"phone\":\"").append(escapeJson(user.getPhone())).append("\",");
        sb.append("\"createdAt\":\"").append(user.getCreatedAt() != null ? user.getCreatedAt().toString() : LocalDateTime.now().toString()).append("\",");
        sb.append("\"active\":").append(user.isActive());
        sb.append("}");
        return sb.toString();
    }

    public static List<UserAccount> parseUsers(String json) {
        List<UserAccount> users = new ArrayList<>();
        if (json == null || json.trim().isEmpty() || "null".equals(json.trim()) || "{}".equals(json.trim())) {
            return users;
        }

        List<Map<String, String>> records = parseJsonObjects(json);
        for (Map<String, String> map : records) {
            try {
                String username = map.get("username");
                if (username == null || username.trim().isEmpty()) continue;

                String passHash = map.getOrDefault("passwordHash", "");
                String roleStr = map.getOrDefault("role", "USER");
                UserRole role = "ADMIN".equalsIgnoreCase(roleStr) ? UserRole.ADMIN : UserRole.USER;
                String name = map.getOrDefault("name", username);
                String email = map.getOrDefault("email", "");
                String phone = map.getOrDefault("phone", "");

                LocalDateTime createdAt = LocalDateTime.now();
                String createdStr = map.get("createdAt");
                if (createdStr != null && !createdStr.isEmpty()) {
                    try { createdAt = LocalDateTime.parse(createdStr); } catch (Exception ignored) {}
                }

                boolean active = !"false".equalsIgnoreCase(map.getOrDefault("active", "true"));

                UserAccount account = new UserAccount(username, passHash, role, name, email, phone, createdAt, active);
                users.add(account);
            } catch (Exception ex) {
                System.err.println("Warning: Skipping malformed user record in Firebase response: " + ex.getMessage());
            }
        }
        return users;
    }

    // =========================================================================
    // Lightweight JSON Tokenizer
    // =========================================================================

    private static List<Map<String, String>> parseJsonObjects(String json) {
        List<Map<String, String>> objects = new ArrayList<>();
        int i = 0;
        int n = json.length();

        while (i < n) {
            int openBrace = json.indexOf('{', i);
            if (openBrace == -1) break;

            int closeBrace = findMatchingCloseBrace(json, openBrace);
            if (closeBrace == -1) break;

            String objStr = json.substring(openBrace, closeBrace + 1);
            Map<String, String> kv = parseSimpleObject(objStr);
            if (!kv.isEmpty()) {
                objects.add(kv);
            }
            i = closeBrace + 1;
        }
        return objects;
    }

    private static int findMatchingCloseBrace(String s, int openIdx) {
        int depth = 0;
        boolean inQuotes = false;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    private static Map<String, String> parseSimpleObject(String objStr) {
        Map<String, String> map = new HashMap<>();
        int i = 1; // skip {
        int n = objStr.length() - 1; // skip }

        while (i < n) {
            int keyStart = objStr.indexOf('\"', i);
            if (keyStart == -1 || keyStart >= n) break;
            int keyEnd = objStr.indexOf('\"', keyStart + 1);
            if (keyEnd == -1 || keyEnd >= n) break;
            String key = objStr.substring(keyStart + 1, keyEnd);

            int colon = objStr.indexOf(':', keyEnd + 1);
            if (colon == -1 || colon >= n) break;

            // Find value start
            int valStart = colon + 1;
            while (valStart < n && Character.isWhitespace(objStr.charAt(valStart))) valStart++;
            if (valStart >= n) break;

            String value = "";
            int nextComma = -1;

            if (objStr.charAt(valStart) == '\"') {
                // String value
                int valEnd = valStart + 1;
                while (valEnd < n) {
                    if (objStr.charAt(valEnd) == '\"' && objStr.charAt(valEnd - 1) != '\\') {
                        break;
                    }
                    valEnd++;
                }
                value = unescapeJson(objStr.substring(valStart + 1, valEnd));
                nextComma = objStr.indexOf(',', valEnd);
            } else {
                // Primitive number, boolean, null
                int valEnd = valStart;
                while (valEnd < n && objStr.charAt(valEnd) != ',' && objStr.charAt(valEnd) != '}') {
                    valEnd++;
                }
                value = objStr.substring(valStart, valEnd).trim();
                nextComma = valEnd < n && objStr.charAt(valEnd) == ',' ? valEnd : -1;
            }

            map.put(key, value);

            if (nextComma != -1) {
                i = nextComma + 1;
            } else {
                break;
            }
        }
        return map;
    }

    public static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String unescapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
