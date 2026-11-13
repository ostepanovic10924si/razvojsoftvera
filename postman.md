RAF platforma za kolokvijume i ispite - zahtevi za Postman
==========================================================

KAKO SE KORISTI

1. Kopiraj ceo blok koji pocinje sa "curl" i nalepi ga u Postman: Import -> Raw text,
   ili direktno u polje za URL novog zahteva. Postman sam postavi metod, URL, zaglavlja i telo.

2. Tokeni se cuvaju u dve globalne promenljive: teacherToken i studentToken
   (Environments -> Globals). Posle logina kopiraj vrednost "token" iz odgovora u odgovarajucu promenljivu.
   Da se to radi automatski, u login zahtevu dodaj u Scripts -> Post-response:
       pm.globals.set("teacherToken", pm.response.json().token);    // login nastavnika
       pm.globals.set("studentToken", pm.response.json().token);    // login studenta

3. ID-jevi u primerima su 1. Ako baza vec ima podatke, koristi ID-jeve iz odgovora.

4. Vremena su u UTC (Srbija je UTC+1). Termin ispod namerno traje dugo, da bi preuzimanje i predaja
   radili kad god testiras. Za pravi termin postavi stvarno vreme pocetka i kraja.

Redosled prati ceo tok: nastavnik unosi podatke, student polaze, nastavnik prati i ocenjuje.

BAZA:
http://localhost:8080/h2-console


### 1. PRIJAVA NASTAVNIKA

--- Login nastavnika (token ide u teacherToken)
curl --location 'http://localhost:8080/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{"username": "nastavnik", "password": "promeni-me"}'

--- Profil nastavnika
curl --location 'http://localhost:8080/api/teacher/me' \
--header 'Authorization: Bearer {{teacherToken}}'


### 2. NASTAVNIK - PREDMETI, ISPITNI ROKOVI I GRUPE

--- Novi predmet
curl --location 'http://localhost:8080/api/teacher/subjects' \
--header 'Authorization: Bearer {{teacherToken}}' \
--header 'Content-Type: application/json' \
--data '{"code": "RS", "name": "Razvoj softvera"}'

--- Lista predmeta
curl --location 'http://localhost:8080/api/teacher/subjects' \
--header 'Authorization: Bearer {{teacherToken}}'

--- Novi ispitni rok
curl --location 'http://localhost:8080/api/teacher/exam-periods' \
--header 'Authorization: Bearer {{teacherToken}}' \
--header 'Content-Type: application/json' \
--data '{"name": "Januarski", "startsOn": "2027-01-10", "endsOn": "2027-01-31"}'

--- Lista ispitnih rokova
curl --location 'http://localhost:8080/api/teacher/exam-periods' \
--header 'Authorization: Bearer {{teacherToken}}'

--- Nova grupa
curl --location 'http://localhost:8080/api/teacher/groups' \
--header 'Authorization: Bearer {{teacherToken}}' \
--header 'Content-Type: application/json' \
--data '{"name": "Grupa 1", "room": "A1"}'

--- Lista grupa
curl --location 'http://localhost:8080/api/teacher/groups' \
--header 'Authorization: Bearer {{teacherToken}}'


### 3. NASTAVNIK - TERMINI I ZADACI

--- Novi termin (namerno sirok opseg vremena, vidi tacku 4 na vrhu)
curl --location 'http://localhost:8080/api/teacher/sessions' \
--header 'Authorization: Bearer {{teacherToken}}' \
--header 'Content-Type: application/json' \
--data '{"name": "Kolokvijum 1", "startsAt": "2026-01-01T08:00:00Z", "endsAt": "2027-12-31T20:00:00Z", "subjectId": 1, "examPeriodId": 1, "groupId": 1}'

--- Lista termina
curl --location 'http://localhost:8080/api/teacher/sessions' \
--header 'Authorization: Bearer {{teacherToken}}'

--- Novi zadatak za termin 1
curl --location 'http://localhost:8080/api/teacher/sessions/1/assignments' \
--header 'Authorization: Bearer {{teacherToken}}' \
--header 'Content-Type: application/json' \
--data '{"title": "Zadatak 1", "description": "Opis zadatka", "projectUrl": "https://github.com/raf/zadatak1"}'

