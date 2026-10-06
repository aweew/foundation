export interface ApiResult<T> {
  code: number;
  data: T;
  msg: string;
}

export interface PageResponse<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}
