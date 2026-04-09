package fpt.project.NeoNHS.constants;

import java.math.BigDecimal;

public final class NotificationMessages {

    private NotificationMessages() {
        // Utility class — prevent instantiation
    }

    // ──── Notification Types ────────────────────────────────────────────────
    public static final String TYPE_EVENT = "EVENT";
    public static final String TYPE_CHECKIN_SUCCESS = "CHECKIN_SUCCESS";
    public static final String TYPE_ORDER_SUCCESS = "ORDER_SUCCESS";

    // ──── Event Notifications ───────────────────────────────────────────────
    public static final String EVENT_TITLE_PREFIX = "New event: ";
    public static final String EVENT_MESSAGE = "Ngu Hanh Son Ward will host a new event soon. Stay tuned!";

    public static String eventTitle(String eventName) {
        return EVENT_TITLE_PREFIX + eventName;
    }

    public static String eventMessage() {
        return EVENT_MESSAGE;
    }

    // ──── Check-in Notifications ────────────────────────────────────────────
    public static final String CHECKIN_TITLE = "\uD83D\uDCCD Check-in Successful!";
    public static final String CHECKIN_MESSAGE_TEMPLATE = "You've checked in at a new location and earned %d reward points.";

    public static String checkinTitle() {
        return CHECKIN_TITLE;
    }

    public static String checkinMessage(int rewardPoints) {
        return String.format(CHECKIN_MESSAGE_TEMPLATE, rewardPoints);
    }

    // ──── Order / Payment Notifications ─────────────────────────────────────
    public static final String ORDER_SUCCESS_TITLE = "🎉 Payment Successful!";
    public static final String ORDER_SUCCESS_MESSAGE_TEMPLATE = "Your order has been paid successfully for the amount of %s VNĐ";

    public static String orderSuccessTitle() {
        return ORDER_SUCCESS_TITLE;
    }

    public static String orderSuccessMessage(BigDecimal finalAmount) {
        return String.format(ORDER_SUCCESS_MESSAGE_TEMPLATE, finalAmount.toString());
    }
}
