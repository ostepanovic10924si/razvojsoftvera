package rs.raf.examplatform;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.raf.examplatform.model.Role;
import rs.raf.examplatform.model.User;
import rs.raf.examplatform.repository.UserRepository;

@Configuration public class BootstrapUsers {
    @Bean CommandLineRunner bootstrap(UserRepository users,PasswordEncoder encoder,
        @Value("${app.bootstrap.teacher-username}") String teacherName,@Value("${app.bootstrap.teacher-password}") String teacherPassword,
        @Value("${app.bootstrap.student-username}") String studentName,@Value("${app.bootstrap.student-password}") String studentPassword){
        return args->{
            if(users.count()==0){
                if("promeni-me".equals(teacherPassword)||"promeni-me".equals(studentPassword)) System.err.println("WARNING: Change bootstrap credentials before using this API outside local development.");
                users.save(new User(teacherName,encoder.encode(teacherPassword),Role.TEACHER,"Nastavnik",null));
                users.save(new User(studentName,encoder.encode(studentPassword),Role.STUDENT,"Student",studentName));
            }
        };
    }
}
