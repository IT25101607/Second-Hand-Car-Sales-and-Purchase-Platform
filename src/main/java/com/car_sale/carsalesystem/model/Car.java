package com.car_sale.carsalesystem.model;

/**
 * Car – base class for all car listings.
 * Demonstrates Encapsulation (private fields + getters/setters).
 *
 * File format (cars.txt): carId|sellerId|brand|model|year|price|condition|description|type|imageUrl
 */
public class Car {

    // ── Private fields (Encapsulation) ────────────────────────────────────────
    private String carId;
    private String sellerId;   // links to User.userId
    private String brand;
    private String model;
    private int    year;
    private double price;
    private String condition;  // e.g. "New", "Used", "Certified"
    private String description;
    private String type;       // "CAR" | "USED_CAR" | "CERTIFIED_CAR"
    private String imageUrl;   // Optional URL to car image

    // ── Constructors ──────────────────────────────────────────────────────────
    public Car() {}

    public Car(String carId, String sellerId, String brand, String model,
               int year, double price, String condition, String description, String type) {
        this.carId       = carId;
        this.sellerId    = sellerId;
        this.brand       = brand;
        this.model       = model;
        this.year        = year;
        this.price       = price;
        this.condition   = condition;
        this.description = description;
        this.type        = type;
        this.imageUrl    = "";
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getCarId()               { return carId; }
    public void   setCarId(String carId)   { this.carId = carId; }

    public String getSellerId()                  { return sellerId; }
    public void   setSellerId(String sellerId)   { this.sellerId = sellerId; }

    public String getBrand()               { return brand; }
    public void   setBrand(String brand)   { this.brand = brand; }

    public String getModel()               { return model; }
    public void   setModel(String model)   { this.model = model; }

    public int  getYear()            { return year; }
    public void setYear(int year)    { this.year = year; }

    public double getPrice()               { return price; }
    public void   setPrice(double price)   { this.price = price; }

    public String getCondition()                 { return condition; }
    public void   setCondition(String condition) { this.condition = condition; }

    public String getDescription()                   { return description; }
    public void   setDescription(String description) { this.description = description; }

    public String getType()              { return type; }
    public void   setType(String type)   { this.type = type; }

    public String getImageUrl()                  { return imageUrl != null ? imageUrl : ""; }
    public void   setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }

    // ── Display method – overridden in subclasses (Polymorphism) ─────────────
    public String displayInfo() {
        return String.format("%s %s (%d) – $%.2f [%s]", brand, model, year, price, condition);
    }

    // ── File serialisation ────────────────────────────────────────────────────
    /**
     * Serialises to pipe-delimited line for cars.txt.
     * Format: carId|sellerId|brand|model|year|price|condition|description|type|imageUrl
     */
    public String toFileString() {
        return String.join("|",
                carId, sellerId, brand, model,
                String.valueOf(year), String.valueOf(price),
                condition, description, type,
                imageUrl != null ? imageUrl : "");
    }

    /**
     * Deserialises a line from cars.txt into the correct subtype.
     */
    public static Car fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 9) return null;

        String id       = p[0];
        String sid      = p[1];
        String brand    = p[2];
        String model    = p[3];
        int    year     = parseIntSafe(p[4]);
        double price    = parseDoubleSafe(p[5]);
        String cond     = p[6];
        String desc     = p[7];
        String type     = p[8];
        // index 9 = imageUrl for base Car; sub-types have their extras at 10+
        String imgUrl   = p.length > 9 ? p[9] : "";

        switch (type.toUpperCase()) {
            case "USED_CAR":
                UsedCar uc = new UsedCar(id, sid, brand, model, year, price, desc);
                uc.setImageUrl(imgUrl);
                if (p.length > 10) uc.setMileage(parseIntSafe(p[10]));
                return uc;
            case "CERTIFIED_CAR":
                CertifiedCar cc = new CertifiedCar(id, sid, brand, model, year, price, desc);
                cc.setImageUrl(imgUrl);
                if (p.length > 10) cc.setCertificationBody(p[10]);
                if (p.length > 11) cc.setWarrantyMonths(parseIntSafe(p[11]));
                return cc;
            default:
                Car car = new Car(id, sid, brand, model, year, price, cond, desc, "CAR");
                car.setImageUrl(imgUrl);
                return car;
        }
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }
    private static double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0.0; }
    }
}
