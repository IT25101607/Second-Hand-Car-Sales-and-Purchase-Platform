package com.car_sale.carsalesystem.util;

import org.springframework.stereotype.Component;

/**
 * ReviewHelper – Thymeleaf utility bean for building star strings.
 * Registered as a Spring bean so Thymeleaf can call it via @reviewHelper.
 */
@Component("reviewHelper")
public class ReviewHelper {

    /** Builds a star string like ★★★☆☆ for a given (possibly decimal) rating. */
    public String buildStars(double rating) {
        int rounded = (int) Math.round(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= rounded ? "★" : "☆");
        return sb.toString();
    }
}
