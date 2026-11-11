# Database Schemas

Every file lives in `src/Data/`. Each is a CSV with no header row: one record per line, columns in the order listed.
IDs come from `IdGenerator.nextId` (highest ID in the file + 1).

## Talent Relations

**Wrestlers.txt**
Wrestler = {id(int), legalName(String), ringName(String), brand(Enum: RAW | SMACKDOWN | NXT), status(Enum: ACTIVE | INJURED | RELEASED), medicalClearance(boolean)}
Example: `1,Oliver,Olliver the Destroyer,RAW,ACTIVE,true`

**Contracts.txt**
Contract = {id(int), wrestlerId(int), yearlySalary(long), startDate(LocalDate), endDate(LocalDate)}
Example: `2,2,850000,2025-01-15,2028-01-15`
A renewal adds a new row and keeps the old one as history.

## Creative

**Storylines.txt**
Storyline = {id(int), name(String), status(Enum: ACTIVE | CONCLUDED)}
Example: `1,The Titan vs Dark Horse,ACTIVE`

**Matches.txt**
Match = {id(int), eventId(int), wrestler1Id(int), wrestler2Id(int), type(Enum: SINGLES | NO_DQ | LADDER | STEEL_CAGE), storylineId(int), winnerId(int), status(Enum: BOOKED | PULLED)}
Example: `1,2,3,5,SINGLES,2,3,BOOKED`
storylineId 0 means a one-off match. winnerId 0 means no contest.

## Live Events

**Events.txt**
Event = {id(int), name(String), type(Enum: WEEKLY_TV | PREMIUM_LIVE_EVENT | HOUSE_SHOW), brand(Enum: RAW | SMACKDOWN | NXT), date(LocalDate), city(String), venueId(int), showId(int, optional), costCents(long), status(Enum: DRAFT | PENDING_APPROVAL | SCHEDULED)}
Example: `2,SmackDown - Chicago,WEEKLY_TV,SMACKDOWN,2026-11-13,Chicago,2,2,3000000,SCHEDULED`
venueId 0 means no venue is held. showId is empty unless a TV show tapes the event.

**Venue.txt**
Venue = {id(int), capacity(int), location(String), rentalCents(long)}
Example: `1,19000,Boston,6000000`

## Ticketing

**Tickets.txt**
Ticket = {id(int), eventId(int), buyerName(String), section(Enum: FLOOR | LOWER_BOWL | UPPER_BOWL), price(long), purchaseDate(LocalDate)}
Example: `2,1,Ava Martinez,FLOOR,150,2026-10-20`

## TV Shows

**Networks.txt**
Network = {id(int), name(String)}
Example: `1,Netflix`

**Shows.txt**
Show = {id(int), name(String), brand(Enum: RAW | SMACKDOWN | NXT), networkId(int), dayOfWeek(String), startTime(LocalTime), lengthMinutes(int)}
Example: `1,Monday Night Raw,RAW,1,Monday,20:00,180`

**LiveBroadcasts.txt**
LiveBroadcast = {id(int), showId(int), eventId(int), airDate(LocalDate), startTime(LocalTime), status(Enum: SCHEDULED | AIRED | CANCELLED)}
Example: `1,2,10,2026-10-30,20:00,AIRED`

## Medical

**medical_records.txt**
Injury = {wrestlerId(int), injury(String), isInjured(boolean), startDate(LocalDate), endDate(LocalDate, optional)}
Example: `8,Sprained Ankle,true,2026-10-30,`
No ID column: a wrestler can have many rows. endDate is empty while the injury is ongoing.
