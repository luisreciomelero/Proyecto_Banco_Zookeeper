package es.upm.dit.fcon.clientes;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

//import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;




public class ActionsDB {
	
	private String idServer;
	
	public ActionsDB(String idServer) {
		this.idServer=idServer;
	}
	
	public  JSONArray readDB (String rutaDB) {
		JSONArray clientList = null;
		try {
			JSONParser jsonParser = new JSONParser();
			FileReader reader = new FileReader(rutaDB);
			Object obj = jsonParser.parse(reader);
			String jsonObj = obj.toString();
			//System.out.println("readDB: ");
			//System.out.println(obj);
			clientList = new JSONArray(jsonObj) ;
			
			return clientList;
			
		}catch( IOException ioe) {
			System.out.println("ERROR EN: "+ioe);
		}
		catch( ParseException pe) {
			System.out.println("ERROR EN: "+pe);
		}
		return clientList;
		
	}
	
	public boolean comprobarUpdate(String nCuenta)  {
		
		String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		JSONArray clientList = readDB(rutaDB);
		boolean comprobacion=false;
		
		for(int i =0; i<clientList.length(); i++) {
			
			JSONObject client = clientList.getJSONObject(i);
			if(nCuenta.equals(client.getJSONObject("cliente").get("Ncuenta"))) {
				comprobacion=true;
				break;
			}
		}

		return comprobacion;
	
	}
	
	public int getIdClient(String nCuenta) throws IOException, ParseException {
		
		//String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		JSONArray clientList = readDB(rutaDB);
		int idClient=-1;
		for(int i =0; i<clientList.length(); i++) {
			JSONObject client = clientList.getJSONObject(i);
			if(nCuenta.equals(client.getJSONObject("cliente").get("Ncuenta"))) {
				idClient=i;
				break;
			}
		}

		return idClient;
	
	}
	
	public void writeDB(JSONArray jsonarray) throws IOException {
		String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		FileWriter file = new FileWriter(rutaDB);
		file.write(jsonarray.toString());
        file.flush();
	}

	public void update(String[] update) {
		String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		JSONArray clientList = readDB(rutaDB);
		int idClient=-1;
		try {
			if(comprobarUpdate(update[3])) {
				System.out.println("ENTRAMOS up:");
				idClient = getIdClient(update[3]);
				System.out.println("conseguimos id");
				System.out.println(idClient);
				JSONObject client = clientList.getJSONObject(idClient);
				JSONObject dataClient = client.getJSONObject("cliente");
				dataClient.put(update[0], update[1]);
				JSONObject newClient = new JSONObject();
				newClient.put("cliente", dataClient);
				clientList.remove(idClient);
				System.out.println("antes:");
				System.out.println(clientList);
				clientList.put(newClient);
				System.out.println("despues:");
				System.out.println(clientList);
				
				writeDB(clientList);
				
			}
			
		} catch (IOException | ParseException e) {
			
			e.printStackTrace();
		}
	}
	
	public JSONObject readClient(String idClient) {
		String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		JSONArray clientList = readDB(rutaDB);
		try {
			int idClientDB = getIdClient(idClient);
			
			JSONObject client = clientList.getJSONObject(idClientDB);
			return client;
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public JSONArray readAllClient() {
		String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		JSONArray clientList = readDB(rutaDB);
		return clientList;
	}

	public JSONObject deleteClient(String idClient) {
		String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		JSONArray clientList = readDB(rutaDB);
		try {
			int idClientDB = getIdClient(idClient);
			
			JSONObject client = clientList.getJSONObject(idClientDB);
			clientList.remove(idClientDB);
			writeDB(clientList);
			return client;
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	
	
	
	

}
