package fpt.project.NeoNHS.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Generic API Response wrapper for standardized response format.
 * Used for both success and error responses across all API endpoints.
 *
 * @param <T> The type of data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * HTTP status code (e.g., 200, 201, 400, 404, 500)
     */
    private int status;

    /**
     * Indicates whether the request was successful
     */
    private boolean success;

    /**
     * Human-readable message describing the result
     */
    private String message;

    /**
     * Response payload data (null for error responses)
     */
    private T data;

    /**
     * Timestamp when the response was generated
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Creates a success response with custom message and data.
     *
     * @param message Success message
     * @param data    Response payload
     * @param <T>     Type of data
     * @return ApiResponse with success status (200 OK)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a success response with default message.
     *
     * @param data Response payload
     * @param <T>  Type of data
     * @return ApiResponse with success status (200 OK)
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a success response with custom status code.
     *
     * @param status  HTTP status
     * @param message Success message
     * @param data    Response payload
     * @param <T>     Type of data
     * @return ApiResponse with specified status
     */
    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with default 400 Bad Request status.
     *
     * @param message Error message
     * @param <T>     Type of data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with custom HTTP status.
     *
     * @param status  HTTP status code
     * @param message Error message
     * @param <T>     Type of data
     * @return ApiResponse with specified error status
     */
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
