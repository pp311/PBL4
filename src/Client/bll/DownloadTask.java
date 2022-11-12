package Client.bll;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import Client.view.Client;
import Server.dto.FileDto;
import Server.pl.DataConnectionHandler;

public class DownloadTask extends SwingWorker<String, String>{
	Client client;
	String downloadPath;
	String saveDir;
	boolean isDirectory;
	String filename;
	String mode;
	private static final int PORT_RANGE_START = 20000;
	private static final int PORT_RANGE_END = 22000;
	public DownloadTask(String downloadPath, String saveDir, Client client, boolean isDirectory, String mode) {
		this.downloadPath = downloadPath;
		this.saveDir = saveDir;
		this.client = client;
		this.isDirectory = isDirectory;
		this.mode = mode;
		this.filename = downloadPath.substring(downloadPath.lastIndexOf("/") + 1);
		
	}
	@Override
	protected String doInBackground() throws Exception {
		if(!isDirectory) {
			downloadSingleFile(downloadPath, saveDir);
		}
		else downloadDirectory(downloadPath, saveDir);
		
		return null;
	}
	@Override
	protected void process(List<String> chunks) {
		client.lblTask.setText(chunks.get(chunks.size()-1));
	} 
	
	private boolean downloadSingleFile(String downloadPath, String saveDir) {
		try {
			FileDto fileInfo = client.listFiles(downloadPath).get(0);
			long size = fileInfo.getSize();
			long downloaded = 0;
			int percentCompleted = 0;
			publish("Downloading " + downloadPath);
			DataInputStream datadis;
			DataOutputStream datados;
			ServerSocket dataServer = null;
			Socket datasoc = null;
			Random generator = new Random();
			//ServerSocket server;
			int port = 0;
			if(mode == "PASV") {
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
				client.dos.writeUTF("PORT (" + InetAddress.getLocalHost().getHostAddress() + "|" + port + ")");
				datasoc = dataServer.accept();
				client.showServerResponse();
			}
			client.dos.writeUTF("RETR " + downloadPath);
			datadis = new DataInputStream(datasoc.getInputStream());
			datados = new DataOutputStream(new FileOutputStream(saveDir));
			
			byte buffer[] = new byte[Client.MAX_BUFFER];
			int read = 0;
			while((read = datadis.read(buffer)) != -1) {
				datados.write(buffer, 0, read);
				datados.flush();
				downloaded += read;
				percentCompleted = (int) (downloaded * 100 / size);
				setProgress(percentCompleted);
				buffer = new byte[Client.MAX_BUFFER];
			}
			//publish("Closing connection...");
			datadis.close();
			datados.close();
			datasoc.close();
			if(dataServer != null) dataServer.close();
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
				            if (aFile.getType().equals("Dir")) {
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
		            int ok = JOptionPane.showOptionDialog(null,
		            		"\"" + filename + "\"" + " has been downloaded successfully!", "Message",
		            		JOptionPane.OK_OPTION,
		                    JOptionPane.INFORMATION_MESSAGE, null, null, null);
		            if(ok == JOptionPane.OK_OPTION) {
		            	client.progressBar.setVisible(false);
		            	client.lblPercent.setText("");
		            	client.lblTask.setText("");
		            }
		            client.loadTable();
		        }
		        client.isFileTransfering = false;
		    }  
	

}
