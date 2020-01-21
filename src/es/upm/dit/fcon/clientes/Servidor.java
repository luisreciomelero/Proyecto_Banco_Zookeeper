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

	private static void doAction(String resp) {
		String[] data = resp.split(":");
		String action = data[0];
		String values = data[1];
		System.out.println("la accion será: ");
		System.out.println(action);
		System.out.println("valores: ");
		System.out.println(values);
		idLeader = zk.getLeader().split("-")[1];
		try {
			switch (action) {
			case "create":
				JSONObject cliente = generateClient(values);
				byte[] clientByte = cliente.toString().getBytes("utf-8");
				
				if(idServer.equals(idLeader)) {

					zk.addOperation(action, clientByte);
					createClient(cliente);
					
				}else {
					
					zk.addOpQueue(action, clientByte);
				}
				
				break;
			case "updateSaldo":
				System.out.print("entramos en updateSaldo");
				byte[] updateByte = values.getBytes("utf-8");
				//String sustituir = "\""+values.split(";")[2]+"\":\""+values.split(";")[3]+"\"";
				String campoSustituir = values.split(";")[2];
				String valorSustituir = values.split(";")[3];
				String campoComprobar = values.split(";")[0];
				String valorComprobar = values.split(";")[1];
				String updateArray[] = {campoSustituir,valorSustituir,campoComprobar, valorComprobar};
				String comprobar = "\""+values.split(";")[0]+"\":\""+values.split(";")[1]+"\"";
				System.out.println("comprobar: ");
				System.out.println(comprobar);
				if(idServer.equals(idLeader)) {
					if(actionsDB.comprobarUpdate(comprobar)) {
						zk.addOperation(action, updateByte);
						actionsDB.updateSaldo(comprobar, updateArray);
					}else {
						System.out.println("No existe ningun cliente con ese número de cuenta");
						
					}
					
					
				}else {
					
					zk.addOpQueue(action, updateByte);
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
			String resp = interfaceCli.interface_cli();
			
			doAction(resp);
			System.out.print("La respuesta ha sido: " + resp);
			
			
		}catch (Exception e) {
			System.out.println(e);
			return;
		}
	}
}
