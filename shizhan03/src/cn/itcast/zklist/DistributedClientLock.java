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
	
	private String hosts="192.168.33.61:2181,192.168.33.62:2181��192.168.33.63:2181";
	private String groupNode = "locks";
	private String subNode="sub";
	private boolean havelock = false;
	
	private ZooKeeper zk;
	//��¼�Լ������Ľڵ�·��
	private volatile String thisPath;
	
	/*
	 * ����zooKeeper
	 */
	public void connectZooKeeper() throws Exception{
		zk = new ZooKeeper(hosts, SESSION_TIMEOUT, new Watcher() {
			public void process(WatchedEvent event) {
				try {
					//�ж��¼����ͣ��˴�ֻ�����ӽڵ�仯ʱ��
					if(event.getType() == EventType.NodeChildrenChanged && event.getPath().equals("/"+groupNode)){
						List<String> childrenNodes = zk.getChildren("/"+groupNode, true);
						String thisNode = thisPath.substring(("/"+groupNode+"/").length());
						//�Ƚ��Ƿ��Լ�����Сid
						Collections.sort(childrenNodes);
						if(childrenNodes.indexOf(thisNode)== 0){
							//���ʹ�����Դ����ҵ�񣬲����ڴ������֮��ɾ����
							doSomething();
							thisPath = zk.create("/" + groupNode + "/" + subNode, null, Ids.OPEN_ACL_UNSAFE,
									CreateMode.EPHEMERAL_SEQUENTIAL);						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		//1.����һ��������ע��һ������zk��
		thisPath= zk.create("/"+groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		
		//waitһС��
		Thread.sleep(2000);
		
		//3.��zk�ĸ�Ŀ¼�»�ȡ�����ӽڵ㣬����ע��Ը��ڵ�ļ���
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
