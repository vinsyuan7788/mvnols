package cn.itcast.user.action;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import cn.itcast.user.action.utils.SendEmail;
import cn.itcast.user.bean.User;
import cn.itcast.user.bean.Userupload;
import cn.itcast.user.service.UserService;

/**
 *	This is an action class to process request:
 *	1. URL for normal test: 
 *	   -- http://localhost:8080/mvnols/home.jsp
 *  2. URL for login validation test: 
 *     -- http://localhost:8080/mvnols/user/queryUser.action?id=1
 *     -- http://localhost:8080/mvnols/exception/returnException.action
 */
@Controller
@RequestMapping("/user")
public class UserAction {

	/*	IOP: IOC & DI	*/
	@Resource
	private UserService userService;
	
	/**
	 * 	This is an action method to query user by id
	 * @param id: to accept the parameter value from post|get request: request parameter binding|acquisition
	 * @param model: to put the processed result into requestScope
	 * @return: return to a view
	 * @throws Exception: can be processed by an global exception resolver
	 */
	@RequestMapping("/queryUser")
	public String queryUser(Integer id, Model model) throws Exception {
		
		User user = userService.queryUserById(id);
		model.addAttribute("user", user);
		
		return "user/queryUser";
	}
	
	/**
	 * 	This is an action method to modify user information
	 * 	1. Format validation & update validation are both completed in JSP view
	 * 	2. If the request comes here, it means the request passes the format & update validation
	 *     -- Hence this action method just needs to:
	 *        -- Create a new instance for processing upload file
	 *        -- Process the upload file
	 *        -- Update & obtain the latest user information & return
	 * @param user
	 * @param uploadFile
	 * @param model
	 * @param httpSession
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/updateUser")
	public String updateUser (User user, MultipartFile uploadFile, String uploadFilePath, Model model, HttpSession httpSession) throws Exception {
		
		/*	Create a new UploadFile instance	*/
		Userupload userupload = new Userupload();
		
		/*	Process the upload file if necessary	*/
		userupload = this.ProcessUploadFile(user, uploadFile, uploadFilePath, userupload);
		
		/*	Update & Save the success message	*/
		userService.updateUser(user, userupload);
		model.addAttribute("successMessage", "Congratulations! User information has been updated!");
		
		/*	Obtain the latest user information & return	*/
		User latestUser = userService.queryUserById(user.getId());
		httpSession.setAttribute("user", latestUser);
		return "forward:/redirection/success.action";
	}
	
	/**
	 * 	This is an action method for delete users in batches
	 * 	1. When deleting a user, the user is not actually removed from database. ONLY the status becomes "disable"
	 * 	2. Use Array to accept the values from <input type = "checkbox" .../>
	 * @param deleteId
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/deleteUsers")
	public String deleteUsers (Integer[] checkedId, Model model) throws Exception {
		
		/*	Transform the array to List	 */
		List<Integer> ids = Arrays.asList(checkedId);
		
		/*	Delete the users	*/
		userService.deleteUsersById(ids);
		
		/*	Save success message & return	*/
		model.addAttribute("successMessage", "Delete users successfully");
		return "forward:/redirection/success.action";
	}
	
	/**
	 * 	This is an action method to recover the users in batches
	 * 	1. When recovering a user, set the state of user to be "unactive" & send the activation email to user
	 * 	2. Use Array to accept the values from <input type = "checkbox" .../>
	 * @param checkedId
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/recoverUsers")
	public String recoverUsers (Integer[] checkedId, Model model) throws Exception {
		
		/*	Transform the array to List	 */
		List<Integer> ids = Arrays.asList(checkedId);
		
		/*	Recover the users 	*/
		List<User> recoveredUsers = userService.recoverUsersById(ids);
		
		/*	Send the email	*/
		for (User user : recoveredUsers) {
//			this.sendEmail(user);
			SendEmail.sendEmail(user);
		}
		
		/*	Save success message & return	*/
		model.addAttribute("successMessage", "Recover users successfully");
		return "forward:/redirection/success.action";
	}

	/**
	 * 	This is an action method for user login
	 * 	1. Format validation & existence validation are both completed in JSP view
	 * 	2. If the request comes here, that means the request passes the format check & existence check
	 *     -- Hence in this method, just need to:
	 *        -- Query the existing user
	 *        -- Save the user information into session scope & return 
	 *  3. For return view: (*****)
	 *     -- If the returnURL is null, return to home page
	 *     -- If not, redirect the request to the returnURL
	 * @param user
	 * @param httpSession
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/userLogin")
	public String userLogin (User user, String returnURL, HttpSession httpSession) throws Exception {
		
		/*	Query the existing user according to login information	*/
		List<User> existingUsers = userService.queryUserByLoginInfo(user);
		User existingUser = existingUsers.get(0);
		
		/*	Save the user information into session scope & return	*/
		httpSession.setAttribute("user", existingUser);
		if (returnURL == null || returnURL.trim().length() == 0) {
			return "redirect:/home.jsp";
		} else {
			return "redirect:" + returnURL;
		}
	}

	/**
	 * 	This is an action method for user logout
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/userLogout")
	public String userLogout (HttpSession session) throws Exception {
		
		session.invalidate();
		return "redirect:/home.jsp";
	}
	
	/**
	 * 	This is an action method for user registration
	 * 	1. Format validation & duplication validation are both completed in JSP view
	 * 	2. If the request comes here, that means the request passes the format check & duplication check
	 *     -- Hence in this method, just need to:
	 *        -- Set initial values for necessary fields
	 *        -- Insert the JavaBean into DB & send the email
	 *        -- Return to success view
	 * @param user
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/userRegister")
	public String userRegister (User user, Model model) throws Exception {
		
		/*	Set user state, role, activation uuid & score	*/
		user.setState("unactive");
		user.setRoleid(1);
		user.setActivationuuid(UUID.randomUUID().toString());
		user.setScore(0F);
		
		/*	Insert the user information into DB & save success message & return  */
		userService.insertUser(user);
		
		/*	Send the email	*/
		SendEmail.sendEmail(user);
		
		/*	Save the success message & return	*/ 
		model.addAttribute("successMessage", "Congratulations! Registration succeeds! Please check your email for activation!");
		return "forward:/redirection/success.action";
	}
	
	/**
	 * 	This is an action method to activate the user
	 * @param activationuuid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/userActivate")
	public String userActivate (String activationuuid, Model model) throws Exception {
		
		userService.activate(activationuuid);
		model.addAttribute("successMessage", "Congratulations! Activation succeeds!");
		return "forward:/redirection/success.action";
	}
	
	//------------------------------------------------------------------------------------------------------------
	
	/**
	 * 	This is an internal action method to process upload file for updateUser()
	 * @param user
	 * @param uploadFile
	 * @param userupload
	 * @return
	 * @throws Exception
	 */
	private Userupload ProcessUploadFile(User user, MultipartFile uploadFile, String uploadFilePath, Userupload userupload) throws Exception {
		
		/*	If there is anything being uploaded, process the upload file	 */
		if (uploadFile.getSize() > 0) {
			
			/*	Set the fields for userupload object	*/
			userupload.setUserId(user.getId());
			userupload.setUploadfile(uploadFilePath);
		}
		
		/*	Else return the Userupload instance directly	*/
		return userupload;
	}
}
