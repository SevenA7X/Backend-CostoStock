package cl.duoc.costostock.transacciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransaccionesApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransaccionesApplication.class, args);
    }
}
