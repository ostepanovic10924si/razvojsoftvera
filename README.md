# RAF platforma za kolokvijume i ispite

Početni serverski deo projekta: REST API razvijen u Spring Boot-u. Trenutno su obuhvaćeni model podataka sa JPA perzistencijom i prijava korisnika uz autorizaciju po ulogama `STUDENT` i `TEACHER`.

## Tehnologije

- Java 17, Spring Boot 3.4.5
- Spring Web, Spring Data JPA, Spring Security
- H2 baza u datoteci `./data/exam-platform`
- JWT Bearer tokeni, BCrypt heširanje lozinki

## Pokretanje

Potrebni su JDK 17+ i Maven 3.9+. Iz korena projekta:

```sh
mvn spring-boot:run
```

Pri prvom pokretanju se kreiraju početni nalozi:

| Uloga | Korisničko ime | Lozinka |
| --- | --- | --- |
| Nastavnik | `nastavnik` | `promeni-me` |
| Student | `student` | `promeni-me` |

Vrednosti zadajte kroz promenljive okruženja `BOOTSTRAP_TEACHER_USERNAME`, `BOOTSTRAP_TEACHER_PASSWORD`, `BOOTSTRAP_STUDENT_USERNAME` i `BOOTSTRAP_STUDENT_PASSWORD`. Token potpisuje `JWT_SECRET`; postavite sopstvenu tajnu od najmanje 32 bajta pre pokretanja van lokalnog razvoja. Nalozi se ubacuju samo kada je tabela korisnika prazna.

## Model i relacije

- `Subject` — predmet.
- `ExamPeriod` — ispitni rok.
- `ExamGroup` — grupa i učionica.
- `ExamSession` — termin povezan sa predmetom, rokom i grupom.
- `Assignment` — zadatak sa opisom i URL-om projekta, vezan za termin.
- `User` — nalog i uloga; studentski nalog čuva indeks studenta.
- `ExamRegistration` — studentova prijava za termin; jedinstven par student/termin.
- `Submission` — studentsko rešenje sa vremenom preuzimanja i predaje, repozitorijumom i poenima.
- `ReviewComment` — komentar nastavnika vezan za predaju, fajl i opseg linija.

Hibernate kreira i ažurira tabele pri pokretanju (`ddl-auto=update`); H2 čuva podatke u lokalnoj datoteci kako bi preživeli restart aplikacije.

## Prijava i kontrola pristupa

`POST /api/auth/login` prima JSON `{ "username": "student", "password": "promeni-me" }` i vraća potpisani JWT. Zaštićene rute očekuju zaglavlje `Authorization: Bearer <token>`.

- `GET /api/student/me` — samo student.
- `GET /api/teacher/me` — samo nastavnik.
- Sve rute ispod `/api/teacher/**` zahtevaju nastavnički token.
- Svaka druga ruta zahteva prijavu; nastavničke rute se grupišu pod `/api/teacher/**`, a studentske pod `/api/student/**`.

## Upravljanje predmetima, terminima, zadacima i prijavama

Nakon prijave kao nastavnik, prosledite token kao `Authorization: Bearer <token>`.

- `POST /api/teacher/subjects` — `{ "code": "RS", "name": "Razvoj softvera" }`
- `POST /api/teacher/exam-periods` — `{ "name": "Januarski", "startsOn": "2027-01-10", "endsOn": "2027-01-31" }`
- `POST /api/teacher/groups` — `{ "name": "Grupa 1", "room": "A1" }`
- `POST /api/teacher/sessions` — `{ "name": "Kolokvijum 1", "startsAt": "2027-01-15T09:00:00Z", "endsAt": "2027-01-15T11:00:00Z", "subjectId": 1, "examPeriodId": 1, "groupId": 1 }`
- `POST /api/teacher/sessions/{sessionId}/assignments` — `{ "title": "Zadatak 1", "description": "Opis zadatka", "projectUrl": "https://example.com/repo.git" }`
- `POST /api/teacher/students` — kreira studentski nalog: `{ "username": "pera", "password": "lozinka", "fullName": "Petar Petrović", "studentIndex": "2026/1234" }`
- `POST /api/teacher/sessions/{sessionId}/registrations` — unosi spisak prijavljenih: `{ "studentUsernames": ["pera", "mila"] }`

Predmet, rok, grupa i termin imaju odgovarajuće `GET` rute za pregled/ID-jeve. Nastavnik može pregledati zadatke termina preko `GET /api/teacher/sessions/{sessionId}/assignments` i spisak prijavljenih preko `GET /api/teacher/sessions/{sessionId}/registrations`. Studentski nalozi se kreiraju i spiskovi unose isključivo nastavničkim tokenom; dupliran unos prijave vraća HTTP 409.

Preuzimanje zadatka, predaja rešenja, monitoring i ocenjivanje slede u narednim funkcionalnostima.
