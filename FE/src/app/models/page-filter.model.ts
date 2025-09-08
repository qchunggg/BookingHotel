export interface PageFilter {
    page?:      number;
    size?:      number;
    sort?:      string;
}

export interface Page<T> {
  content:       T[];
  totalElements: number;
  totalPages:    number;
  number:        number;   // trang hiện tại (0-based)
  size:          number;   // limit
}
