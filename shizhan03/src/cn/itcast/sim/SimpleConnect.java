package cn.itcast.sim;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

public class SimpleConnect {
	private static final String connectString = "192.168.33.61:2181,192.168.33.62:2181,192.168.33.63:2181";
	private final int sessionTimeout = 5000;
	ZooKeeper zkClient=null;
	
	@Before
	public void init() throws Exception{
		zkClient=new ZooKeeper(connectString, sessionTimeout, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				// TODO Auto-generated method stub
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
	
	/*
	 * 创建节点在zooKeeper
	 */
	@Test
	public void createNode() throws KeeperException, InterruptedException {
		String zkNode = zkClient.create("/bbbb", "hellozk".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}
	
	/*
	 * 获取节点
	 */
	@Test
	public void getChildren() throws Exception{
		List<String> children = zkClient.getChildren("/", true);
		for(String child : children){
			System.out.println(child);
		}
		Thread.sleep(Long.MAX_VALUE);
	}
	
	
}
