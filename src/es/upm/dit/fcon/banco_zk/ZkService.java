package es.upm.dit.fcon.banco_zk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.json.simple.JSONObject;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import java.nio.ByteBuffer;

public class ZkService implements Watcher{

	
	private static final int SESSION_TIMEOUT = 5000;

	private static final String ROOT_OPERACIONES = "/operaciones";
	private static final String ROOT_SERVIDORES = "/servidores";
	private static final String ROOT_COLA = "/cola";
	private static String myId;
	private static String leader;
	
	
	
	private String  aServer     = "/Server-";
	
	
	private String servidor;
	// This is static. A list of zookeeper can be provided for decide where to connect
	String[] hosts = {"127.0.0.1:2181", "127.0.0.1:2182", "127.0.0.1:2183"};

	private ZooKeeper zk;
	
	
	@Override
	public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		
	}

	public ZkService() {
		
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
				Stat o = zk.exists(ROOT_OPERACIONES, watcherOperaciones); //this);
				String servidores = new String();
				Stat s = zk.exists(ROOT_SERVIDORES, watcherServidores); //this);
				String cola = new String();
				Stat c = zk.exists(ROOT_COLA, watcherCola); //this);
				
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
	
	public void addOperation(String action, byte[] clientByte) throws KeeperException, InterruptedException {
		
		zk.create(ROOT_OPERACIONES + "/"+ action +"-", clientByte, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
	}
	
	public void addOpQueue(String action, byte[] clientByte) throws KeeperException, InterruptedException {
		
		zk.create(ROOT_COLA + "/"+ action + "-" , clientByte, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			
		
	}
	

	
	public static String getLeader() {
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
	
	
	public static void main(String[] args) {
		//Declaramos las acciones permitidas por las aplicaciones
		ZkService zks = new ZkService();
		
		
		try {
			Thread.sleep(300000); 			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
