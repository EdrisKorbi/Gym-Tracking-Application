export interface Page<T> {
    content: T[];      // The actual list of items (e.g., array of Users)
    pageable: {
        pageNumber: number;
        pageSize: number;
        offset: number;
    };
    last: boolean;     // True if this is the last page
    totalElements: number; // Total items across all pages in the database
    totalPages: number;
    size: number;
    number: number;    // Current page number (0-indexed)
    first: boolean;    // True if this is the first page
    empty: boolean;    // True if the content array is empty
}