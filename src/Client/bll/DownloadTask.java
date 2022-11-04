package Client.bll;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import Client.view.Client;
import Server.dto.FileDto;

public class DownloadTask extends SwingWorker<Void, Void>{
	Client client;
	String downloadPath;
	String saveDir;
	boolean isDirectory;
	String filename;
	
	public DownloadTask(String downloadPath, String saveDir, Client client, boolean isDirectory) {
		this.downloadPath = downloadPath;
		this.saveDir = saveDir;
		this.client = client;
		this.isDirectory = isDirectory;
		
		this.filename = downloadPath.substring(downloadPath.lastIndexOf("/") + 1);
		
	}
	@Override
	protected Void doInBackground() throws Exception {
		if(!isDirectory) {
			downloadSingleFile(downloadPath, saveDir);
		}
		else downloadDirectory(downloadPath, saveDir);
		
		return null;
	}
	
	private boolean downloadSingleFile(String downloadPath, String saveDir) {
		try {
			client.dos.writeUTF("PASV");
			String response = client.showServerResponse();
			int port = Integer.valueOf(response.substring(response.indexOf("(")+1, response.indexOf(")")));
			Socket datasoc = new Socket(client.server, port);
			client.dos.writeUTF("RETR " + downloadPath);
			DataInputStream datadis = new DataInputStream(datasoc.getInputStream());
			DataOutputStream datados = new DataOutputStream(new FileOutputStream(saveDir));
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
			return false;
		}
		return true;
	}
	
	//để tải 1 folder, thực hiện tải theo đệ quy
			//đầu tiên liệt kê hết folder và file có trong folder cần tải
			//nếu gặp file thì tải file đó về
			//nếu gặp folder thì tạo folder đó ở local, r gọi đệ quy hàm để tải folder con đó về
			public boolean downloadDirectory( String remoteDirPath, String saveDirPath) throws IOException {
			    String dirToList = remoteDirPath;
			    try {
			    	ArrayList<FileDto> subFiles = client.listFiles(dirToList);
					 
				    if (subFiles != null && subFiles.size() > 0) {
				        for (FileDto aFile : subFiles) {
				            String remoteFilePath = remoteDirPath + "/" + aFile.getName();
				            //lưu ý currentLocalPath có thể là dg dẫn tới folder hoặc file
				            String currentLocalPath = saveDirPath + File.separator + aFile.getName();
				            if (aFile.getType() == "Dir") {
				                // create the directory in saveDir
				                File newDir = new File(currentLocalPath);
				                boolean created = newDir.mkdirs();
				                if (created) {
				                    System.out.println("CREATED the directory: " + currentLocalPath);
				                } else {
				                	throw new Exception("COULD NOT create the directory: " + currentLocalPath);			                 
				                }
				                // download the sub directory
				                downloadDirectory(remoteFilePath, currentLocalPath);
				            } else {
				                // download the file
				                boolean success = downloadSingleFile(remoteFilePath, currentLocalPath);
				                if (success) {
				                    System.out.println("DOWNLOADED the file: " + currentLocalPath);
				                } else {
				                	throw new Exception("COULD NOT download the file: "+ currentLocalPath);
				                }
				            }
				        }
				    }
				} catch (Exception e) {
					// TODO: handle exception
					System.err.println(e);
					return false;
				}
			    return true;
			}
			@Override
		    protected void done() {
		        if (!isCancelled()) {
		            JOptionPane.showMessageDialog(null,
		            		"\"" + filename + "\"" + " has been uploaded successfully!", "Message",
		                    JOptionPane.INFORMATION_MESSAGE);
		            client.loadTable();
		            client.isFileTransfering = false;
		        }
		    }  
	

}
