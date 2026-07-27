package com.cocinarubi.presentation.dto.response.busqueda;

import java.util.List;

/**
 * Respuesta de búsqueda cross-catálogo con estructura equivalente a Spring Page.
 * Cada ítem de content incluye el campo categoria para identificar su tipo.
 */
public class ResultadoBusquedaResponseDTO {

    private List<ItemBusquedaResponseDTO> content;
    private PageableInfo pageable;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private int size;
    private int number;
    private SortInfo sort;
    private int numberOfElements;
    private boolean empty;

    public ResultadoBusquedaResponseDTO() {}

    public ResultadoBusquedaResponseDTO(List<ItemBusquedaResponseDTO> content,
                                        PageableInfo pageable,
                                        long totalElements, int totalPages,
                                        boolean first, boolean last,
                                        int size, int number,
                                        SortInfo sort,
                                        int numberOfElements, boolean empty) {
        this.content = content;
        this.pageable = pageable;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.size = size;
        this.number = number;
        this.sort = sort;
        this.numberOfElements = numberOfElements;
        this.empty = empty;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public List<ItemBusquedaResponseDTO> getContent() { return content; }
    public void setContent(List<ItemBusquedaResponseDTO> content) { this.content = content; }

    public PageableInfo getPageable() { return pageable; }
    public void setPageable(PageableInfo pageable) { this.pageable = pageable; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public SortInfo getSort() { return sort; }
    public void setSort(SortInfo sort) { this.sort = sort; }

    public int getNumberOfElements() { return numberOfElements; }
    public void setNumberOfElements(int numberOfElements) { this.numberOfElements = numberOfElements; }

    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }

    // ── Clases internas — equivalen a los sub-objetos de Spring Page ─────────

    public static class PageableInfo {
        private int pageNumber;
        private int pageSize;
        private SortInfo sort;
        private long offset;
        private boolean unpaged;
        private boolean paged;

        public PageableInfo() {}

        public PageableInfo(int pageNumber, int pageSize, SortInfo sort,
                            long offset, boolean unpaged, boolean paged) {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.sort = sort;
            this.offset = offset;
            this.unpaged = unpaged;
            this.paged = paged;
        }

        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }

        public SortInfo getSort() { return sort; }
        public void setSort(SortInfo sort) { this.sort = sort; }

        public long getOffset() { return offset; }
        public void setOffset(long offset) { this.offset = offset; }

        public boolean isUnpaged() { return unpaged; }
        public void setUnpaged(boolean unpaged) { this.unpaged = unpaged; }

        public boolean isPaged() { return paged; }
        public void setPaged(boolean paged) { this.paged = paged; }
    }

    public static class SortInfo {
        private boolean empty;
        private boolean sorted;
        private boolean unsorted;

        public SortInfo() {}

        public SortInfo(boolean empty, boolean sorted, boolean unsorted) {
            this.empty = empty;
            this.sorted = sorted;
            this.unsorted = unsorted;
        }

        public boolean isEmpty() { return empty; }
        public void setEmpty(boolean empty) { this.empty = empty; }

        public boolean isSorted() { return sorted; }
        public void setSorted(boolean sorted) { this.sorted = sorted; }

        public boolean isUnsorted() { return unsorted; }
        public void setUnsorted(boolean unsorted) { this.unsorted = unsorted; }
    }
}
