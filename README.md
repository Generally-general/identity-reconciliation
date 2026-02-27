# Identity Reconciliation Service

This service implements the `/identify` endpoint as specified in the BiteSpeed Backend Engineering Intern assignment.

It consolidates customer identity across multiple purchases by:

- Resolving primary contact based on oldest `createdAt`
- Merging multiple primary clusters
- Flattening contact relationships
- Creating secondary contacts only when new information is introduced
- Returning consolidated identity response

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Hosted on Render

## 📌 Endpoint

```
POST /identify
```

### Request Body

```json
{
  "email": "string (optional)",
  "phoneNumber": "string (optional)"
}
```

> At least one of `email` or `phoneNumber` must be present.

### 📤 Response Format

```json
{
  "contact": {
    "primaryContatctId": number,
    "emails": [],
    "phoneNumbers": [],
    "secondaryContactIds": []
  }
}
```
#### Note: The API contract mentioned in the specifications had a field named "primaryContatctId" with a typo. So to respect the contract, the similar field name had been kept

## 🏗Design Decisions

- Oldest contact (by `createdAt`) becomes primary
- All clusters are flattened to a single root
- Secondary created only when new email/phone is introduced
- Deterministic ordering using `LinkedHashSet`
- Entire operation wrapped in `@Transactional`

## Sample curl

```bash
curl -X POST https://your-render-url/identify \
  -H "Content-Type: application/json" \
  -d '{"email":"doc@fluxkart.com","phoneNumber":"111111"}'
```

## Hosted Endpoint

```
https://identity-reconciliation-89q4.onrender.com
```