/* ReportDownloadServlet.java - created on Jul 15, 2011, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.uim.gui.cp.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.europeana.uim.gui.cp.server.util.PdfReportGenerator;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Alena Fedasenka
 *
 */
public class PDFReportGenerationServlet extends HttpServlet {
    
	private static final long serialVersionUID = 1L;

	 /**
     * Initialization method
     */
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

    }
   
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    	resp.setContentType("application/pdf");  
    	String collection = req.getParameter("collectionId");
    	String provider = req.getParameter("providerId");
		long dateStart = StringUtils.isNotEmpty(req.getParameter("dateStart"))?Long.parseLong(req.getParameter("dateStart")):0l;
    	String recordsCount = req.getParameter("recordsCount");
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + PdfReportGenerator.getFileName(collection) + "\"");
    	resp.setHeader("Cache-Control", "no-cache"); 
    	resp.setDateHeader("Expires", 0);  
    	resp.setHeader("Pragma", "No-cache");
		try {
			System.out.println("*** Generating PDF... ***");
			ServletOutputStream op = resp.getOutputStream();
			String requestUrl = req.getRequestURL().toString();
			String contextPath = req.getContextPath();
			String path = requestUrl.substring(0, requestUrl.indexOf(contextPath) + contextPath.length()); 
			PdfReportGenerator.generatePDFReport(op, provider, collection, dateStart, recordsCount, path);
			op.flush();
			op.close();
			System.out.println("*** CRF statistics report was successfully generated! ***");
		} catch (Exception e) {
			System.out.println("*** Failure of PDF generation! ***");
			e.printStackTrace();
		}
    }
}