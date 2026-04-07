# Contributing Guide

How to develop, test, and contribute to IntelliJ Agent Controller.

## Development Setup

### Prerequisites

- **Java 21+**
- **IntelliJ IDEA** (Community or Ultimate, any recent version)
- **Git**
- **Gradle Wrapper** (included in repo)

### Environment Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/amoghng7/intellij-agent-controller.git
   cd intellij-agent-controller
   ```

2. **Open in IntelliJ:**
   - File → Open → Select the project root
   - IntelliJ will auto-detect the Gradle project
   - Wait for Gradle sync to complete

3. **Verify setup:**
   ```bash
   ./gradlew clean build
   ```

## Project Structure

```
intellij-agent-controller/
├── core/                  # Core tool implementations (pure Kotlin)
│   └── src/main/kotlin/.../
│       ├── tools/         # 10 tool classes
│       ├── models/        # Data models
│       └── ...            # Utilities, exceptions
├── cli/                   # CLI interface (Clikt)
│   └── src/main/kotlin/.../
│       ├── commands/      # Command groups
│       ├── Main.kt        # Entry point
│       └── ...
├── mcp-server/            # MCP server (Ktor)
│   └── src/main/kotlin/.../
│       └── ...
├── docs/                  # Documentation
├── examples/              # Example scripts
└── build.gradle.kts       # Root build config
```

## Build Commands

```bash
# Build all modules
./gradlew build

# Build and run tests
./gradlew test

# Build fat JAR (CLI)
./gradlew shadowJar

# Run IDE warnings check
./gradlew :cli:run --args="--help"

# Run MCP server for testing
./gradlew :mcp-server:run --args="stdio"

# Clean build artifacts
./gradlew clean
```

## Code Organization

### Adding a New Tool

Steps to add a new tool feature:

1. **Create tool class** in `core/src/main/kotlin/com/github/intellijagent/core/tools/`

   Example: `core/src/main/kotlin/com/github/intellijagent/core/tools/MyNewTools.kt`

   ```kotlin
   class MyNewTools(private val connectionManager: ConnectionManager) {
       
       fun myNewAction(param: String): Result {
           val response = connectionManager.post(
               "/api/mynew/action",
               mapOf("param" to param)
           )
           return Result(response.getAsJsonObject("data"))
       }
   }
   ```

2. **Add CLI command** in `cli/src/main/kotlin/com/github/intellijagent/cli/commands/`

   File: `MyNewCommands.kt`

   ```kotlin
   class MyNewCommand : CliktCommand(name = "my-new-action") {
       private val param by argument()
       
       override fun run() {
           val tools = MyNewTools(connectionManager)
           val result = tools.myNewAction(param)
           echo(result.message)
       }
   }
   ```

3. **Register command** in `Main.kt`

   ```kotlin
   subcommand(MyNewCommand())
   ```

4. **Register in MCP** in `mcp-server/src/main/kotlin/com/github/intellijagent/mcp/McpServer.kt`

   ```kotlin
   registerTool("my_new_action") {
       // Tool implementation
       val tools = MyNewTools(connectionManager)
       tools.myNewAction(it.getString("param"))
   }
   ```

### Adding a CLI Command Group

1. Create new file: `cli/.../commands/MyCommandGroup.kt`
2. Create command classes (each extends `CliktCommand`)
3. Register in `Main.kt` via `subcommand()`

### Code Style

- **Kotlin idioms:** Use scope functions, sequences, etc.
- **Null safety:** Prefer `?.let` over null checks
- **Immutability:** Prefer `val` over `var`
- **Naming:** camelCase for functions/properties, PascalCase for classes

## Testing

### Current State

Tests are **not yet implemented**. This is a good area to contribute!

### Recommended Testing Approach

1. **Unit tests** for tool logic (mock ConnectionManager):
   ```kotlin
   @Test
   fun testClickComponent() {
       val mockConnectionManager = mockk<ConnectionManager>()
       every { mockConnectionManager.post(...) } returns mockResponse()
       
       val tools = InteractionTools(mockConnectionManager)
       assertTrue(tools.click("//JButton[@text='OK']"))
   }
   ```

2. **Integration tests** against real robot-server:
   - Start IntelliJ with robot-server plugin
   - Create test fixtures (open project, etc.)
   - Run actual commands, verify results

### Manual Testing

Test changes manually before submitting:

```bash
# Build
./gradlew shadowJar

# Test a command
java -jar cli/build/libs/intellij-agent-controller.jar screenshot

