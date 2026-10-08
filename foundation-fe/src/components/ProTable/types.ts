import type { CSSProperties } from 'vue';

export type ProTableAlign = 'left' | 'center' | 'right';
export type ProTableColumnType =
  | 'selection'
  | 'index'
  | 'text'
  | 'status'
  | 'datetime'
  | 'number'
  | 'image'
  | 'action';

export interface ProTableStatusOption {
  label: string;
  type?: 'success' | 'warning' | 'danger' | 'info' | 'primary';
}

export interface ProTableFilterOption {
  text: string;
  value: string | number | boolean;
}

export interface ProTableColumn<T extends Record<string, unknown> = Record<string, unknown>> {
  prop?: keyof T & string;
  label?: string;
  width?: number | string;
  minWidth?: number | string;
  fixed?: boolean | 'left' | 'right';
  align?: ProTableAlign;
  type?: ProTableColumnType;
  formatter?: (row: T, column: ProTableColumn<T>, value: unknown) => unknown;
  copyable?: boolean;
  sortable?: boolean | 'custom';
  visible?: boolean;
  slot?: string;
  statusMap?: Record<string | number, ProTableStatusOption>;
  filters?: ProTableFilterOption[];
  filterMethod?: (value: string | number | boolean, row: T, column: ProTableColumn<T>) => boolean;
  imageSize?: number;
  className?: string;
}

export interface ProTablePagination {
  current: number;
  size: number;
}

export interface ProTableProps<T extends Record<string, unknown> = Record<string, unknown>> {
  columns: ProTableColumn<T>[];
  data: T[];
  loading?: boolean;
  error?: string;
  total?: number;
  pagination?: ProTablePagination;
  rowKey?: string | ((row: T) => string | number);
  storageKey?: string;
  emptyDescription?: string;
  tableHeight?: string | number;
  selectable?: boolean;
  stripe?: boolean;
  headerCellStyle?: CSSProperties;
}
