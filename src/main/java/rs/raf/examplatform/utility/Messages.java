package rs.raf.examplatform.utility;

/**
 * User-facing API messages, kept in one place so the same error always has the same text.
 */
public final class Messages {

    // Authentication and accounts
    public static final String INVALID_CREDENTIALS = "Neispravno korisnicko ime ili lozinka";
    public static final String ACCOUNT_NOT_FOUND = "Nalog nije pronadjen";

    // Subjects
    public static final String SUBJECT_CODE_REQUIRED = "Sifra predmeta je obavezna";
    public static final String SUBJECT_NAME_REQUIRED = "Naziv predmeta je obavezan";
    public static final String SUBJECT_NOT_FOUND = "Predmet nije pronadjen";

    // Exam periods
    public static final String PERIOD_NAME_REQUIRED = "Naziv ispitnog roka je obavezan";
    public static final String PERIOD_END_BEFORE_START = "Kraj ispitnog roka ne moze biti pre pocetka";
    public static final String PERIOD_NOT_FOUND = "Ispitni rok nije pronadjen";

    // Groups
    public static final String GROUP_NAME_REQUIRED = "Naziv grupe je obavezan";
    public static final String GROUP_NOT_FOUND = "Grupa nije pronadjena";

    // Exam sessions
    public static final String SESSION_NAME_REQUIRED = "Naziv termina je obavezan";
    public static final String SESSION_INVALID_TIME_RANGE = "Termin mora imati validan pocetak i kraj";
    public static final String SESSION_REFERENCES_REQUIRED = "Predmet, ispitni rok i grupa su obavezni";
    public static final String SESSION_NOT_FOUND = "Termin nije pronadjen";
    public static final String SESSION_NOT_STARTED = "Termin jos nije poceo";
    public static final String SESSION_ENDED = "Termin je zavrsen";

    // Assignments
    public static final String ASSIGNMENT_TITLE_REQUIRED = "Naziv zadatka je obavezan";
    public static final String ASSIGNMENT_PROJECT_URL_REQUIRED = "URL projekta je obavezan";
    public static final String ASSIGNMENT_NOT_FOUND = "Zadatak nije pronadjen";
    public static final String SESSION_HAS_NO_ASSIGNMENTS = "Za ovaj termin jos nema zadatka";

    // Student accounts
    public static final String USERNAME_REQUIRED = "Korisnicko ime je obavezno";
    public static final String PASSWORD_REQUIRED = "Lozinka je obavezna";
    public static final String STUDENT_INDEX_REQUIRED = "Broj indeksa je obavezan";
    public static final String USERNAME_TAKEN = "Korisnicko ime vec postoji";
    /** Use with {@code formatted(username)}. */
    public static final String STUDENT_NOT_FOUND = "Student nije pronadjen: %s";

    // Registration lists
    public static final String REGISTRATION_LIST_EMPTY = "Navedite bar jednog studenta";
    public static final String REGISTRATION_LIST_INVALID = "Spisak sadrzi prazno ili ponovljeno korisnicko ime";
    public static final String REGISTRATION_LIST_NOT_STUDENTS = "Spisak moze sadrzati samo studentske naloge";
    /** Use with {@code formatted(username)}. */
    public static final String STUDENT_ALREADY_REGISTERED = "Student je vec prijavljen: %s";
    public static final String NOT_REGISTERED_FOR_SESSION = "Niste na spisku prijavljenih za ovaj termin";

    // Student check-in
    public static final String CHECK_IN_FIELDS_REQUIRED = "Termin, grupa i broj indeksa su obavezni";
    public static final String GROUP_NOT_IN_SESSION = "Izabrana grupa ne polaze u ovom terminu";
    public static final String STUDENT_INDEX_MISMATCH = "Broj indeksa ne odgovara prijavljenom nalogu";

    // Submissions
    public static final String REPOSITORY_URL_REQUIRED = "URL repozitorijuma je obavezan";
    public static final String REPOSITORY_URL_INVALID = "URL repozitorijuma nije ispravan";
    public static final String ASSIGNMENT_NOT_DOWNLOADED = "Zadatak prvo mora da se preuzme";
    public static final String SUBMISSION_CLOSED = "Termin je zavrsen, predaja vise nije moguca";

    private Messages() {
    }
}
