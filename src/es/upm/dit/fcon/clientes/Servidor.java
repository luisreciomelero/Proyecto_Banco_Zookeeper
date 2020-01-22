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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;



public class Servidor {
	
	private  String idServer;
	private  String ruta ="";
	//private static ZkService zk;
	private  InterfaceCli interfaceCli;
	private  ActionsDB actionsDB;
	private  String idLeader;
	private  String resp;
	
	
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
			
			List<String> list = zk.getChildren(ROOT_SERVIDORES, watcherServerLd);
			//ElectionLeader
			Collections.sort(list);
			Object[] Sortedlist = list.toArray();
			leader = (String) Sortedlist[0];
			System.out.println("El lider es: ");
			System.out.println(leader);
		}
		
		public void createServer(String idServer) throws KeeperException, InterruptedException {
			String servidor = new String();
			Stat s = zk.exists(ROOT_SERVIDORES + aServer + idServer, watcherServidores);
			
			try {
				if(s == null) {
					servidor = zk.create(ROOT_SERVIDORES + aServer + idServer , new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					System.out.println("Response servidor: " + servidor);
					
				}else {
					System.out.print("conectados a servidor:" + aServer + idServer);
				}
				selectLeader();
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
			
			zk.create(ROOT_COLA + "/"+ action + "-" , clientByte, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			nCola++;
				
			
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
					List<String> list = zk.getChildren(ROOT_OPERACIONES,  watcherOperaciones); //this);
					printList(list, "operaciones");
					
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
					List<String> list = zk.getChildren(ROOT_COLA,  watcherCola); //this);
					printList(list, "cola");
					
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
	
	
	private void createServerZk () throws KeeperException, InterruptedException {
		createServer(idServer);
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
		
		
		
		idLeader = getLeader().split("-")[1];
		try {
			switch (action) {
			case "create":
				JSONObject cliente = generateClient(values);
				byte[] clientByte = cliente.toString().getBytes("utf-8");
				
				if(idServer.equals(idLeader)) {
					String comprob = values.split(";")[2];
					boolean comp= actionsDB.comprobarUpdate(comprob);
					if(comp) {
						System.out.println("Ya existe un cliente con ese numero de cuenta");
						resp = interfaceCli.interface_cli();
					}else {
						addOperation(action, clientByte);
						createClient(cliente);
						resp = interfaceCli.interface_cli();
					}
					
					
				}else {
					
					addOpQueue(action, clientByte);
					resp = interfaceCli.interface_cli();
				}
				
				break;
			case "updateSaldo":
				campoSustituir = values.split(";")[2];
				valorSustituir = values.split(";")[3];
				campoComprobar = values.split(";")[0];
				valorComprobar = values.split(";")[1];
				String updateArray[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				System.out.println("entramos en updateSaldo");
			
				if(idServer.equals(idLeader)) {
					System.out.println("es lider: ");
					boolean comp= actionsDB.comprobarUpdate(valorComprobar);
					if(comp) {
						addOperation(action, updateByte);
						actionsDB.update(updateArray);
						resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						resp = interfaceCli.interface_cli();
						
					}
					
					
				}else {
					
					addOpQueue(action, updateByte);
					resp = interfaceCli.interface_cli();
				}
			case "updateNombre":
				System.out.println("entramos en updateNombre");
				campoSustituir = values.split(";")[2];
				valorSustituir = values.split(";")[3];
				campoComprobar = values.split(";")[0];
				valorComprobar = values.split(";")[1];
				String updateArrayN[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				
				if(idServer.equals(idLeader)) {
					System.out.println("es lider: ");
					boolean comp= actionsDB.comprobarUpdate(valorComprobar);
					if(comp) {
						addOperation(action, updateByte);
						actionsDB.update(updateArrayN);
						
						resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						resp = interfaceCli.interface_cli();
					}
					
					
				}else {
					
					addOpQueue(action, updateByte);
					resp = interfaceCli.interface_cli();
				}
			case "updateCuenta":
				System.out.println("entramos en updateCuenta");
				
				campoSustituir = values.split(";")[2];
				valorSustituir = values.split(";")[3];
				campoComprobar = values.split(";")[0];
				valorComprobar = values.split(";")[1];
				String updateArrayC[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				if(idServer.equals(idLeader)) {
					System.out.println("es lider: ");
					boolean comp= actionsDB.comprobarUpdate(valorComprobar);
					if(comp) {
						addOperation(action, updateByte);
						actionsDB.update(updateArrayC);
						
						resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						resp = interfaceCli.interface_cli();
						
					}
					
					
				}else {
					
					addOpQueue(action, updateByte);
					resp = interfaceCli.interface_cli();
				}
			case "read":
				System.out.println("entramos en read");
				valorComprobar = values;
				
				boolean comp= actionsDB.comprobarUpdate(valorComprobar);
				if(comp) {
					org.json.JSONObject client = actionsDB.readClient(valorComprobar);
					System.out.println("El cliente que se corresponde con este id es: ");
					System.out.println(client);
					
					resp = interfaceCli.interface_cli();
				}else {
					System.out.println("No existe ningun cliente con ese número de cuenta");
					resp = interfaceCli.interface_cli();
					
				}
					
			case "readAll":
				System.out.println("entramos en readAll");
				org.json.JSONArray clients = actionsDB.readAllClient();
				System.out.println("Los clientes registrados son: ");
				System.out.println(clients);
				resp = interfaceCli.interface_cli();
				
			case "delete":
				
				valorComprobar = values;
				if(idServer.equals(idLeader)) {
					System.out.println("es lider: ");
					boolean compr= actionsDB.comprobarUpdate(valorComprobar);
					if(compr) {
						addOperation(action, updateByte);
						org.json.JSONObject client = actionsDB.deleteClient(valorComprobar);
						System.out.println("El cliente eliminado ha sido: ");
						System.out.println(client);
						resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						resp = interfaceCli.interface_cli();
						
					}
					
					
				}else {
					
					addOpQueue(action, updateByte);
					resp = interfaceCli.interface_cli();
				}
				
			default:
				break;
			}
		}catch(Exception e) {
			
		}
	}
		
	public static void main(String[] args){
		
		//ZkService zks = new ZkService();
		Servidor serv = new Servidor();
		serv.idServer=args[0];
		serv.ZkService();
		serv.interfaceCli = new InterfaceCli(serv.idServer);
		serv.actionsDB = new ActionsDB(serv.idServer);
		
		serv.ruta="/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+serv.idServer+".json";
		
		try {
			serv.createServerZk();
			//resp meter: accion:datos y filtrar por acción
			//dentro de datos campo;nombre para generar de forma generica
			
			serv.createDB(serv.ruta);
			serv.resp = serv.interfaceCli.interface_cli();
			
			
			serv.doAction(serv.resp);
			System.out.print("La respuesta ha sido: " + serv.resp);
			
			
		}catch (Exception e) {
			System.out.println(e);
			return;
		}
	}
}
