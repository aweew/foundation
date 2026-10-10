import type { CSSProperties } from 'vue';

/** 表格列内容的水平对齐方式 */
export type ProTableAlign = 'left' | 'center' | 'right';

/** 表格列支持的内置渲染类型 */
export type ProTableColumnType = 'selection' | 'index' | 'text' | 'status' | 'datetime' | 'number' | 'image' | 'action';

/** 状态列的展示配置 */
export interface ProTableStatusOption {
  /** 状态文本 */
  label: string;
  /** 状态颜色类型 */
  type?: 'success' | 'warning' | 'danger' | 'info' | 'primary';
}

/** 表格列筛选项 */
export interface ProTableFilterOption {
  /** 筛选项展示文本 */
  text: string;
  /** 筛选项提交值 */
  value: string | number | boolean;
}

/** 表格列定义 */
export interface ProTableColumn<T extends Record<string, unknown> = Record<string, unknown>> {
  /** 绑定的数据字段 */
  prop?: keyof T & string;
  /** 表头文本 */
  label?: string;
  /** 列固定宽度 */
  width?: number | string;
  /** 列最小宽度 */
  minWidth?: number | string;
  /** 是否固定列或固定方向 */
  fixed?: boolean | 'left' | 'right';
  /** 单元格内容对齐方式 */
  align?: ProTableAlign;
  /** 内置列渲染类型 */
  type?: ProTableColumnType;
  /** 自定义单元格格式化函数 */
  formatter?: (row: T, column: ProTableColumn<T>, value: unknown) => unknown;
  /** 是否显示复制按钮 */
  copyable?: boolean;
  /** 是否支持排序，custom 表示由业务侧处理排序 */
  sortable?: boolean | 'custom';
  /** 是否显示该列 */
  visible?: boolean;
  /** 自定义单元格插槽名称 */
  slot?: string;
  /** 状态值到展示配置的映射 */
  statusMap?: Record<string | number, ProTableStatusOption>;
  /** 列筛选项 */
  filters?: ProTableFilterOption[];
  /** 自定义筛选匹配函数 */
  filterMethod?: (value: string | number | boolean, row: T, column: ProTableColumn<T>) => boolean;
  /** 图片列的宽高尺寸 */
  imageSize?: number;
  /** 追加到列单元格的 CSS 类名 */
  className?: string;
}

/** 表格分页参数 */
export interface ProTablePagination {
  /** 当前页码，从 1 开始 */
  current: number;
  /** 每页显示条数 */
  size: number;
}

/** ProTable 组件参数 */
export interface ProTableProps<T extends Record<string, unknown> = Record<string, unknown>> {
  /** 表格列定义 */
  columns: ProTableColumn<T>[];
  /** 表格数据 */
  data: T[];
  /** 是否显示加载状态 */
  loading?: boolean;
  /** 加载失败时展示的错误信息 */
  error?: string;
  /** 数据总条数，用于分页器 */
  total?: number;
  /** 分页状态，不传时隐藏分页页脚 */
  pagination?: ProTablePagination;
  /** 行唯一标识字段或计算函数 */
  rowKey?: string | ((row: T) => string | number);
  /** 列配置本地持久化使用的存储键 */
  storageKey?: string;
  /** 无数据时展示的说明文本 */
  emptyDescription?: string;
  /** 表格高度，传入后启用固定高度滚动 */
  tableHeight?: string | number;
  /** 是否启用选择能力 */
  selectable?: boolean;
  /** 是否使用斑马纹行样式 */
  stripe?: boolean;
  /** 表头单元格自定义样式 */
  headerCellStyle?: CSSProperties;
}
