package com.precisely.infrastructure.aws;

import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FindClassInJar {
	public static void main(String[] args){
		String dirPath = "C:\\Users\\ba004na\\Downloads\\RJS_DataTesting_v1.8jars\\rjslib";

		String fileName = "com/mapinfo/routing/RoutingClient";

		while(fileName.indexOf('.') != -1){
			fileName = fileName.replace('.', '/');
		}

		System.out.println(fileName);
		findJar(new File(dirPath), fileName);
		
	}
	
	static void findJar(File dir, String fileName){
		
		File[] files = dir.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			if(files[i].getName().startsWith(".")){
				continue;
			}
			
			if(files[i].isDirectory()){
				findJar(files[i], fileName);
			}else{
				if(files[i].getName().toLowerCase().endsWith(".zip") ||
						files[i].getName().toLowerCase().endsWith(".jar")){
					findFile(files[i], fileName);
				}
			}
		}
	}
	static void findFile(File file, String fileName){
		try{
//			System.out.println("Searching in >> "+file.getAbsolutePath());
		JarFile jarFile = new JarFile(file);
		Enumeration enumeration = jarFile.entries();
		while(enumeration.hasMoreElements()){
			JarEntry jarEntry = (JarEntry)enumeration.nextElement();
			if(jarEntry.getName().startsWith(fileName)) {
				System.out.println("Entry "+jarEntry.getName()+" Found in "+file.getAbsolutePath());
			}
			if(jarEntry.getName().equals(fileName)){
				System.out.println("Entry "+jarEntry.getName()+" Found in "+file.getAbsolutePath());
			}
		}
		}catch(Exception ex){System.out.println(ex.getMessage() + " " + file.getName());
		}
	}
}
