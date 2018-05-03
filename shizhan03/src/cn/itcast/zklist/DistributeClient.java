package cn.itcast.zklist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class DistributeClient {

	private static final String connectString="192.168.33.61:2181,192.168.33.62:2181,192.168.33.63:2181";
	private static final int sessionTimeout=20000;
	ZooKeeper zkClient = null;
	private static final String parentNode = "/servers";
	private volatile List<String> serverList;
	
	public void getConnect() throws Exception{
		zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
			public void process(WatchedEvent event) {
				// TODO Auto-generated method stub
				try {
					getServerList();
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	public void getServerList() throws KeeperException, InterruptedException{
		List<String> children = zkClient.getChildren(parentNode, true);
		List<String> servers = new ArrayList<String>();
		for (String child : children) {
			byte[] data = zkClient.getData(parentNode+"/"+child, false, null);
			servers.add(new String(data));
		}
		serverList=servers;
		System.out.println(serverList);
	}
	
	public void handleBussiness() throws Exception{
		System.out.println("client start working.....");
		Thread.sleep(Long.MAX_VALUE);
	}
	
	public static void main(String[] args) throws Exception {
		DistributeClient distributeClient = new DistributeClient();
		//连接
		distributeClient.getConnect();
		//获取servers节点的信息，从中获取服务器信息列表
		distributeClient.getServerList();
		//线程启动
		distributeClient.handleBussiness();
	}
	
}
