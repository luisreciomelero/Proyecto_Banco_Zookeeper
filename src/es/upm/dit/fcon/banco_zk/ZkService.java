package es.upm.dit.fcon.banco_zk;

import java.util.ArrayList;
import java.util.Arrays;
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
	private Integer mutex        = -1;

	private static final String ROOT_OPERACIONES = "/operaciones";
	private static final String ROOT_SERVIDORES = "/servidores";
	private static final String ROOT_COLA = "/cola";
	private static String myId;
	private static String leader;
	private int nCola = 0;
	private int nOperations = 0;
	private List<String> listCola = null;
	private List<String> listOperations = null;
	
	
	
	
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
					synchronized(mutex) {
						mutex.wait();
					}
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
	
	public void addOperation(String action, byte[] clientByte) throws KeeperException, InterruptedException {
		
		zk.create(ROOT_OPERACIONES + "/"+ action +"-", clientByte, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
		nOperations++;
	}
	
	public void addOpQueue(String action, byte[] clientByte) throws KeeperException, InterruptedException {
		
		zk.create(ROOT_COLA + "/"+ action + "-" , clientByte, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
		nCola++;
			
		
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
	
	public Object[] productCola() {
		Stat s = null;
		String path = null;
		String data = "";
		Object[] opCola= new Object[2];
		while (nCola > 0) {
			try {
				listCola = zk.getChildren(ROOT_COLA, false, s); 
			} catch (Exception e) {
				System.out.println("Unexpected Exception process barrier");
				break;
			}
			if (listCola.size() > 0) {
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
					
                    //ByteBuffer buffer = ByteBuffer.wrap(b);
                    //data = buffer.getInt();
					data = Arrays.toString(b);
                    opCola[0]=s;
					opCola[1]=data;
                    nCola--;
              
                    return opCola;
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
				
			} else {
				try {
					zk.getChildren(ROOT_COLA, watcherCola, s);
					synchronized(mutex) {
						mutex.wait();
					}
				} catch (Exception e) {
					System.out.println("Unexpected Exception process barrier");
					break;
				}
			}
		return null;
		
	}
		return opCola;
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
