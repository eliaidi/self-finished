package action.test;

import org.jessma.mvc.ActionSupport;
import org.jessma.mvc.FormBean;
import org.jessma.dao.FacadeProxy;

import dao.hbn.UserDao;

@FormBean
public class HibernateDeleteUser extends ActionSupport
{
	private int id;
	
	@Override
	public String execute() throws Exception
	{
		UserDao dao = FacadeProxy.getManualCommitProxy(UserDao.class);
		dao.deleteUser(id);
		
		return SUCCESS;
	}

	public void setId(int id)
	{
		this.id = id;
	}
}
