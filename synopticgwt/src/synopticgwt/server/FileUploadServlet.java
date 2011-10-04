 package synopticgwt.server;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	/**
	 * Handles HTTP GET requests.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(request, resp);
	}
	
	/**
	 * Handles HTTP POST requests. Response outputs contents of uploaded .txt file.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		// Process only multipart requests.
		if (ServletFileUpload.isMultipartContent(request)) {
			FileItem uploadedItem = getFileItem(request);
			if (uploadedItem == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Error occurred while processing file.");
				return;
			}
			if (!uploadedItem.getName().endsWith(".txt")) {
				response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
						"Incorect file format, supports .txt files only.");
				return;
			}
   			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(uploadedItem.getString());
		}	
	}
	
	// Returns uploaded file from request.
	@SuppressWarnings("unchecked")
	private FileItem getFileItem(HttpServletRequest request) {
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();
	
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		try {
			List<FileItem> items = upload.parseRequest(request);
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				
				// FileItem is uploaded file and not simple form field
				if (!item.isFormField()) {
					return item;
				}
			}
		} catch (FileUploadException e) {
			return null;
		}
		return null;
	}
}
