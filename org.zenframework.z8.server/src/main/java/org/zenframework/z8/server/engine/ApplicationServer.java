package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.job.scheduler.Scheduler;
import org.zenframework.z8.server.base.table.system.SystemDomains;
import org.zenframework.z8.server.base.table.system.SystemFiles;
import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.ie.RmiTransportProcedure;
import org.zenframework.z8.server.ie.TransportEngine;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IMonitor;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.IResponse;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.request.RequestProcessor;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class ApplicationServer extends RmiServer implements IApplicationServer {

	static private final ThreadLocal<IRequest> currentRequest = new ThreadLocal<IRequest>();

	static public String id = guid.create().toString();

	static private ApplicationServer instance;

	private IAuthorityCenter authorityCenter;

	static public IApplicationServer launch(ServerConfig config) throws RemoteException {
		if(instance == null) {
			instance = new ApplicationServer();
			instance.start();
		}
		return instance;	
	}

	static public IRequest getRequest() {
		IRequest request = currentRequest.get();
		if(request == null)
			request = new Request(new Session());
		return request;
	}

	static public ISession getSession() {
		return getRequest().getSession();
	}

	static public IMonitor getMonitor() {
		return getRequest().getMonitor();
	}

	static public boolean isSystemInstalled() {
		return ServerConfig.database().isSystemInstalled();
	}
	
	public static IUser getUser() {
		return getSession().user();
	}

	public static void setRequest(IRequest request) {
		if(request != null)
			currentRequest.set(request);
		else
			currentRequest.remove();
	}

	public static void disableEvents() {
		getRequest().disableEvents();
	}

	public static void enableEvents() {
		getRequest().enableEvents();
	}

	public static boolean events() {
		return getRequest().events();
	}

	private ApplicationServer() throws RemoteException {
		super(ServerConfig.applicationServerPort(), IApplicationServer.class);
	}

	@Override
	public String id() throws RemoteException {
		return id;
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		checkSchemaVersion();

		ServerConfig.authorityCenter().register(this);

		Scheduler.start();

		Trace.logEvent("JVM startup options: " + ManagementFactory.getRuntimeMXBean().getInputArguments().toString() + "\n\t" + RequestDispatcher.getMemoryUsage());
	}

	@Override
	public void stop() throws RemoteException {
		Scheduler.stop();

		TransportEngine.getInstance().stop();

		authorityCenter.unregister(this);

		super.stop();
	}

	@Override
	public String[] domains() {
		return SystemDomains.newInstance().getNames().toArray(new String[0]);
	}

	@Override
	public IUser login(String login) {
		return User.load(login);
	}

	@Override
	public IUser login(String login, String password) {
		return User.load(login, password);
	}

	@Override
	public file download(file file) throws IOException {
		try {
			return SystemFiles.newInstance().getFile(file);
		} finally {
			ConnectionManager.release();
		}
	}

	@Override
	public GNode processRequest(ISession session, GNode node) {
		IRequest request = new Request(node.getAttributes(), node.getFiles(), session);
		IResponse response = request.getResponse();

		setRequest(request);

		new RequestProcessor().processRequest(request, response);

		setRequest(null);

		return new GNode(response.getContent());
	}

	@Override
	public boolean accept(Object object) {
		return RmiTransportProcedure.accept(object);
	}
	
	private void checkSchemaVersion() {
		String version = Runtime.version();
		System.out.println("Runtime schema version: " + version);
	}
}
