package cn.itcast.global.session;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 	This is the session provider interface to offer session-scope operation
 *  1. This interface can be added more methods in it, which depends on the requirement during the development
 *  2. These sets of arguments offer more flexibility to perform distributed session caching
 * @author Vince Xu Yuan
 */
public interface SessionProvider {

	/*	Set attribute into session scope	*/
	public void setAttribute(String attributeName, Serializable value, HttpServletRequest request, HttpServletResponse response);
	
	/*	Get attribute from session scope	*/
	public Serializable getAttribute(String attributeName, HttpServletRequest request, HttpServletResponse response);
	
	/*	Session invalidation	*/
	public void invalidate (HttpServletRequest request, HttpServletResponse response);
	
	/*	Get session ID	 */
	public String getSessionId (HttpServletRequest request, HttpServletResponse response);

	/*	Remove attribute from session scope	 */
	public void removeAttribute(String attributeName, HttpServletRequest request, HttpServletResponse response);
}
