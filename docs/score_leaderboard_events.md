# Score and Leaderboard WebSocket Events

## SCORE_UPDATE (Broadcast to all players in room)

Sent when a player scores points (correct guess or drawer bonus).

```json
{
  "type": "SCORE_UPDATE",
  "roomId": "ROOM123",
  "playerId": "player_456",
  "playerName": "Alice",
  "pointsAwarded": 450,
  "totalScore": 1250,
  "guessOrder": 1,
  "timestamp": 1734567890500
}
```

### Fields:
- `pointsAwarded`: Points earned this action
- `totalScore`: Player's new cumulative score
- `guessOrder`: 1st, 2nd, etc. guesser (0 for drawer bonus)

---

## LEADERBOARD_UPDATE (Broadcast to all players in room)

Sent after any score change.

```json
{
  "type": "LEADERBOARD_UPDATE",
  "roomId": "ROOM123",
  "leaderboard": [
    {
      "rank": 1,
      "playerId": "player_456",
      "playerName": "Alice",
      "score": 1250,
      "hasGuessedThisRound": true
    },
    {
      "rank": 2,
      "playerId": "player_789",
      "playerName": "Bob",
      "score": 800,
      "hasGuessedThisRound": false
    },
    {
      "rank": 3,
      "playerId": "player_123",
      "playerName": "Charlie",
      "score": 650,
      "hasGuessedThisRound": true
    }
  ],
  "timestamp": 1734567890500
}
```

### Fields:
- `rank`: Position on leaderboard (1-indexed)
- `hasGuessedThisRound`: Whether player has guessed correctly this round

---

## Scoring Formula

### Guesser Score:
```
score = max(10, timeLeftSeconds × 10)
```

Example with 45 seconds remaining:
- `score = max(10, 45 × 10) = 450 points`

Example with 2 seconds remaining:
- `score = max(10, 2 × 10) = 20 points`

Example with 0 seconds remaining:
- `score = max(10, 0 × 10) = 10 points` (minimum)

### Drawer Bonus:
- Drawer receives **50 points** when the first player guesses correctly
- Awarded only once per round

---

## Error Events

### Already Scored
```json
{
  "type": "ERROR",
  "code": "ALREADY_SCORED",
  "message": "Score already awarded for this round"
}
```
