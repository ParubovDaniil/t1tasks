package com.example.t1tasks.t1tasks;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.t1tasks.t1tasks.service.DataGeneratorService;

@SpringBootApplication
public class T1tasksApplication {

	public static void main(String[] args) {
		SpringApplication.run(T1tasksApplication.class, args);
	}

	@Bean
	public CommandLineRunner dataLoader(DataGeneratorService generator){
		return args -> {
			generator.generateClients(10);//10 челов
			generator.generateAccounts(20);//20 счетов
			generator.generateTransactions(50);//50 операций
		};
	}
}
