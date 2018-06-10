package burp;

import java.io.PrintWriter;
import java.util.List;

import net.logicaltrust.MyHttpListener;
import net.logicaltrust.SimpleLogger;
import net.logicaltrust.mock.MockEntry;
import net.logicaltrust.mock.MockHolder;
import net.logicaltrust.mock.MockRule;
import net.logicaltrust.mock.MockSettingsSaver;
import net.logicaltrust.server.MyMockServer;
import net.logicaltrust.tab.MockTabPanel;

public class BurpExtender implements IBurpExtender {

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		
		PrintWriter stderr = new PrintWriter(callbacks.getStderr(), true);
		SimpleLogger logger = new SimpleLogger(new PrintWriter(callbacks.getStdout(), true), stderr);

		MockSettingsSaver settingSaver = new MockSettingsSaver(callbacks, logger);
		//settingSaver.clear();
		List<MockEntry> entries = settingSaver.loadEntries();
		
		if (entries.isEmpty()) {
			logger.debug("No entries loaded");
			String resp = "HTTP/1.0 200 File not found\nServer: SimpleHTTP/9.9 Python/2.7.15\nDate: Sun, 03 Jun 2018 11:28:24 GMT\nConnection: close\nContent-Type: text/html\n\n<head>\n<title>Error response</title>\n</head>\n<body>\n<h1>Error response</h1>\n<p>EOKOK OKOK 200.\n<p>Message: File not found.\n<p>AAAAA code explanation: 200 = Nothing matches the given URI.\n</body>";
			MockRule r = new MockRule("http", "^localhost$", "8000", "^/abc.*");
			MockEntry e = new MockEntry(r, resp.getBytes());
			e.setId(1);
			entries.add(e);
			resp = "HTTP/1.0 200 File not found\nServer: SimpleHTTP/9.9 Python/2.7.15\nDate: Sun, 03 Jun 2018 11:28:24 GMT\nConnection: close\nContent-Type: text/html\n\n<head>\n<title>Error response</title>\n</head>\n<body>\n<h1>Error response</h1>\n<p>EOKOK OKOK 200.\n<p>Message: File not found.\n<p>AAAAA code explanation: 200 = Nothing matches the given URI.\n</body>";
			r = new MockRule("http", "^localhost$", "8000", "^/test.*");
			e = new MockEntry(r, resp.getBytes());
			e.setId(2);
			entries.add(e);
			settingSaver.saveIdList(entries);
			for (MockEntry ee : entries) {
				settingSaver.saveEntry(ee);
			}
		}
		
		MockHolder mockHolder = new MockHolder(entries, settingSaver);

		MyHttpListener httpListener = new MyHttpListener(callbacks.getHelpers(), logger, mockHolder);
		callbacks.registerHttpListener(httpListener);
		callbacks.registerContextMenuFactory(new MyContextMenuFactory(logger, callbacks.getHelpers(), mockHolder));
		MyMockServer myMockServer = new MyMockServer(logger);
		callbacks.registerExtensionStateListener(myMockServer);
		new Thread(() -> {
			myMockServer.run();
		}).start();
		
		MockTabPanel tab = new MockTabPanel(logger, callbacks, mockHolder);
		callbacks.addSuiteTab(tab);
	}
	
}