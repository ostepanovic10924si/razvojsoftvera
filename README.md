# RAF platforma za kolokvijume i ispite

Serverski deo projekta: REST API razvijen u Spring Boot-u. Obuhvata model podataka sa JPA perzistencijom, prijavu korisnika uz autorizaciju po ulogama `STUDENT` i `TEACHER`, upravljanje predmetima, terminima, zadacima i spiskovima prijavljenih, proveru prava i preuzimanje zadatka, predaju rešenja, podatke za monitoring i pregled i ocenjivanje rešenja. Zadatak i rešenje se razmenjuju kao URL repozitorijuma.

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
- Sve rute ispod `/api/student/**` zahtevaju studentski token.
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

Za pregled i ID-jeve postoje `GET /api/teacher/subjects`, `GET /api/teacher/exam-periods`, `GET /api/teacher/groups`, `GET /api/teacher/sessions` i `GET /api/teacher/students`. Nastavnik može pregledati zadatke termina preko `GET /api/teacher/sessions/{sessionId}/assignments` i spisak prijavljenih preko `GET /api/teacher/sessions/{sessionId}/registrations`. Studentski nalozi se kreiraju i spiskovi unose isključivo nastavničkim tokenom; dupliran unos prijave vraća HTTP 409.

## Studentski tok

Sve rute u ovom delu zahtevaju studentski token. Student mora biti na spisku prijavljenih za termin, koji unosi nastavnik.

- `GET /api/student/sessions` — termini koji još nisu završeni, za izbor termina i grupe.
- `POST /api/student/check-in` — provera prava na polaganje: `{ "sessionId": 1, "groupId": 1, "studentIndex": "2026/1234" }`. Proverava da grupa polaže u terminu, da termin nije završen, da indeks odgovara nalogu i da je student na spisku; ništa ne upisuje u bazu. Vraća podatke o terminu, `serverTime` i `status` (`NOT_STARTED` ili `IN_PROGRESS`).
- `POST /api/student/sessions/{sessionId}/download` — preuzimanje zadataka termina, samo dok termin traje. Vraća naslov, opis i `projectUrl` svakog zadatka. Pri prvom preuzimanju upisuje vreme preuzimanja; ponovno preuzimanje ga ne menja.
- `POST /api/student/assignments/{assignmentId}/submission` — predaja rešenja kao URL repozitorijuma: `{ "repositoryUrl": "https://github.com/pera/zadatak1" }`. Moguća je samo za preuzet zadatak i samo do kraja termina; ponovna predaja menja URL i vreme predaje. Vraća potvrdu sa vremenom preuzimanja i predaje.
- `GET /api/student/submissions` — predata rešenja prijavljenog studenta sa dodeljenim poenima i komentarima nastavnika. Student vidi isključivo svoja rešenja.

## Monitoring

- `GET /api/teacher/sessions/{sessionId}/monitoring` — za termin vraća broj prijavljenih studenata (`registered`), broj studenata koji su preuzeli zadatak (`downloaded`), koji su u izradi (`inProgress`) i koji su predali rad (`submitted`), prosečno vreme izrade u minutima (`averageWorkMinutes`), `serverTime` i status svakog studenta: `NOT_DOWNLOADED`, `IN_PROGRESS` ili `SUBMITTED`. Veb aplikacija za monitoring ovu rutu poziva periodično, pa se podaci osvežavaju bez ponovnog učitavanja stranice.

## Pregled i ocenjivanje

- `GET /api/teacher/submissions` — predata rešenja, uz opcione filtere koji se mogu kombinovati: `subjectId`, `examPeriodId`, `sessionId`, `groupId`, `room` (učionica) i `student` (korisničko ime ili broj indeksa). Primer: `/api/teacher/submissions?sessionId=1&room=A1`.
- `GET /api/teacher/submissions/{submissionId}` — jedno rešenje sa poenima i svim komentarima, poređanim po fajlu i liniji.
- `POST /api/teacher/submissions/{submissionId}/comments` — komentar vezan za fajl i opseg linija: `{ "filePath": "src/Main.java", "startLine": 10, "endLine": 12, "body": "Ovde fali provera" }`. `endLine` nije obavezan.
- `PUT /api/teacher/submissions/{submissionId}/points` — dodeljeni poeni: `{ "points": 8.5 }`; ponovni poziv menja poene.

Komentari i poeni mogu da se unesu samo za predato rešenje.

## Greške i formati

Greške se vraćaju kao JSON sa HTTP statusom i porukom, npr. `{ "timestamp": "…", "status": 403, "error": "Forbidden", "message": "Niste na spisku prijavljenih za ovaj termin", "path": "/api/student/check-in" }`. Tekstovi poruka su na jednom mestu, u `src/main/java/rs/raf/examplatform/utility/Messages.java`. Vremena su u UTC formatu ISO 8601, npr. `2027-01-15T09:00:00Z`.
