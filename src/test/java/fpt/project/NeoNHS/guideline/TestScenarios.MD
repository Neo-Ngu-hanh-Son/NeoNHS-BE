# Test Scenarios Guideline for API Features

This document provides a standard guideline for writing API test scenarios for Workshop features (and other similar features). It follows the structure established during the `Create Workshop Template` implementation.

## Template Format

Whenever you write test scenarios for a new API endpoint, explicitly define them using the following structure:

### [Scenario ID] - [Scenario Description]
*   **Condition**
    *   **+Precondition**: [State requirements before execution, e.g., User is authenticated with VENDOR role, resource exists in DB, etc.]
    *   **+Input**: [Request body, path variables, query params, headers]
*   **Confirm**
    *   **+Return**: [Expected HTTP Status Code and Response Body jsonPath assertions]
    *   **+Exception**: [Expected Exception thrown by the service layer, e.g., BadRequestException, ResourceNotFoundException, or None]
    *   **+Log message**: [Expected log output, e.g., output from `andDo(print())` or specific application logger messages]
*   **Result**
    *   **+Type**: [(N : Normal) for happy paths, (A : Abnormal) for validation/error scenarios, (B : Boundary) for edge cases]
    *   **+Passed/Failed**: [Passed/Failed]

---

## Examples (Based on Create Workshop Template)

### UTCID01 - Normal successful creation (Happy Path)
*   **Condition**
    *   **+Precondition**: User is authenticated with `VENDOR` role. Vendor is verified.
    *   **+Input**: Valid `CreateWorkshopTemplateRequest` payload with correct lengths, participants, and valid tag IDs.
*   **Confirm**
    *   **+Return**: HTTP 201 Created. Response JSON contains `{"message": "Workshop template created successfully"}`.
    *   **+Exception**: None
    *   **+Log message**: Request and response details printed to console.
*   **Result**
    *   **+Type**: N (Normal)
    *   **+Passed/Failed**: Passed

### UTCID04 - Vendor not verified
*   **Condition**
    *   **+Precondition**: User is authenticated with `VENDOR` role, but their vendor profile is UNVERIFIED.
    *   **+Input**: Valid `CreateWorkshopTemplateRequest` payload.
*   **Confirm**
    *   **+Return**: HTTP 400 Bad Request. JSON contains `{"success": false, "message": "Only verified vendors can create workshop templates"}`.
    *   **+Exception**: `BadRequestException`
    *   **+Log message**: Request and response details printed to console.
*   **Result**
    *   **+Type**: A (Abnormal)
    *   **+Passed/Failed**: Passed

### UTCID09 - Boundary condition: minParticipants equals maxParticipants
*   **Condition**
    *   **+Precondition**: User is authenticated with `VENDOR` role and is verified.
    *   **+Input**: Valid `CreateWorkshopTemplateRequest` payload where `minParticipants` = 10 and `maxParticipants` = 10.
*   **Confirm**
    *   **+Return**: HTTP 201 Created. Response JSON contains `{"message": "Workshop template created successfully"}`.
    *   **+Exception**: None
    *   **+Log message**: Request and response details printed to console.
*   **Result**
    *   **+Type**: B (Boundary)
    *   **+Passed/Failed**: Passed
