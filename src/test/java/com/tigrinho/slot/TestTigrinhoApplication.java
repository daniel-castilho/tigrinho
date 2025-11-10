package com.tigrinho.slot;

import org.springframework.boot.SpringApplication;

public class TestTigrinhoApplication {

	public static void main(String[] args) {
		SpringApplication.from(TigrinhoApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
