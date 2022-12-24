package Client.bll;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import Client.view.Client;

public class UploadTask extends SwingWorker<String, String>{
	private File localFile;
	private String uploadDir;
	private Client client;
	private boolean isError = false;
	String mode;
	private static final int PORT_RANGE_START = 20000;
	private static final int PORT_RANGE_END = 22000;
	DataInputStream datadis;
	DataOutputStream datados;
	
	public UploadTask(File localFile, String uploadDir, Client client, String mode) {
		this.localFile = localFile;
		this.uploadDir = uploadDir;
		this.client = client;
		this.mode = mode;
	}
	@Override
	protected String doInBackground() throws Exception {
		// TODO Auto-generated method stub
		long start = System.nanoTime(); 
		if(localFile.isFile()) {
			uploadSingleFile(localFile, uploadDir);			
		} else {
			uploadDirectory(localFile, uploadDir);
		}
		long totalTime = System.nanoTime() - start;
		System.out.println(TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS));
		return null;
	}
	
	@Override
	protected void process(List<String> chunks) {
		client.lblTask.setText(chunks.get(chunks.size()-1));
	} 
	
	private void uploadSingleFile(File localFile, String uploadDir) {
		try {
			Path p = Paths.get(localFile.getAbsolutePath());
			long size = Files.size(p);
			long uploaded = 0;
			int percentCompleted = 0;
			publish("Uploading " + localFile.getAbsolutePath());
			ServerSocket dataServer = null;
			Socket datasoc = null;
			int port = 0;
			Random generator = new Random();
			if(this.mode == "PASV" ) {
				client.dos.writeUTF("PASV");
				String response = client.showServerResponse();
				port = Integer.valueOf(response.substring(response.indexOf("(")+1, response.indexOf(")")));
				datasoc = new Socket(client.server, port);
			}
			else {
				while(true) {
					 port = generator.nextInt((PORT_RANGE_END - PORT_RANGE_START) + 1) + PORT_RANGE_START;
					try {
						dataServer = new ServerSocket(port);
						break;
					} catch (IOException e) {
						//nếu đã có kết nối ở port dc chon thì sẽ có lôĩ
						//catch ở đây để vòng lặp while dc tiếp tuc lặp
					}
				}
				
				String ipAddr = "";
				Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
				for (NetworkInterface netint : Collections.list(nets)) {
				    if (!netint.isLoopback()) {
				        //theOneAddress = Collections.list(netint.getInetAddresses()).stream().findFirst().orElse(null);
				    	ArrayList<InetAddress> list = Collections.list(netint.getInetAddresses());
				    	for(int i = 0; i < list.size(); i++) {
				    		if(list.get(i).toString().contains(":")) {
				    			continue;
				    		}
				    		else {
				    			ipAddr = list.get(i).toString();
				    			ipAddr = ipAddr.substring(1);
				    		}
				    	}
				    }
				}
				client.dos.writeUTF("PORT (" + ipAddr + "|" + port + ")");
				datasoc = dataServer.accept();
				client.showServerResponse();
			}
			client.dos.writeUTF("STOR " + uploadDir);
			datadis = new DataInputStream(new FileInputStream(localFile));
			datados = new DataOutputStream(datasoc.getOutputStream());
			datados.writeLong(size);
			byte buffer[] = new byte[Client.MAX_BUFFER];
			int read = 0;
			while((read = datadis.read(buffer)) != -1) {
				datados.write(buffer, 0, read);
				datados.flush();
				uploaded += read;
				percentCompleted = (int) (uploaded * 100 / size);
				setProgress(percentCompleted);
				buffer = new byte[Client.MAX_BUFFER];
			}
			datadis.close();
			datados.close();
			datasoc.close();
			if(dataServer != null) dataServer.close();
			isError = !client.showServerResponse1();
			if(isError) 
				JOptionPane.showMessageDialog(null, "Upload failed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
//	tương tự khi download, upload folder theo đệ quy
//	có thể coi File như 1 String chỉ đg dẫn bth
	public void uploadDirectory( File localDir, String remoteDirPath) throws IOException {	
		//tạo folder trên server
		boolean created = client.makeDirectory(remoteDirPath);
        if (created) {
            System.out.println("CREATED the directory: " + remoteDirPath);
          //duyệt toàn bộ file trong folder cần upload
		    File[] subFiles = localDir.listFiles();
		    if (subFiles != null && subFiles.length > 0) {
		        for (File item : subFiles) {
		            String remoteFilePath = remoteDirPath + "/" + item.getName();
		            String currentLocalPath = localDir + File.separator + item.getName();
		            if (item.isFile()) {
		                // upload the file
		            	File localFile = new File(currentLocalPath);
		            	
		                uploadSingleFile(localFile, remoteFilePath);
//		                UploadTask task = new UploadTask(localFile, remoteFilePath, this);
//		                task.execute();
		                
		                //threadPool.submit(task);
//		                if (uploaded) {
//		                    System.out.println("UPLOADED a file to: " + remoteFilePath);
//		                } else {
//		                    System.out.println("COULD NOT upload the file: " + currentLocalPath);
//		                }
		            } else {
		                // upload the sub directory
		                uploadDirectory( new File(currentLocalPath), remoteFilePath);
		            }
		        }
		    }
        } else {
            System.out.println("COULD NOT create the directory: " + remoteDirPath);
        }
        
	}
	
	@Override
    protected void done() {
        if (!isCancelled() && !isError) {
        	int ok = JOptionPane.showOptionDialog(null,
            		"\"" + localFile.getName() + "\"" + " has been uploaded successfully!", "Message",
            		JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, null, null);
            if(ok == JOptionPane.OK_OPTION || ok == JOptionPane.CANCEL_OPTION) {
            	client.progressBar.setVisible(false);
            	client.btnStop.setVisible(false);
            	client.lblPercent.setText("");
            	client.lblPercent.setVisible(false);
            	client.lblTask.setText("");
            }
            client.loadTable();
        } else {
        	client.progressBar.setVisible(false);
        	client.btnStop.setVisible(false);
        	client.lblPercent.setText("");
        	client.lblPercent.setVisible(false);
        	client.lblTask.setText("");
        	try {
				if(datadis != null) datadis.close();
				if(datados != null) datados.close();
				if(!isError)
					client.showServerResponse();
			} catch (Exception e) {
				System.err.println(e);
			}
        }
        client.isFileTransfering = false;
	}
}
