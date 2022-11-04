package Client.bll;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import Client.view.Client;

public class UploadTask extends SwingWorker<Void, Void>{
	private File localFile;
	private String uploadDir;
	private Client client;
	
	public UploadTask(File localFile, String uploadDir, Client client) {
		this.localFile = localFile;
		this.uploadDir = uploadDir;
		this.client = client;
	}
	@Override
	protected Void doInBackground() throws Exception {
		// TODO Auto-generated method stub
		if(localFile.isFile()) {
			uploadSingleFile(localFile, uploadDir);			
		} else {
			uploadDirectory(localFile, uploadDir);
		}
		
		return null;
	}
	
	private void uploadSingleFile(File localFile, String uploadDir) {
		try {
			client.dos.writeUTF("PASV");
			String response = client.showServerResponse();
			int port = Integer.valueOf(response.substring(response.indexOf("(")+1, response.indexOf(")")));
			Socket datasoc = new Socket(client.server, port);
			client.dos.writeUTF("STOR " + uploadDir);
			DataInputStream datadis = new DataInputStream(new FileInputStream(localFile));
			DataOutputStream datados = new DataOutputStream(datasoc.getOutputStream());
			byte buffer[] = new byte[Client.MAX_BUFFER];
			int read = 0;
			while((read = datadis.read(buffer)) != -1) {
				datados.write(buffer, 0, read);
				datados.flush();
				buffer = new byte[Client.MAX_BUFFER];
			}
			datadis.close();
			datados.close();
			datasoc.close();
			client.showServerResponse();
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
        if (!isCancelled()) {
            JOptionPane.showMessageDialog(null,
                    "\"" + localFile.getName() + "\"" + " has been uploaded successfully!", "Message",
                    JOptionPane.INFORMATION_MESSAGE);
            client.loadTable();
            client.isFileTransfering = false;
        }
    }  
	
}
