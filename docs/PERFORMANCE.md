# Performance Guide

Optimization strategies and tuning tips for IntelliJ Agent Controller.

## Understanding Latency

Typical operation latencies:

| Operation | Typical Time | Range |
|-----------|--------------|-------|
| Simple find | 50ms | 20-100ms |
| Click button | 100ms | 50-200ms |
| Type text | 150ms | 100-300ms |
| Open file | 300ms | 200-800ms |
| Take screenshot | 500ms | 200-2000ms |
| Invoke action | 200ms | 100-500ms |
| Smart wait | 5-60s | Depends on project |
| MCP (stdio) overhead | +500ms | Per operation |

**Key insight:** Most operations are fast (< 500ms). Slowness usually comes from IDE state (indexing, builds) or complex operations (large screenshots, full hierarchies).

---

## Bottlenecks

### 1. IDE Indexing

IntelliJ continuously indexes the codebase, blocking some operations.

**Impact:** 5-60+ seconds depending on project size

**Detection:**
```bash
java -jar intellij-agent-controller.jar status
# Look for "indexing" indicator

java -jar intellij-agent-controller.jar screenshot
# Look for "Indexing..." progress bar
```

**Optimization:**
```bash
# Wait for indexing to complete before heavy operations
java -jar intellij-agent-controller.jar smart-wait --timeout 120000

# Then run your operations
java -jar intellij-agent-controller.jar click "//JButton[@text='Run']"
```

### 2. Background Builds

IntelliJ may run builds in background, slowing responsiveness.

**Detection:**
```bash
java -jar intellij-agent-controller.jar screenshot
# Look for build progress indicators
```

**Optimization:**
- Disable "Build project on frame deactivation" in settings
- Use `smart-wait` to wait for build completion

### 3. Large Screenshots

Taking full-screen screenshots of large IDEs is slow.

**Performance:**
- Full IDE screenshot: 500-2000ms
- Component screenshot: 100-300ms

**Optimization:**
```bash
# Instead of full screenshot
java -jar intellij-agent-controller.jar screenshot --output full.png

# Capture only the editor
java -jar intellij-agent-controller.jar screenshot \
  --component "//EditorTabbedContainer" \
  --output editor.png
```

### 4. Full Component Hierarchy / find-all

Finding all components on screen requires traversing the entire UI tree.

**Performance:**
- Small IDE: 100-200ms
- Large IDE: 500-2000ms

**Optimization:**
```bash
# Instead of finding all components
java -jar intellij-agent-controller.jar find-all

# Use specific XPath query
java -jar intellij-agent-controller.jar find "//JButton[@enabled='true']"
```

### 5. MCP / Stdio Transport

MCP uses stdin/stdout, which has serialization overhead.

**Impact:** +500ms to +2000ms per operation compared to direct CLI

**Optimization:**
- Use CLI directly for performance-critical workflows
- Use SSE transport for MCP if available

### 6. Network Latency (Remote IDE)

If IDE is on another machine:

**Impact:** Add 50-200ms per operation depending on network

**Optimization:**
```bash
# SSH tunnel for better latency
ssh -L 8580:remote-machine:8580 user@remote-machine
# Then connect to localhost:8580
```

---

## Optimization Strategies

### Strategy 1: Batch Operations

Combine multiple operations into a single workflow to avoid repeated setup.

**Before (slow):**
```bash
java -jar cli.jar open-file "src/Main.kt"
java -jar cli.jar editor-action "ReformatCode"
java -jar cli.jar get-editor-text > content.txt
```
Total: ~3 seconds

**After (fast):**
```bash
#!/bin/bash
JAR="cli.jar"
java -jar "$JAR" open-file "src/Main.kt" && \
java -jar "$JAR" editor-action "ReformatCode" && \
java -jar "$JAR" get-editor-text > content.txt
```
Total: ~1-1.5 seconds (eliminate subprocess overhead)

### Strategy 2: Specific XPath Queries

Specific selectors are faster than broad searches.

**Before (slow):**
```bash
java -jar cli.jar find-all --json | jq '.[] | select(.text == "OK")'
```
Time: 500+ms (find all + filter)

**After (fast):**
```bash
java -jar cli.jar find "//JButton[@text='OK']"
```
Time: 100ms (direct query)

### Strategy 3: Strategic Waits

Wait only when necessary.

**Before (slow):**
```bash
java -jar cli.jar click "//JButton[@text='Run']"
java -jar cli.jar smart-wait --timeout 60000  # Always wait
```

**After (fast):**
```bash
java -jar cli.jar click "//JButton[@text='Run']"
# Wait only if expecting a dialog
if java -jar cli.jar component-exists "//ProgressBar"; then
    java -jar cli.jar wait-for "//ProgressBar" --timeout 30000 --condition enabled
fi
```

### Strategy 4: Reduce Screenshot Size

Capture only what you need.

**Before (slow):**
```bash
java -jar cli.jar screenshot --output full.png
# 1000-2000ms for large IDE
```

**After (fast):**
```bash
# Capture just the build output panel
java -jar cli.jar screenshot \
  --component "//ContentPanel[@id='toolWindowContent']" \
  --output build-output.png
# 100-300ms
```

### Strategy 5: Use Smart Waits Effectively

`smart-wait` waits for IDE readiness (indexing, build completion).

**Recommended use:**
```bash
java -jar cli.jar launch --ide IC --version 2024.1
java -jar cli.jar smart-wait --timeout 60000

# Now all operations will be responsive
java -jar cli.jar open-file "src/Main.kt"
java -jar cli.jar screenshot
```

### Strategy 6: Disable Unnecessary Plugins

