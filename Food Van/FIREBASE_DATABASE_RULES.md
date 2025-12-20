# Firebase Database Rules for Food Van App

## Overview
This document explains the Firebase Realtime Database rules implemented for the Food Van application to resolve the "Permission denied" error when submitting support tickets.

## Database Rules Structure

The `database.rules.json` file defines security rules for different sections of the database:

### Support Section
- **FAQs** (`support/faqs`): Public read access, admin write access only
- **Support Contacts** (`config/supportContacts`): Public read access, admin write access only

### Support Tickets
- **Tickets** (`supportTickets`): Authenticated users can read and write
- **Validation**: Each ticket must contain required fields (ticketId, customerId, category, description, createdAt, updatedAt)

### Orders
- **Orders** (`orders`): Authenticated users can only read their own orders
- **Write Access**: Authenticated users can write (for creating new orders)
- **Security**: Users can only access orders where they are the customerId

## How to Deploy Rules

1. Install Firebase CLI:
   ```
   npm install -g firebase-tools
   ```

2. Login to Firebase:
   ```
   firebase login
   ```

3. Deploy the rules:
   ```
   firebase deploy --only database
   ```

## Troubleshooting

If you still encounter "Permission denied" errors:

1. Ensure the user is properly authenticated before submitting tickets
2. Verify the Firebase project URL matches the one in `google-services.json`
3. Check that the rules have been deployed to Firebase Console
4. Confirm the user has a valid Firebase Authentication token

## Security Notes

- All write operations require authentication
- Users can only read their own data (orders and support tickets)
- Admin users have additional privileges for managing FAQs and support contacts
- Validation rules ensure data integrity for support tickets