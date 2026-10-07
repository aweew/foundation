# Foundation 后端脚手架

基于 Java 17、Spring Boot 3.5、MyBatis-Plus、Sa-Token、Redis 和 Flyway，提供用户角色菜单、操作审计、统一异常、分页和对象存储能力。

## 本地启动

1. 使用 Java 17，配置 IDE 的 Maven 项目。
2. 在项目目录运行 `docker compose up -d`，启动仅绑定本机地址的 MySQL 和 Redis。
3. 启动 `FoundationApplication`，默认使用 `local` 环境。Flyway 会初始化 `foundation` 数据库。
4. API 根路径为 `http://localhost:6001/foundation`，接口文档为 `http://localhost:6001/foundation/swagger-ui.html`。

`.env.example` 提供本地变量示例。Docker Compose 和 Spring Boot 都会读取启动工作目录下的 `.env`；Spring Boot 通过 `spring.config.import` 按 Properties 格式加载，值不要加引号或 `export`。IDE 的工作目录应设为 `foundation-be`。已将实际 `.env` 文件加入 Git 忽略规则。

连接已有数据库时，在 `.env` 中设置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`。如果缺少 `DB_URL`，本地环境会使用默认的 `localhost:3306/foundation`。

初始账号和角色来自 `V1__foundation_schema.sql`。管理员密码采用 BCrypt 存储，登录、创建用户和改密的密码传输格式必须与前端约定一致。部署前应确认初始管理员账号归属并重置密码。

## 环境配置

| 环境 | 用途 | 默认行为 |
| --- | --- | --- |
| `local` | 本地开发 | 本机数据库、开发密钥、Swagger 和 SQL 调试日志 |
| `test` | 集成测试 | `foundation_test` 数据库、Redis DB 2、独立业务键前缀，关闭 Swagger |
| `prod` | 生产部署 | 强制预检连接信息和密钥，关闭 Swagger、SQL 明细日志，启用优雅关闭 |

生产通过 `SPRING_PROFILES_ACTIVE=prod` 启用，不能与 `local` 或 `test` 同时启用。

| 环境变量 | 说明 |
| --- | --- |
| `DB_URL` | JDBC 地址，生产使用 `jdbc:mysql://...` |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号和密码 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 连接信息，生产必须配置密码 |
| `REDIS_DATABASE` | Redis 数据库编号，默认 1，测试默认 2 |
| `REDIS_KEY_PREFIX` | 业务键前缀，多个项目共用 Redis 时设置独立前缀 |
| `JWT_SECRET_KEY` | JWT 签名密钥，生产至少 32 字节 |
| `FOUNDATION_STORAGE_SECRET_KEY` | 存储凭据加密密钥，生产至少 32 字节，建议随机生成 32 个 ASCII 字符 |
| `CORS_ALLOWED_ORIGIN` | 允许跨域的前端地址，默认本地 3000 端口 |
| `SERVER_PORT` | 后端端口，默认 6001 |

生产配置错误会在数据库和 Redis Bean 初始化前终止启动，错误信息仅显示配置项名称。开发密钥不能用于生产，JWT 与存储加密必须使用不同密钥。

存储凭据加密密钥必须长期保留。已有数据库迁移时应配置原密钥，密钥变更后需要重新录入存储认证信息；已有默认开发密钥的数据迁入生产时，应先重新录入并加密认证信息。替换 JWT 密钥会使旧令牌失效。

Flyway 默认校验迁移文件，关闭自动 baseline。已有非空数据库首次接入时，应核对表结构和迁移版本后显式建立 baseline。已执行的迁移文件保持不变，新结构使用新增版本迁移。

## 接口开发约定

- 接口默认必须登录，匿名接口使用 `@SaIgnore` 显式声明。
- 管理接口添加 `@SaCheckPermission`，权限标识与菜单配置一致；登录校验不能代替业务权限校验。
- 健康探针、接口文档资源和错误页面通过明确路径放行；生产默认关闭接口文档。
- 前端沿用 `satoken` 请求头，跨域允许该头；响应中的 `traceId` 可由前端读取。
- 分页接口接收 `@Valid PageRequest`，通过 `createPage(Entity.class)` 构造数据库分页对象，禁止直接绑定 MyBatis-Plus `Page`。
- 分页参数保留 `current`、`size`，默认 1、10，每页最多 200 条；排序使用成对的 `orderBy`、`orderType`，如 `orderBy=create_time,id&orderType=desc,asc`。
- 排序字段必须属于当前实体的 MyBatis 数据库列映射；非法字段、方向和分页范围返回统一参数错误。服务端补充排序使用 `addOrderItem`。
- 业务异常通过 `BusinessException` 和 `ErrorCodeEnum` 返回；写操作检查实际结果。
- 新增功能遵循 Java 全局规范：字段注入、DTO 字段注释、方法 Javadoc、业务变量命名和统一工具类。

## 代码生成

安装 EasyCode 插件，导入 `docs/easyCode/EasyCodeConfig.json`，在数据库表上生成代码。

Controller 模板已包含安全分页、权限注解、审计注解、请求校验和写操作结果检查。默认权限标识为 `模块名:list/view/save/update/delete`，生成后按业务命名空间调整，并登记对应菜单权限。

模板生成的是基础 CRUD，业务校验、关联关系、事务边界和敏感字段处理需要在相应 Service 中补齐。

## 验证

鉴权、分页和生产配置测试独立于 MySQL、Redis，可单独运行：

```sh
mvn -Dtest=PageRequestTest,DefaultAuthenticationTest,ProductionEnvironmentPostProcessorTest test
```

`FoundationApplicationTests` 使用 `test` 环境，需要先创建 `foundation_test` 数据库、授予测试账号权限并准备 Redis。默认本地 Compose 只创建 `foundation` 数据库，测试数据库应单独准备。

本批改造范围为环境配置、默认鉴权、安全分页和代码生成模板。限流原子性、异步线程池、模块条件装配及 CI 留待后续批次。
