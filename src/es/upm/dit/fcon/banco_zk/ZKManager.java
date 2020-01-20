package es.upm.dit.fcon.banco_zk;

import java.io.UnsupportedEncodingException;

import org.apache.zookeeper.KeeperException;

public interface ZKManager {
   
	public void create(String path, byte[] data) throws KeeperException, InterruptedException;
    
	public Object getZNodeData(String path, boolean watchFlag) throws KeeperException, InterruptedException, UnsupportedEncodingException;
    
	public void update(String path, byte[] data) throws KeeperException, InterruptedException;
}
