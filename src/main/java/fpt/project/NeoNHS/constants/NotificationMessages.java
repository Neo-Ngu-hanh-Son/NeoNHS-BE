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
    public static final String TYPE_WALLET_PAYOUT = "WALLET_PAYOUT";

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

    // ──── Report Notifications ───────────────────────────────────────────────
    public static final String TYPE_REPORT_RESOLVED = "REPORT_RESOLVED";
    public static final String TYPE_REPORT_REJECTED = "REPORT_REJECTED";

    public static final String REPORT_RESOLVED_TITLE = "✅ Report Resolved";
    public static final String REPORT_RESOLVED_MESSAGE_TEMPLATE = "Thank you for your contribution! Your report about \"%s\" has been reviewed and resolved. We appreciate your effort to improve Ngu Hanh Son.";

    public static final String REPORT_REJECTED_TITLE = "📋 Report Reviewed";
    public static final String REPORT_REJECTED_MESSAGE_TEMPLATE = "Thank you for your report about \"%s\". After careful review, we determined no further action is needed at this time. We still appreciate your vigilance!";

    public static final String ADMIN_NOTE_SUFFIX = "\n\nAdmin's note: \"%s\"";

    public static String reportResolvedTitle() {
        return REPORT_RESOLVED_TITLE;
    }

    public static String reportResolvedMessage(String targetName, String adminNote) {
        String base = String.format(REPORT_RESOLVED_MESSAGE_TEMPLATE, targetName);
        if (adminNote != null && !adminNote.isBlank()) {
            base += String.format(ADMIN_NOTE_SUFFIX, adminNote);
        }
        return base;
    }

    public static String reportRejectedTitle() {
        return REPORT_REJECTED_TITLE;
    }

    public static String reportRejectedMessage(String targetName, String adminNote) {
        String base = String.format(REPORT_REJECTED_MESSAGE_TEMPLATE, targetName);
        if (adminNote != null && !adminNote.isBlank()) {
            base += String.format(ADMIN_NOTE_SUFFIX, adminNote);
        }
        return base;
    }

    // ──── Wallet Payout Notifications ───────────────────────────────────────
    public static final String WALLET_PAYOUT_TITLE = "💰 Wallet Credited!";
    public static final String WALLET_PAYOUT_MESSAGE_TEMPLATE = "You have received %s VNĐ into your wallet for the workshop session: %s";

    public static String walletPayoutTitle() {
        return WALLET_PAYOUT_TITLE;
    }

    public static String walletPayoutMessage(BigDecimal amount, String sessionTitle) {
        return String.format(WALLET_PAYOUT_MESSAGE_TEMPLATE, amount.toString(), sessionTitle);
    }
}
