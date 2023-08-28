package com.vhc.ec.filemgmt.controller;

import com.vhc.ec.filemgmt.util.EncryptorAes;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("aaa");
		try {
    		Files.createDirectories(Paths.get("D:\\econtract\\econtract-service\\application\\ec-file-srv\\tmp"));
        } catch (IOException e) {
            System.out.println(e);
        }

		final var temFilePath = String.format("D:\\econtract\\econtract-service\\application\\ec-file-srv\\tmp\\%s", "031b995f-a0d8-42a1-bba5-47b010fe0a11.pdf");
		FileOutputStream output = new FileOutputStream(temFilePath);

		FileInputStream input = new FileInputStream("C:\\Users\\thang\\Desktop\\031b995f-a0d8-42a1-bba5-47b010fe0a11.pdf");

		byte[] b = EncryptorAes.decryptFile(input.readAllBytes());

		output.write(b);
		output.close();
		System.out.println("ok");
	}

}