--- Lista zadataka termina 1
curl --location 'http://localhost:8080/api/teacher/sessions/1/assignments' \
--header 'Authorization: Bearer {{teacherToken}}'


### 4. NASTAVNIK - STUDENTI I SPISAK PRIJAVLJENIH

--- Novi student
curl --location 'http://localhost:8080/api/teacher/students' \
--header 'Authorization: Bearer {{teacherToken}}' \
--header 'Content-Type: application/json' \
--data '{"username": "pera", "password": "lozinka", "fullName": "Petar Petrovic", "studentIndex": "2026/1234"}'

--- Lista studenata
curl --location 'http://localhost:8080/api/teacher/students' \
--header 'Authorization: Bearer {{teacherToken}}'

--- Unos spiska prijavljenih za termin 1
curl --location 'http://localhost:8080/api/teacher/sessions/1/registrations' \
--header 'Authorization: Bearer {{teacherToken}}' \
--header 'Content-Type: application/json' \
--data '{"studentUsernames": ["pera"]}'

--- Spisak prijavljenih za termin 1
curl --location 'http://localhost:8080/api/teacher/sessions/1/registrations' \
--header 'Authorization: Bearer {{teacherToken}}'


### 5. PRIJAVA STUDENTA

--- Login studenta (token ide u studentToken)
curl --location 'http://localhost:8080/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{"username": "pera", "password": "lozinka"}'

--- Profil studenta
curl --location 'http://localhost:8080/api/student/me' \
--header 'Authorization: Bearer {{studentToken}}'


### 6. STUDENT - POLAGANJE

--- Termini koji jos nisu zavrseni
curl --location 'http://localhost:8080/api/student/sessions' \
--header 'Authorization: Bearer {{studentToken}}'

--- Provera prava na polaganje (check-in)
curl --location 'http://localhost:8080/api/student/check-in' \
--header 'Authorization: Bearer {{studentToken}}' \
--header 'Content-Type: application/json' \
--data '{"sessionId": 1, "groupId": 1, "studentIndex": "2026/1234"}'

--- Preuzimanje zadataka termina 1 (samo dok termin traje)
curl --location --request POST 'http://localhost:8080/api/student/sessions/1/download' \
--header 'Authorization: Bearer {{studentToken}}'

--- Predaja resenja za zadatak 1 (URL repozitorijuma)
curl --location 'http://localhost:8080/api/student/assignments/1/submission' \
--header 'Authorization: Bearer {{studentToken}}' \
--header 'Content-Type: application/json' \
--data '{"repositoryUrl": "https://github.com/pera/zadatak1"}'


### 7. NASTAVNIK - MONITORING

--- Monitoring termina 1
curl --location 'http://localhost:8080/api/teacher/sessions/1/monitoring' \
--header 'Authorization: Bearer {{teacherToken}}'


### 8. NASTAVNIK - PREGLED I OCENJIVANJE

--- Sva predata resenja
curl --location 'http://localhost:8080/api/teacher/submissions' \
--header 'Authorization: Bearer {{teacherToken}}'

--- Predata resenja sa filterima (svi su opcioni: subjectId, examPeriodId, sessionId, groupId, room, student)
curl --location 'http://localhost:8080/api/teacher/submissions?sessionId=1&room=A1&student=pera' \
--header 'Authorization: Bearer {{teacherToken}}'

--- Jedno resenje sa poenima i komentarima
curl --location 'http://localhost:8080/api/teacher/submissions/1' \
--header 'Authorization: Bearer {{teacherToken}}'

--- Komentar na fajl i linije (endLine nije obavezan)
curl --location 'http://localhost:8080/api/teacher/submissions/1/comments' \
--header 'Authorization: Bearer {{teacherToken}}' \
--header 'Content-Type: application/json' \
--data '{"filePath": "src/Main.java", "startLine": 10, "endLine": 12, "body": "Ovde fali provera ulaza"}'

--- Poeni (ponovni poziv menja poene)
curl --location --request PUT 'http://localhost:8080/api/teacher/submissions/1/points' \
--header 'Authorization: Bearer {{teacherToken}}' \
--header 'Content-Type: application/json' \
--data '{"points": 8.5}'


### 9. STUDENT - REZULTATI

--- Moja resenja sa poenima i komentarima
curl --location 'http://localhost:8080/api/student/submissions' \
--header 'Authorization: Bearer {{studentToken}}'
