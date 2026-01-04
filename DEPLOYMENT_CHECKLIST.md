# Deployment Checklist — Backend

Purpose: Ensure commit `3e01c40` (fix for drawer selection / GAME_STARTED broadcast) is built, deployed, and verified.

Steps:

1. Build
   - On a machine or CI with Maven installed, run: `mvn -DskipTests package` in the `backend` directory.
   - Confirm the build completes successfully.

2. Deploy to Railway (or your chosen host)
   - If using Railway's GitHub integration, ensure it's tracking `origin/main` and that the latest commit is present.
   - Trigger a deploy (manual redeploy from Railway dashboard if auto-deploy isn't active).
   - Watch build logs for successful artifact creation and service start.

3. Verify startup logs
   - Look for server logs indicating successful startup and no binding/port errors.
   - Confirm the application log contains `GameWebSocketHandler` logger initialization messages.

4. Functional verification (manual test)
   - Start a new game in a 2-player room.
   - Observe server logs for the following sequence:
     - `Drawer selected: roomId=..., drawerId=..., drawerName=...`
     - `Word selection started: roomId=..., drawerId=..., optionsCount=...`
     - `Game started: roomId=..., ..., drawerId=...` (note: drawerId should NOT be null)
   - On clients: the drawer should receive `SEND_WORD_OPTIONS`; guessers should receive `WORD_SELECTION_STARTED` and not `SEND_WORD_OPTIONS`.

5. If `GAME_STARTED` contains `currentDrawerId=null` still:
   - Capture full server logs around game start (from `GameStartingBroadcast` to `GameStartedBroadcast`).
   - Temporarily increase logging verbosity for `com.skribble.websocket` package or add extra debug logs around `startGame` and `selectNextDrawerAndStartWordSelection` (see `backend/src/main/java/...`).
   - If issue persists after redeploy, collect logs and forward them for analysis.

6. Rollback plan
   - If deploy causes new failures, roll back to previous working commit on Railway.

7. Post-verification
   - Remove any temporary verbose logging after the issue is confirmed fixed and verified in production.

Contact: Ask me to inspect logs if the sequence still shows `currentDrawerId=null` after redeploy; I can add temporary debug traces or a hotfix branch if needed.