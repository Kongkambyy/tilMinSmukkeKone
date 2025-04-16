package com.example.tilminsmukkekone.application;

import com.example.tilminsmukkekone.domain.classes.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class LoveService {

    private static final List<String> LOVE_COUPONS = new ArrayList<>();
    private static final LocalDate DEFAULT_ANNIVERSARY = LocalDate.of(2018, 2, 4);

    static {
        LOVE_COUPONS.add("Fuld massage ❤️");
        LOVE_COUPONS.add("Træk en pille ❤️");
        LOVE_COUPONS.add("Tryk en gang til ❤️");
        LOVE_COUPONS.add("Vælg noget vi skal lave sammen ❤️");
        LOVE_COUPONS.add("Gør rent eller støvsug hele huset for dig ❤️");
        LOVE_COUPONS.add("Et dejligt fodbad eller fodmassage ❤️");
        LOVE_COUPONS.add("Et kys ❤️");
        LOVE_COUPONS.add("Du bestemmer film og snacks til en hyggeaften ❤️");
        LOVE_COUPONS.add("Jeg bestemmer date-aktivitet ❤️");
        LOVE_COUPONS.add("Jeg bestemmer hvad vi skal lave sammen ❤️");
        LOVE_COUPONS.add("Et kys ❤️");
        LOVE_COUPONS.add("Et kys ❤️");
        LOVE_COUPONS.add("Et kys ❤️");
        LOVE_COUPONS.add("Et kys ❤️");
    }

    public long getDaysTogether(User user) {
        LocalDate anniversary = getAnniversaryDate(user);
        return ChronoUnit.DAYS.between(anniversary, LocalDate.now());
    }

    public long getDaysUntilNextAnniversary(User user) {
        LocalDate anniversary = getAnniversaryDate(user);
        LocalDate nextAnniversary = getNextAnniversaryDate(anniversary);
        return ChronoUnit.DAYS.between(LocalDate.now(), nextAnniversary);
    }

    public LocalDate getNextAnniversaryDate(User user) {
        LocalDate anniversary = getAnniversaryDate(user);
        return getNextAnniversaryDate(anniversary);
    }

    public int getRelationshipYears(User user) {
        LocalDate anniversary = getAnniversaryDate(user);
        return Period.between(anniversary, LocalDate.now()).getYears();
    }

    public String getRandomLoveCoupon() {
        Random random = new Random();
        int index = random.nextInt(LOVE_COUPONS.size());
        return LOVE_COUPONS.get(index);
    }

    public List<String> getAllLoveCoupons() {
        return new ArrayList<>(LOVE_COUPONS);
    }

    private LocalDate getAnniversaryDate(User user) {
        if (user != null && user.getAnniversary() != null) {
            return user.getAnniversary();
        }
        return DEFAULT_ANNIVERSARY;
    }

    private LocalDate getNextAnniversaryDate(LocalDate anniversary) {
        LocalDate now = LocalDate.now();
        LocalDate nextAnniversary = anniversary.withYear(now.getYear());

        if (nextAnniversary.isBefore(now) || nextAnniversary.isEqual(now)) {
            nextAnniversary = nextAnniversary.plusYears(1);
        }

        return nextAnniversary;
    }
}