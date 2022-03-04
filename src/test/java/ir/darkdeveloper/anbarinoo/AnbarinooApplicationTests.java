package ir.darkdeveloper.anbarinoo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class AnbarinooApplicationTests {

	@Test
	void contextLoads() {
		//Fri Mar 04 2022 11:15:01
		var dateFormatter = DateTimeFormatter.ofPattern("EE MMM dd yyyy HH:mm:ss");
		var l = LocalDateTime.now().format(dateFormatter);
		System.out.println(l);
		var l2 = LocalDateTime.parse(l, dateFormatter);
		System.out.println(l2);

	}

}
