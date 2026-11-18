# Command Pattern Refactor Log

Branch: `command-refractor` (confirmed by the user as the right branch; the instructions named it `command-pattern-refactor`).

## 1. Phase 1: Plan

### What the code has now
- `Department` (abstract) runs the menu loop in `open(Scanner)`: prints `getUseCases()`, reads a number, calls `runUseCase(index, in)`. Each subclass maps the index to a private method with a `switch`.
- Shared helpers in `Department`: `askPositiveLong`, `askNonNegativeLong`, `askText`, `askDate`, `askOption`, `askIndex`, `parseChoice` (all `static`).
- Shared state and helpers per department:
  - **TVShows**: field `schedule` (`BroadcastSchedule`) used by all 4 use cases; helpers `askIdOrCancel`, `askTimeOrCancel`, `askOrCancel`, `askYesOrCancel`, `showName`.
  - **TalentDepartment**: helpers `askEndDate`, `saveNewContract`, `saveNewWrestler`, `reactivate`, `endContracts`, `askBrand`.
  - **CreativeDepartment**: helpers `eventProblem`, `eligibleWrestlers`, `isEligible`, `saveMatch` (also used by its test `main`).
  - **Ticketing**: helpers `saveNewTicket`, `askSection`.
  - **LiveEvents**: helpers `planEvent` (shared by Create Live Event and Resume Draft), `askShowId`, `addVenue`, `saveEvent`, `notifyDepartments`, `askDateNotPast`, `askYes`.
  - **MedicalDepartment** (Singleton): static `wrestlerInjuries` map and private nested class `Injury`.
  - **FinanceDepartment** (Singleton): static `transactionLog` map, private nested `Receipt` class and `TransactionType` enum, helper `askPositiveDouble`.
- The use-case list in the instructions matches the code: 7 departments, 30 use cases, same names and order.

### Design
- **`Command` interface**: `String getName()` and `void execute(Scanner in)`.
- **Invoker**: `Department` holds a `List<Command>`. `open()` prints each `getName()` as the numbered menu and calls `execute(in)` on the chosen one. The menu loop, prompt, `back` handling and "Invalid choice." message stay exactly as they are.
- **ConcreteCommand**: one class per use case, named `<UseCase>Command` (e.g. `ScheduleLiveBroadcastCommand`). Each takes its **receiver** (its department) through the constructor and its `execute` calls the receiver's use-case method.
- **Receiver**: the department. Its use-case methods go from `private` to package-private so the command can call them. Use-case bodies, shared fields and shared helpers stay in the department, so nothing is copied.
- **Registration**: each department adds its commands in its constructor via `addCommand(...)`, in menu order. Singletons register commands inside their private constructor, so the commands hold the one singleton instance and never create a new department.
- **Transition**: while departments are converted, `open()` uses the command list if it isn't empty, otherwise the old `getUseCases()`/`runUseCase()`. Those two become non-abstract with empty defaults, so converted departments can drop them. Phase 4 removes them.

### Why the commands delegate instead of holding the use-case code
- This is the GoF shape of the pattern: a ConcreteCommand binds a Receiver to an action.
- It keeps the shared state (`schedule`, the singleton maps) and the shared helpers in one place, as the rules require.
- Medical and Finance use private nested classes (`Injury`, `Receipt`, `TransactionType`). Moving their UI code into separate classes would mean exposing those, which changes the teammates' encapsulation.
- Lowest risk of changing behavior in a structural-only refactor.
- Trade-off: the departments are still large, because the use-case code didn't move. See section 4, decision 1.

### Testing approach
- Before any code change, compile the original code into a separate folder and record the output of a fixed input script for every department and for the top-level menu (the baseline).
- After each phase, run the same scripts and `diff` against the baseline. Any difference means behavior changed.
- `git restore src/Data` after every run.
- Finance and Bank use `src/Data/...` paths, while everything else uses `Data/...`. So Finance is run from the repo root and everything else from `src`. This is a pre-existing issue (section 7), not changed by this refactor.

## 2. Progress by phase

### Phase 1: baseline
- Compiled the untouched code into a separate folder and ran 8 input scripts: the top-level menu plus one per department, together covering all 30 use cases. Saved the output as the baseline.
- Compile: OK. All 8 runs exited normally. `git restore src/Data` after each.

### Phase 2: foundation + TVShows
- Added `Command.java`. `Department` now holds `List<Command>`, has `addCommand()`, and `open()` runs the chosen command. `getUseCases()`/`runUseCase()` became non-abstract defaults, used only when a department has no commands.
- TVShows: removed `getUseCases()`/`runUseCase()` (the switch). The constructor registers 4 commands. The 4 use-case methods went from `private` to package-private. `schedule` and the ask helpers didn't move.
- New classes: `ScheduleLiveBroadcastCommand`, `ViewLiveBroadcastsCommand`, `RecordBroadcastRatingsCommand`, `CancelLiveBroadcastCommand`. All 30 command classes come from one template, so they share the same structure.
- Compile: OK. Test: all 8 scripts byte-identical to the baseline, so TV (commands) and the 6 departments still on the switch behave exactly as before.
- Problems: none.

