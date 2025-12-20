# Guess Validation WebSocket Events

## Input: SUBMIT_GUESS

```json
{
  "type": "SUBMIT_GUESS",
  "roomId": "ROOM123",
  "playerId": "player_456",
  "guess": "elephant",
  "timestamp": 1734567890123
}
```

## Output: CORRECT_GUESS (Broadcast to all players in room)

```json
{
  "type": "CORRECT_GUESS",
  "roomId": "ROOM123",
  "playerId": "player_456",
  "playerName": "Alice",
  "guessOrder": 1,
  "timestamp": 1734567890500
}
```

## Output: GUESS_FEEDBACK (Sent only to guessing player)

### Correct Guess Feedback
```json
{
  "type": "GUESS_FEEDBACK",
  "roomId": "ROOM123",
  "correct": true,
  "message": "Correct! You guessed the word!",
  "timestamp": 1734567890500
}
```

### Incorrect Guess Feedback
```json
{
  "type": "GUESS_FEEDBACK",
  "roomId": "ROOM123",
  "correct": false,
  "message": "Incorrect guess. Try again!",
  "timestamp": 1734567890500
}
```

## Error Events (Sent only to guessing player)

### Room Not Found
```json
{
  "type": "ERROR",
  "code": "ROOM_NOT_FOUND",
  "message": "Room does not exist: ROOM123"
}
```

### Player Not In Room
```json
{
  "type": "ERROR",
  "code": "NOT_IN_ROOM",
  "message": "You are not in this room"
}
```

### Drawer Cannot Guess
```json
{
  "type": "ERROR",
  "code": "DRAWER_CANNOT_GUESS",
  "message": "The drawer cannot submit guesses"
}
```

### Game Not Active
```json
{
  "type": "ERROR",
  "code": "GAME_NOT_ACTIVE",
  "message": "No active game round"
}
```

### Round Expired
```json
{
  "type": "ERROR",
  "code": "ROUND_EXPIRED",
  "message": "Round time has expired"
}
```

### Already Guessed
```json
{
  "type": "ERROR",
  "code": "ALREADY_GUESSED",
  "message": "You have already guessed the word correctly"
}
```

### Invalid Payload
```json
{
  "type": "ERROR",
  "code": "INVALID_PAYLOAD",
  "message": "SUBMIT_GUESS requires: roomId, playerId, guess (non-empty)"
}
```
