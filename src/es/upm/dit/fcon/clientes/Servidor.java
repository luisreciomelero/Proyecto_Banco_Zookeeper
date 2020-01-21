package es.upm.dit.fcon.clientes;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import com.google.gson.JsonObject;

import es.upm.dit.fcon.banco_zk.ZkService;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;



public class Servidor {
	
	private static String idServer;
	private static String ruta ="";
	private static ZkService zk;
	private static InterfaceCli interfaceCli;
	private static ActionsDB actionsDB;
	private static String idLeader;
	private static String resp;
	
	
	public Servidor() {
		
	}
	
	
	
	
	private static void createDB(String r) throws IOException {
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
	
	private static JSONObject generateClient (String data) {
		JSONObject clientDetails = new JSONObject();
		String[] parts = data.split(";");
		
		clientDetails.put("name", parts[0]);
		clientDetails.put("saldo", parts[1]);
		clientDetails.put("Ncuenta", parts[2]);
		
		JSONObject client = new JSONObject();
		client.put("cliente",clientDetails);
		
		return client;
		
	}
	
	private static void createClient(JSONObject client) {
		
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
	
	
	private static void createServerZk () throws KeeperException, InterruptedException {
		zk.createServer(idServer);
	}

	private static void doAction(String resp) throws UnsupportedEncodingException {
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
		
		
		
		idLeader = zk.getLeader().split("-")[1];
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
						zk.addOperation(action, clientByte);
						createClient(cliente);
						resp = interfaceCli.interface_cli();
					}
					
					
				}else {
					
					zk.addOpQueue(action, clientByte);
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
						zk.addOperation(action, updateByte);
						actionsDB.update(updateArray);
						resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						resp = interfaceCli.interface_cli();
						
					}
					
					
				}else {
					
					zk.addOpQueue(action, updateByte);
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
						zk.addOperation(action, updateByte);
						actionsDB.update(updateArrayN);
						
						resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						resp = interfaceCli.interface_cli();
					}
					
					
				}else {
					
					zk.addOpQueue(action, updateByte);
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
						zk.addOperation(action, updateByte);
						actionsDB.update(updateArrayC);
						
						resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						resp = interfaceCli.interface_cli();
						
					}
					
					
				}else {
					
					zk.addOpQueue(action, updateByte);
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
						zk.addOperation(action, updateByte);
						org.json.JSONObject client = actionsDB.deleteClient(valorComprobar);
						System.out.println("El cliente eliminado ha sido: ");
						System.out.println(client);
						resp = interfaceCli.interface_cli();
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						resp = interfaceCli.interface_cli();
						
					}
					
					
				}else {
					
					zk.addOpQueue(action, updateByte);
					resp = interfaceCli.interface_cli();
				}
				
			default:
				break;
			}
		}catch(Exception e) {
			
		}
	}
		
	public static void main(String[] args){
		idServer=args[0];
		zk = new ZkService();
		interfaceCli = new InterfaceCli(idServer);
		actionsDB = new ActionsDB(idServer);
		
		ruta="/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		
		try {
			createServerZk();
			//resp meter: accion:datos y filtrar por acción
			//dentro de datos campo;nombre para generar de forma generica
			
			createDB(ruta);
			resp = interfaceCli.interface_cli();
			
			
			doAction(resp);
			System.out.print("La respuesta ha sido: " + resp);
			
			
		}catch (Exception e) {
			System.out.println(e);
			return;
		}
	}
}
