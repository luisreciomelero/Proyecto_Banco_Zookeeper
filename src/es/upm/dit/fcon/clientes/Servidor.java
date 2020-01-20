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
	private static int idOperation;
	private static int idOpQueue;
	
	
	public Servidor() {
		Servidor.idOperation=0;
		Servidor.idOpQueue=0;
	}
	
	
	private static String interfaceCli() {
		String res="";
		Scanner sn = new Scanner(System.in);
        boolean salir = false;
        String opcion; //Guardaremos la opcion del usuario
        String actualizacion;
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
                        	res = res+"create:"+name+";"+saldo+";"+cuenta;
                        	return res;
                        }catch (Exception e) {
                        	System.out.println("Ha ocurrido un error");
        	                
        	            }
                                               
                        break;
                    case "2":
                        System.out.println("Has seleccionado la opcion Actualizar cuenta");
                        Scanner s2 = new Scanner(System.in);
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
                        			res=res+"updateSaldo:"+nuevoSaldo;
                        			return res;
                        			
                        		case "2":
                        			System.out.println("Introduza el nuevo numero de cuenta");
                        			Scanner s4 = new Scanner(System.in);
                        			int nuevoNumero = s4.nextInt();
                        			System.out.println("El nuevo numero de cuenta sera: " + nuevoNumero);
                        			res=res+"updateCuenta:"+nuevoNumero;
                        			return res;
                        		case "3":
                        			System.out.println("Introduza el nuevo nombre del propietario");
                        			Scanner s5 = new Scanner(System.in);
                        			int nuevoPropietario = s5.nextInt();
                        			System.out.println("El nuevo propietario sera: " + nuevoPropietario);
                        			res=res+"updateNombre:"+nuevoPropietario;
                        			return res;
                 
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
                        res=res+"read:"+consultaC;
                        return res;
                    case "4":
                        System.out.println("Obteniendo el listado de clientes...");
                        res=res+opcion;
                        return res;
                    case "5":
                    	Scanner c3 = new Scanner(System.in);
                        int eliminarC = c3.nextInt();
                        System.out.println("Se eliminará la cuenta: " +eliminarC);
                        res=res+"delete:"+eliminarC;
                        return res;
                    case "6":
                    	System.out.println("Cancelando operaciones " );
                    	res=res+"salir";
                        return res;
                    default:
                        System.out.println("Solo números entre 1 y 6");
                        return res;
                }
            } catch (InputMismatchException e) {
                System.out.println("Debes insertar un número");
                sn.next();
            }
        }
		return res;
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
		try {
			switch (action) {
			case "create":
				JSONObject cliente = generateClient(values);
				String idLeader = zk.getLeader().split("-")[1];
				
				if(idServer.equals(idLeader)) {
					System.out.println("ID: "+ idOperation);
					byte[] clientByte = cliente.toString().getBytes("utf-8");
					
					zk.addOperation(action, clientByte, idOperation);
					idOperation = idOperation+1;
					createClient(cliente);
					
				}else {
					byte[] clientByte = cliente.toString().getBytes("utf-8");
					zk.addOpQueue(action, clientByte, idOpQueue);
					idOpQueue +=1;
				}
				
				break;

			default:
				break;
			}
		}catch(Exception e) {
			
		}
	}
		
	public static void main(String[] args){
		idServer=args[0];
		zk = new ZkService();
		
		ruta="/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		
		try {
			createServerZk();
			//resp meter: accion:datos y filtrar por acción
			//dentro de datos campo;nombre para generar de forma generica
			
			createDB(ruta);
			
			String resp = interfaceCli();
			doAction(resp);
			//generateClient(data);
			System.out.print("La respuesta ha sido: " + resp);
			//JsonObject object = toJson(resp);
			
		}catch (Exception e) {
			System.out.println(e);
			return;
		}
	}
}