### Phase 3: remaining departments (one at a time, in menu order)
Same steps for each: remove `getUseCases()`/`runUseCase()`, register commands in the constructor, make the use-case methods package-private, generate the command classes, compile, run every script, diff against the baseline, `git restore src/Data`.

| Department | Commands added | Compile | Test vs baseline | Problems |
|---|---|---|---|---|
| TalentDepartment | 7 | OK | identical | none |
| CreativeDepartment | 3 | OK | identical; its own self-test `java CreativeDepartment` also prints "CreativeDepartment ok" | none. `viewMatches()`/`viewStorylines()` take no Scanner, so those commands ignore `in` |
| Ticketing | 4 | OK | identical | none |
| LiveEvents | 4 | OK | identical | none. `viewEvents()` takes no Scanner |
| MedicalDepartment (Singleton) | 3 | OK | identical | none. Commands are made in the private constructor, so they hold `INSTANCE`; the private constructor and `getInstance()` are unchanged |
| FinanceDepartment (Singleton) | 5 | OK | identical (run from repo root, see section 7) | none. Same singleton approach as Medical. Its `default -> "Invalid choice."` switch branch could never run (`Department.open` already rejects bad numbers), so removing it changes nothing |

After Phase 3, no department overrides `getUseCases()`/`runUseCase()`.

### Phase 4: cleanup
- `Department`: removed the transition code (`getUseCases()`, `runUseCase()`, the "use commands?" branch). `open()` now only lists `commands` and calls `execute(in)`.
- `Main`: converted. The `switch` on the typed text became a `Map<String, Command>` with every alias (`departments`/`d`, `help`/`h`, `quit`/`q`/`exit`) pointing to `DepartmentsCommand`, `HelpCommand` or `QuitCommand`. Empty input is still skipped and unknown input still prints the same message. `QuitCommand` prints "Goodbye!" and calls `Main.stop()`, which ends the loop (before, the switch did `return`). `printHelp()` and `departmentMenu()` went from `private` to package-private for the commands.
- Compile: OK. Test: identical.
- Problem: my `sed` edits added LF-only lines to CRLF files, and a first fix added a stray `\r` to the last line of `Ticketing.java` and `FinanceDepartment.java`, which made git show them as whole-file changes. Fixed by restoring their original ending (no newline after the final `}`). `git diff` now shows only real changes and no line-ending warnings.

### Phase 5: final check
- Deleted `out/`, compiled from scratch: OK (no errors or warnings).
- Ran 10 scripts: the 8 baseline scripts, plus top-level aliases in mixed case with spaces, blank lines, unknown input and `EXIT` (`8_main_aliases`), plus input ending inside a department (`9_eof`). The extra two were also recorded against the original code first.
- All 10 outputs are byte-identical to the original program, including every menu screen, prompt and message. `git restore src/Data` after each; `src/Data` is unchanged.
- `CreativeDepartment`'s own self-test still prints "CreativeDepartment ok".
- `out/` is still in `.gitignore`.

## 3. Summary table

| Department | Use cases | Converted | Command classes created | Notes |
|---|---|---|---|---|
| TVShows | 4 | yes | ScheduleLiveBroadcastCommand, ViewLiveBroadcastsCommand, RecordBroadcastRatingsCommand, CancelLiveBroadcastCommand | Shared `BroadcastSchedule` stays a field of TVShows |
| TalentDepartment | 7 | yes | SignWrestlerCommand, ReleaseWrestlerCommand, PromoteToMainRosterCommand, ViewWrestlersCommand, RenewContractCommand, CreateContractCommand, ViewContractsCommand | |
| CreativeDepartment | 3 | yes | BookMatchCommand, ViewMatchesCommand, ViewStorylinesCommand | Self-test `main` still passes |
| Ticketing | 4 | yes | SellTicketCommand, ViewTicketsCommand, SetEventPriceCommand, RefundTicketCommand | |
| LiveEvents | 4 | yes | CreateLiveEventCommand, ReviewPendingEventsCommand, ViewEventsCommand, ResumeDraftCommand | `planEvent` stays shared by Create and Resume |
| MedicalDepartment | 3 | yes | AddInjuryCommand, ClearInjuryCommand, ViewMedicalRecordsCommand | Singleton unchanged; commands hold `INSTANCE` |
| FinanceDepartment | 5 | yes | DepositFundsCommand, WithdrawFundsCommand, ViewAllTransactionsCommand, ViewTransactionByIdCommand, LogPastTransactionCommand | Singleton unchanged; commands hold `INSTANCE` |
| Main (top level) | 3 | yes | DepartmentsCommand, HelpCommand, QuitCommand | Not a department; converted in Phase 4 |

