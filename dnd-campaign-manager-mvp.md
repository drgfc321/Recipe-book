# D&D Campaign Manager - MVP Document

## Project Overview

A web application for managing Dungeons & Dragons campaigns, characters, and sessions. Built with a separated frontend/backend architecture.

---

## Tech Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| Frontend | Vaadin 24 + Spring Boot 3 | UI Components & Frontend Server |
| Backend | Quarkus 3 | REST API & Business Logic |
| Database | PostgreSQL | Data Storage |
| Authentication | JWT (JSON Web Tokens) | Stateless Auth |
| ORM | Hibernate + Panache | Database Access |
| Build Tool | Maven | Dependency Management |
| IDE | IntelliJ IDEA | Development |

---

## Architecture

```
┌─────────────────────────────────┐
│           FRONTEND              │
│      Vaadin + Spring Boot       │
│         Port: 8081              │
├─────────────────────────────────┤
│  • Views (UI Pages)             │
│  • i18n (Translations)          │
│  • JWT Token Storage            │
│  • API Client (REST calls)      │
└───────────────┬─────────────────┘
                │
                │ HTTP/REST + JWT
                ▼
┌─────────────────────────────────┐
│           BACKEND               │
│           Quarkus               │
│         Port: 8080              │
├─────────────────────────────────┤
│  • REST Controllers             │
│  • JWT Validation               │
│  • Business Logic               │
│  • Database Access (Panache)    │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│         PostgreSQL              │
│         Port: 5432              │
└─────────────────────────────────┘
```

---

## MVP Features (Version 1.0)

### 1. Authentication & Users
- [x] User registration (email, username, password)
- [x] User login (returns JWT token)
- [x] Password hashing (BCrypt)
- [x] Protected routes (must be logged in)
- [x] User roles: PLAYER, DUNGEON_MASTER
- [x] User preferred language setting

### 2. Internationalization (i18n)
- [x] English (default)
- [x] Romanian
- [x] Language switcher in UI
- [x] Language preference saved to user account

### 3. Campaigns
- [x] Create campaign (name, description, setting)
- [x] Edit campaign
- [x] Delete campaign
- [x] List my campaigns (as DM)
- [x] Campaign status (active, paused, completed)
- [x] View campaign details

### 4. Characters
- [x] Create character (PC - Player Character)
- [x] Basic info: name, race, class, level
- [x] Stats: STR, DEX, CON, INT, WIS, CHA
- [x] Combat stats: HP, AC, Speed
- [x] Edit character
- [x] Delete character
- [x] Assign character to campaign
- [x] Backstory text field

### 5. Sessions
- [x] Create session log for a campaign
- [x] Session number, date
- [x] Session summary/notes
- [x] XP awarded
- [x] Edit session
- [x] Delete session
- [x] List sessions for a campaign

### 6. Dice Roller
- [x] Roll any dice: d4, d6, d8, d10, d12, d20, d100
- [x] Add modifier to roll
- [x] Roll multiple dice (e.g., 2d6)
- [x] Advantage / Disadvantage rolls (d20)
- [x] Display result

---

## NOT in MVP (Future Versions)

- Password reset via email
- OAuth login (Google, Discord)
- Combat tracker with initiative
- NPC management
- Locations / Maps
- Inventory system with items database
- Spell book
- Bestiary (monsters)
- Image uploads (portraits, maps)
- Real-time features (WebSockets)
- Random name/loot generators
- Campaign invites / sharing
- Mobile-responsive design improvements

---

## Database Schema (MVP)

### Users Table
```sql
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) UNIQUE NOT NULL,
    username        VARCHAR(100) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50) DEFAULT 'PLAYER',
    language        VARCHAR(10) DEFAULT 'en',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login      TIMESTAMP
);
```

