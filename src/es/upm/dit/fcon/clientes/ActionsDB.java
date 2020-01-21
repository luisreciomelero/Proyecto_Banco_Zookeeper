package es.upm.dit.fcon.clientes;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import es.upm.dit.fcon.banco_zk.ZkService;

public class ActionsDB {
	
	private static String idServer;
	
	public ActionsDB(String idServer) {
		this.idServer=idServer;
	}
	
	private static JSONArray readDB (String rutaDB) {
		JSONArray clientList = new JSONArray();
		try {
			JSONParser jsonParser = new JSONParser();
			FileReader reader = new FileReader(rutaDB);
			Object obj = jsonParser.parse(reader);
			clientList = (JSONArray) obj;
			
			return clientList;
			
		}catch( IOException ioe) {
			System.out.println("ERROR EN: "+ioe);
		}
		catch( ParseException pe) {
			System.out.println("ERROR EN: "+pe);
		}
		return clientList;
		
	}
	
	public static boolean comprobarUpdate(String nCuenta) throws IOException, ParseException {
	
		//String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		JSONArray clientList = readDB(rutaDB);
		String clientListString = clientList.get(0).toString();
		System.out.println("clientListString: ");
		System.out.println(clientListString);
	
		
		
		
		if(clientListString.contains(nCuenta)) {
			return true;
		};
		return false;
		
	}

	public void updateSaldo(String comprobar, String[] update) {
		String rutaDB = "/Users/luisreciomelero/Desktop/eclipse-workspace/Banco_Zookeeper/src/es/upm/dit/fcon/clientes/bd"+idServer+".json";
		JSONArray clientList = readDB(rutaDB);
		
		
		
		try {
			if(comprobarUpdate(comprobar)) {
				
				
			}
			
		} catch (IOException | ParseException e) {
			
			e.printStackTrace();
		}
	}
	
	
	
	

}
