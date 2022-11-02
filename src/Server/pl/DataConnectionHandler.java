package Server.pl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Server.dto.*;


public class DataConnectionHandler extends Thread{
	private ServerSocket soc;
	private ListenHandler producer;
	private static final int MAX_BUFFER = 8192;
	private DataInputStream dis;
	private DataOutputStream dos;
	private byte[] buffer;
	private int read;
	private volatile String response = "";
	public DataConnectionHandler(ServerSocket soc, ListenHandler producer) {
		this.soc = soc;
		this.producer = producer;
	}
	
	public void run() {
		try {
			Socket clientSoc = soc.accept();
			
				String message = producer.getMessage();
	            String cmd = message.substring(0, message.indexOf(" "));
	            String params = message.substring(message.indexOf(" ")+1);
	            String baseDir = producer.baseDir; 
	            switch (cmd) {
				case "LIST": {
					ObjectOutputStream oos = new ObjectOutputStream(clientSoc.getOutputStream());
					
					File[] files = new File(baseDir + params).listFiles();
					ArrayList<FileDto> result = new ArrayList<FileDto>();
					if(files != null) {
						for(File file : files) {
							FileDto fDto = new FileDto();
							fDto.setName(file.getName());
							Path p = Paths.get(file.getAbsolutePath());
							BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
							
							fDto.setSize(attr.size());
							fDto.setType(attr.isDirectory() ? "Dir" : "File");
							DateFormat df=new SimpleDateFormat("DD/MM/YYYY");
							fDto.setCreatedDate(new Date(attr.creationTime().toMillis()));
							result.add(fDto);
						}
					}
					oos.writeObject(result);
					response = "226 Directory send OK";
					break;
				}
				case "STOR":
					dis = new DataInputStream(clientSoc.getInputStream());
					dos = new DataOutputStream(new FileOutputStream(baseDir+params));
					
					buffer = new byte[MAX_BUFFER];
					read = 0;
					while((read = dis.read(buffer)) != -1) {
						dos.write(buffer, 0, read);
						dos.flush();
						buffer = new byte[MAX_BUFFER];
					}
					response = "226 Transfer completed";
					dis.close();
					dos.close();
					
					break;
				case "RETR":
					DataInputStream dis = new DataInputStream(new FileInputStream(baseDir+params));
					DataOutputStream dos = new DataOutputStream(clientSoc.getOutputStream());
					
					buffer = new byte[MAX_BUFFER];
					read = 0;
					while((read = dis.read(buffer)) != -1) {
						dos.write(buffer, 0, read);
						dos.flush();
						buffer = new byte[MAX_BUFFER];
					}
					response = "226 Transfer completed";
					dis.close();
					dos.close();
					break;
				default:
					
				}
	            clientSoc.close();
	            soc.close();
	            
			
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();	
		}
	}
	public String getResponseMessage() {
		return response;
	}
}
