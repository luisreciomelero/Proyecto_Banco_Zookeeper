package es.upm.dit.fcon.clientes;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.google.gson.*;



public class Server {
	
	private static ArrayList<Server> instances = new ArrayList<Server>();
	private static int numReplicas = 0;
	
	private Server() {
		
	}
	
	private static void createInstance(int numInstancias) {
		System.out.println("entramos en createInstance crearemos: "+ numInstancias);
		System.out.println("se ha establecido el numReplicas a: "+ numReplicas);
		System.out.println(instances);
		
		if(numReplicas<numInstancias){
			System.out.println("Como máximo se admiten: " + numReplicas + " replicas");
			return;
		}
		
		
		else if(instances.size()==0) {
			for(int i=0; i<numInstancias; i++) {
				Server replica = new Server();
				instances.add(replica);
				System.out.println("HAY "+instances.size()+" creadas");
			}
			return;
		}
		else if (instances.size()!=0) {
			
			int instanciasCreadas = instances.size();
			
			if((instanciasCreadas+numInstancias)>numReplicas) {
				int replicasPermitidas = numReplicas-instanciasCreadas;
				System.out.println("unicamente se pueden crear " + replicasPermitidas +" replicas más");
				return;
			}
			else {
				for(int i=0; i<numInstancias; i++) {
					Server replica = new Server();
					instances.add(replica);
				}
				return;
			}
		}else {
			System.out.println("Algo falla");
			return;
		}
	}
	
	public static Server getInstance() {
		int i = (int) Math.floor(Math.random()* instances.size());
		Server server = instances.get(i);
		return server;
	}
	
	static TimerTask timerTask = new TimerTask() {
		public void run() {
			System.out.println("Entro en en timer");
			System.out.println("el numero de instancias es: "+instances.size());
			if(instances.size()<numReplicas) { 
				int replicas =numReplicas-instances.size(); 
				createInstance(replicas); 
			} 
		}
	};
	
	public static void main(String[] args){
		if(args.length >= 2) {
			System.out.print("Esta clase solo admite un argumento numerico");
			return;
		}
		String r = args[0];
		try {
			int replicas = Integer.parseInt(r);
			numReplicas = replicas;
			
			System.out.println("Se crearán "+replicas+" servidores");
			createInstance(replicas);
			System.out.println("Se han creado "+instances.size()+" servidores");
			Timer timer = new Timer();
			timer .scheduleAtFixedRate(timerTask, 5000, 10000);
		}catch (NumberFormatException nfe) {
			System.out.print("Esta clase solo admite un argumento numerico");
			
			return;
		}
	}
	

}