# Test MCP
java -jar cli/build/libs/intellij-agent-controller.jar mcp-server --transport stdio
```

### Testing Workflows

Before committing, verify:

1. **CLI works:**
   ```bash
   java -jar cli/build/libs/intellij-agent-controller.jar --help
   java -jar cli/build/libs/intellij-agent-controller.jar connect
   java -jar cli/build/libs/intellij-agent-controller.jar screenshot
   ```

2. **MCP works:**
   ```bash
   java -jar cli/build/libs/intellij-agent-controller.jar mcp-server --transport stdio
   # Should start without errors
   ```

3. **No compilation errors:**
   ```bash
   ./gradlew build
   ```

## Common Development Tasks

### Debug a command

Add `--debug` flag:

```bash
java -jar cli/build/libs/intellij-agent-controller.jar --debug click "//JButton[@text='OK']"
```

This logs HTTP requests and responses.

### Test against a specific IDE instance

Configure host/port:

```bash
java -jar cli/build/libs/intellij-agent-controller.jar \
  --host 192.168.1.100 \
  --port 8580 \
  screenshot
```

### Modify build configuration

Edit `build.gradle.kts` files:

- **Root:** `build.gradle.kts` (version, common config)
- **core:** `core/build.gradle.kts` (dependencies, compile settings)
- **cli:** `cli/build.gradle.kts` (Shadow JAR config, fat JAR settings)
- **mcp-server:** `mcp-server/build.gradle.kts` (Ktor, MCP SDK versions)

## Dependency Management

### Adding a dependency

1. Edit `core/build.gradle.kts`, `cli/build.gradle.kts`, or `mcp-server/build.gradle.kts`

2. Add to `dependencies { ... }`

   ```kotlin
   implementation("com.example:library:1.0.0")
   ```

3. Sync Gradle:
   ```bash
   ./gradlew build
   ```

### Gradle properties

Edit `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2g
org.gradle.parallel=true
kotlin.compiler.incremental=true
```

## Documentation

### Update docs when:
- Adding new commands
- Changing behavior or defaults
- Adding new features
-Fixing bugs with user impact

### Documentation files:

| File | Purpose |
|------|---------|
| `README.md` | Project overview, quick-start, tech stack |
| `docs/GETTING_STARTED.md` | Installation and first commands |
| `docs/CLI_REFERENCE.md` | Complete command reference |
| `docs/MCP_INTEGRATION.md` | AI agent integration |
| `docs/ARCHITECTURE.md` | Design and internals |
| `docs/TROUBLESHOOTING.md` | Common issues and solutions |
| `docs/CONTRIBUTING.md` | This file |
| `docs/API_REFERENCE.md` | Core module API |
| `docs/PERFORMANCE.md` | Optimization tips |
| `docs/FAQ.md` | Frequently asked questions |

### Update examples when:
- Adding new commands
- Popular use cases emerge
- Workflows change

## Version Management

Currently using ad-hoc versioning. No formal release process yet.

When ready to release:

1. Update version in `gradle.properties` or `build.gradle.kts`
2. Tag Git: `git tag v1.0.0`
3. Push tag: `git push origin v1.0.0`
4. Create GitHub release with JAR artifact

## Commit Guidelines

- **Commits should be atomic:** One logical change per commit
- **Write clear messages:** "Add click command" not "fix stuff"
- **Reference issues:** "Fix #42: Add timeout option to wait-for"
- **Run tests:** Ensure `./gradlew build` passes before committing

## Pull Request Workflow

1. **Create feature branch:**
   ```bash
   git checkout -b feat/my-feature
   ```

2. **Make changes:**
   - Add code
   - Update docs
   - Add tests if possible

3. **Test locally:**
   ```bash
   ./gradlew build
   ```

4. **Commit with clear messages:**
   ```bash
   git add .
   git commit -m "Add my-feature: Description of what changed"
   ```

5. **Push and open PR:**
   ```bash
   git push origin feat/my-feature
   ```
   Then open PR on GitHub

6. **Address feedback:**
   - Respond to review comments
   - Make requested changes
   - Push updates (automatic re-check)

## Release Checklist

Before releasing a new version:

- [ ] Bump version in `gradle.properties`
- [ ] Update `README.md` with new features
- [ ] Update relevant doc files
- [ ] Run `./gradlew clean build`
- [ ] Test snapshot JAR: `java -jar cli/build/libs/intellij-agent-controller.jar --help`
- [ ] Create Git tag: `git tag v<VERSION>`
- [ ] Push tag and trigger release workflow

## Getting Help

- **Questions?** Open a GitHub issue (Discussion tab)
- **Bug report?** Include `./gradlew build` output and `--debug` logs
- **Feature request?** Describe use case and why it's needed
- **Code review?** Reference the PR and ask specific questions

## References

- [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html)
- [Gradle Documentation](https://gradle.org/docs/)
- [Clikt Guide](https://ajalt.github.io/clikt/)
- [Ktor Documentation](https://ktor.io/)
- [MCP Specification](https://modelcontextprotocol.io)
