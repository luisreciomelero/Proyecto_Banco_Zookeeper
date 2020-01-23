package es.upm.dit.fcon.clientes;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;



import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;



public class Servidor {
	
	private  String idServer;
	private  String ruta ="";
	private  String idBBDD ="";
	//private static ZkService zk;
	private  InterfaceCli interfaceCli;
	private  ActionsDB actionsDB;
	private  String idLeader;
	private  String resp="";
	
	
	private static final int SESSION_TIMEOUT = 5000;
	private static Integer mutex        = -1;

	private static final String ROOT_OPERACIONES = "/operaciones";
	private static final String ROOT_SERVIDORES = "/servidores";
	private static final String ROOT_COLA = "/cola";
	private  String myId;
	private  String leader;
	private int nCola = 0;
	private  int nOperations = 0;
	private List<String> listCola = null;
	private List<String> listOperations = null;
	
	
	private String  aServer     = "/Server-";
	
	static String[]  hosts = {"127.0.0.1:2181", "127.0.0.1:2182", "127.0.0.1:2183"};

	private ZooKeeper zk;
	
	
	
	
	public Servidor() {
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	///////////////////FUNCIONES ANTERIORES DE ZKSERVICE.JAVA///////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	public void ZkService() {
			
			// Select a random zookeeper server
			Random rand = new Random();
			int i = rand.nextInt(hosts.length);
			
			// Create a session and wait until it is created.
			// When is created, the watcher is notified
			try {
				if (zk == null) {
					zk = new ZooKeeper(hosts[i], SESSION_TIMEOUT, sessionWatcher);
					try {
						// Wait for creating the session. Use the object lock
						wait();
						//zk.exists("/",false);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			} catch (Exception e) {
				System.out.println("Error creando sesion");
			}
			
			if(zk!=null) {
				try {
					
					// Create a folder, if it is not created
					String operaciones = new String();
					Stat o = zk.exists(ROOT_OPERACIONES, false); //this);
					String servidores = new String();
					Stat s = zk.exists(ROOT_SERVIDORES, false); //this);
					String cola = new String();
					Stat c = zk.exists(ROOT_COLA, false); //this);
					
					
					if (o == null) {
						// Created the znode, if it is not created.
						operaciones = zk.create(ROOT_OPERACIONES, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						System.out.println("Response rootOperaciones: " + operaciones);
						
					}
					if (s == null) {
						// Created the znode, if it is not created.
						servidores = zk.create(ROOT_SERVIDORES, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						System.out.println("Response rootServidores: " + servidores);
					}
					if (c == null) {
						// Created the znode, if it is not created.
						cola = zk.create(ROOT_COLA, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						System.out.println("Response rootCola: " + cola);
						zk.getChildren(ROOT_COLA, watcherCola);
					}
					
					
					
					
				} catch (KeeperException e) {
					System.out.println("The session with Zookeeper failes. Closing");
					return;
				} catch (InterruptedException e) {
					System.out.println("InterruptedException raised");
				}
			}
			
		};
		
		private void selectLeader() throws KeeperException, InterruptedException {
			
			List<String> list = zk.getChildren(ROOT_SERVIDORES, false);
			//ElectionLeader
			Collections.sort(list);
			Object[] Sortedlist = list.toArray();
			leader = (String) Sortedlist[0];
			System.out.println("El lider es: ");
			System.out.println(leader);
		}
		
		public void createServer() throws KeeperException, InterruptedException, IOException {
			String servidor = new String();
			//Stat s = zk.exists(ROOT_SERVIDORES + aServer + idServer, watcherServidores);
			
			try {
				
				idServer = zk.create(ROOT_SERVIDORES + aServer , new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
				idServer= idServer.replace(ROOT_SERVIDORES + "/", "");
				System.out.println("Mi ID es: " + idServer);
				selectLeader();
				//replicateDBNewServer();
				zk.getChildren(ROOT_SERVIDORES,  watcherServidores);
				
				if (!idServer.equals(leader)) {
					zk.getChildren(ROOT_OPERACIONES, watcherOperaciones);
				}
				
				
				
				
			} catch (KeeperException e) {
				System.out.println("The session with Zookeeper failes. Closing");
				return;
			} catch (InterruptedException e) {
				System.out.println("InterruptedException raised");
			}
			
		}
		
		public void process(WatchedEvent event) {
			// TODO Auto-generated method stub
			
		}
		public void addOperation(String action, byte[] clientByte) throws KeeperException, InterruptedException {
			
			zk.create(ROOT_OPERACIONES + "/"+ action +"-", clientByte, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			nOperations++;
		}
		
		public void addOpQueue(String action, byte[] clientByte) throws KeeperException, InterruptedException {
			nCola++;
			zk.create(ROOT_COLA + "/"+ action + "-" , clientByte, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			
				
			
		}
		

		
		public String getLeader() {
			return leader;
		}
		
		private Watcher sessionWatcher = new Watcher() {
			public void process (WatchedEvent e) {
				System.out.println("sesion iniciada");
				System.out.println(e.toString());
				notify();
			}
		};
		
		
		// Notified when the number of children in /member is updated
		private Watcher  watcherOperaciones = new Watcher() {
			public void process(WatchedEvent event) {
				System.out.println("------------------Watcher Operaciones------------------\n");		
				try {
					System.out.println("        Update!!");
					List<String> list = zk.getChildren(ROOT_OPERACIONES,  false); //this);
					printList(list, "operaciones");
					produceOperations();
					
				} catch (Exception e) {
					System.out.println("Exception: wacherOperaciones");
				}
			}
		};
		private Watcher  watcherServidores = new Watcher() {
			public void process(WatchedEvent event) {
				System.out.println("------------------Watcher Servidores------------------\n");		
				try {
					System.out.println("        Update!!");
					List<String> list = zk.getChildren(ROOT_SERVIDORES,  watcherServidores); //this);
					printList(list, "servidores");
					selectLeader();
					
				} catch (Exception e) {
					System.out.println("Exception: wacherServidores");
				}
			}
		};
		
		private Watcher  watcherCola = new Watcher() {
			public void process(WatchedEvent event) {
				System.out.println("------------------Watcher Cola------------------\n");		
				try {
					System.out.println("        Update!!");
					List<String> list = zk.getChildren(ROOT_COLA,  false); //this);
					printList(list, "cola");
					produceCola();
					
				} catch (Exception e) {
					System.out.println("Exception: wacherCola");
				}
			}
		};
		
		private Watcher  watcherServerLd = new Watcher() {
			public void process(WatchedEvent event) {
				System.out.println("------------------Watcher ServerLd------------------\n");		
				try {
					System.out.println("        Update!!");
					List<String> list = zk.getChildren(ROOT_SERVIDORES,  watcherServerLd); //this);
					printList(list, ROOT_OPERACIONES);
					
				} catch (Exception e) {
					System.out.println("Exception: wacherMemberLd");
				}
			}
		};

		
		
		private void printList (List<String> list, String dir) {
			System.out.println("Remaining #:" + dir + list.size());
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				System.out.print(string + ", ");				
			}
			System.out.println();
		}

		
		
		
		////////////////////////////////////////////////////////////////////////////////////
		///////////////////FUNCIONES REPLICACION DE ZKSERVICES.JAVA/////////////////////////
		////////////////////////////////////////////////////////////////////////////////////
		
		public void produceCola() {
			Stat s = null;
			String path = null;
			String data = "";
			Object[] opCola= new Object[2];
			
			try {
				listCola = zk.getChildren(ROOT_COLA, false, s); 
			} catch (Exception e) {
				System.out.println("Unexpected Exception process barrier");
			}
	
			try {
				//System.out.println(listProducts.get(0));
				path = ROOT_COLA+"/"+listCola.get(0);
				//System.out.println(path);
				byte[] b = zk.getData(path, false, s);
				s = zk.exists(path, false);
				
				//System.out.println(s.getVersion());
				zk.delete(path, s.getVersion());
				
				// Generate random delay
				Random rand = new Random();
				int r = rand.nextInt(10);
				// Loop for rand iterations
				for (int j = 0; j < r; j++) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {

					}
				}
				zk.getChildren(ROOT_COLA, watcherCola, s); 
                //ByteBuffer buffer = ByteBuffer.wrap(b);
                //data = buffer.getInt();
				Charset charset = Charset.forName("ISO-8859-1");
				ByteBuffer buffer = ByteBuffer.wrap(b);
				data=charset.decode(buffer).toString();
				//data = Arrays.toString(buffer.array());
				
				
                doAction(data);
                
          
                
                //System.out.println("++++ Produce. Data: " + data + "; Path: " + path + "; Number of products: " + nProducts);
			} catch (Exception e) {
				// The exception due to a race while getting the list of children, get data and delete. Another
				// consumer may have deleted a child while the previous access. Then, the exception is simply
				// implies that the data has not been produced.
				System.out.println("Exception when accessing the data");
				
				//System.err.println(e);
				//e.printStackTrace();
				//break;
			}
			
		}
		
		private void produceOperations() {
			System.out.print("ENTRAMOS EN PRODUCEOPERATIONS, idServer: ");
			System.out.print(idServer);
			Stat s = null;
			String path = null;
			String data = "";
			//Object[] opCola= new Object[2];
			
			try {
				listOperations = zk.getChildren(ROOT_OPERACIONES, false, s); 
			} catch (Exception e) {
				System.out.println("Unexpected Exception process barrier");
			}
	
			try {
				//System.out.println(listProducts.get(0));
				path = ROOT_OPERACIONES+"/"+listOperations.get(0);
				//System.out.println(path);
				byte[] b = zk.getData(path, false, s);
				s = zk.exists(path, false);
				
				//System.out.println(s.getVersion());
				//zk.delete(path, s.getVersion());
				createZnodeRepOp(path);
				
				// Generate random delay
				Random rand = new Random();
				int r = rand.nextInt(10);
				// Loop for rand iterations
				for (int j = 0; j < r; j++) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {

					}
				}
				zk.getChildren(ROOT_OPERACIONES, watcherOperaciones, s); 
                //ByteBuffer buffer = ByteBuffer.wrap(b);
                //data = buffer.getInt();
				Charset charset = Charset.forName("ISO-8859-1");
				ByteBuffer buffer = ByteBuffer.wrap(b);
				data=charset.decode(buffer).toString();
				//data = Arrays.toString(buffer.array());
				
				
				doActionSlave(data);
                
          
                
                //System.out.println("++++ Produce. Data: " + data + "; Path: " + path + "; Number of products: " + nProducts);
			} catch (Exception e) {
				// The exception due to a race while getting the list of children, get data and delete. Another
				// consumer may have deleted a child while the previous access. Then, the exception is simply
				// implies that the data has not been produced.
				System.out.println("Exception when accessing the data");
				
				//System.err.println(e);
				//e.printStackTrace();
				//break;
			}
			
		}
		
		public void createZnodeRepOp(String path) throws KeeperException, InterruptedException {
			System.out.print("LLEGA PATH: ");
			System.out.print(path);
			System.out.print("LLEGA idServer: ");
			System.out.print(idServer);
			zk.create(path + "/"+idServer, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		
		////////////////////////////////////////////////////////////////////////////////////
		///////////////////FUNCIONES ANTERIORES DE SERVIDOR.JAVA////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////
		
	private void createDB(String r) throws IOException {
		File archivo = new File(r);
		if(!archivo.exists()) {
			JSONArray clientList = new JSONArray();
			
			try (FileWriter file = new FileWriter(archivo)) {
				 
	            file.write(clientList.toJSONString());
	            file.flush();
	 
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
		}else {
			return;
		}
		
	}
	
	public void replicateDBNewServer() throws IOException {
		if (leader != null) {
			idLeader=leader.split("-")[1];
			String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idLeader+".json";
			org.json.JSONArray clientList = actionsDB.readDB(rutaDB);
			actionsDB.writeDB(clientList);
		}
	}
	
	private JSONObject generateClient (String data) {
		JSONObject clientDetails = new JSONObject();
		String[] parts = data.split(";");
		
		clientDetails.put("name", parts[0]);
		clientDetails.put("saldo", parts[1]);
		clientDetails.put("Ncuenta", parts[2]);
		
		JSONObject client = new JSONObject();
		client.put("cliente",clientDetails);
		
		return client;
		
	}
	
	private void createClient(JSONObject client) {
		
		try {
			JSONParser jsonParser = new JSONParser();
			FileReader reader = new FileReader(ruta);
			Object obj = jsonParser.parse(reader);
			JSONArray clientList = (JSONArray) obj;
			clientList.add(client);
			
			FileWriter file = new FileWriter(ruta);
			file.write(clientList.toJSONString());
            file.flush();
            
        }catch(Exception e) {
        	
        }
	}
	
	
	private void createServerZk () throws KeeperException, InterruptedException, IOException {
		createServer();
	}

	private void doAction(String resp) throws UnsupportedEncodingException {
		String[] data = resp.split(":");
		String action = data[0];
		String values = data[1];
		System.out.println("la accion será: ");
		System.out.println(action);
		System.out.println("valores: ");
		System.out.println(values);
		byte[] updateByte = values.getBytes("utf-8");
		//String sustituir = "\""+values.split(";")[2]+"\":\""+values.split(";")[3]+"\"";
		String campoSustituir = "";
		String valorSustituir = "";
		String campoComprobar = "";
		String valorComprobar = "";
		
		
		
		//idLeader = getLeader().split("-")[1];
		try {
			switch (action) {
			case "create":
				JSONObject cliente = generateClient(values);
				byte[] clientByte = cliente.toString().getBytes("utf-8");
				
				if(idServer.equals(leader)) {
					String comprob = values.split(";")[2];
					boolean comp= actionsDB.comprobarUpdate(comprob);
					if(comp) {
						System.out.println("Ya existe un cliente con ese numero de cuenta");
						//resp = interfaceCli.interface_cli();
					}else {
						byte [] respBytes = resp.getBytes("utf-8");
						addOperation(action, respBytes);
						createClient(cliente);
						//resp = interfaceCli.interface_cli();
					}
					
					
				}else {
					byte [] respBytes = resp.getBytes("utf-8");
					addOpQueue(action, respBytes);
					//resp = interfaceCli.interface_cli();
				}
				
				break;
			case "updateSaldo":
				campoSustituir = values.split(";")[2];
				valorSustituir = values.split(";")[3];
				campoComprobar = values.split(";")[0];
				valorComprobar = values.split(";")[1];
				String updateArray[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				System.out.println("entramos en updateSaldo");
			
				if(idServer.equals(leader)) {
					System.out.println("es lider: ");
					boolean comp= actionsDB.comprobarUpdate(valorComprobar);
					if(comp) {
						byte [] respBytes = resp.getBytes("utf-8");
						addOperation(action, respBytes);
						actionsDB.update(updateArray);
						//resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						//resp = interfaceCli.interface_cli();
						
					}
					
					
				}else {
					byte [] respBytes = resp.getBytes("utf-8");
					addOpQueue(action, respBytes);
					//resp = interfaceCli.interface_cli();
				}
			case "updateNombre":
				System.out.println("entramos en updateNombre");
				campoSustituir = values.split(";")[2];
				valorSustituir = values.split(";")[3];
				campoComprobar = values.split(";")[0];
				valorComprobar = values.split(";")[1];
				String updateArrayN[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				
				if(idServer.equals(leader)) {
					System.out.println("es lider: ");
					boolean comp= actionsDB.comprobarUpdate(valorComprobar);
					if(comp) {
						addOperation(action, updateByte);
						actionsDB.update(updateArrayN);
						
						//resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						//resp = interfaceCli.interface_cli();
					}
					
					
				}else {
					byte [] respBytes = resp.getBytes("utf-8");
					addOpQueue(action, respBytes);
					//resp = interfaceCli.interface_cli();
				}
			case "updateCuenta":
				System.out.println("entramos en updateCuenta");
				
				campoSustituir = values.split(";")[2];
				valorSustituir = values.split(";")[3];
				campoComprobar = values.split(";")[0];
				valorComprobar = values.split(";")[1];
				String updateArrayC[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				if(idServer.equals(leader)) {
					System.out.println("es lider: ");
					boolean comp= actionsDB.comprobarUpdate(valorComprobar);
					if(comp) {
						byte [] respBytes = resp.getBytes("utf-8");
						addOperation(action, respBytes);
						actionsDB.update(updateArrayC);
						
						//resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						//resp = interfaceCli.interface_cli();
						
					}
					
					
				}else {
					byte [] respBytes = resp.getBytes("utf-8");
					addOpQueue(action, respBytes);
					//resp = interfaceCli.interface_cli();
				}
			case "read":
				System.out.println("entramos en read");
				valorComprobar = values;
				
				boolean comp= actionsDB.comprobarUpdate(valorComprobar);
				if(comp) {
					org.json.JSONObject client = actionsDB.readClient(valorComprobar);
					System.out.println("El cliente que se corresponde con este id es: ");
					System.out.println(client);
					
					//resp = interfaceCli.interface_cli();
				}else {
					System.out.println("No existe ningun cliente con ese número de cuenta");
					//resp = interfaceCli.interface_cli();
					
				}
					
			case "readAll":
				System.out.println("entramos en readAll");
				org.json.JSONArray clients = actionsDB.readAllClient();
				System.out.println("Los clientes registrados son: ");
				System.out.println(clients);
				//resp = interfaceCli.interface_cli();
				
			case "delete":
				
				valorComprobar = values;
				if(idServer.equals(leader)) {
					System.out.println("es lider: ");
					boolean compr= actionsDB.comprobarUpdate(valorComprobar);
					if(compr) {
						byte [] respBytes = resp.getBytes("utf-8");
						addOperation(action, respBytes);
						org.json.JSONObject client = actionsDB.deleteClient(valorComprobar);
						System.out.println("El cliente eliminado ha sido: ");
						System.out.println(client);
						//resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						//resp = interfaceCli.interface_cli();
						
					}
					
					
				}else {
					byte [] respBytes = resp.getBytes("utf-8");
					addOpQueue(action, respBytes);
					//resp = interfaceCli.interface_cli();
				}
				
			default:
				break;
			}
		}catch(Exception e) {
			
		}
	}
	
	private void doActionSlave(String resp) throws UnsupportedEncodingException {
		String[] data = resp.split(":");
		String action = data[0];
		String values = data[1];
		System.out.println("la accion será: ");
		System.out.println(action);
		System.out.println("valores: ");
		System.out.println(values);
		byte[] updateByte = values.getBytes("utf-8");
		//String sustituir = "\""+values.split(";")[2]+"\":\""+values.split(";")[3]+"\"";
		String campoSustituir = "";
		String valorSustituir = "";
		String campoComprobar = "";
		String valorComprobar = "";
		
		
		
		//idLeader = getLeader().split("-")[1];
		try {
			switch (action) {
			case "create":
				JSONObject cliente = generateClient(values);
				byte[] clientByte = cliente.toString().getBytes("utf-8");
				
				if(!idServer.equals(leader)) {
					String comprob = values.split(";")[2];
					boolean comp= actionsDB.comprobarUpdate(comprob);
					if(comp) {
						System.out.println("Ya existe un cliente con ese numero de cuenta");
						//resp = interfaceCli.interface_cli();
					}else {
						byte [] respBytes = resp.getBytes("utf-8");
						createClient(cliente);
						//resp = interfaceCli.interface_cli();
					}
					
					
				}
				
				break;
			case "updateSaldo":
				campoSustituir = values.split(";")[2];
				valorSustituir = values.split(";")[3];
				campoComprobar = values.split(";")[0];
				valorComprobar = values.split(";")[1];
				String updateArray[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				System.out.println("entramos en updateSaldo");
			
				if(!idServer.equals(leader)) {
					System.out.println("es lider: ");
					boolean comp= actionsDB.comprobarUpdate(valorComprobar);
					if(comp) {
						byte [] respBytes = resp.getBytes("utf-8");
						
						actionsDB.update(updateArray);
						//resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						//resp = interfaceCli.interface_cli();
						
					}
					}
					
			case "updateNombre":
				System.out.println("entramos en updateNombre");
				campoSustituir = values.split(";")[2];
				valorSustituir = values.split(";")[3];
				campoComprobar = values.split(";")[0];
				valorComprobar = values.split(";")[1];
				String updateArrayN[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				
				if(!idServer.equals(leader)) {
					System.out.println("es lider: ");
					boolean comp= actionsDB.comprobarUpdate(valorComprobar);
					if(comp) {
						actionsDB.update(updateArrayN);
						
						//resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						//resp = interfaceCli.interface_cli();
					}
				}
			case "updateCuenta":
				System.out.println("entramos en updateCuenta");
				
				campoSustituir = values.split(";")[2];
				valorSustituir = values.split(";")[3];
				campoComprobar = values.split(";")[0];
				valorComprobar = values.split(";")[1];
				String updateArrayC[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				if(!idServer.equals(leader)) {
					System.out.println("es lider: ");
					boolean comp= actionsDB.comprobarUpdate(valorComprobar);
					if(comp) {
						byte [] respBytes = resp.getBytes("utf-8");
						
						actionsDB.update(updateArrayC);
						
						//resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						//resp = interfaceCli.interface_cli();
						
					}
				}
			case "read":
				System.out.println("entramos en read");
				valorComprobar = values;
				
				boolean comp= actionsDB.comprobarUpdate(valorComprobar);
				if(comp) {
					org.json.JSONObject client = actionsDB.readClient(valorComprobar);
					System.out.println("El cliente que se corresponde con este id es: ");
					System.out.println(client);
					
					//resp = interfaceCli.interface_cli();
				}else {
					System.out.println("No existe ningun cliente con ese número de cuenta");
					//resp = interfaceCli.interface_cli();
					
				}
					
			case "readAll":
				System.out.println("entramos en readAll");
				org.json.JSONArray clients = actionsDB.readAllClient();
				System.out.println("Los clientes registrados son: ");
				System.out.println(clients);
				//resp = interfaceCli.interface_cli();
				
			case "delete":
				
				valorComprobar = values;
				if(!idServer.equals(leader)) {
					System.out.println("es lider: ");
					boolean compr= actionsDB.comprobarUpdate(valorComprobar);
					if(compr) {
						byte [] respBytes = resp.getBytes("utf-8");
						org.json.JSONObject client = actionsDB.deleteClient(valorComprobar);
						System.out.println("El cliente eliminado ha sido: ");
						System.out.println(client);
						//resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						//resp = interfaceCli.interface_cli();
						
					}
				}
				
			default:
				break;
			}
		}catch(Exception e) {
			
		}
	}
	
	public void interface_cli() throws UnsupportedEncodingException {
		
		Scanner sn = new Scanner(System.in);
        boolean salir = false;
        String opcion; //Guardaremos la opcion del usuario
        String actualizacion;
        String idClient;
        while (!salir) {
        	System.out.println("Puede elegir entre: ");
            System.out.println("1. Dar de alta a un cliente");
            System.out.println("2. Actualizar informacion de un cliente");
            System.out.println("3. Obtener información de un cliente");
            System.out.println("4. Obtener el listado de clientes");
            System.out.println("5. Dar de baja a un cliente");
            System.out.println("6. Salir");
 
            try {
 
                System.out.println("Eliga la opcion que prefiera introduciendo su número");
                opcion = sn.nextLine();
                
                switch (opcion) {
                    case "1":
                        System.out.println("Introduce los datos del cliente");
                        Scanner c1 = new Scanner(System.in);
                        try {
                        	System.out.println("Nombre:");
                        	String name = c1.nextLine();
                        	System.out.println("Numero de cuenta:");
                        	int cuenta = c1.nextInt();
                        	System.out.println("Saldo en euros");
                        	int saldo = c1.nextInt();
                        	System.out.println("El cliente tendra el nombre: "+ name+" con un saldo de: " + saldo+ " en la cuenta: "+ cuenta);
                        	resp = ""+"create:"+name+";"+saldo+";"+cuenta;
                        	doAction(resp);
                        }catch (Exception e) {
                        	System.out.println("Ha ocurrido un error");
        	                
        	            }
                                               
                        break;
                    case "2":
                        System.out.println("Has seleccionado la opcion Actualizar cuenta");
                        Scanner s2 = new Scanner(System.in);
                        System.out.println("Introduce la el número de cuenta a actualizar");
                        idClient=s2.nextLine();
                        System.out.println("1. Modificar su saldo");
                        System.out.println("2. Modificar su numero de cuenta");
                        System.out.println("3. Modificar su propietario");
                        try {
                        	actualizacion = s2.nextLine();
                        	switch(actualizacion) {
                        		case "1":
                        			System.out.println("Introduza el nuevo saldo en euros");
                        			Scanner s3 = new Scanner(System.in);
                        			int nuevoSaldo = s3.nextInt();
                        			System.out.println("El nuevo saldo sera: " + nuevoSaldo + "€");
                        			resp=""+"updateSaldo:"+"Ncuenta;"+idClient+";"+"saldo;"+nuevoSaldo;
                        			doAction(resp);
                        			
                        		case "2":
                        			System.out.println("Introduza el nuevo numero de cuenta");
                        			Scanner s4 = new Scanner(System.in);
                        			int nuevoNumero = s4.nextInt();
                        			System.out.println("El nuevo numero de cuenta sera: " + nuevoNumero);
                        			resp=""+"updateCuenta:"+"Ncuenta;"+idClient+";"+"Ncuenta;"+nuevoNumero;
                        			doAction(resp);
                        		case "3":
                        			System.out.println("Introduza el nuevo nombre del propietario");
                        			Scanner s5 = new Scanner(System.in);
                        			String nuevoPropietario = s5.next();
                        			System.out.println("El nuevo propietario sera: " + nuevoPropietario);
                        			resp=""+"updateNombre:"+"Ncuenta;"+idClient+";"+"name;"+nuevoPropietario;
                        			doAction(resp);
                 
                        	}
                        	
                        }catch (Exception e) {
                        	System.out.println("Ha ocurrido un error");
                        	
        	                salir=true;
        	            }
                        break;
                    case "3":
                        System.out.println("Introduzca el numero de cuenta a consultar: ");
                        Scanner c2 = new Scanner(System.in);
                        int consultaC = c2.nextInt();
                        System.out.println("Obteniendo datos de la cuenta: " +consultaC + " ...");
                        resp=""+"read:"+consultaC;
                        doAction(resp);
                    case "4":
                        System.out.println("Obteniendo el listado de clientes...");
                        resp=""+"readAll: ";
                        doAction(resp);
                    case "5":
                    	System.out.println("Introduzca el numero de cuenta a eliminar: ");
                    	Scanner c3 = new Scanner(System.in);
                        int eliminarC = c3.nextInt();
                        System.out.println("Se eliminará la cuenta: " +eliminarC);
                        resp=""+"delete:"+eliminarC;
                        doAction(resp);
                    case "6":
                    	System.out.println("Cancelando operaciones " );
                    	salir = true;
                        
                    default:
                        System.out.println("Solo números entre 1 y 6");
                        
                }
            } catch (InputMismatchException e) {
                System.out.println("Debes insertar un número");
                sn.next();
            }
        }
		
	}
		
	public static void main(String[] args){
		
		//ZkService zks = new ZkService();
		Servidor serv = new Servidor();
		
		serv.ZkService();
		//serv.interfaceCli = new InterfaceCli(serv.idServer);
		
		
			try {
				serv.createServerZk();
				serv.idBBDD = serv.idServer.split("-")[1];
				
				serv.ruta="/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+serv.idBBDD+".json";
				
				//resp meter: accion:datos y filtrar por acción
				//dentro de datos campo;nombre para generar de forma generica
				
				serv.actionsDB = new ActionsDB(serv.idBBDD);
				serv.createDB(serv.ruta);
				serv.replicateDBNewServer();
				serv.interface_cli();
				
				
				
				serv.doAction(serv.resp);
				System.out.print("La respuesta ha sido: " + serv.resp);
				
				
			}catch (Exception e) {
				System.out.println(e);
				return;
			}
		}
		
		
	

	
}