## 4. Decisions, assumptions, and points for a human to decide
1. **Commands delegate to the department; they don't contain the use-case code.** This is the GoF shape (ConcreteCommand → Receiver.action()) and keeps shared state and helpers in one place, as required. A deeper refactor would move each use case's code into its command. That would shrink the departments, but it needs the shared helpers exposed, and Medical's `Injury` and Finance's `Receipt`/`TransactionType` made non-private. This is the team's call. Estimate in section 6.
2. **Use-case methods went from `private` to package-private** so commands can call them. Any class in the (default) package can now call them too.
3. **33 new command files sit flat in `src/`** next to everything else (there are no packages). Moving them into a `commands/` package would tidy `src/` but changes the compile command (`javac -d ../out *.java` no longer finds them). Not done.
4. **Top-level `getName()` isn't used for the help text.** `printHelp()` is still hand-written so its spacing stays identical. The names are there to satisfy the interface and for any future menu built from the commands.
5. **Branch name:** the instructions said `command-pattern-refactor`; the repo branch is `command-refractor`. The user confirmed it's the right one.
6. **Finance/Bank path bug (pre-existing, not fixed):** `FinanceDepartment` and `Bank` use `src/Data/...` paths while everything else uses `Data/...`. Run from `src` as documented, Finance can't find `Bank.txt` and crashes on its first use case. Fixing it changes behavior, so it was left alone. Recommend the Finance owner change both paths to `Data/...`.
7. **New command files use LF line endings** while the existing files are CRLF in the working copy. Git normalizes this (`core.autocrlf=true`), so it has no effect after commit.

## 5. Final numbers
- Departments converted: **7 / 7**
- Use cases converted: **30 / 30** (plus 3 top-level menu commands in Main)
- Command classes: **33 concrete** (30 use cases + 3 top-level) **+ 1 `Command` interface**
- New files: 34 Java files (724 lines) + this log
- `git diff --stat` (tracked files only; new files aren't counted):

```
 src/CreativeDepartment.java | 27 ++++++++-------------
 src/Department.java         | 25 ++++++++++----------
 src/FinanceDepartment.java  | 45 ++++++++++++++---------------------
 src/LiveEvents.java         | 32 +++++++++++--------------
 src/Main.java               | 50 ++++++++++++++++++++++++++++-----------
 src/MedicalDepartment.java  | 33 +++++++++-----------------
 src/TVShows.java            | 36 +++++++++++-----------------
 src/TalentDepartment.java   | 57 ++++++++++++++++++---------------------------
 src/Ticketing.java          | 35 +++++++++++-----------------
 9 files changed, 150 insertions(+), 190 deletions(-)
```

## 6. Not finished / remaining work
**Nothing in the requested scope is unfinished.** All 7 departments, all 30 use cases and Main are converted, and every test output matches the original.

**Optional follow-up** (decision 1, only if the team wants the use-case code inside the commands): estimated **10–14 hours** for a person.
- Counts: 10 of the 30 use cases only display data (the "View ..." ones, 3–25 lines each). Moving one plus a test is about 15 minutes, so about 2.5 h.
- The other 20 are interactive flows (15–85 lines each). Several share helpers (`planEvent` in LiveEvents, `saveMatch`/`eventProblem` in Creative, the ask helpers in TVShows). Each needs those helpers made package-private, careful moving, and a re-test of every branch: about 25–30 minutes each, so 8–10 h.
- Medical and Finance need their private nested classes exposed or wrapped, plus an agreement with their owners: about 1–1.5 h more.
- Basis: in this refactor, converting one department by delegation took one short edit, generated classes and one test run, because no code moved. Moving bodies is mostly careful reading and re-testing, which is why the per-use-case estimate is much higher.

## 7. Behavior differences noticed
- **Program output: none.** All 10 scripts are byte-identical to the original, covering every menu option, invalid input, `back`, aliases and end of input.
- **Stack traces** (only seen when the program crashes, e.g. the pre-existing Finance path bug) now show a `...Command.execute` frame instead of `runUseCase`, and line numbers in the edited files moved.
- **Unreachable code removed:** Medical and Finance had a `default -> "Invalid choice."` branch in their switch that could never run, because `Department.open()` already rejects bad numbers.
- **Menu names are fixed when a department is created** (from its commands). Before, `getUseCases()` was called each time the menu opened. The lists never changed at runtime, so nothing is visible.
- **Quit:** `QuitCommand` sets a flag that ends the main loop instead of `return`ing from `main`. Same output, same exit.
