package cn.itcast.zklist;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;

import org.apache.zookeeper.ZooKeeper;
import java.util.Collections;

public class DistributedClientLock {

	private static final int SESSION_TIMEOUT=20000;
	
	private String hosts="192.168.33.61:2181,192.168.33.62:2181，192.168.33.63:2181";
	private String groupNode = "locks";
	private String subNode="sub";
	private boolean havelock = false;
	
	private ZooKeeper zk;
	//记录自己创建的节点路径
	private volatile String thisPath;
	
	/*
	 * 连接zooKeeper
	 */
	public void connectZooKeeper() throws Exception{
		zk = new ZooKeeper(hosts, SESSION_TIMEOUT, new Watcher() {
			public void process(WatchedEvent event) {
				try {
					//判断事件类型，此处只处理子节点变化时间
					if(event.getType() == EventType.NodeChildrenChanged && event.getPath().equals("/"+groupNode)){
						List<String> childrenNodes = zk.getChildren("/"+groupNode, true);
						String thisNode = thisPath.substring(("/"+groupNode+"/").length());
						//比较是否自己是最小id
						Collections.sort(childrenNodes);
						if(childrenNodes.indexOf(thisNode)== 0){
							//访问共享资源处理业务，并且在处理完成之后删除锁
							doSomething();
							thisPath = zk.create("/" + groupNode + "/" + subNode, null, Ids.OPEN_ACL_UNSAFE,
									CreateMode.EPHEMERAL_SEQUENTIAL);						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		//1.程序一进来就先注册一把锁在zk上
		thisPath= zk.create("/"+groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		
		//wait一小会
		Thread.sleep(2000);
		
		//3.从zk的父目录下获取所有子节点，并且注册对父节点的监听
		List<String> childrenNodes = zk.getChildren("/"+ groupNode, true);
		
		if(childrenNodes.size()==1){
			doSomething();
			thisPath=zk.create("/" + groupNode + "/" + subNode, null, Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
		}
	}
	
	public void doSomething() throws Exception{
		try {
			Thread.sleep(2000);
		} finally {
			zk.delete(this.thisPath, -1);
		}
	}
	
	public static void main(String[] args) throws Exception {
		DistributedClientLock dl = new DistributedClientLock();
		dl.connectZooKeeper();
		Thread.sleep(Long.MAX_VALUE);
	}
	
}
