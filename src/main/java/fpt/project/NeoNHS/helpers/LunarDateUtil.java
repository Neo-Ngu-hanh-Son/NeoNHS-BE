package fpt.project.NeoNHS.helpers;

import java.time.LocalDate;

/**
 * Utility class for converting Solar (Gregorian) dates to Vietnamese Lunar dates.
 * Based on the algorithm by Hồ Ngọc Đức (https://www.informatik.uni-leipzig.de/~duc/amlich/).
 * Timezone: GMT+7 (Vietnam).
 */
public class LunarDateUtil {

    private static final double PI = Math.PI;
    private static final int TIMEZONE_OFFSET = 7; // Vietnam GMT+7

    /**
     * Converts a Solar (Gregorian) date to a Vietnamese Lunar date string.
     *
     * @param solarDate the solar date to convert
     * @return a string in the format "dd/MM (Âm lịch)", e.g. "15/01 (Âm lịch)"
     */
    public static String convertSolarToLunar(LocalDate solarDate) {
        int dd = solarDate.getDayOfMonth();
        int mm = solarDate.getMonthValue();
        int yy = solarDate.getYear();

        int[] lunarDate = convertSolar2Lunar(dd, mm, yy, TIMEZONE_OFFSET);
        int lunarDay = lunarDate[0];
        int lunarMonth = lunarDate[1];
        int lunarYear = lunarDate[2];
        boolean isLeapMonth = lunarDate[3] != 0;

        String monthStr = String.format("%02d", lunarMonth);
        if (isLeapMonth) {
            monthStr += " (nhuận)";
        }

        return String.format("%02d/%s/%d (Âm lịch)", lunarDay, monthStr, lunarYear);
    }

    /**
     * Converts a Solar date to Lunar date.
     *
     * @return int[4] = {lunarDay, lunarMonth, lunarYear, isLeapMonth (0 or 1)}
     */
    public static int[] convertSolar2Lunar(int dd, int mm, int yy, int timeZone) {
        long dayNumber = jdFromDate(dd, mm, yy);
        int k = (int) Math.floor((dayNumber - 2415021.076998695) / 29.530588853);

        long monthStart = getNewMoonDay(k + 1, timeZone);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone);
        }

        long a11 = getLunarMonth11(yy, timeZone);
        long b11 = a11;
        int lunarYear;
        if (a11 >= monthStart) {
            lunarYear = yy;
            a11 = getLunarMonth11(yy - 1, timeZone);
        } else {
            lunarYear = yy + 1;
            b11 = getLunarMonth11(yy + 1, timeZone);
        }

        int lunarDay = (int) (dayNumber - monthStart + 1);
        int diff = (int) Math.floor((monthStart - a11) / 29.0 + 0.5);
        int isLeapMonth = 0;
        int lunarMonth = diff + 11;

        if (b11 - a11 > 365) {
            int leapMonthDiff = getLeapMonthOffset(a11, timeZone);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    isLeapMonth = 1;
                }
            }
        }

        if (lunarMonth > 12) {
            lunarMonth = lunarMonth - 12;
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1;
        }

        return new int[]{lunarDay, lunarMonth, lunarYear, isLeapMonth};
    }

    // ============ Private helper methods (Hồ Ngọc Đức algorithm) ============

    private static long jdFromDate(int dd, int mm, int yy) {
        int a = (14 - mm) / 12;
        int y = yy + 4800 - a;
        int m = mm + 12 * a - 3;
        long jd = dd + (153 * m + 2) / 5 + 365L * y + y / 4 - y / 100 + y / 400 - 32045;
        if (jd < 2299161L) {
            jd = dd + (153 * m + 2) / 5 + 365L * y + y / 4 - 32083;
        }
        return jd;
    }

    private static long getNewMoonDay(int k, int timeZone) {
        double T = k / 1236.85;
        double T2 = T * T;
        double T3 = T2 * T;
        double dr = PI / 180.0;

        double Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
        Jd1 = Jd1 + 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);

        double M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3;
        double Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3;
        double F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3;

        double C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr)
                + 0.0021 * Math.sin(2 * dr * M)
                - 0.4068 * Math.sin(Mpr * dr)
                + 0.0161 * Math.sin(dr * 2 * Mpr)
                - 0.0004 * Math.sin(dr * 3 * Mpr)
                + 0.0104 * Math.sin(dr * 2 * F)
                - 0.0051 * Math.sin(dr * (M + Mpr))
                - 0.0074 * Math.sin(dr * (M - Mpr))
                + 0.0004 * Math.sin(dr * (2 * F + M))
                - 0.0004 * Math.sin(dr * (2 * F - M))
                - 0.0006 * Math.sin(dr * (2 * F + Mpr))
                + 0.0010 * Math.sin(dr * (2 * F - Mpr))
                + 0.0005 * Math.sin(dr * (2 * Mpr + M));

        double Jd;
        if (T < -11) {
            Jd = Jd1 + 0.000297 + 0.01495 * T2;
        } else {
            Jd = Jd1 + C1 - 0.00015;
        }

        return (long) Math.floor(Jd + 0.5 + (double) timeZone / 24.0);
    }

    private static long getSunLongitude(long jdn, int timeZone) {
        double T = ((double) jdn - 2451545.5 - (double) timeZone / 24.0) / 36525.0;
        double T2 = T * T;
        double dr = PI / 180.0;

        double M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
        double L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
        double DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M);
        DL = DL + (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.00029 * Math.sin(dr * 3 * M);
        double L = L0 + DL;
        L = L * dr;
        L = L - PI * 2 * Math.floor(L / (PI * 2));
        return (long) Math.floor(L / PI * 6);
    }

    private static long getLunarMonth11(int yy, int timeZone) {
        double off = jdFromDate(31, 12, yy) - 2415021.076998695;
        int k = (int) Math.floor(off / 29.530588853);
        long nm = getNewMoonDay(k, timeZone);
        long sunLong = getSunLongitude(nm, timeZone);
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1, timeZone);
        }
        return nm;
    }

    private static int getLeapMonthOffset(long a11, int timeZone) {
        int k = (int) Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        long last;
        int i = 1;
        long arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
        do {
            last = arc;
            i++;
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
        } while (arc != last && i < 14);
        return i - 1;
    }
}
