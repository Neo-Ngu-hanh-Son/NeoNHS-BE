package fpt.project.NeoNHS.constants;

/**
 * Constants for pagination configuration across the application.
 */
public final class PaginationConstants {

    private PaginationConstants() {
        // Prevent instantiation
    }

    /**
     * Default page number (0-indexed)
     */
    public static final String DEFAULT_PAGE = "0";

    /**
     * Default page size
     */
    public static final String DEFAULT_SIZE = "10";

    /**
     * Default sort field
     */
    public static final String DEFAULT_SORT_BY = "createdAt";

    /**
     * Default sort direction (descending)
     */
    public static final String DEFAULT_SORT_DIR = "desc";

    /**
     * Sort direction: ascending
     */
    public static final String SORT_ASC = "asc";

    /**
     * Sort direction: descending
     */
    public static final String SORT_DESC = "desc";

    /**
     * Maximum allowed page size
     */
    public static final int MAX_PAGE_SIZE = 100;
}
