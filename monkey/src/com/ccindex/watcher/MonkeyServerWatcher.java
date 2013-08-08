package com.ccindex.watcher;

import java.io.IOException;

import org.apache.zookeeper.WatchedEvent;

import com.ccindex.operator.ChildrenChange;

public class MonkeyServerWatcher extends WatcherImpl {

	private ChildrenChange getChild;

	public void setGetChild(ChildrenChange getChild) {
		this.getChild = getChild;
	}

	@Override
	public boolean dialOtherSigProcess(WatchedEvent event) {
		// TODO Auto-generated method stub
		if (getChild != null && getChild.isPath(event.getPath())) {
			getChild.process(event);
		}

		return true;
	}

}
