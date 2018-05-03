package cn.itcast.zklist;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class DistributeServer {

	public static final String connectString="192.168.33.61:2181,192.168.33.62:2181,192.168.33.63:2181";
	public static final int sessionTimeout=20000;
	private static final String parentNode = "/servers";
	private ZooKeeper zkClient = null;
	
	public void getConnect() throws Exception{
		zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
			public void process(WatchedEvent event) {
				System.out.println(event.getType() + "---" + event.getPath());
				try {
					zkClient.getChildren("/", true);
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
	
	public void regsiter(String hostname) throws KeeperException, InterruptedException{
		String create = zkClient.create(parentNode+"/server", hostname.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println(hostname + "is online.." + create);
	}
	
	public void handleBussiness(String hostname) throws Exception{
		System.out.println(hostname+" is starting");
		Thread.sleep(Long.MAX_VALUE);
	}
	
	
	public static void main(String[] args) throws Exception {
		DistributeServer distributeServer = new DistributeServer();
		//创建链接
		distributeServer.getConnect();
		//注册写入节点
		distributeServer.regsiter(args[0]);
		//开始工作
		distributeServer.handleBussiness(args[0]);
	}
}