Fewer plugins = faster IDE.

1. Open IntelliJ
2. Settings → Plugins
3. Disable plugins you don't need for testing
4. Restart IDE

Common heavy plugins:
- IDE plugins (Copilot, ChatGPT integrations)
- Language plugins for unused languages
- Analytics/tracking plugins

---

## CLI vs MCP Performance

### CLI (Fast)

```bash
java -jar cli.jar click "//JButton[@text='OK']"
```
- No serialization overhead
- Direct tool invocation
- Latency: 100ms

### MCP Stdio (Slower)

```
Claude asks: "Click OK button"
  ↓ MCP serializes request to JSON
  ↓ MCP sends over stdin
  ↓ Runs tool
  ↓ MCP serializes response as JSON
  ↓ MCP sends over stdout
  ↓ Claude receives
```
- Serialization overhead: +300-500ms
- Latency: 400-600ms

### MCP SSE (Medium)

```
Same as stdio but over HTTP (SSE)
- Slightly faster than stdio (native HTTP)
- Latency: 300-500ms
```

**Recommendation:** Use CLI for performance-critical workflows, MCP for convenience.

---

## Measurement & Profiling

### Measure Command Runtime

```bash
# time the command
time java -jar cli.jar screenshot
# Output shows real, user, sys time

# Example output:
# real    0m1.234s
# user    0m0.891s
# sys     0m0.312s
```

### Debug Mode Shows Timings

```bash
java -jar cli.jar --debug click "//JButton[@text='OK']"

# Output includes:
# [DEBUG] POST http://127.0.0.1:8580/api/interaction/click (took 145ms)
```

### Identify Slow Operations

```bash
#!/bin/bash
JAR="cli.jar"

echo "Finding components..."
time java -jar "$JAR" find-all > /dev/null

echo "Taking screenshot..."
time java -jar "$JAR" screenshot --output test.png

echo "Clicking button..."
time java -jar "$JAR" click "//JButton[@text='OK']"
```

---

## Resource Usage

### Memory Impact

- **IDE process:** 1-4 GB depending on project size
- **Controller JAR:** ~100 MB heap
- **Combined:** Typically 2-5 GB for medium projects

### CPU Impact

Controller operations:
- Idle: ~0% CPU
- During operation: Short CPU spike (100-500ms)
- MCP on active conversation: Minimal impact

### Network I/O

For localhost communication:
- Per operation: ~1-10 KB JSON
- Network overhead: Negligible for localhost
- Remote IDE: Add latency but not significant bandwidth

---

## Tuning Configuration

### JVM Heap Size

Increase for very large projects:

```bash
export JAVA_OPTS="-Xmx4g"
java -jar cli.jar launch
```

Default: 2GB (from gradle.properties)

### IDE Heap Size

Affect IDE performance (not controller):

In IDE settings:
- Help → Edit Custom VM Options
- Set: `-Xmx4g`
- Restart

### Gradle Parallel Builds

In `gradle.properties`:

```properties
org.gradle.parallel=true
org.gradle.workers.max=4
```

This speeds up `./gradlew` commands, not runtime operations.

---

## Best Practices Checklist

- ✅ Use specific XPath queries (not `find-all`)
- ✅ Wait only when necessary
- ✅ Use `smart-wait` after IDE launch
- ✅ Capture component screenshots instead of full screen
- ✅ Batch operations in scripts
- ✅ Disable `--debug` in production
- ✅ Use CLI for latency-sensitive workflows
- ✅ Use small timeout values (don't wait 60s if 5s is enough)
- ✅ Keep IDE tidy (fewer plugins, close unnecessary tabs)
- ✅ Monitor IDE state (`status` command)

---

## Example: Optimized Workflow

```bash
#!/bin/bash
JAR="cli.jar"

# 1. Launch IDE (happens once)
java -jar "$JAR" launch --ide IC --version 2024.1
java -jar "$JAR" smart-wait --timeout 60000

# 2. Open file (250ms)
java -jar "$JAR" open-file "src/main/kotlin/Main.kt"

# 3. Execute specific action (150ms)
java -jar "$JAR" editor-action "ReformatCode"

# 4. Find specific component (100ms)
java -jar "$JAR" find "//JButton[@text='Run' and @enabled='true']"

# 5. Click (100ms)
java -jar "$JAR" click "//JButton[@text='Run']"

# 6. Wait for result only if needed (1000ms)
java -jar "$JAR" wait-for "//BuildPanel[@status='success']" --timeout 30000

# Total: ~1.7 seconds
```

vs.

```bash
# Unoptimized version (would take ~5+ seconds)
# Uses find-all instead of specific queries
# Uses full screenshots
# Uses full waits even when not needed
```

---

## Performance Monitoring

### Monitor IDE State Continuously

```bash
watch -n 2 'java -jar cli.jar --json status'
```

This updates every 2 seconds showing:
- Connection status
- Indexing progress
- Build status
- Memory usage

### Log Performance Metrics

```bash
#!/bin/bash
{
    echo "Operation,Time(ms),Success"
    for i in {1..10}; do
        START=$(($(date +%s%N)/1000000))
        java -jar cli.jar screenshot > /dev/null 2>&1
        SUCCESS=$?
        END=$(($(date +%s%N)/1000000))
        DURATION=$((END - START))
        echo "screenshot,$DURATION,$SUCCESS"
    done
} | tee performance.csv
```

---

## References

- [Java Profiling Guide](https://www.oracle.com/java/technologies/profiling.html)
- [Gradle Performance Tuning](https://gradle.org/guides/performance/)
- [JVM Tuning Best Practices](https://www.baeldung.com/jvm-tuning)
