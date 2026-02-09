package fpt.project.NeoNHS.enums;

public enum WorkshopStatus {
    DRAFT,      // Template saved but not submitted for approval
    PENDING,    // Submitted and awaiting admin approval
    ACTIVE,     // Approved by admin and published
    REJECTED    // Rejected by admin
}