### Campaigns Table
```sql
CREATE TABLE campaigns (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    setting         VARCHAR(255),
    status          VARCHAR(50) DEFAULT 'ACTIVE',
    dm_id           BIGINT REFERENCES users(id),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Characters Table
```sql
CREATE TABLE characters (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    race            VARCHAR(100),
    class           VARCHAR(100),
    level           INT DEFAULT 1,
    
    -- Stats
    strength        INT DEFAULT 10,
    dexterity       INT DEFAULT 10,
    constitution    INT DEFAULT 10,
    intelligence    INT DEFAULT 10,
    wisdom          INT DEFAULT 10,
    charisma        INT DEFAULT 10,
    
    -- Combat
    hit_points      INT DEFAULT 10,
    armor_class     INT DEFAULT 10,
    speed           INT DEFAULT 30,
    
    backstory       TEXT,
    
    -- Relationships
    user_id         BIGINT REFERENCES users(id),
    campaign_id     BIGINT REFERENCES campaigns(id),
    
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Sessions Table
```sql
CREATE TABLE sessions (
    id              BIGSERIAL PRIMARY KEY,
    session_number  INT NOT NULL,
    session_date    DATE,
    summary         TEXT,
    xp_awarded      INT DEFAULT 0,
    notes           TEXT,
    
    -- Relationships
    campaign_id     BIGINT REFERENCES campaigns(id),
    
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## API Endpoints (MVP)

### Authentication
```
POST   /api/auth/register     - Register new user
POST   /api/auth/login        - Login, returns JWT
GET    /api/auth/me           - Get current user info
```

### Users
```
GET    /api/users/{id}        - Get user profile
PUT    /api/users/{id}        - Update user (language, etc.)
```

### Campaigns
```
GET    /api/campaigns         - List user's campaigns
POST   /api/campaigns         - Create campaign
GET    /api/campaigns/{id}    - Get campaign details
PUT    /api/campaigns/{id}    - Update campaign
DELETE /api/campaigns/{id}    - Delete campaign
```

### Characters
```
GET    /api/characters        - List user's characters
POST   /api/characters        - Create character
GET    /api/characters/{id}   - Get character details
PUT    /api/characters/{id}   - Update character
DELETE /api/characters/{id}   - Delete character
```

### Sessions
```
GET    /api/campaigns/{id}/sessions     - List campaign sessions
POST   /api/campaigns/{id}/sessions     - Create session
GET    /api/sessions/{id}               - Get session details
PUT    /api/sessions/{id}               - Update session
DELETE /api/sessions/{id}               - Delete session
```

### Dice
```
POST   /api/dice/roll         - Roll dice (body: { dice: "2d6", modifier: 3 })
```

---

## Project Structure

### Backend (Quarkus)
```
dnd-backend/
├── pom.xml
└── src/main/java/com/dnd/
    ├── entity/
    │   ├── User.java
    │   ├── Campaign.java
    │   ├── Character.java
    │   └── Session.java
    ├── repository/
    │   ├── UserRepository.java
    │   ├── CampaignRepository.java
    │   ├── CharacterRepository.java
    │   └── SessionRepository.java
    ├── service/
    │   ├── AuthService.java
    │   ├── CampaignService.java
    │   ├── CharacterService.java
    │   ├── SessionService.java
    │   └── DiceService.java
    ├── controller/
    │   ├── AuthController.java
    │   ├── CampaignController.java
    │   ├── CharacterController.java
    │   ├── SessionController.java
    │   └── DiceController.java
    ├── dto/
    │   ├── LoginRequest.java
    │   ├── RegisterRequest.java
    │   └── ... (other DTOs)
    └── security/
        └── JwtUtils.java
```

### Frontend (Vaadin + Spring Boot)
```
dnd-frontend/
├── pom.xml
└── src/main/
    ├── java/com/dnd/frontend/
    │   ├── views/
    │   │   ├── LoginView.java
    │   │   ├── RegisterView.java
    │   │   ├── MainLayout.java
    │   │   ├── DashboardView.java
    │   │   ├── CampaignsView.java
    │   │   ├── CampaignDetailView.java
    │   │   ├── CharactersView.java
    │   │   ├── CharacterDetailView.java
    │   │   ├── SessionsView.java
    │   │   └── DiceRollerView.java
    │   ├── service/
    │   │   ├── ApiClient.java
    │   │   ├── AuthService.java
    │   │   └── ... (other services)
    │   ├── security/
    │   │   └── SecurityConfig.java
    │   └── i18n/
    │       └── TranslationProvider.java
    └── resources/
        ├── application.properties
        └── i18n/
            ├── messages.properties
            └── messages_ro.properties
```

---

## UI Pages (MVP)

1. **Login Page** - Email/password login
2. **Register Page** - Create new account
3. **Dashboard** - Overview, quick stats
4. **Campaigns List** - All my campaigns
5. **Campaign Detail** - Single campaign with sessions
6. **Characters List** - All my characters
7. **Character Detail/Edit** - Character sheet
8. **Session Detail/Edit** - Session notes
9. **Dice Roller** - Roll dice tool
10. **Settings** - Language, profile

---

## Development Phases

### Phase 1: Setup (Week 1)
- [ ] Set up PostgreSQL database
- [ ] Create Quarkus backend project
- [ ] Create Vaadin frontend project
- [ ] Basic project structure
- [ ] Test connection between frontend ↔ backend

### Phase 2: Authentication (Week 2)
- [ ] User entity and repository
- [ ] Registration endpoint
- [ ] Login endpoint with JWT
- [ ] Frontend login/register pages
- [ ] JWT token handling in frontend

### Phase 3: Core Features (Week 3-4)
- [ ] Campaigns CRUD (backend + frontend)
- [ ] Characters CRUD (backend + frontend)
- [ ] Sessions CRUD (backend + frontend)

### Phase 4: Extras (Week 5)
- [ ] Dice roller
- [ ] i18n implementation
- [ ] Polish UI
- [ ] Testing & bug fixes

---

## Success Criteria

MVP is complete when:
1. ✅ User can register and login
2. ✅ User can create/edit/delete campaigns
3. ✅ User can create/edit/delete characters with stats
4. ✅ User can log sessions for campaigns
5. ✅ User can roll dice
6. ✅ App works in 2 languages
7. ✅ All data persists in database
8. ✅ Only authenticated users can access the app

---

## Notes

- Start simple, add complexity later
- Commit to Git frequently
- Test each feature before moving on
- Don't perfectionist — working > perfect
