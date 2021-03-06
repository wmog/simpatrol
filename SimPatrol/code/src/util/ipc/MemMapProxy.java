package util.ipc;

import java.util.LinkedList;
import java.util.List;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Stanley  Wang
 * @version 1.0
 */

public class MemMapProxy {
	
  private static MemMapProxy proxy;
//private static MemMapProxy proxy = null;
  private List<MemMapFileObserver> observers;
  private MemMapFileObserver observer;
  
  static {
    System.loadLibrary("MemMapProxyLib");
  }
  
  public static MemMapProxy getInstance(){
	  if( proxy == null ) {
		  proxy = new MemMapProxy("");
		  System.out.println("New Proxy");
	 }
	  return proxy;
  }
  public static MemMapProxy getInstance(String messageDataReady) {
	  
	  proxy = new MemMapProxy(messageDataReady);
	  //proxy.registerMessage(messageDataReady);
	  System.err.println("New Proxy Server"+proxy.toString());	  
	 
	  return proxy;
	}
 
  public MemMapProxy(MemMapFileObserver observer) {
    this.observer = observer;    
  }
  public MemMapProxy(String message) {
	    this.observers = new LinkedList<MemMapFileObserver>();	    
	    init(message);
 }
  
  public void addObserver(MemMapFileObserver observer){
	  this.observers.add(observer);
  }
  
  public void setObserver(MemMapFileObserver observer){
	  this.observer = observer;
  }

  public void fireDataReadyEvent() {
	 observer.onDataReady();
  }

  private native boolean init(String msg);
  public native void registerMessage(String name);
  public native void destroy();

}