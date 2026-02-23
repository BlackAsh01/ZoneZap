# User Data Structure in Firebase

This document describes how user data is stored in Firebase Firestore for the ZoneZap application, including user types and information used for AI training.

## 📋 User Document Structure

### Collection: `users/{userId}`

Each user document contains the following fields:

```json
{
  "userId": "firebase_auth_uid",
  "email": "user@example.com",
  "name": "User Name",
  "type": "user" | "guardian",
  "createdAt": "Firestore Timestamp",
  "updatedAt": "Firestore Timestamp",
  "guardians": ["guardian1_id", "guardian2_id"],  // For users only
  "wards": ["user1_id", "user2_id"]              // For guardians only
}
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|-----------|-------------|
| `userId` | String | Yes | Firebase Auth UID (same as document ID) |
| `email` | String | Yes | User's email address |
| `name` | String | No | User's display name (defaults to email prefix if not provided) |
| `type` | String | Yes | Either `"user"` or `"guardian"` |
| `createdAt` | Timestamp | Yes | When the user account was created |
| `updatedAt` | Timestamp | Yes | Last update timestamp |
| `guardians` | Array<String> | Conditional | Array of guardian user IDs (only for `type: "user"`) |
| `wards` | Array<String> | Conditional | Array of user IDs this guardian watches (only for `type: "guardian"`) |

## 🔄 User Type Behavior

### Regular Users (`type: "user"`)

- Have a `guardians` array (initially empty)
- Can be assigned guardians who monitor them
- Location data is tracked in `movement_logs` collection
- Can send emergency alerts
- Navigate to `HomeActivity` after login

### Guardians (`type: "guardian"`)

- Have a `wards` array (initially empty)
- Can monitor multiple users (wards)
- Can view alerts from their wards
- Can resolve alerts
- Navigate to `GuardianActivity` after login

## 📊 Data Flow for AI Training

### Location Data Collection

Location data is stored in the `movement_logs` collection:

```json
{
  "userId": "user_id",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "timestamp": "Firestore Timestamp",
  "speed": 1.4,
  "heading": 45.0
}
```

### AI Training Process

1. **Data Collection**: User location is logged to `movement_logs` with `userId`
2. **Training**: AI engine (`train.py`) fetches data from Firestore:
   ```bash
   python train.py --user-id <userId>
   ```
3. **Features Extracted**:
   - Distance from home (median location)
   - Velocity (speed)
   - Heading change (erratic movement indicator)
   - Time-based features (hour, day of week)
4. **Anomaly Detection**: Isolation Forest model detects unusual patterns

### Training Data Requirements

- Minimum 100 samples recommended per user
- Data should span multiple days/weeks for better patterns
- Both normal and anomalous behavior helps improve detection

## 🔐 Security Rules

Firestore security rules ensure:

1. **Users can only read/write their own document**
2. **Guardians can read user documents of their wards**
3. **Location logs are accessible by users and their guardians**

See `backend/firestore.rules` for complete rules.

## 🚀 Usage Examples

### Creating a User Account

When a user signs up:
1. Firebase Auth creates authentication record
2. `UserService.createOrUpdateUser()` creates Firestore document
3. User type is set based on mode selection during signup

### Linking Guardian to User

To assign a guardian to a user:

```kotlin
// Update user document to add guardian
firestore.collection("users")
    .document(userId)
    .update("guardians", FieldValue.arrayUnion(guardianId))
```

### Querying User Data

```kotlin
// Get user document
val userData = userService.getUser(userId)

// Get all users with a specific guardian
val usersSnapshot = firestore.collection("users")
    .whereArrayContains("guardians", guardianId)
    .get()
```

## 📈 AI Training Commands

### Train on All Users
```bash
cd ai-engine
python train.py
```

### Train on Specific User
```bash
python train.py --user-id <userId>
```

### Train with Custom Parameters
```bash
python train.py --user-id <userId> --limit 10000 --contamination 0.05
```

## 🔍 Monitoring User Data

### Check User Type
```kotlin
val userData = userService.getUser(userId)
val userType = userData?.get("type") as? String
```

### Count Location Logs
```kotlin
val logsSnapshot = firestore.collection("movement_logs")
    .whereEqualTo("userId", userId)
    .get()
val logCount = logsSnapshot.documents.size
```

## 📝 Notes

- User documents are automatically created on signup
- User type cannot be changed after creation (would require manual update)
- Guardians can be added/removed dynamically
- Location data is continuously collected for users (not guardians)
- AI training can be done per-user or globally
