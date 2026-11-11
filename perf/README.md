# 社区项目压测脚本

这些脚本只对你指定的服务发请求。请在独立测试环境运行，不要对简历中的公网演示站点直接施加高负载。

## 放置位置

脚本位于项目根目录的 `perf/`：

- `read.js`：博客列表或详情的稳定吞吐量、P95/P99、业务失败率。
- `seed-users.js`：在测试环境创建点赞压测账号，只运行一次。使用新的 `USER_PREFIX` 可以创建新一批账号。
- `interactions.js`：多账号执行点赞、取消点赞，记录成功写入速率和耗时。

测试前先确保应用与 MySQL、Redis、RabbitMQ 正常启动。当前工作区的 `RateLimitInterceptor.java` 中有 `equallsIgnoreCase` 拼写错误，会阻止编译；先修复后再运行项目。`docker-compose.yml` 里的应用服务当前被注释，需另行启动应用。

在 Windows 上可按 [k6 官方安装说明](https://grafana.com/docs/k6/latest/set-up/install-k6/)运行 `winget install k6 --source winget`。以下命令均从项目根目录执行。先建立结果目录：

```powershell
New-Item -ItemType Directory -Force perf\results | Out-Null
```

## 1. 读取压测

先用 10 次/秒运行 1 分钟，确认脚本和数据正常：

```powershell
k6 run -e BASE_URL=http://127.0.0.1:8082 -e RATE=10 -e DURATION=1m --summary-export=perf/results/list-10.json perf/read.js
```

然后逐级提高 `RATE`，分别保存结果。`MODE=list` 是默认值，测试首页第一页缓存；详情测试需指定已发布博客 ID：

```powershell
k6 run -e BASE_URL=http://127.0.0.1:8082 -e MODE=detail -e BLOG_ID=123 -e RATE=10 -e DURATION=1m --summary-export=perf/results/detail-10.json perf/read.js
```

**注意：**当前博客详情实现每次请求都会更新 MySQL 浏览量，因此详情结果代表“缓存读取 + 浏览量写入”的完整链路。列表和详情结果分开记录，不要把详情称为纯缓存读取。两者还有各自每秒 1000 次的全局限流。

若 `business_failures` 不为零，先用较短的一轮查看失败原因。脚本会按业务码统计 `failure_reasons`，并最多打印三条失败响应样本：

```powershell
k6 run -e BASE_URL=http://127.0.0.1:8082 -e RATE=10 -e DURATION=15s -e DEBUG_FAILURES=1 perf/read.js
```

在失败原因未查清之前，不要把该轮 P95 或吞吐量写进简历。
脚本默认要求列表数据非空，避免缓存类型异常导致空列表也被算作成功；确实要测试空列表时传 `-e EXPECT_NONEMPTY=0`。`success_duration` 与 `failure_duration` 分别统计两类响应耗时。

## 2. 点赞压测

先选一个已发布博客，在测试环境创建专用账号。以下是 10 个账号的例子，实际账号为 `loaduser001` 至 `loaduser010`。密码只用于测试环境，不要写入脚本或提交到 Git：

```powershell
k6 run -e BASE_URL=http://127.0.0.1:8082 -e USER_PREFIX=loaduser -e TEST_PASSWORD=ReplaceWithTestPassword -e USERS=10 perf/seed-users.js
```

然后用这些从未点赞过测试博客的账号运行：

```powershell
k6 run -e BASE_URL=http://127.0.0.1:8082 -e USER_PREFIX=loaduser -e TEST_PASSWORD=ReplaceWithTestPassword -e BLOG_ID=123 -e VUS=10 -e RATE=1 -e DURATION=1m --summary-export=perf/results/thumb-1.json perf/interactions.js
```

一个迭代会发出点赞和取消点赞 **两个写请求**，因此 `RATE=1` 表示每秒开始一个迭代，目标约为每秒两个写请求。首次使用虚拟用户时还有登录请求，不能直接用总 `http_reqs` 当成点赞吞吐；看 `successful_writes` 的每秒速率和 `write_duration` 的 P95。k6 的 `noCookiesReset` 用于让每个虚拟用户的登录 Cookie 跨迭代保留。

博客 ID 直接以原始数字文本放入请求体，避免 JavaScript 对较大的 `Long` ID 产生精度损失。

## 3. 判断结果是否有效

每轮保存：目标速率、`http_reqs` 或 `successful_writes` 的实际每秒速率、`http_req_duration` 或 `write_duration` 的 P95/P99、`business_failures`、`dropped_iterations`、测试时长及服务器配置。脚本检查响应 JSON 的 `code`，因为业务异常可能仍返回 HTTP 200。

若 `dropped_iterations > 0`，说明负载机未按目标速率发出所有迭代；先增加 `VUS` 和相应测试账号，再检查负载机资源。正式数据建议在稳定速率下持续 10 分钟，而不采用瞬时峰值。

点赞测试结束后，等待异步消息处理完成，再核对测试博客在 Redis 和 MySQL 的点赞数是否回到压测前基线、测试账号是否都处于未点赞状态，并检查消息积压和失败记录。`business_failures` 为零只证明接口返回成功，不能单独证明最终数据一致。

测试时可以另开终端运行 `docker stats` 查看容器 CPU 和内存。不要在同一台资源紧张的机器上同时跑高负载 k6 和被测服务；若必须同机运行，在结果中注明。
