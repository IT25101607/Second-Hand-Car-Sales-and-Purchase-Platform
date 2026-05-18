package com.car_sale.carsalesystem.model;

/**
 * SearchPreference – stores a user's saved search/filter settings.
 * Persisted to search_prefs.txt.
 * File format: prefId|userId|keyword|brand|minPrice|maxPrice|fromYear|toYear|type
 */
public class SearchPreference {

    private String prefId;
    private String userId;
    private String keyword;   // general text search
    private String brand;
    private double minPrice;
    private double maxPrice;
    private int    fromYear;
    private int    toYear;
    private String type;      // CAR | USED_CAR | CERTIFIED_CAR | ""

    // ── Constructors ──────────────────────────────────────────────────────────
    public SearchPreference() {}

    public SearchPreference(String prefId, String userId, String keyword,
                            String brand, double minPrice, double maxPrice,
                            int fromYear, int toYear, String type) {
        this.prefId   = prefId;
        this.userId   = userId;
        this.keyword  = keyword;
        this.brand    = brand;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.fromYear = fromYear;
        this.toYear   = toYear;
        this.type     = type;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getPrefId()              { return prefId; }
    public void   setPrefId(String id)     { this.prefId = id; }

    public String getUserId()              { return userId; }
    public void   setUserId(String uid)    { this.userId = uid; }

    public String getKeyword()             { return keyword; }
    public void   setKeyword(String kw)    { this.keyword = kw; }

    public String getBrand()               { return brand; }
    public void   setBrand(String brand)   { this.brand = brand; }

    public double getMinPrice()            { return minPrice; }
    public void   setMinPrice(double p)    { this.minPrice = p; }

    public double getMaxPrice()            { return maxPrice; }
    public void   setMaxPrice(double p)    { this.maxPrice = p; }

    public int  getFromYear()              { return fromYear; }
    public void setFromYear(int y)         { this.fromYear = y; }

    public int  getToYear()                { return toYear; }
    public void setToYear(int y)           { this.toYear = y; }

    public String getType()               { return type; }
    public void   setType(String type)    { this.type = type; }

    // ── Serialisation ─────────────────────────────────────────────────────────
    public String toFileString() {
        return String.join("|",
                prefId, userId,
                keyword  == null ? "" : keyword,
                brand    == null ? "" : brand,
                String.valueOf(minPrice), String.valueOf(maxPrice),
                String.valueOf(fromYear), String.valueOf(toYear),
                type     == null ? "" : type);
    }

    public static SearchPreference fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 9) return null;
        SearchPreference sp = new SearchPreference();
        sp.prefId   = p[0];
        sp.userId   = p[1];
        sp.keyword  = p[2];
        sp.brand    = p[3];
        sp.minPrice = parseDouble(p[4]);
        sp.maxPrice = parseDouble(p[5]);
        sp.fromYear = parseInt(p[6]);
        sp.toYear   = parseInt(p[7]);
        sp.type     = p[8];
        return sp;
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }
    private static int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    /** Display-friendly summary of active filters. */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (keyword  != null && !keyword.isBlank())  sb.append("\"").append(keyword).append("\" ");
        if (brand    != null && !brand.isBlank())    sb.append("Brand:").append(brand).append(" ");
        if (minPrice > 0)                            sb.append("Min:$").append((int)minPrice).append(" ");
        if (maxPrice > 0)                            sb.append("Max:$").append((int)maxPrice).append(" ");
        if (fromYear > 0)                            sb.append("From:").append(fromYear).append(" ");
        if (toYear   > 0)                            sb.append("To:").append(toYear).append(" ");
        if (type     != null && !type.isBlank())     sb.append("Type:").append(type);
        return sb.toString().trim();
    }
}
